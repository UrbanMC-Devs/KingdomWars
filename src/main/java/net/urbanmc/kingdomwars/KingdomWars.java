package net.urbanmc.kingdomwars;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import com.earth2me.essentials.Essentials;
import com.palmergames.bukkit.towny.Towny;
import net.urbanmc.kingdomwars.command.TWarsCmd;
import net.urbanmc.kingdomwars.listener.ChangeKingListener;
import net.urbanmc.kingdomwars.listener.NationListener;
import net.urbanmc.kingdomwars.listener.WarBlocksListener;
import net.urbanmc.kingdomwars.listener.WarListener;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import net.urbanmc.kingdomwars.manager.LeaderboardManager;
import net.urbanmc.kingdomwars.manager.WarArchiveManager;
import net.urbanmc.kingdomwars.manager.WarManager;
import net.urbanmc.kingdomwars.util.QuestionUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;
import java.util.logging.Logger;

public class KingdomWars extends JavaPlugin {

	private static Logger pluginLogger;

	private Towny towny;
	private QuestionUtil questionUtil;
	private Essentials essentials;

	private WarManager warManager;
	private LeaderboardManager lbManager;
	private WarArchiveManager archiveManager;

	@Override
	public void onEnable() {
		pluginLogger = getLogger();

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

		// Check for soft depend essentials (EssentialsX)
		if (getServer().getPluginManager().isPluginEnabled("Essentials")) {
			essentials = getPlugin(Essentials.class);
		}

		questionUtil = new QuestionUtil(this);

		warManager = new WarManager(this);
		lbManager = new LeaderboardManager(getDataFolder());
		archiveManager = new WarArchiveManager(getDataFolder());

		// Load current war data
		warManager.loadCurrentWars();

		// Load leaderboard
		lbManager.loadLeaderboard();

		if (!archiveManager.testAccess()) {
			getLogger().severe("Error loading last wars!");
		}
		else {
			// Load last war
			archiveManager.loadRecentWars();
		}

		// Load config options
		new ConfigManager();

		// Register Command
		PaperCommandManager cmdManager = new PaperCommandManager(this);
		TWarsCmd.registerResolvers(this, cmdManager);
		BaseCommand cmd = new TWarsCmd();
		cmdManager.registerCommand(cmd);

		// Register events
		PluginManager pm = getServer().getPluginManager();

		pm.registerEvents(new NationListener(this), this);
		pm.registerEvents(new WarListener(this), this);

		// Register events that are only in the Silver version of Towny
		registerChangeLeaderListener();
		registerWarBlocksListener();
	}

	@Override
	public void onDisable() {
		warManager.saveCurrentWars();
	}

	private void registerChangeLeaderListener() {
		// Check if event class exists
		try {
			Class.forName("com.palmergames.bukkit.towny.event.NationChangeLeaderEvent");
		} catch (ClassNotFoundException ex) {
			getLogger().warning("Could not find NationChangeLeader Event. Skipping registering listener for that event!");
			return;
		}

		new ChangeKingListener(this);
	}

	private void registerWarBlocksListener() {
		// Check if event class exists
		try {
			Class.forName("com.palmergames.bukkit.towny.event.NationBonusCalculationEvent");
		} catch (ClassNotFoundException ex) {
			getLogger().warning("Could not find calculate NationBonusCalculationEvent. Skipping registering listener for that event!");
			return;
		}

		Bukkit.getPluginManager().registerEvents(new WarBlocksListener(), this);
	}

	public Towny getTowny() {
		return towny;
	}

	public QuestionUtil getQuestionUtil() { return questionUtil; }

	public boolean hasEssentials() {
		return essentials != null;
	}

	public Essentials getEssentials() {
		return essentials;
	}

	public static boolean playerIsJailed(Player p) {
		return (p.hasMetadata("townyoutlawjailed"));
	}

	public WarManager getWarManager() { return warManager; }

	public LeaderboardManager getLeaderboard() { return lbManager; }


	public WarArchiveManager getArchiveManager() {
		return archiveManager;
	}

	public static Logger logger() {
		return pluginLogger;
	}

}
