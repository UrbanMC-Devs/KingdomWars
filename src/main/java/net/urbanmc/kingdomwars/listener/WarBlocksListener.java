package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.event.NationCalculateBonusBlocksEvent;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WarBlocksListener implements Listener {

    @EventHandler
    public void onBonusBlockCalc(NationCalculateBonusBlocksEvent event) {
        event.setBonusBlocks(event.getBonusBlocks() + TownyUtil.getNationWarBlocks(event.getNation()));
    }

}
