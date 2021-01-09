package dev.logal.logalbot.commands.administration;

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

import java.util.concurrent.TimeUnit;

import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.DataManager;
import dev.logal.logalbot.utils.StringUtil;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class Settings implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final Guild guild = executor.getGuild();

        if (arguments.length == 0) {
            final CommandResponse response = new CommandResponse("wrench",
                    executor.getAsMention() + ", these are the current settings for this guild:");
            final EmbedBuilder builder = new EmbedBuilder();
            builder.setTitle("**Current Settings for " + StringUtil.sanitize(guild.getName()) + "**");

            final String commandCharacter = DataManager.getGuildValue(guild, "commandCharacter");
            if (commandCharacter == null) {
                builder.addField("Command Character", "Not Set", true);
            } else {
                builder.addField("Command Character", commandCharacter, true);
            }

            final String defaultVolume = DataManager.getGuildValue(guild, "defaultVolume");
            if (defaultVolume == null) {
                builder.addField("Default Volume", "10%", true);
            } else {
                builder.addField("Default Volume", defaultVolume + "%", true);
            }

            final String maxSlots = DataManager.getGuildValue(guild, "maximumSlots");
            if (maxSlots == null) {
                builder.addField("Maximum Queue Slots Per User", "Unlimited", true);
            } else {
                builder.addField("Maximum Queue Slots Per User", maxSlots, true);
            }

            response.attachEmbed(builder.build());
            return response;
        }

        if (arguments[0].equalsIgnoreCase("commandcharacter") || arguments[0].equalsIgnoreCase("cmdchar")) {
            if (arguments.length == 1) {
                DataManager.deleteGuildKey(guild, "commandCharacter");
                return new CommandResponse("white_check_mark",
                        executor.getAsMention() + ", the command character has been disabled.");
            } else {
                final char[] input = arguments[1].replaceAll("\n", "").toCharArray();
                if (input.length > 1) {
                    return new CommandResponse("x",
                            "Sorry " + executor.getAsMention()
                                    + ", but the command character must be a single character.").setDeletionDelay(30,
                                            TimeUnit.SECONDS);
                } else if (input.length == 0) {
                    DataManager.deleteGuildKey(guild, "commandCharacter");
                    return new CommandResponse("white_check_mark",
                            executor.getAsMention() + ", the command character has been disabled.");
                } else {
                    DataManager.setGuildValue(guild, "commandCharacter", "" + input[0]);
                    return new CommandResponse("white_check_mark",
                            executor.getAsMention() + ", the command character has been set to `" + input[0] + "`.");
                }
            }
        } else if (arguments[0].equalsIgnoreCase("defaultvolume") || arguments[0].equalsIgnoreCase("volume")) {
            if (arguments.length == 1) {
                DataManager.deleteGuildKey(guild, "defaultVolume");
                if (!AudioUtil.isTrackLoaded(guild)) {
                    AudioUtil.setVolume(guild, 10);
                }
                return new CommandResponse("white_check_mark",
                        executor.getAsMention() + ", the default volume has been reset to `10%`.");
            } else {
                final int volume;
                try {
                    volume = Integer.parseInt(arguments[1]);
                } catch (final NumberFormatException exception) {
                    return new CommandResponse("x",
                            "Sorry " + executor.getAsMention() + ", but the default volume must be an integer.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
                }

                if (volume == 10) {
                    DataManager.deleteGuildKey(guild, "defaultVolume");
                    return new CommandResponse("white_check_mark",
                            executor.getAsMention() + ", the default volume has been reset to `10%`.");
                }

                if (volume <= 150 && volume >= 1) {
                    if (!AudioUtil.isTrackLoaded(guild)) {
                        AudioUtil.setVolume(guild, volume);
                    }
                    DataManager.setGuildValue(guild, "defaultVolume", "" + volume);
                    return new CommandResponse("white_check_mark",
                            executor.getAsMention() + ", the default volume has been set to `" + volume + "%`.");
                } else {
                    return new CommandResponse("x",
                            "Sorry " + executor.getAsMention()
                                    + ", but the default volume must be between 1% and 150%.").setDeletionDelay(30,
                                            TimeUnit.SECONDS);
                }
            }
        } else if (arguments[0].equalsIgnoreCase("maximumSlots")) {
            final int maxSlots;
            try {
                maxSlots = Integer.parseInt(arguments[1]);
            } catch (final NumberFormatException exception) {
                return new CommandResponse("x",
                        "Sorry " + executor.getAsMention()
                                + ", but the maximum amount of queue slots per user must be an integer.")
                                        .setDeletionDelay(30, TimeUnit.SECONDS);
            }

            if (maxSlots == 0) {
                DataManager.deleteGuildKey(guild, "maximumSlots");
                return new CommandResponse("white_check_mark", executor.getAsMention()
                        + ", the maximum amount of queue slots per user has been reset to unlimited.");
            }

            if (maxSlots <= 249 && maxSlots >= 1) {
                DataManager.setGuildValue(guild, "maximumSlots", "" + maxSlots);
                return new CommandResponse("white_check_mark", executor.getAsMention()
                        + ", the maximum amount of queue slots per user has been set to `" + maxSlots + "`.");
            } else {
                return new CommandResponse("x",
                        "Sorry " + executor.getAsMention()
                                + ", but the maximum amount of queue slots per user must be between 1 and 249.")
                                        .setDeletionDelay(30, TimeUnit.SECONDS);
            }
        } else {
            return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but that setting is unknown.")
                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }
    }
}