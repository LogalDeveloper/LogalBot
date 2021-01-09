package dev.logal.logalbot.commands.audio;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import dev.logal.logalbot.audio.TrackScheduler;
import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.DataManager;
import dev.logal.logalbot.utils.PermissionManager;
import dev.logal.logalbot.utils.VoiceChannelUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;

public final class Play implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final Guild guild = channel.getGuild();
        if (AudioUtil.isTrackLoaded(guild) && !VoiceChannelUtil.isInCurrentVoiceChannel(executor)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but you need to be in voice channel `"
                            + AudioUtil.getVoiceChannelConnectedTo(guild).getName()
                            + "` in order to add songs to the queue.").setDeletionDelay(30, TimeUnit.SECONDS);
        }

        final VoiceChannel targetChannel = VoiceChannelUtil.getVoiceChannelMemberIsConnectedTo(executor);
        if (targetChannel == null) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention()
                            + ", but you need to be in a voice channel in order to add songs to the queue.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        final Member selfMember = guild.getSelfMember();
        if (!selfMember.hasPermission(targetChannel, Permission.VOICE_CONNECT)
                || !selfMember.hasPermission(targetChannel, Permission.VOICE_SPEAK)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention()
                            + ", but I do not have the required permissions to use your current voice channel.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (arguments.length == 0) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention()
                            + ", but you need to provide a search query or a link to a specific track or playlist.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        final TrackScheduler scheduler = AudioUtil.getTrackScheduler(guild);
        if (scheduler.isQueueLocked() && !PermissionManager.isWhitelisted(executor)) {
            return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but the queue is locked.")
                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (scheduler.isQueueFull()) {
            return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but the queue is full.")
                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        try {
            final int maxSlots = Integer.parseInt(DataManager.getGuildValue(guild, "maximumSlots"));

            if ((scheduler.occupiedSlotCount(executor) >= maxSlots) && !PermissionManager.isWhitelisted(executor)) {
                return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", you cannot request more than "
                        + maxSlots + " tracks at the same time.").setDeletionDelay(30, TimeUnit.SECONDS);
            }
        } catch (final NumberFormatException exception) {
        }

        boolean isLink;
        try {
            new URL(arguments[0]);
            isLink = true;
        } catch (final MalformedURLException exception) {
            isLink = false;
        }

        final StringBuilder query;
        if (isLink) {
            query = new StringBuilder(arguments[0]);
        } else {
            query = new StringBuilder("ytsearch:");
            for (final String part : arguments) {
                query.append(part).append(" ");
            }
        }

        AudioUtil.findTrack(query.toString(), executor, channel);
        return null;
    }
}