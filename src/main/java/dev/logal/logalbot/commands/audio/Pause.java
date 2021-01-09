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

public final class Pause implements Command {
    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        final Guild guild = channel.getGuild();
        if (!AudioUtil.isTrackLoaded(guild)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention()
                            + ", but there must be a track playing in order to pause or resume the track player.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (!VoiceChannelUtil.isInCurrentVoiceChannel(executor)) {
            return new CommandResponse("x",
                    "Sorry " + executor.getAsMention() + ", but you must be in voice channel `"
                            + AudioUtil.getVoiceChannelConnectedTo(guild).getName()
                            + "` in order to pause or resume the track player.").setDeletionDelay(30, TimeUnit.SECONDS);
        }

        if (AudioUtil.isPlayerPaused(guild)) {
            AudioUtil.setPausedState(guild, false);
            return new CommandResponse("arrow_forward", executor.getAsMention() + " resumed the track player.");
        } else {
            AudioUtil.setPausedState(guild, true);
            return new CommandResponse("pause_button", executor.getAsMention() + " paused the track player.");
        }
    }
}