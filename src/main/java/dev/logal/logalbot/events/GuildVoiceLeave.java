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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.tasks.ResetAudioPlayerTask;
import dev.logal.logalbot.utils.AudioUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;

public final class GuildVoiceLeave extends ListenerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GuildVoiceLeave.class);

    @Override
    public final void onGuildVoiceLeave(final GuildVoiceLeaveEvent event) {
        Checks.notNull(event, "Event");

        final Guild guild = event.getGuild();
        if (!AudioUtil.isAudioConnectionOpen(guild)) {
            return;
        }

        if (!AudioUtil.isTrackLoaded(guild)) {
            return;
        }

        final Member member = event.getMember();

        if (member.getUser().equals(event.getJDA().getSelfUser())) {
            return;
        }

        final VoiceChannel leftChannel = event.getChannelLeft();
        if (leftChannel.equals(AudioUtil.getVoiceChannelConnectedTo(guild))) {
            final List<Member> members = leftChannel.getMembers();
            if (members.size() == 1 && members.get(0).getUser().equals(event.getJDA().getSelfUser())) {
                logger.info("All listeners left " + leftChannel.getName() + " (" + leftChannel.getId() + ") in "
                        + guild.getName() + " (" + guild.getId() + ").");
                MainThread.scheduleImmediately(new ResetAudioPlayerTask(guild));
            }
        }
    }
}