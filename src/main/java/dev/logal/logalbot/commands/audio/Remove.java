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

import java.util.concurrent.TimeUnit;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.audio.RequestedTrack;
import dev.logal.logalbot.audio.TrackScheduler;
import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.tasks.CommandExecutionTask;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.ReactionCallbackManager;
import dev.logal.logalbot.utils.StringUtil;
import dev.logal.logalbot.utils.TrackUtil;
import dev.logal.logalbot.utils.VoiceChannelUtil;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class Remove implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final TrackScheduler scheduler = AudioUtil.getTrackScheduler(channel.getGuild());
        if (scheduler.isQueueEmpty()) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but there are no tracks in the queue to remove.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        final Guild guild = channel.getGuild();
        if (AudioUtil.isTrackLoaded(guild) && !VoiceChannelUtil.isInCurrentVoiceChannel(executor)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but you need to be in voice channel `"
                            + AudioUtil.getVoiceChannelConnectedTo(guild).getName()
                            + "` in order to remove tracks from the queue.").setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (arguments.length == 0) {
            if (!guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_ADD_REACTION)) {
                return new CommandResponse("x", "Sorry " + executor.getAsMention()
                        + ", but I do not have the required permissions to create a reaction selection dialog in this text channel.")
                                .setDeletionDelay(30, TimeUnit.SECONDS);
            }

            final CommandResponse response = new CommandResponse("question",
                    executor.getAsMention() + ", which track would you like to remove from the top of the queue?");
            response.attachEmbed(TrackUtil.trackListInfoEmbed(scheduler.getQueue(), true));

            for (int i = 0; i < scheduler.getQueue().size(); i++) {
                final int trackNumber = i + 1;
                if (trackNumber == 11) {
                    break;
                }

                response.addReactionCallback(StringUtil.intToKeycapEmoji(trackNumber).getAliases().get(0),
                        (reactor, message) -> {
                            ReactionCallbackManager.unregisterMessage(message, true);
                            MainThread.scheduleImmediately(
                                    new CommandExecutionTask(("remove " + trackNumber).split(" "), reactor, channel));
                        });
            }

            response.setReactionCallbackTarget(executor);
            response.setDeletionDelay(1, TimeUnit.MINUTES);
            return response;
        }

        final int index;
        try {
            index = Integer.parseInt(arguments[0]);
        } catch (final NumberFormatException exception) {
            return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but the index must be an integer.")
                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        try {
            final RequestedTrack removedTrack = scheduler.getQueue().get(index - 1);

            scheduler.removeFromQueue(index - 1);
            final CommandResponse response = new CommandResponse("scissors",
                    executor.getAsMention() + " removed the following track from the queue:");
            response.attachEmbed(TrackUtil.trackInfoEmbed(removedTrack));
            return response;
        } catch (final IndexOutOfBoundsException exception) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but that index is outside the bounds of the queue.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }
    }
}