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

import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.VoiceChannelUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class Volume implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final Guild guild = channel.getGuild();
        if (!AudioUtil.isTrackLoaded(guild)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention()
                            + ", but there must be a track playing in order to change the volume of the track player.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (!VoiceChannelUtil.isInCurrentVoiceChannel(executor)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but you must be in voice channel `"
                            + AudioUtil.getVoiceChannelConnectedTo(guild).getName()
                            + "` in order to change the volume of the track player.").setDeletionDelay(30,
                                    TimeUnit.SECONDS);
        }

        if (arguments.length == 0) {
            if (AudioUtil.getVolume(guild) >= 75) {
                return new CommandResponse("loud_sound", executor.getAsMention() + ", the volume is currently set to `"
                        + AudioUtil.getVolume(guild) + "%`.");
            } else {
                return new CommandResponse("sound", executor.getAsMention() + ", the volume is currently set to `"
                        + AudioUtil.getVolume(guild) + "%`.");
            }
        }

        final int volume;
        try {
            volume = Integer.parseInt(arguments[0]);
        } catch (final NumberFormatException exception) {
            return new CommandResponse("x", "Sorry " + executor.getAsMention() + ", but the volume must be an integer.")
                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (volume <= 150 && volume >= 1) {
            final int oldVolume = AudioUtil.getVolume(guild);
            AudioUtil.setVolume(guild, volume);
            if (volume >= 75) {
                return new CommandResponse("loud_sound",
                        executor.getAsMention() + " set the volume from `" + oldVolume + "%` to `" + volume + "%`.");
            } else {
                return new CommandResponse("sound",
                        executor.getAsMention() + " set the volume from `" + oldVolume + "%` to `" + volume + "%`.");
            }
        } else {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but the volume must be between 1% and 150%.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }
    }
}