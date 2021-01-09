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

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.Checks;

public final class VoiceChannelUtil {
    private VoiceChannelUtil() {
        // Static access only.
    }

    public static final VoiceChannel getVoiceChannelMemberIsConnectedTo(final Member member) {
        Checks.notNull(member, "Member");

        for (final VoiceChannel channel : member.getGuild().getVoiceChannels()) {
            if (channel.getMembers().contains(member)) {
                return channel;
            }
        }
        return null;
    }

    public static final boolean isInCurrentVoiceChannel(final Member member) {
        Checks.notNull(member, "Member");

        return AudioUtil.getVoiceChannelConnectedTo(member.getGuild()).getMembers().contains(member);
    }
}