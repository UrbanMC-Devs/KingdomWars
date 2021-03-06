package net.urbanmc.kingdomwars.util;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public class JSONMessageBuilder {

    private final ComponentBuilder builder;
    private TextComponent lastComp;

    // Persistant Format
    private net.md_5.bungee.api.ChatColor compColor;
    private ClickEvent persistantClick;
    private HoverEvent persistantHover;

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

    public JSONMessageBuilder flush() {
        if (lastComp != null) {
            applyPersistentFormatting();
            builder.append(lastComp);
            lastComp = null;
        }
        return this;
    }

    public JSONMessageBuilder then(String text) {
        flush();
        lastComp = new TextComponent(text);
        lastComp.setHoverEvent(null);
        lastComp.setClickEvent(null);
        return this;
    }

    public JSONMessageBuilder then(TextComponent comp) {
        flush();
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

    public ClickEvent clickCommand(String command) {
        if (command != null)
            return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        return null;
    }

    public JSONMessageBuilder click(ClickEvent clickEvent) {
        lastComp.setClickEvent(clickEvent);
        return this;
    }

    public JSONMessageBuilder persistClick(ClickEvent event) {
        this.persistantClick = event;
        return this;
    }

    public JSONMessageBuilder tooltip(String tooltip) {
        lastComp.setHoverEvent(tooltipHover(tooltip));
        return this;
    }

    public static HoverEvent tooltipHover(JSONMessageBuilder tooltip) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, tooltip.compile());
    }

    public static HoverEvent tooltipHover(String tooltip) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(tooltip).create());
    }

    public JSONMessageBuilder tooltip(JSONMessageBuilder tooltip) {
        lastComp.setHoverEvent(tooltipHover(tooltip));
        return this;
    }

    public JSONMessageBuilder tooltip(HoverEvent hoverEvent) {
        lastComp.setHoverEvent(hoverEvent);
        return this;
    }

    public JSONMessageBuilder persistTooltip(HoverEvent event) {
        this.persistantHover = event;
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
        flush();
        return builder.create();
    }

    public BaseComponent[] toBaseComponentArray() {
        return compile();
    }

    public TextComponent getLastComponent() {
        return lastComp;
    }

    private void applyPersistentFormatting() {
        if (lastComp != null) {
            if (compColor != null)
                lastComp.setColor(compColor);
            if (persistantClick != null && lastComp.getClickEvent() == null)
                lastComp.setClickEvent(persistantClick);
            if (persistantHover != null && lastComp.getHoverEvent() == null)
                lastComp.setHoverEvent(persistantHover);
        }
    }

    public void send(CommandSender... senders) {
        if (senders == null) return;

        final BaseComponent[] compiled = compile();

        for (CommandSender sender : senders) {
            sender.spigot().sendMessage(compiled);
        }
    }

    public void send(Collection<? extends CommandSender> senders) {
        if (senders == null) return;

        final BaseComponent[] compiled = compile();

        for (CommandSender sender : senders) {
            sender.spigot().sendMessage(compiled);
        }
    }



}
