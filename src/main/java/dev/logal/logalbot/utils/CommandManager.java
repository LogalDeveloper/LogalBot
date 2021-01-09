package dev.logal.logalbot.utils;

// Copyright 2019 Logan Fick

// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at

// https://apache.org/licenses/LICENSE-2.0

// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.util.Arrays;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.Checks;

public final class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    private static final HashMap<String, Command> commandMap = new HashMap<>();
    private static final HashMap<String, Boolean> permissionMap = new HashMap<>();
    private static final HashMap<String, String> aliasMap = new HashMap<>();

    private CommandManager() {
        // Static access only.
    }

    public static final void executeCommand(final String[] command, final Member executor, final TextChannel channel) {
        Checks.notEmpty(command, "Command");
        Checks.notNull(executor, "Executor");
        Checks.notNull(channel, "Channel");

        String commandName = command[0].toLowerCase();
        final String[] arguments = Arrays.copyOfRange(command, 1, command.length);
        final Guild guild = channel.getGuild();
        CommandResponse response;

        if (aliasMap.containsKey(commandName)) {
            commandName = aliasMap.get(commandName);
        }

        logger.info(executor.getEffectiveName() + " (" + executor.getUser().getId() + ") executed command '"
                + commandName + "' with arguments '" + String.join(" ", arguments) + "' in " + guild.getName() + " ("
                + guild.getId() + ").");
        if (!commandMap.containsKey(commandName)) {
            response = new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but that command is unknown.");
            response.setDeletionDelay(30, TimeUnit.SECONDS);
            response.sendResponse(channel);
            return;
        }

        if (permissionMap.get(commandName) && !PermissionManager.isWhitelisted(executor)) {
            logger.info(executor.getEffectiveName() + " (" + executor.getUser().getId()
                    + ") was denied access to a command due to not being whitelisted in " + guild.getName() + " ("
                    + guild.getId() + ").");
            response = new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but you are not allowed to use that command.");
            response.setDeletionDelay(30, TimeUnit.SECONDS);
            response.sendResponse(channel);
            return;
        }

        try {
            response = commandMap.get(commandName).execute(arguments, executor, channel);
        } catch (final Throwable exception) {
            logger.error(
                    "An error occurred while executing a command for " + executor.getEffectiveName() + " ("
                            + executor.getUser().getId() + ") in " + guild.getName() + " (" + guild.getId() + ").",
                    exception);
            response = new CommandResponse("sos",
                    "Sorry " + executor.getAsMention() + ", but an error occurred while executing your command.");
        }

        if (response != null) {
            response.sendResponse(channel);
        }
    }

    public static final void registerCommand(final String command, final Command commandObject,
            final boolean mustBeWhitelisted) {
        Checks.notEmpty(command, "Command");
        Checks.notNull(commandObject, "Command object");
        Checks.notNull(mustBeWhitelisted, "Whitelist requirement");

        commandMap.put(command, commandObject);
        permissionMap.put(command, mustBeWhitelisted);
    }

    public static final void registerCommandAlias(final String alias, final String command) {
        Checks.notEmpty(alias, "Alias");
        Checks.notEmpty(command, "Command");

        aliasMap.put(alias, command);
    }

    public static final void unregisterCommand(final String command) {
        Checks.notEmpty(command, "Command");

        commandMap.remove(command);
        permissionMap.remove(command);
    }

    public static final void unregisterCommandAlias(final String command) {
        Checks.notEmpty(command, "Command");

        aliasMap.remove(command);
    }
}