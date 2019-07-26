package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.object.Nation;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarUtil;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarRequestAlliesEvent;
import net.urbanmc.kingdomwars.event.WarStartEvent;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class CallAlliesSub {

    public CallAlliesSub(Player p) {
        if (!p.hasPermission("kingdomwars.callallies")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        Nation nation1 = TownyUtil.getNation(p);

        if (nation1 == null) {
            p.sendMessage(ChatColor.RED + "You are not in a nation!");
            return;
        }

        if (WarUtil.inWar(nation1)) {
            p.sendMessage(ChatColor.RED + "It's too late to call allies! You're already in a war!");
            return;
        }

        PreWar preWar = WarUtil.getPreWar(nation1.getName());

        if (preWar == null) {
            p.sendMessage(ChatColor.RED + "You aren't in a war!");
            return;
        }

        if (!preWar.isMainNation(nation1.getName())) {
            p.sendMessage(ChatColor.RED + "You must be one of the main nations in the war!");
            return;
        }

        if (preWar.calledForAllies()) {
            p.sendMessage(ChatColor.RED + "Allies have already been called for!");
            return;
        }

        WarRequestAlliesEvent event = new WarRequestAlliesEvent(preWar);

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        preWar.callAllies();

        Nation declaringNation = nation1.getName().equalsIgnoreCase(preWar.getDeclaringNation()) ? nation1 : TownyUtil.getNation(preWar.getDeclaringNation());
        Nation declaredNation = declaringNation.getName().equalsIgnoreCase(nation1.getName()) ? TownyUtil.getNation(preWar.getDeclaredNation()) : nation1;

        TownyUtil.sendNationMessage(nation1, "Your nation has called for allies in the war against " + (nation1.equals(declaringNation) ? declaredNation : declaringNation).getName() + ". The war will now begin in 10 minutes!");
        TownyUtil.sendNationMessage(nation1.equals(declaringNation) ? declaredNation : declaringNation, nation1.getName() + " has called for allies in the war against your nation. Allies may join you as well! The war will now begin in 10 minutes!");

        preWar.cancelTask();

        preWar.setTask(Bukkit.getScheduler().runTaskLater(KingdomWars.getInstance(), () -> {
                    WarUtil.removePreWar(preWar);
                    startWar(preWar, declaringNation, declaredNation);
                }
                , 20* 60 * event.getPreparationTime()));  //20 ticks per second * 60 seconds per minute * Time till War in minutes generates the amount of ticks.
    }

    private void startWar(PreWar preWar, Nation nation1, Nation nation2) {
        War war = new War(nation1.getName(), nation2.getName());

        preWar.getAllies(true).forEach(war::addNation1Ally);

        preWar.getAllies(false).forEach(war::addNation2Ally);

        WarStartEvent event = new WarStartEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        WarUtil.startWar(war);

        TownyUtil.sendNationMessage(nation1, "Your nation has started a war against " + nation2.getName() + "!");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has began a war against your nation!");

        Nation tempNat;
        for (String ally : war.getAllies(true)) {
            tempNat = TownyUtil.getNation(ally);

            if(tempNat == null) continue;

            TownyUtil.sendNationMessage(tempNat, "The war between " + nation1.getName() + " and " + nation2.getName() + " has started! Join the fight!");
        }

        for (String ally : war.getAllies(false)) {
            tempNat = TownyUtil.getNation(ally);

            if(tempNat == null) continue;

            TownyUtil.sendNationMessage(tempNat, "The war between " + nation1.getName() + " and " + nation2.getName() + " has started! Join the fight!");
        }
    }

}
