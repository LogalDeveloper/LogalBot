package dev.logal.logalbot.audio;

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

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.Checks;

public final class RequestedTrack {
    private final AudioTrack track;
    private final Member requester;

    public RequestedTrack(final AudioTrack track, final Member requester) {
        Checks.notNull(track, "Track");
        Checks.notNull(requester, "Requester");

        this.track = track;
        this.requester = requester;
    }

    public final AudioTrack getTrack() {
        return this.track;
    }

    public final Member getRequester() {
        return this.requester;
    }
}