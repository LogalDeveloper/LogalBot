package dev.logal.logalbot.events;

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
import java.util.List;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.tasks.CommandExecutionTask;
import dev.logal.logalbot.utils.DataManager;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;

public final class GuildMessageReceived extends ListenerAdapter {
    @Override
    public final void onGuildMessageReceived(final GuildMessageReceivedEvent event) {
        Checks.notNull(event, "Event");

        final Guild guild = event.getGuild();
        final Member self = guild.getSelfMember();
        final TextChannel channel = event.getChannel();
        final Message message = event.getMessage();
        if (event.getAuthor().isBot() || message.isTTS() || !self.hasPermission(channel, Permission.MESSAGE_WRITE)
                || !self.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
            return;
        }

        final String content = message.getContentRaw();
        final SelfUser selfUser = event.getJDA().getSelfUser();
        final List<Member> mentionedMembers = message.getMentionedMembers();
        final Member author = event.getMember();
        if (mentionedMembers.size() >= 1 && mentionedMembers.get(0).getUser().getIdLong() == selfUser.getIdLong()
                && (content.startsWith(self.getAsMention()) || content.startsWith(selfUser.getAsMention()))) {
            final String[] rawCommand = content.split(" ");
            final String[] command = Arrays.copyOfRange(rawCommand, 1, rawCommand.length);
            if (command.length >= 1) {
                if (self.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
                    message.delete().reason("LogalBot Command Execution").queue();
                }
                MainThread.scheduleImmediately(new CommandExecutionTask(command, author, channel));
            }
        } else {
            final String commandCharacter = DataManager.getGuildValue(guild, "commandCharacter");
            if (commandCharacter == null) {
                return;
            }

            final char commandChar = commandCharacter.toCharArray()[0];

            if (content.length() > 1 && content.charAt(0) == commandChar) {
                final String[] command = content.substring(1).split(" ");
                if (self.hasPermission(channel, Permission.MESSAGE_MANAGE)) {
                    message.delete().reason("LogalBot Command Execution").queue();
                }
                MainThread.scheduleImmediately(new CommandExecutionTask(command, author, channel));
            }
        }
    }
}