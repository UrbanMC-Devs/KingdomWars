package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.event.nation.NationKingChangeEvent;
import net.urbanmc.kingdomwars.KingdomWars;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChangeKingListener implements Listener {

    private KingdomWars plugin;

    public ChangeKingListener(KingdomWars plugin) {
        this.plugin = plugin;
        // Self-Register Listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onNationChangeKing(NationKingChangeEvent event) {
        // Check if nation is in war
        if (plugin.getWarManager().inWar(event.getNation())) {
            // Cancel event (prevent leader change) if nation is in war
            event.setCancelled(true);
            event.setCancelMessage("Cannot change nation leader while in war!");
        }
    }

}
