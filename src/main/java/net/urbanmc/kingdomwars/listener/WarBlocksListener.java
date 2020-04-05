package net.urbanmc.kingdomwars.listener;

import com.palmergames.bukkit.towny.event.NationBonusCalculationEvent;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class WarBlocksListener implements Listener {

    @EventHandler
    public void onBonusBlockCalc(NationBonusCalculationEvent event) {
        event.setBonusBlocks(event.getBonusBlocks() + TownyUtil.getNationWarBlocks(event.getNation()));
    }

}
