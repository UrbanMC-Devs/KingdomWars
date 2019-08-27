package net.urbanmc.kingdomwars;

import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.Towny;
import net.urbanmc.kingdomwars.command.BaseCommand;
import net.urbanmc.kingdomwars.listener.NationListener;
import net.urbanmc.kingdomwars.listener.WarListener;
import net.urbanmc.kingdomwars.util.ConfigManager;
import net.urbanmc.kingdomwars.util.QuestionUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class KingdomWars extends JavaPlugin {

	private static Towny towny;
	private static QuestionUtil questionUtil;
	private static Essentials essentials;
	private static KingdomWars instance;

	public static KingdomWars getInstance() { return instance; }

	@Override
	public void onEnable() {

		// Check for Towny
		if (!getServer().getPluginManager().isPluginEnabled("Towny")) {
			getLogger().log(Level.SEVERE, "Towny plugin was not found! Disabling..");
			setEnabled(false);
			return;
		}

		towny = getPlugin(Towny.class);

		if (towny.isError()) {
			getLogger().log(Level.SEVERE, "There was an error while enabling Towny. Disabling..");
			setEnabled(false);
			return;
		}

		// Set static instance
		instance = this;

		// Check for soft depend essentials (EssentialsX)
		if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
			essentials = getPlugin(Essentials.class);
		}

		questionUtil = new QuestionUtil(this);

		// Load current war data
		WarUtil.loadWarData();

		// Load config options
		new ConfigManager();

		WarUtil.filterLeaderboard();

		// Register command
		getCommand("townywar").setExecutor(new BaseCommand());

		// Register events
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new NationListener(this), this);
		pm.registerEvents(new WarListener(), this);
	}

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

	public static boolean playerIsJailed(Player p) {
		return (p.hasMetadata("townyoutlawjailed"));
	}

}
