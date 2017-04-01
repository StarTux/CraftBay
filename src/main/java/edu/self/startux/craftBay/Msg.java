package edu.self.startux.craftBay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONValue;

public final class Msg {
    private Msg() { }

    public static String format(String msg, Object... args) {
        if (msg == null) return "";
        msg = ChatColor.translateAlternateColorCodes('&', msg);
        if (args.length > 0) {
            msg = String.format(msg, args);
        }
        return msg;
    }

    public static void send(CommandSender to, String msg, Object... args) {
        to.sendMessage(format(msg, args));
    }

    public static void info(CommandSender to, String msg, Object... args) {
        to.sendMessage(format("&r[&3CraftBay&r] ") + format(msg, args));
    }

    public static void warn(CommandSender to, String msg, Object... args) {
        to.sendMessage(format("&r[&cCraftBay&r] &c") + format(msg, args));
    }

    static void consoleCommand(String cmd, Object... args) {
        if (args.length > 0) cmd = String.format(cmd, args);
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), cmd);
    }

    public static void raw(Player player, Object... obj) {
        if (obj.length == 0) return;
        if (obj.length == 1) {
            consoleCommand("minecraft:tellraw %s %s", player.getName(), JSONValue.toJSONString(obj[0]));
        } else {
            consoleCommand("minecraft:tellraw %s %s", player.getName(), JSONValue.toJSONString(Arrays.asList(obj)));
        }
    }

    public static void sendActionBar(Player player, String msg, Object... args) {
        Object o = button(null, format(msg, args), null, null, null);
        consoleCommand("minecraft:title %s actionbar %s", player.getName(), JSONValue.toJSONString(o));
    }

    public static Object button(ChatColor color, String chat, String insertion, String tooltip, String command) {
        Map<String, Object> map = new HashMap<>();
        map.put("text", format(chat));
        if (color != null) {
            map.put("color", color.name().toLowerCase());
        }
        if (insertion != null) {
            map.put("insertion", insertion);
        }
        if (command != null) {
            Map<String, Object> clickEvent = new HashMap<>();
            map.put("clickEvent", clickEvent);
            clickEvent.put("action", command.endsWith(" ") ? "suggest_command" : "run_command");
            clickEvent.put("value", command);
        }
        if (tooltip != null) {
            Map<String, Object> hoverEvent = new HashMap<>();
            map.put("hoverEvent", hoverEvent);
            hoverEvent.put("action", "show_text");
            hoverEvent.put("value", format(tooltip));
        }
        return map;
    }

    public static Object button(ChatColor color, String chat, String tooltip, String command) {
        return button(color, chat, null, tooltip, command);
    }

    public static Object button(String chat, String tooltip, String command) {
        return button(ChatColor.WHITE, chat, null, tooltip, command);
    }

    public static String camelCase(String msg) {
        StringBuilder sb = new StringBuilder();
        for (String tok: msg.split("_")) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(tok.substring(0, 1).toUpperCase());
            sb.append(tok.substring(1).toLowerCase());
        }
        return sb.toString();
    }

    public static Object parseJson(String s) {
        return JSONValue.parse(s);
    }

    public static String toJsonString(Object o) {
        return JSONValue.toJSONString(o);
    }

    public static List<String> wrap(String what, int maxLineLength) {
        String[] words = what.split(" ");
        List<String> lines = new ArrayList<>();
        if (words.length == 0) return lines;
        StringBuilder line = new StringBuilder(words[0]);
        int lineLength = ChatColor.stripColor(words[0]).length();
        for (int i = 1; i < words.length; ++i) {
            String word = words[i];
            int wordLength = ChatColor.stripColor(word).length();
            if (lineLength + wordLength > maxLineLength) {
                lines.add(line.toString());
                line = new StringBuilder(word);
                lineLength = wordLength;
            } else {
                line.append(" ");
                line.append(word);
                lineLength += wordLength + 1;
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        return lines;
    }

    public static String wrap(String what, int maxLineLength, String endl) {
        List<String> lines = wrap(what, maxLineLength);
        return fold(lines, endl);
    }

    public static String fold(List<String> ls, String glue) {
        if (ls.isEmpty()) return "";
        StringBuilder sb = new StringBuilder(ls.get(0));
        for (int i = 1; i < ls.size(); ++i) sb.append(glue).append(ls.get(i));
        return sb.toString();
    }
}
