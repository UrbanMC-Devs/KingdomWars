package net.urbanmc.kingdomwars;

import java.util.logging.Level;

import org.bukkit.plugin.java.JavaPlugin;

import ca.xshade.bukkit.questioner.Questioner;

public class KingdomWars extends JavaPlugin {

	static Questioner questionerplugin;
	
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
		} else 
			questionerplugin = (Questioner) getServer().getPluginManager().getPlugin("Questioner");

		
	}
}
