/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * Copyright 2012 StarTux
 *
 * This file is part of CraftBay.
 *
 * CraftBay is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CraftBay is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with CraftBay.  If not, see <http://www.gnu.org/licenses/>.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package edu.self.startux.craftBay.command;

import edu.self.startux.craftBay.CraftBayPlugin;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.bukkit.command.CommandSender;

public class CommandParser {
        protected CraftBayPlugin plugin;
        private Map<Class<?>, ParamAttribute> paramMap = new HashMap<Class<?>, ParamAttribute>();
        private Map<String, CommandAttribute> commandMap = new HashMap<String, CommandAttribute>();

        public CommandParser(CraftBayPlugin plugin) {
                this.plugin = plugin;
                registerParameters(this);
                registerCommands(this);
        }

        public void registerCommands(Object instance) {
                for (Method method : instance.getClass().getMethods()) {
                        if (method.isAnnotationPresent(SubCommand.class)) {
                                SubCommand subCommand = method.getAnnotation(SubCommand.class);
                                CommandAttribute attr = new CommandAttribute(this, subCommand, method, instance);
                                commandMap.put(attr.getName().toLowerCase(), attr);
                                for (String alias : attr.getAliases()) {
                                        commandMap.put(alias.toLowerCase(), attr);
                                }
                        }
                }
	}

        public void registerParameters(Object instance) {
                for (Method method : instance.getClass().getMethods()) {
                        if (method.isAnnotationPresent(Parameter.class)) {
                                Parameter param = method.getAnnotation(Parameter.class);
                                ParamAttribute attr = new ParamAttribute(param, method, instance);
                                paramMap.put(attr.getType(), attr);
                        }
                }
        }

        public void callCommand(CommandSender sender, String cmd, List<String> args) {
                CommandAttribute attr = commandMap.get(cmd.toLowerCase());
                if (attr == null) {
                        plugin.warn(sender, plugin.getMessage("command.NoEntry").set("cmd", cmd));
                        return;
                }
                if (!attr.checkPermission(sender)) {
                        plugin.warn(sender, plugin.getMessage("command.NoPerm").set("cmd", cmd));
                        return;
                }
                try {
                        attr.call(sender, preprocess(args));
                } catch (CommandParseException cpe) {
                        plugin.warn(sender, cpe.getLocaleMessage().set(sender).set("cmd", cmd));
                        return;
                }
        }

        public List<String> preprocess(List<String> argl) throws CommandParseException {
                List<String> result = new LinkedList<String>();
                String part = null;
                for (String arg : argl) {
                        if (part == null) {
                                if (arg.length() == 0) {
                                        continue;
                                } else if (arg.charAt(0) == '"') {
                                        part = arg.substring(1, arg.length());
                                } else {
                                        result.add(arg);
                                }
                        } else {
                                if (arg.length() == 0) {
                                        continue;
                                } else if (arg.charAt(arg.length() - 1) == '"') {
                                        result.add(part + " " + arg.substring(0, arg.length() - 1));
                                        part = null;
                                } else {
                                        part += " " + arg;
                                }
                        }
                }
                if (part != null) {
                        throw new CommandParseException(plugin.getMessage("command.UnclosedQuote"));
                }
                return result;
        }

        public ParamAttribute getParam(Class<?> type) {
                return paramMap.get(type);
        }
}
