package net.urbanmc.kingdomwars.command;


import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.ConditionFailedException;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import co.aikar.commands.annotation.CatchUnknown;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Conditions;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.contexts.IssuerAwareContextResolver;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.object.Nation;
import net.md_5.bungee.api.chat.TextComponent;
import net.urbanmc.kingdomwars.KingdomWars;
import net.urbanmc.kingdomwars.WarBoard;
import net.urbanmc.kingdomwars.data.LastWar;
import net.urbanmc.kingdomwars.data.PreWar;
import net.urbanmc.kingdomwars.data.war.War;
import net.urbanmc.kingdomwars.event.WarDeclareEvent;
import net.urbanmc.kingdomwars.event.WarRequestAlliesEvent;
import net.urbanmc.kingdomwars.event.WarTruceEvent;
import net.urbanmc.kingdomwars.manager.ConfigManager;
import net.urbanmc.kingdomwars.util.ArchiveFormatter;
import net.urbanmc.kingdomwars.util.JSONMessageBuilder;
import net.urbanmc.kingdomwars.util.TownyUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

@CommandAlias("townywar|twar|twars")
public class TWarsCmd extends BaseCommand {

    @Dependency
    KingdomWars plugin;

    // Register all the resolvers
    public static void registerResolvers(KingdomWars plugin, PaperCommandManager cmdManager) {
        final IssuerAwareContextResolver<Nation, BukkitCommandExecutionContext> nationContext = (c) -> {
            Nation nation;

            if (c.hasFlag("self")) {
                Player p = c.getPlayer();
                nation = TownyUtil.getNation(p);

                if (nation == null)
                    throw new InvalidCommandArgument("You are not in a nation!", false);
            }
            else {
                String nationName = c.popFirstArg();
                nation = nationName != null ? TownyUtil.getNation(nationName) : null;

                if (nation == null)
                    throw new InvalidCommandArgument("Invalid nation specified!", false);
            }

            return nation;
        };

        cmdManager.getCommandContexts().registerIssuerAwareContext(Nation.class, nationContext);

        cmdManager.getCommandContexts().registerIssuerAwareContext(War.class, c -> {
            Nation nation = nationContext.getContext(c);

            War war = plugin.getWarManager().getWar(nation);

            if (war == null) {
                throw new InvalidCommandArgument((c.hasFlag("self") ? "Your nation" : nation.getName()) + " is not at war!",
                        false);
            }

            return war;
        });

        cmdManager.getCommandContexts().registerIssuerAwareContext(PreWar.class, c -> {
            Nation nation;

            if (c.hasFlag("previous")) {
                nation = (Nation) c.getResolvedArg(Nation.class);
                if (nation == null)
                    throw new InvalidCommandArgument("Error previously resolving nation!", false);
            }
            else {
                nation = nationContext.getContext(c);
            }

            String prefix = c.hasFlag("self") ? "Your" : "That";

            if (plugin.getWarManager().inWar(nation)) {
                throw new InvalidCommandArgument("It's too late! " + prefix + " nation is already at war!", false);
            }

            PreWar preWar = plugin.getWarManager().getPreWar(nation.getName());

            if (preWar == null) {
                throw new InvalidCommandArgument(prefix + " nation is not scheduled for war!",false);
            }

            return preWar;
        });

        cmdManager.getCommandConditions().addCondition(Nation.class, "non-neutral", (c, exec, value) -> {
            if (value.isNeutral())
                throw new ConditionFailedException("Nation " + value.getName() + " is neutral!");
        });

        cmdManager.getCommandConditions().addCondition(Nation.class, "peacetime", (c, exec, value) -> {
            if (plugin.getWarManager().alreadyScheduledForWar(value.getName()))
                throw new ConditionFailedException(value.getName() + " is already scheduled to fight in a war!");
            else if (plugin.getWarManager().inWar(value))
                throw new ConditionFailedException(value.getName() + "is already fighting in a war!");
        });

        cmdManager.getCommandCompletions().registerAsyncCompletion("nations", c -> TownyUtil.getNationNames());
    }

