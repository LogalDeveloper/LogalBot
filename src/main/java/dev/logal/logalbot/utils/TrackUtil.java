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

import java.util.List;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import dev.logal.logalbot.audio.RequestedTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

public final class TrackUtil {
    private TrackUtil() {
        // Static access only.
    }

    public static final MessageEmbed trackInfoEmbed(final RequestedTrack queuedTrack) {
        Checks.notNull(queuedTrack, "Track");

        final AudioTrack track = queuedTrack.getTrack();
        final EmbedBuilder builder = new EmbedBuilder();
        final AudioTrackInfo info = track.getInfo();
        final User requester = queuedTrack.getRequester().getUser();
        builder.addField(StringUtil.sanitize(info.title), StringUtil.sanitize(info.author) + " - "
                + StringUtil.formatTime(track.getDuration()) + " - [View](" + info.uri + ")", false).build();
        builder.setFooter(
                "Requested by " + StringUtil.sanitize(requester.getName()) + "#" + requester.getDiscriminator(),
                requester.getEffectiveAvatarUrl());
        return builder.build();
    }

    public static final MessageEmbed currentTrackInfoEmbed(final Guild guild) {
        Checks.notNull(guild, "Guild");

        final EmbedBuilder builder = new EmbedBuilder();
        final AudioTrack track = AudioUtil.getLoadedTrack(guild).getTrack();
        final AudioTrackInfo info = track.getInfo();
        final User requester = AudioUtil.getLoadedTrack(guild).getRequester().getUser();
        builder.addField(StringUtil.sanitize(info.title),
                StringUtil.sanitize(info.author) + " - " + StringUtil.formatTime(track.getPosition()) + "/"
                        + StringUtil.formatTime(track.getDuration()) + " - [View](" + info.uri + ")",
                false).build();
        builder.setFooter(
                "Requested by " + StringUtil.sanitize(requester.getName()) + "#" + requester.getDiscriminator(),
                requester.getEffectiveAvatarUrl());
        return builder.build();
    }

    public static final MessageEmbed trackListInfoEmbed(final List<RequestedTrack> tracks, final boolean numbered) {
        Checks.notNull(tracks, "Tracks");

        final EmbedBuilder builder = new EmbedBuilder();
        for (int i = 0; i < tracks.size(); i++) {
            if (i == 10) {
                break;
            }

            final AudioTrack track = tracks.get(i).getTrack();
            final AudioTrackInfo info = track.getInfo();
            if (numbered) {
                builder.addField(
                        "**" + (i + 1) + ":** " + StringUtil.sanitize(info.title), StringUtil.sanitize(info.author)
                                + " - " + StringUtil.formatTime(track.getDuration()) + " - [View](" + info.uri + ")",
                        false);
            } else {
                builder.addField(StringUtil.sanitize(info.title), StringUtil.sanitize(info.author) + " - "
                        + StringUtil.formatTime(track.getDuration()) + " - [View](" + info.uri + ")", false);
            }
        }

        if (tracks.size() > 10) {
            builder.setTitle("**Top 10 Tracks - " + (tracks.size() - 10) + " Not Shown**");
        }
        return builder.build();
    }

    public static final MessageEmbed pagedTrackListInfoEmbed(final List<RequestedTrack> tracks, int page) {
        Checks.notNull(tracks, "Tracks");

        final EmbedBuilder builder = new EmbedBuilder();
        if (page < 1) {
            page = 1;
        }

        final int pages = (int) Math.ceil(tracks.size() / 10d);

        if (page > pages) {
            page = pages;
        }

        page = page - 1;
        final int start = page * 10;
        final int end = start + 10;

        for (int i = start; i < end && i < tracks.size(); i++) {
            final AudioTrack track = tracks.get(i).getTrack();
            final AudioTrackInfo info = track.getInfo();
            builder.addField("**" + (i + 1) + ":** " + StringUtil.sanitize(info.title), StringUtil.sanitize(info.author)
                    + " - " + StringUtil.formatTime(track.getDuration()) + " - [View](" + info.uri + ")", false);
        }

        builder.setTitle("**" + tracks.size() + " Total Tracks - Page " + (page + 1) + "/" + pages + "**");
        return builder.build();
    }

    public static final boolean doesGreaterPageExist(final List<RequestedTrack> tracks, int page) {
        Checks.notNull(tracks, "Tracks");

        final int pages = (int) Math.ceil(tracks.size() / 10d);
        return (page < pages);
    }
}