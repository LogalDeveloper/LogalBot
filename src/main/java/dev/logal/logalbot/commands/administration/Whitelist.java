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
import dev.logal.logalbot.utils.PermissionManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class Whitelist implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final Guild guild = channel.getGuild();
        if (!executor.hasPermission(Permission.ADMINISTRATOR)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but you are not allowed to use this command.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (arguments.length == 0) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention()
                            + ", but you need to specify a user to add or remove from the whitelist.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        final String userID = arguments[0].replaceFirst("<@[!]?([0-9]*)>", "$1");
        final Member member;
        try {
            member = guild.getMemberById(userID);
        } catch (final NumberFormatException exception) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but that doesn't appear to be a valid user.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (member == null) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but that doesn't appear to be a valid user.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return new CommandResponse("x", "Sorry " + executor.getAsMention()
                    + ", but you cannot remove that user from the whitelist because they are a guild administrator.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (member.getUser().isBot()) {
            return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but you cannot whitelist bots.")
                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (PermissionManager.isWhitelisted(member)) {
            PermissionManager.removeFromWhitelist(member);
            return new CommandResponse("heavy_multiplication_x",
                    executor.getAsMention() + " has removed " + member.getAsMention() + " from the whitelist.");
        } else {
            PermissionManager.addToWhitelist(member);
            return new CommandResponse("heavy_check_mark",
                    executor.getAsMention() + " has added " + member.getAsMention() + " to the whitelist.");
        }
    }
}