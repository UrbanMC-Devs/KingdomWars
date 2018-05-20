package net.urbanmc.kingdomwars;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.urbanmc.kingdomwars.listener.NationListener;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.Towny;

import ca.xshade.bukkit.questioner.Questioner;
import net.urbanmc.kingdomwars.command.BaseCommand;
import net.urbanmc.kingdomwars.listener.WarListener;

public class KingdomWars extends JavaPlugin {

	private static Towny towny;
	private static Questioner questioner;
	private static Essentials essentials;

	private static double startAmount, finishAmount, truceAmount;
	private static int winningKills;
	private static long lastTime, endTime;

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

		if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
			essentials = getPlugin(Essentials.class);
		}

		towny = getPlugin(Towny.class);
		questioner = getPlugin(Questioner.class);

		if (towny.isError()) {
			getLogger().log(Level.SEVERE, "There was an error while enabling Towny. Disabling..");
			setEnabled(false);
			return;
		}

		loadConfig();
		WarUtil.filterLeaderboard();

		getCommand("townywar").setExecutor(new BaseCommand());
		getServer().getPluginManager().registerEvents(new WarListener(), this);
		getServer().getPluginManager().registerEvents(new NationListener(), this);
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

		startAmount = data.getDouble("start-amount");
		finishAmount = data.getDouble("finish-amount");
		truceAmount = data.getDouble("truce-amount");
		winningKills = data.getInt("winning-kills");
		lastTime = TimeUnit.HOURS.toMillis(data.getInt("hours-between"));
		endTime = TimeUnit.HOURS.toMillis(data.getInt("hours-end"));
	}

	public static Towny getTowny() {
		return towny;
	}

	public static Questioner getQuestioner() {
		return questioner;
	}

	public static boolean hasEssentials() {
		return essentials != null;
	}

	public static Essentials getEssentials() {
		return essentials;
	}

	public static double getStartAmount() {
		return startAmount;
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

	public static long getLastTime() {
		return lastTime;
	}

	public static long getEndTime() {
		return endTime;
	}
}
