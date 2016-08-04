package net.urbanmc.kingdomwars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.Towny;

import ca.xshade.bukkit.questioner.Questioner;
import net.milkbowl.vault.economy.Economy;
import net.urbanmc.kingdomwars.command.BaseCommand;
import net.urbanmc.kingdomwars.listeners.WarListener;

public class KingdomWars extends JavaPlugin {

	private static Towny towny;
	private static Questioner questioner;
	private static Economy econ;

	private static double finishAmount;
	private static double truceAmount;
	private static int winningKills;

	@Override
	public void onEnable() {
		if (!getServer().getPluginManager().isPluginEnabled("Towny")) {
			getLogger().log(Level.SEVERE, "Towny plugin was not found! Disabling..");
			setEnabled(false);
			return;
		}

		if (!getServer().getPluginManager().isPluginEnabled("Questioner")) {
			getLogger().log(Level.SEVERE, "Questioner plugin was not found! Disabling..");
			setEnabled(false);
			return;
		}

		if (!setupEconomy()) {
			getLogger().log(Level.SEVERE, "Vault could not be loaded!");
			setEnabled(false);
			return;
		}

		towny = getPlugin(Towny.class);
		questioner = getPlugin(Questioner.class);

		loadConfig();

		getCommand("townywar").setExecutor(new BaseCommand());
		getServer().getPluginManager().registerEvents(new WarListener(), this);
	}

	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);

		if (rsp != null) {
			econ = rsp.getProvider();
		}

		return econ != null;
	}

	private void loadConfig() {
		File file = new File("plugins/KingdomWars/config.yml");

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}

		if (!file.exists()) {
			try {
				InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml");
				OutputStream output = new FileOutputStream(file);

				IOUtils.copy(input, output);
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		FileConfiguration data = YamlConfiguration.loadConfiguration(file);

		finishAmount = data.getDouble("finish-amount");
		truceAmount = data.getDouble("truce-amount");
		winningKills = data.getInt("winning-kills");
	}

	public static Towny getTowny() {
		return towny;
	}

	public static Questioner getQuestioner() {
		return questioner;
	}

	public static Economy getEcon() {
		return econ;
	}

	public static double getFinishAmount() {
		return finishAmount;
	}

	public static double getTruceAmount() {
		return truceAmount;
	}

	public static int getWinningKills() {
		return winningKills;
	}
}
