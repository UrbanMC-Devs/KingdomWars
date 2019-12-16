package net.urbanmc.kingdomwars.command.subs;

import com.palmergames.bukkit.towny.TownySettings;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.event.WarDeclareEvent;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import net.urbanmc.kingdomwars.manager.WarManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.palmergames.bukkit.towny.object.Nation;

import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.util.TownyUtil;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarStartEvent;

public class Start {

    public Start(Player p, String[] args, final KingdomWars plugin) {

        if (!p.hasPermission("kingdomwars.start")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to do this!");
            return;
        }

        Nation nation1 = TownyUtil.getNation(p);

        if (nation1 == null) {
            p.sendMessage(ChatColor.RED + "You are not in a nation!");
            return;
        }

        if (plugin.getWarManager().alreadyScheduledForWar(nation1.getName())) {
            p.sendMessage(ChatColor.RED + "You are starting a war soon!");
            return;
        }

        if (args.length == 1) {
            p.sendMessage(ChatColor.RED + "Please specify a nation to declare war against.");
            return;
        }

        Nation nation2 = TownyUtil.getNation(args[1]);

        if (nation2 == null) {
            p.sendMessage(ChatColor.RED + "You have not specified a valid nation.");
            return;
        }

        if (plugin.getWarManager().inWar(nation1)) {
            p.sendMessage(ChatColor.RED + "You are already in a war!");
            return;
        }

        if (plugin.getWarManager().inWar(nation2)) {
            p.sendMessage(ChatColor.RED + "That nation is already in a war!");
            return;
        }

        if (plugin.getWarManager().alreadyScheduledForWar(nation2.getName())) {
            p.sendMessage(ChatColor.RED + "That nation is already planning to go to war!");
            return;
        }

        if (plugin.getWarManager().isGraceNation(nation2.getName())) {
            p.sendMessage(ChatColor.RED + "That nation cannot be attacked right now!");
            return;
        }

        if (nation1.isNeutral()) {
            p.sendMessage(ChatColor.RED + "Your nation is peaceful!");
            return;
        }

        if (nation1.getName().equals(nation2.getName())) {
            p.sendMessage(ChatColor.RED + "This plug-in does not support civil wars!");
            return;
        }

        if (nation1.hasAlly(nation2)) {
            p.sendMessage(ChatColor.RED + "You cannot have a war with your ally!");
            return;
        }

        boolean revenge = false;

        if (plugin.getLastWarManager().hasLast(nation1.getName(), nation2.getName())) {
            if (plugin.getLastWarManager().canRevenge(nation1, nation2)) {
                revenge = true;
            } else {
                p.sendMessage(ChatColor.RED + "You cannot have another war with this nation until " + getLast(plugin, nation1, nation2)
                        + " from now!");
                return;
            }
        }

        if (!revenge && nation2.isNeutral()) {
            p.sendMessage(ChatColor.RED + "That nation is peaceful!");
            return;
        }

        if (TownyUtil.getNationBalance(nation1) < ConfigManager.getStartAmount()) {
            p.sendMessage(ChatColor.RED + "Your nation balance does not have the required amount to start a war! "
                    + ChatColor.GREEN + "($" + ConfigManager.getStartAmount() + ")");
            return;
        }

        if (nation2.getExtraBlocks() <= ConfigManager.getNegTownBlockMin() || TownySettings.getNationBonusBlocks(nation2) < ConfigManager.getTownBlockLoss()) {
            p.sendMessage(ChatColor.RED + "Warning: That nation has already lost a lot of town blocks. Winning a war against them will not give you any extra town blocks!");
        }

        WarDeclareEvent declareEvent = new WarDeclareEvent(nation1.getName(), nation2.getName(), 5);
        Bukkit.getPluginManager().callEvent(declareEvent);

        if (declareEvent.isCancelled())
            return;

        TownyUtil.sendNationMessage(nation1, "Your nation has will be at war against " + nation2.getName() + " in " + declareEvent.getTimeTillWar() + " minutes!");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has declared war against your nation! The war will begin in " + declareEvent.getTimeTillWar() + " minutes!");

        PreWar preWar = new PreWar(nation1.getName(), nation2.getName());
        plugin.getWarManager().addPreWar(preWar);

        preWar.setTask(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    plugin.getWarManager().removePreWar(preWar);
                    startWar(plugin.getWarManager(), nation1, nation2);
                }
                , 20* 60 * declareEvent.getTimeTillWar()));  //20 ticks per second * 60 seconds per minute * Time till War in minutes generates the amount of ticks.

    }

    private String getLast(KingdomWars plugin, Nation nation1, Nation nation2) {
        LastWar last = plugin.getLastWarManager().getLast(nation1, nation2);

        long time = last.isLosingNation(nation1.getName()) ? last.getRevengeMillis() : last.getMillisTillNextWar();

        time -= System.currentTimeMillis();

        if (time < 0) {
            time = 0;
            last.setRevengeMillis(System.currentTimeMillis());
        }

        return ConfigManager.formatTime(time / 1000);
    }

    private void startWar(WarManager warManager, Nation nation1, Nation nation2) {
        War war = new War(nation1.getName(), nation2.getName());

        WarStartEvent event = new WarStartEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        warManager.startWar(war);

        TownyUtil.sendNationMessage(nation1, "Your nation has started a war against " + nation2.getName() + "!");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has began a war against your nation!");
    }
}
