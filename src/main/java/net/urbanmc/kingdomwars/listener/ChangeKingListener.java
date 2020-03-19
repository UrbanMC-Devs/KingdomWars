package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.event.NationChangeLeaderEvent;
import com.palmergames.bukkit.towny.event.TownChangeMayorEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Nation;
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
    public void onNationChangeKing(NationChangeLeaderEvent event) {
        // Check if nation is in war
        if (plugin.getWarManager().inWar(event.getNation())) {
            // Cancel event (prevent leader change) if nation is in war
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onTownChangeMayor(TownChangeMayorEvent event) {
        // Check if town has nation
        if (!event.getTown().hasNation())
            return;

        // Get nation
        Nation nat = null;

        try {
            nat = event.getTown().getNation();
        } catch (NotRegisteredException ignore) {
            return;
        }

        // Check
        if (!nat.getCapital().equals(event.getTown())) {
            return;
        }

        // Check if nation is in war
        if (plugin.getWarManager().inWar(nat)) {
            // Cancel event (prevent leader change) if nation is in war
            event.setCancelled(true);
        }
    }

}
