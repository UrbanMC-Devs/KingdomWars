package net.urbanmc.townwars;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

public class TownWars extends JavaPlugin {

	@Override
	public void onEnable() {
		if (!getServer().getPluginManager().isPluginEnabled("Towny")) {
			getLogger().log(Level.SEVERE, "Towny plugin was not found! Disabling..");
			setEnabled(false);
			return;
		}

		
	}
}