    @Subcommand("start")
    @CommandPermission("kingdomwars.start")
    @Description("Start a war with another nation!")
    @CommandCompletion("@nations")
    public void startWar(Player p, @Flags("self") @Conditions("non-neutral|peacetime") Nation playerNation,
                                                    @Conditions("peacetime") Nation enemyNation) {

        if (plugin.getWarManager().isGraceNation(enemyNation)) {
            p.sendMessage(ChatColor.RED + "That nation cannot be attacked right now!");
            return;
        }

        if (TownyUtil.isSameNation(playerNation, enemyNation)) {
            p.sendMessage(ChatColor.RED + "This plugin does not support civil wars!");
            return;
        }

        if (playerNation.hasAlly(enemyNation)) {
            p.sendMessage(ChatColor.RED + "You cannot have a war with your ally!");
            return;
        }

        boolean revenge = false;

        if (plugin.getArchiveManager().hasRecentWar(playerNation.getName(), enemyNation.getName())) {
            if (plugin.getArchiveManager().canRevenge(playerNation, enemyNation)) {
                revenge = true;
            } else {
                p.sendMessage(ChatColor.RED + "You cannot have another war with this nation until " + getLast(plugin, playerNation, enemyNation)
                        + " from now!");
                return;
            }
        }

        if (!revenge && enemyNation.isNeutral()) {
            p.sendMessage(ChatColor.RED + "That nation is peaceful!");
            return;
        }

        if (TownyUtil.getNationBalance(playerNation) < ConfigManager.getStartAmount()) {
            p.sendMessage(ChatColor.RED + "Your nation balance does not have the required amount to start a war! "
                    + ChatColor.GREEN + "($" + ConfigManager.getStartAmount() + ")");
            return;
        }

        if (TownyUtil.getNationWarBlocks(enemyNation) <= ConfigManager.getNegTownBlockMin() || TownySettings.getNationBonusBlocks(enemyNation) < ConfigManager.getTownBlockLoss()) {
            p.sendMessage(ChatColor.RED + "Warning: That nation has already lost a lot of town blocks. Winning a war against them will not give you any extra town blocks!");
        }

        declareWar(playerNation, enemyNation);
    }

    @Subcommand("forcestart")
    @CommandPermission("kingdomwars.forcestart")
    @Description("Force start a war between two nations!")
    @CommandCompletion("@nations @nations")
    public void forceStart(CommandSender sender, @Conditions("non-neutral|peacetime") Nation nation1,
                           @Conditions("non-neutral|peacetime") Nation nation2) {
        String nation1Name = nation1.getName(),
                nation2Name = nation2.getName();

        if (TownyUtil.isSameNation(nation1, nation2)) {
            sender.sendMessage(ChatColor.RED + "This plug-in does not support civil wars!");
            return;
        }

        if (nation1.hasAlly(nation2)) {
            sender.sendMessage(ChatColor.RED + nation1.getName() + " is allied with " + nation2.getName() + "!");
            return;
        }

        boolean revenge = false;

        if (plugin.getArchiveManager().hasRecentWar(nation1Name, nation2Name)) {
            if (plugin.getArchiveManager().canRevenge(nation1, nation2)) {
                revenge = true;
            } else {
                sender.sendMessage(ChatColor.RED + "You cannot have another war with this nation until " + getLast(plugin, nation1, nation2)
                        + " seconds from now!");
                return;
            }
        }

        if (!revenge && nation2.isNeutral()) {
            sender.sendMessage(ChatColor.RED + "That nation is neutral!");
            return;
        }

        if (TownyUtil.getNationBalance(nation1) < ConfigManager.getStartAmount()) {
            sender.sendMessage(ChatColor.RED + "Your nation balance does not have the required amount to start a war! "
                    + ChatColor.GREEN + "($" + ConfigManager.getStartAmount() + ")");
            return;
        }

        declareWar(nation1, nation2);
    }

