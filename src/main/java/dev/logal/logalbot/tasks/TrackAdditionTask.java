package dev.logal.logalbot.tasks;

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

import java.util.LinkedList;
import java.util.List;

import dev.logal.logalbot.audio.RequestedTrack;
import dev.logal.logalbot.audio.TrackScheduler;
import dev.logal.logalbot.utils.AudioUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.utils.Checks;

public final class TrackAdditionTask implements Runnable {
    private final Guild guild;
    private final LinkedList<RequestedTrack> tracks;

    public TrackAdditionTask(final Guild guild, final RequestedTrack track) {
        Checks.notNull(guild, "Guild");
        Checks.notNull(track, "Track");

        this.guild = guild;
        tracks = new LinkedList<RequestedTrack>();
        tracks.add(track);
    }

    public TrackAdditionTask(final Guild guild, final List<RequestedTrack> tracks) {
        Checks.notNull(guild, "Guild");
        Checks.noneNull(tracks, "Tracks");

        this.guild = guild;
        this.tracks = (LinkedList<RequestedTrack>) tracks;
    }

    @Override
    public final void run() {
        final TrackScheduler scheduler = AudioUtil.getTrackScheduler(this.guild);
        for (final RequestedTrack track : this.tracks) {
            scheduler.addToQueue(track);
        }
    }
}