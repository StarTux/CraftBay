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
import java.lang.Class;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.bukkit.command.CommandSender;

public class CommandAttribute {
        private CommandParser parent;
        private Method method;
        private Object instance;
        private String name;
        private List<String> aliases;
        private String permission = null;
        private boolean shortcut;
        private int optionalArgc = 0;
        private ParamAttribute[] params;

        public CommandAttribute(CommandParser parent, SubCommand subCommand, Method method, Object instance) {
                this.parent = parent;
                this.method = method;
                this.instance = instance;
                if (subCommand.name().length() != 0) {
                        name = subCommand.name();
                } else {
                        name = method.getName();
                }
                aliases = new ArrayList<String>(Arrays.asList(subCommand.aliases()));
                shortcut = subCommand.shortcut();
                if (shortcut) aliases.add("" + name.charAt(0));
                if (subCommand.perm().length() != 0) {
                        permission = "auction." + subCommand.perm();
                }
                this.optionalArgc = subCommand.optional();
                params = getParams(method.getParameterTypes());
        }

        public String getName() {
                return name;
        }

        public List<String> getAliases() {
                return aliases;
        }

        private ParamAttribute[] getParams(Class<?>[] types) {
                ParamAttribute[] result = new ParamAttribute[types.length];
                for (int i = 0; i < types.length; ++i) {
                        result[i] = parent.getParam(types[i]);
                }
                return result;
        }

        public boolean checkPermission(CommandSender sender) {
                if (sender.isOp()) return true;
                if (permission != null && !sender.hasPermission(permission)) return false;
                return true;
        }

        public void call(CommandSender sender, List<String> argl) throws CommandParseException {
                int skippedArgs = 0;
                Iterator<String> iter = argl.iterator();
                Object[] args = new Object[params.length];
                for (int i = 0; i < params.length; ++i) {
                        ParamAttribute param = params[i];
                        if (param.takesArgument()) {
                                if (!iter.hasNext()) {
                                        if (++skippedArgs > optionalArgc) {
                                                throw new CommandParseException(CraftBayPlugin.getInstance().getMessage("command.ArgsTooSmall"));
                                        }
                                        args[i] = null;
                                } else {
                                        args[i] = param.parse(sender, iter.next());
                                }
                        } else {
                                args[i] = param.parse(sender, null);
                        }
                }
                if (iter.hasNext()) {
                        throw new CommandParseException(CraftBayPlugin.getInstance().getMessage("command.ArgsTooBig"));
                }
                try {
                        method.invoke(instance, args);
                } catch (IllegalAccessException iae) {
                        iae.printStackTrace();
                        return;
                } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        return;
                } catch (InvocationTargetException ite) {
                        for (Throwable thrown = ite; thrown != null; thrown = thrown.getCause()) {
                                if (thrown instanceof CommandParseException) {
                                        throw new CommandParseException(((CommandParseException)thrown).getLocaleMessage());
                                }
                        }
                        ite.printStackTrace();
                        return;
                }
                return;
        }
}
