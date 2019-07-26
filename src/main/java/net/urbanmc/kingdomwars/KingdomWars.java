package net.urbanmc.kingdomwars;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.Towny;
import net.urbanmc.kingdomwars.command.BaseCommand;
import net.urbanmc.kingdomwars.listener.NationListener;
import net.urbanmc.kingdomwars.listener.WarListener;
import net.urbanmc.kingdomwars.util.QuestionUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class KingdomWars extends JavaPlugin {

	private static Towny towny;
	private static QuestionUtil questionUtil;
	private static Essentials essentials;
	private static KingdomWars instance;

	private static double startAmount, finishAmount, truceAmount;
	private static int winningKills, allyKills;
	private static long lastTime, lastTimeRevenge, endTime;

	public static Towny getTowny() {
		return towny;
	}

	public static QuestionUtil getQuestionUtil() { return questionUtil; }

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

	public static int getAllyKills() { return allyKills; }

	public static long getLastTime() {
		return lastTime;
	}

	public static long getLastTimeRevenge() {
		return lastTimeRevenge;
	}

	public static long getEndTime() {
		return endTime;
	}

	public static KingdomWars getInstance() { return instance; }

	public static boolean playerIsJailed(Player p) {
		return (p.hasMetadata("townyoutlawjailed"));
	}

	@Override
	public void onEnable() {
		if (!getServer().getPluginManager().isPluginEnabled("Towny")) {
			getLogger().log(Level.SEVERE, "Towny plugin was not found! Disabling..");
			setEnabled(false);
			return;
		}

		instance = this;

		if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
			essentials = getPlugin(Essentials.class);
		}

		towny = getPlugin(Towny.class);
		questionUtil = new QuestionUtil(this);

		if (towny.isError()) {
			getLogger().log(Level.SEVERE, "There was an error while enabling Towny. Disabling..");
			setEnabled(false);
			return;
		}

		WarUtil.loadWarData();

		loadConfig();
		WarUtil.filterLeaderboard();

		getCommand("townywar").setExecutor(new BaseCommand());

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new NationListener(), this);
		pm.registerEvents(new WarListener(), this);
	}

	private void loadConfig() {
		File file = new File("plugins/KingdomWars/config.yml");

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdir();
		}

		if (!file.exists()) {
			try {
				InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml");

				Files.copy(input, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
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
		lastTimeRevenge = TimeUnit.HOURS.toMillis(data.getInt("hours-between-revenge"));
		endTime = TimeUnit.HOURS.toMillis(data.getInt("hours-end"));
		allyKills = data.getInt("ally-bonus-kills", 5);
	}

}
