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

import java.lang.Class;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import org.bukkit.command.CommandSender;

public class ParamAttribute {
        private boolean takeArgument;
        private Method method;
        private Object instance;
        private Class<?> paramType;

        public ParamAttribute(Parameter param, Method method, Object instance) {
                this.method = method;
                this.instance = instance;

                Class<?>[] params = method.getParameterTypes();
                if (params.length == 2 && params[0] == CommandSender.class && params[1] == String.class) {
                        takeArgument = true;
                } else if (params.length == 1 && params[0] == CommandSender.class) {
                        takeArgument = false;
                } else {
                        System.err.println(getClass().getName() + ": method " + method.getName() + " cannot be used for parameter parsing");
                }
                paramType = method.getReturnType();
        }

        public Object parse(CommandSender sender, String arg) throws CommandParseException {
                try {
                        if (takeArgument) {
                                return method.invoke(instance, sender, arg);
                        } else {
                                return method.invoke(instance, sender);
                        }
                } catch (IllegalAccessException iae) {
                        iae.printStackTrace();
                        return null;
                } catch (IllegalArgumentException iae) {
                        iae.printStackTrace();
                        return null;
                } catch (InvocationTargetException ite) {
                        for (Throwable thrown = ite; thrown != null; thrown = thrown.getCause()) {
                                if (thrown instanceof CommandParseException) {
                                        throw new CommandParseException(((CommandParseException)thrown).getLocaleMessage());
                                }
                        }
                        ite.printStackTrace();
                        return null;
                }
        }

        public Class<?> getType() {
                return paramType;
        }

        public boolean takesArgument() {
                return takeArgument;
        }
}