    private String getLast(KingdomWars plugin, Nation nation1, Nation nation2) {
        LastWar last = plugin.getArchiveManager().getRecentWar(nation1, nation2);

        long nextWarDelay = last.isLosingNation(nation1.getName()) ? ConfigManager.getLastTimeRevenge() : ConfigManager.getLastTime();

        nextWarDelay += last.getEndTime();

        nextWarDelay -= System.currentTimeMillis();

        if (nextWarDelay < 0) {
            nextWarDelay = 0;
        }

        return ConfigManager.formatTime(nextWarDelay / 1000);
    }

    private void declareWar(Nation nation1, Nation nation2) {
        WarDeclareEvent declareEvent = new WarDeclareEvent(nation1.getName(), nation2.getName(), ConfigManager.getStartDelay());
        Bukkit.getPluginManager().callEvent(declareEvent);

        if (declareEvent.isCancelled())
            return;

        TownyUtil.sendNationMessage(nation1, "Your nation has will be at war against " + nation2.getName() + " in " + declareEvent.getTimeTillWar() + " minutes!");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has declared war against your nation! The war will begin in " + declareEvent.getTimeTillWar() + " minutes!");

        PreWar preWar = new PreWar(nation1.getName(), nation2.getName());

        plugin.getWarManager().declareWar(preWar);

        preWar.setTask(Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getWarManager().startWar(preWar),
                20* 60 * declareEvent.getTimeTillWar()));  //20 ticks per second * 60 seconds per minute * Time till War in minutes generates the amount of ticks.

    }

    @Subcommand("end")
    @CommandPermission("kingdomwars.end")
    @Description("End a war you started!")
    public void endWar(Player p, @Flags("self") Nation nation1, @Flags("self") War war) {
        String nationName = nation1.getName();

        int declaringPoints = war.getDeclaringPoints(), declaredPoints = war.getDeclaredPoints();

        if (war.isDeclaringNation(nationName) && declaredPoints > declaringPoints ||
                !war.isDeclaringNation(nationName) && declaringPoints > declaredPoints) {
            p.sendMessage(ChatColor.RED + "Your nation cannot end the war because your nation is losing!");
            return;
        } else if (declaringPoints == declaredPoints && !war.isDeclaringNation(nationName)) {
            p.sendMessage(ChatColor.RED + "Your nation cannot end the war because your nation did not start it!");
            return;
        }

        plugin.getWarManager().declaringEndWar(war);

        Nation nation2 = war.getOtherNation(nation1);

        if (nation2 == null) {
            Bukkit.getLogger()
                    .log(Level.WARNING, "[KingdomWars] Error while getting nation " + war.getDeclaredNation());
            return;
        }

        TownyUtil.sendNationMessage(nation1, "Your nation has ended the war against " + nation2.getName() + ".");
        TownyUtil.sendNationMessage(nation2, nation1.getName() + " has ended the war against your nation.");
    }

    @Subcommand("truce")
    @CommandPermission("kingdomwars.truce")
    @Description("Declare a truce with the nation you are at war against!")
    public void truceWar(Player p, @Flags("self") Nation nation, @Flags("self") War war) {
        String nationName = nation.getName();

        int declaringPoints = war.getDeclaringPoints(), declaredPoints = war.getDeclaredPoints();

        if (war.isDeclaringNation(nationName) && declaringPoints > declaredPoints ||
                !war.isDeclaringNation(nationName) && declaredPoints > declaringPoints) {
            p.sendMessage(
                    ChatColor.RED + "Your nation is winning the war! You must do /twars end instead.");
            return;
        } else if (declaringPoints == declaredPoints && war.isDeclaringNation(nationName)) {
            p.sendMessage(
                    ChatColor.RED + "You are the nation that started the war! You must do /twars end instead.");
            return;
        }

        Nation receivingNation = war.getOtherNation(nation);

        WarTruceEvent event = new WarTruceEvent(war);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        TownyMessaging.sendNationMessage(nation, "Your nation has requested a truce with " + receivingNation.getName());
        TownyUtil.truceQuestion(plugin, receivingNation, nation);
    }

    @Subcommand("status")
    @CommandPermission("kingdomwars.status")
    @Description("Toggle the war scoreboard")
    public void statusBoard(Player p, @Flags("self") War war) {
        UUID id = p.getUniqueId();

        boolean disabled = war.isDisabled(id);
        war.setDisabled(id, !disabled);

        if (disabled) {
            WarBoard.showBoard(plugin, p);
        } else {
            p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        p.sendMessage(ChatColor.GOLD + "War scoreboard has been " + (disabled ? "enabled" : "disabled") + ".");
    }

    @Subcommand("forceend")
    @CommandPermission("kingdomwars.forceend")
    @Description("Force end a war between two nations!")
    @CommandCompletion("@nations")
    public void forceEnd(CommandSender sender, Nation nation) {
        if (plugin.getWarManager().alreadyScheduledForWar(nation.getName())) {
            PreWar preWar = plugin.getWarManager().getPreWar(nation.getName());

            if (preWar.isAlly(nation.getName())) {
                sender.sendMessage(ChatColor.RED + "This nation is scheduled to be an ally to another nation in a war!");
                return;
            }

            sender.sendMessage(ChatColor.GOLD + "You have cancelled the scheduled war.");

            preWar.cancelTask();
            plugin.getWarManager().cancelDeclaredWar(preWar);

            Nation otherNation = TownyUtil.getNation(preWar.getOtherNation(nation.getName()));

            if (otherNation != null) {
                TownyUtil.sendNationMessage(nation,
                        "Your war against " + otherNation.getName() + " has been cancelled by an admin.");
            }

            TownyUtil.sendNationMessage(otherNation,
                    "Your war against " + nation.getName() + " has been cancelled by an admin.");
        }
        else if (!plugin.getWarManager().inWar(nation)) {
            sender.sendMessage(ChatColor.RED + "That nation is not in a war!");
        }
        else {
            War war = plugin.getWarManager().getWar(nation);

            plugin.getWarManager().forceEnd(war);

            sender.sendMessage(ChatColor.GOLD + "Ended war.");

            Nation otherNation = war.getOtherNation(nation);

            TownyUtil.sendNationMessage(nation,
                    "Your war against " + otherNation.getName() + " has been ended by an admin.");
            TownyUtil.sendNationMessage(otherNation,
                    "Your war against " + nation.getName() + " has been ended by an admin.");
        }
    }

    @Subcommand("wars")
    @CommandPermission("kingdomwars.wars")
    @Description("View the current wars!")
    public void wars(Player p) {
        plugin.getWarManager().checkForceEndAll();
        p.sendMessage(ChatColor.GREEN + " === Current Wars ===");

        Collection<War> wars = plugin.getWarManager().getCurrentWars();

        if (!wars.isEmpty()) {
            JSONMessageBuilder builder = JSONMessageBuilder.create();

            final TextComponent vsComp = new TextComponent(" vs ");
            vsComp.setItalic(true);
            vsComp.setColor(net.md_5.bungee.api.ChatColor.WHITE);
            vsComp.setHoverEvent(null);

            int iterator = 1;
            for (War war : wars) {
                builder.then(war.getDeclaringNation()).color(ChatColor.RED).style(ChatColor.BOLD).tooltip(""+war.getDeclaringPoints())
                        .then(vsComp)
                        .then(war.getDeclaredNation()).color(ChatColor.AQUA).style(ChatColor.BOLD).tooltip("" + war.getDeclaredPoints());

                if (iterator++ < wars.size()) {
                    builder.then("\n");
                }
            }

            builder.send(p);
        } else {
            p.sendMessage(ChatColor.ITALIC + "No Current Wars!");
        }

        p.sendMessage(ChatColor.GREEN + "=======");
    }

    @Subcommand("leaderboard")
    @CommandPermission("kingdomwars.leaderboard")
    @Description("Check out which nation has the most wins and losses!")
    public void leaderboard(Player player) {
        player.sendMessage(ChatColor.GREEN + "Fetching leaderboard...");
        plugin.getLeaderboard().getLeaderboard()
                .thenAccept(lbList -> {
                   if (lbList.isEmpty()) {
                       player.sendMessage(
                               ChatColor.GREEN + "=== Kingdom Wars Leaderboard ===\n" + ChatColor.GRAY + "No current data!"
                       );
                   }
                   else {
                       JSONMessageBuilder msgBuilder = JSONMessageBuilder.create();
                       for (int i = 0; i < lbList.size(); i++) {
                           msgBuilder.then(i + 1 + ". ").color(net.md_5.bungee.api.ChatColor.YELLOW);
                           ArchiveFormatter.buildLeaderBoard(msgBuilder, lbList.get(i));

                           if (i + 1 < lbList.size())
                               msgBuilder.then("\n");
                       }

                       player.sendMessage(ChatColor.GREEN + "=== Kingdom Wars Leaderboard ===\n"
                               + ChatColor.GREEN + "Nation " + ChatColor.AQUA + "Wins " + ChatColor.RED + "Losses");
                       msgBuilder.send(player);
                   }
                });
    }

    @Subcommand("reload")
    @Description("Reload the plugin!")
    @CommandPermission("kingdomwars.reload")
    public void reload(CommandSender sender) {
        new ConfigManager();
        plugin.getWarManager().loadCurrentWars();
        plugin.getArchiveManager().loadRecentWars();
        sender.sendMessage(ChatColor.GREEN + "KingdomWars has been reloaded!");
    }

    @Subcommand("save")
    @CommandPermission("kingdomwars.save")
    @Description("Save the plugin and its data!")
    public void save(CommandSender sender) {
        plugin.getWarManager().saveCurrentWars();
        sender.sendMessage(ChatColor.GREEN + "KingdomWars has been saved!");
    }

    @Subcommand("callallies")
    @CommandPermission("kingdomwars.callallies")
    @Description("Call for allies in a war. Must be before the war starts during the preparation period!")
    public void callAllies(Player p, @Flags("self") Nation nation1, @Flags("self") PreWar preWar) {
        if (!preWar.isMainNation(nation1.getName())) {
            p.sendMessage(ChatColor.RED + "You must be one of the main nations in the war!");
            return;
        }

        if (preWar.hasAllies()) {
            p.sendMessage(ChatColor.RED + "Allies have already been called for!");
            return;
        }

        WarRequestAlliesEvent event = new WarRequestAlliesEvent(preWar, ConfigManager.getAllyStartDelay());

        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) return;

        preWar.callAllies();

        Nation declaringNation = nation1.getName().equalsIgnoreCase(preWar.getDeclaringNation()) ? nation1 : TownyUtil.getNation(preWar.getDeclaringNation());
        Nation declaredNation = declaringNation.getName().equalsIgnoreCase(nation1.getName()) ? TownyUtil.getNation(preWar.getDeclaredNation()) : nation1;

        // Send message to allied nations
        for (Nation allyNation : declaringNation.getAllies()) {
            if (allyNation == null) continue;

            TownyUtil.sendNationMessage(allyNation, declaringNation.getName() + " has called for allies! Join them in the war!");
        }

        for (Nation allyNation : declaredNation.getAllies()) {
            if (allyNation == null) continue;

            TownyUtil.sendNationMessage(allyNation, declaredNation.getName() + " is under attack! Their war allows for allies, join them!");
        }

        Nation oppNation = (nation1.equals(declaringNation) ? declaredNation : declaringNation);

        TownyUtil.sendNationMessage(nation1, "Your nation has called for allies in the war against " + oppNation.getName() + ". The war will now begin in 10 minutes!");
        TownyUtil.sendNationMessage(oppNation, nation1.getName() + " has called for allies in the war against your nation. Allies may join you as well! The war will now begin in 10 minutes!");

        preWar.cancelTask();

        //20 ticks per second * 60 seconds per minute * Time till War in minutes generates the amount of ticks.
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getWarManager().startWar(preWar),
                20* 60 * event.getPreparationTime());

        preWar.setTask(task);
    }

    @Subcommand("joinwar")
    @CommandPermission("kingdomwars.joinwar")
    @Description("Join an ally's war if they have called for you!")
    @CommandCompletion("@nations")
    public void joinWar(Player p, @Flags("self") @Conditions("peacetime") Nation playerNation, Nation targetNation) {
        if (plugin.getWarManager().inWar(targetNation)) {
            p.sendMessage(ChatColor.RED + "That nation is already in a war! It is too late to join them!");
            return;
        }

        PreWar preWar = plugin.getWarManager().getPreWar(targetNation.getName());

        if (preWar == null) {
            p.sendMessage(ChatColor.RED + "That nation is not stating a war soon!");
            return;
        }

        if (!preWar.isAlly(targetNation.getName())) {
            p.sendMessage(ChatColor.RED + "That nation is allying in another nation's war!");
            return;
        }

        if (!playerNation.getAllies().contains(targetNation) || !targetNation.getAllies().contains(playerNation)) {
            p.sendMessage(ChatColor.RED + "Your nation must be allied with the other nation! That nation must also be allied with you!");
            return;
        }

        if (!preWar.hasAllies()) {
            p.sendMessage(ChatColor.RED + "You can't join a war when no allies have been requested!");
            return;
        }

        boolean isDeclaringAlly = preWar.isDeclaringNation(targetNation.getName());

        if (isDeclaringAlly)
            preWar.addDeclaringAlly(playerNation.getName());
        else
            preWar.addDeclaredAlly(playerNation.getName());

        TownyUtil.sendNationMessage(targetNation, playerNation.getName() + " has now joined your side in the upcoming war!");
        TownyUtil.sendNationMessage(playerNation, "Your nation is now participating in the war between "  + preWar.getDeclaringNation() + " vs " + preWar.getDeclaredNation() + "!");

        String oppositeSideNationName = isDeclaringAlly ? preWar.getDeclaredNation() : preWar.getDeclaringNation();;

        Nation oppositeSide = TownyUtil.getNation(oppositeSideNationName);
        if (oppositeSide != null)
            TownyUtil.sendNationMessage(oppositeSide, playerNation.getName() + " has joined the war in alliance with " + targetNation.getName() + "!");
    }

    @Subcommand("warblocks")
    @CommandPermission("kingdomwars.warblocks")
    @Description("View and modify the townblocks a nation has from fighting in wars!")
    @CommandCompletion("@nations")
    public void warBlocks(CommandSender sender, Nation targetNation, @Optional Integer amount) {
        int warBlocks = TownyUtil.getNationWarBlocks(targetNation);

        if (amount == null) {
            sender.sendMessage(ChatColor.GREEN + "Current War Townblocks: " + warBlocks);
        }
        else{
            TownyUtil.addNationWarBlocks(targetNation, amount - warBlocks);
            sender.sendMessage(ChatColor.GREEN + "Changed nation's war townblocks to " + amount + " from " + warBlocks);
        }
    }

    @Private
    @Subcommand("accept")
    @CommandPermission("kingdomwars.truce")
    public void acceptTrue(Player p, @Flags("self") Nation nation) {
        acceptDenyTruce(p, nation, true);
    }

    @Private
    @Subcommand("deny")
    @CommandPermission("kingdomwars.truce")
    public void denyTruce(Player p, @Flags("self") Nation nation) {
        acceptDenyTruce(p, nation, false);
    }

    private void acceptDenyTruce(Player p, Nation nation, boolean accept) {
        if (!plugin.getQuestionUtil().hasTruceRequest(nation.getUuid())) {
            p.sendMessage(ChatColor.RED + "Your nation does not have a truce request!");
            return;
        }

        plugin.getQuestionUtil().runRunnable(nation.getUuid(), accept);
    }

    @Default
    @CatchUnknown
    public void info(CommandSender sender) {
        List<RegisteredCommand> cmds = getRegisteredCommands();

        String label = ChatColor.AQUA + "/";

        List<String> output = new ArrayList<>(cmds.size() + 1);
        output.add(ChatColor.AQUA + "=== Kingdom Wars ===");

        for (RegisteredCommand cmd : cmds) {
            if (cmd.isPrivate() || cmd.getHelpText().isEmpty())
                continue;

            boolean hasPerm = true;
            for (Object requiredPermission : cmd.getRequiredPermissions()) {
                if (!sender.hasPermission((String) requiredPermission)) {
                    hasPerm = false;
                    break;
                }
            }

            if (!hasPerm)
                continue;

            output.add(label + cmd.getCommand() + ChatColor.WHITE + ": " + cmd.getHelpText());
        }

        sender.sendMessage(String.join("\n", output));
    }

    @Subcommand("archive")
    @CommandPermission("kingdomwars.archive")
    public class ArchiveCmd extends BaseCommand {

        @Subcommand("nation")
        @CommandPermission("kingdomwars.archive.nation")
        @CommandCompletion("@nations")
        public void view(Player player, Nation target) {
            player.sendMessage(ChatColor.GREEN + "Fetching wars for " + target.getName() + "...");
            final String nationName = target.getName();
            plugin.getArchiveManager().getWarFromNation(target.getUuid()).thenAccept(lw -> {
                if (lw.isEmpty()) {
                    player.sendMessage(ChatColor.GREEN + "No wars for that nation were found!");
                }
                else {
                    JSONMessageBuilder builder = JSONMessageBuilder.create();
                    for (int i = 0; i < lw.size(); i++) {
                        try {
                            builder.then(i + 1 + ". ").color(ChatColor.AQUA);
                            ArchiveFormatter.quickDisplay(builder, lw.get(i));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        if (i + 1 != lw.size())
                            builder.then("\n");
                    }

                    player.sendMessage(ChatColor.GREEN + "=\\= " + ChatColor.GOLD + nationName + ChatColor.GREEN +  "'s Wars =/=:");
                    builder.send(player);
                }
            });
        }

        @Subcommand("id")
        @CommandPermission("kingdomwars.archive.id")
        public void view(Player player, int archiveID) {
            player.sendMessage(ChatColor.GREEN + "Fetching war with that ID...");
            // Display war
            plugin.getArchiveManager().getWarFromID(archiveID).thenAccept(lw -> {
                if (lw == null) {
                    player.sendMessage(ChatColor.RED + "No wars with that ID were found!");
                }
                else {
                    JSONMessageBuilder builder = JSONMessageBuilder.create();
                    ArchiveFormatter.fullDisplay(builder, lw);
                    builder.send(player);
                }
            });
        }

        @Subcommand("refresh")
        @CommandPermission("kingdomwars.archive.refresh")
        public void refreshCache(CommandSender sender, Nation targetNation) {
            plugin.getArchiveManager().removeNationCache(targetNation.getUuid());
            sender.sendMessage(ChatColor.GREEN + "Refreshed archive cache for nation " + targetNation.getName());
        }

        @Subcommand("remove")
        @CommandPermission("kingdomwars.archive.remove")
        public void remove(CommandSender sender, int archiveID) {
            plugin.getArchiveManager().removeWar(archiveID);
            sender.sendMessage(ChatColor.GREEN + "War with id " + archiveID + " will be deleted!");
        }

    }

}
