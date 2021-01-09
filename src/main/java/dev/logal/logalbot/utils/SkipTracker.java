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

import java.util.ArrayList;
import java.util.HashMap;

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.Checks;

public final class SkipTracker {
    private static final HashMap<Long, ArrayList<Long>> skipVotesDictionary = new HashMap<>();

    private SkipTracker() {
        // Static access only.
    }

    public static final void registerVote(final Member member) {
        Checks.notNull(member, "Member");

        final Guild guild = member.getGuild();
        if (!skipVotesDictionary.containsKey(guild.getIdLong())) {
            resetVotes(guild);
        }

        final ArrayList<Long> registeredVotes = skipVotesDictionary.get(guild.getIdLong());
        if (!registeredVotes.contains(member.getUser().getIdLong())) {
            registeredVotes.add(member.getUser().getIdLong());
        }
    }

    public static final boolean hasVoted(final Member member) {
        Checks.notNull(member, "Member");

        return skipVotesDictionary.get(member.getGuild().getIdLong()).contains(member.getUser().getIdLong());
    }

    public static final void resetVotes(final Guild guild) {
        Checks.notNull(guild, "Guild");

        final long guildID = guild.getIdLong();
        if (skipVotesDictionary.containsKey(guildID)) {
            skipVotesDictionary.get(guildID).clear();
        } else {
            skipVotesDictionary.put(guildID, new ArrayList<>());
        }
    }

    public static final int getRemainingRequired(final Guild guild) {
        Checks.notNull(guild, "Guild");

        final int listeners = (int) AudioUtil.getVoiceChannelConnectedTo(guild).getMembers().stream()
                .filter(member -> !member.getUser().isBot()).count();
        final int required = (int) Math.ceil(listeners * .55);

        return (required - skipVotesDictionary.get(guild.getIdLong()).size());
    }

    public static final boolean shouldSkip(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return getRemainingRequired(guild) <= 0;
    }
}