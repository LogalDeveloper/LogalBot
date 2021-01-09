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

import java.util.List;
import java.util.concurrent.TimeUnit;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.audio.RequestedTrack;
import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.tasks.CommandExecutionTask;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.ReactionCallbackManager;
import dev.logal.logalbot.utils.TrackUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class Queue implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final Guild guild = channel.getGuild();
        if (AudioUtil.getTrackScheduler(guild).isQueueEmpty()) {
            return new CommandResponse("information_source", executor.getAsMention() + ", the queue is empty.");
        }

        final CommandResponse response = new CommandResponse("bookmark_tabs",
                executor.getAsMention() + ", the following tracks are in the queue:");

        final int page;
        if (arguments.length == 0) {
            page = 1;
        } else {
            try {
                page = Integer.parseInt(arguments[0]);
            } catch (final NumberFormatException exception) {
                return new CommandResponse("x",
                        "Sorry " + executor.getAsMention() + ", but the page number must be an integer.")
                                .setDeletionDelay(30, TimeUnit.SECONDS);
            }
        }

        final List<RequestedTrack> queue = AudioUtil.getTrackScheduler(guild).getQueue();
        if (page != 1) {
            response.addReactionCallback("arrow_left", (reactor, message) -> {
                ReactionCallbackManager.unregisterMessage(message, true);
                MainThread.scheduleImmediately(
                        new CommandExecutionTask(("queue " + (page - 1)).split(" "), reactor, channel));
            });
        }

        if (TrackUtil.doesGreaterPageExist(queue, page)) {
            response.addReactionCallback("arrow_right", (reactor, message) -> {
                ReactionCallbackManager.unregisterMessage(message, true);
                MainThread.scheduleImmediately(
                        new CommandExecutionTask(("queue " + (page + 1)).split(" "), reactor, channel));
            });
        }

        response.setReactionCallbackTarget(executor);
        response.setReactionCallbackExpireDelay(1, TimeUnit.MINUTES);
        response.attachEmbed(TrackUtil.pagedTrackListInfoEmbed(queue, page));
        return response;
    }
}