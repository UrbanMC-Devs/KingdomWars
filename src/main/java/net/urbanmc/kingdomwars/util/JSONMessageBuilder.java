package net.urbanmc.kingdomwars.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;

public class JSONMessageBuilder {

    private final ComponentBuilder builder;
    private TextComponent lastComp;

    private JSONMessageBuilder(String text) {
        lastComp = new TextComponent(text);
        builder = new ComponentBuilder("");
    }

    public static JSONMessageBuilder create() {
        return create("");
    }

    public static JSONMessageBuilder create(String text) {
        return new JSONMessageBuilder(text);
    }

    public JSONMessageBuilder then(String text) {
        builder.append(lastComp);

        lastComp = new TextComponent(text);
        lastComp.setHoverEvent(null);
        lastComp.setClickEvent(null);
        return this;
    }

    public JSONMessageBuilder then(TextComponent comp) {
        builder.append(lastComp);
        lastComp = comp;
        return this;
    }

    public JSONMessageBuilder color(ChatColor color) {
        lastComp.setColor(net.md_5.bungee.api.ChatColor.valueOf(color.name()));
        return this;
    }

    public JSONMessageBuilder color(net.md_5.bungee.api.ChatColor color) {
        lastComp.setColor(color);
        return this;
    }

    public JSONMessageBuilder runCommand(String command) {
        lastComp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return this;
    }

    public JSONMessageBuilder tooltip(String tooltip) {
        lastComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tooltip).create()));
        return this;
    }

    public JSONMessageBuilder tooltip(JSONMessageBuilder tooltip) {
        lastComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip.compile()));
        return this;
    }

    public JSONMessageBuilder style(ChatColor style) {
        switch (style) {
            case BOLD:
                lastComp.setBold(true);
                break;
            case STRIKETHROUGH:
                lastComp.setStrikethrough(true);
                break;
            case ITALIC:
                lastComp.setItalic(true);
                break;
            case MAGIC:
                lastComp.setObfuscated(true);
                break;
            case RESET:
                lastComp.setBold(false);
                lastComp.setObfuscated(false);
                lastComp.setItalic(false);
                lastComp.setStrikethrough(false);
                break;
            default:
                break;
        }

        return this;
    }

    private BaseComponent[] compile() {
        if (lastComp != null) builder.append(lastComp);
        return builder.create();
    }

    public BaseComponent[] toBaseComponentArray() {
        return compile();
    }

    public TextComponent getLastComponent() {
        return lastComp;
    }

    public void send(Player... players) {
        if (players == null) return;

        final BaseComponent[] compiled = compile();
        this.lastComp = null;

        for (Player player : players) {
            player.spigot().sendMessage(compiled);
        }
    }

    public void send(Collection<Player> players) {
        if (players == null) return;

        final BaseComponent[] compiled = compile();
        this.lastComp = null;

        for (Player player : players) {
            player.spigot().sendMessage(compiled);
        }
    }



}
