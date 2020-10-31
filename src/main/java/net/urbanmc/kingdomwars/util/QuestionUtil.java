package net.urbanmc.kingdomwars.util;

import com.sun.org.apache.xpath.internal.operations.Bool;
import net.urbanmc.kingdomwars.KingdomWars;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class QuestionUtil {

    private HashMap<UUID, Runnable[]> nationQuestionMap = new HashMap<>();
    private KingdomWars plugin;

    public QuestionUtil(KingdomWars plugin) {
        this.plugin = plugin;
    }

    public void askQuestion(String question, UUID nationUUID, Runnable accept, Runnable deny, List<Player> targets) {
        if (nationQuestionMap.containsKey(nationUUID))
            return;

        JSONMessageBuilder builder = JSONMessageBuilder.create();

        builder.then("[").color(ChatColor.DARK_AQUA).then("Question").color(ChatColor.GREEN).then("]").color(ChatColor.DARK_AQUA).then(" ")
                .then(question).color(ChatColor.AQUA)
                .then("\n")
                .then("[Accept]").color(ChatColor.GREEN).runCommand("/twars accept").tooltip(JSONMessageBuilder.create("Click to accept the truce!").color(ChatColor.YELLOW))
                .then(" ")
                .then("[Deny]").color(ChatColor.RED).runCommand("/twars deny").tooltip(JSONMessageBuilder.create("Click to deny the truce!").color(ChatColor.RED));

        Runnable[] runnables = new Runnable[2];

        runnables[0] = accept;
        runnables[1] = deny;

        nationQuestionMap.put(nationUUID, runnables);

        builder.send(targets);

        scheduleRemoval(nationUUID);
    }

    private void scheduleRemoval(UUID nationUUID) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin,
                () -> nationQuestionMap.remove(nationUUID),
                20*30);
    }

    public boolean hasTruceRequest(UUID nationID) {
        return nationQuestionMap.containsKey(nationID);
    }

    public void runRunnable(UUID nationID, boolean accept) {
        if (!nationQuestionMap.containsKey(nationID)) return;

       Runnable run =  nationQuestionMap.get(nationID)[accept ? 0 : 1];

       nationQuestionMap.remove(nationID);

       run.run();
    }

}
