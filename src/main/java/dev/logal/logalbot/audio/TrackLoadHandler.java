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

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.tasks.TrackAdditionTask;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.PermissionManager;
import dev.logal.logalbot.utils.TrackUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.Checks;

public final class TrackLoadHandler implements AudioLoadResultHandler {
    private final Logger logger = LoggerFactory.getLogger(TrackLoadHandler.class);

    private final Member requester;
    private final TextChannel channel;

    public TrackLoadHandler(final Member requester, final TextChannel channel) {
        Checks.notNull(requester, "Requester");
        Checks.notNull(channel, "Channel");

        this.requester = requester;
        this.channel = channel;
    }

    @Override
    public final void trackLoaded(final AudioTrack track) {
        Checks.notNull(track, "Track");

        final CommandResponse response;
        final Guild guild = this.channel.getGuild();
        final TrackScheduler scheduler = AudioUtil.getTrackScheduler(guild);

        final AudioTrackInfo info = track.getInfo();
        if (info.isStream) {
            response = new CommandResponse("x",
                    "Sorry " + this.requester.getAsMention() + ", but streams cannot be added to the queue.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
            response.sendResponse(this.channel);
            return;
        }

        if (!(info.length >= 60000 && info.length <= 900000) && !PermissionManager.isWhitelisted(this.requester)) {
            response = new CommandResponse("x",
                    "Sorry " + this.requester.getAsMention()
                            + ", but you can only add tracks between 1 and 15 minutes in length to the queue.")
                                    .setDeletionDelay(30, TimeUnit.SECONDS);
            response.sendResponse(this.channel);
            return;
        }

        if (scheduler.isQueued(track)) {
            response = new CommandResponse("x",
                    "Sorry " + this.requester.getAsMention() + ", but that track is already in the queue.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
            response.sendResponse(this.channel);
            return;
        }

        final RequestedTrack requestedTrack = new RequestedTrack(track, requester);
        MainThread.scheduleImmediately(new TrackAdditionTask(guild, requestedTrack));
        response = new CommandResponse("notes",
                this.requester.getAsMention() + " added the following track to the queue:");
        response.attachEmbed(TrackUtil.trackInfoEmbed(requestedTrack));
        response.sendResponse(this.channel);
    }

    @Override
    public final void playlistLoaded(final AudioPlaylist playlist) {
        Checks.notNull(playlist, "Playlist");

        CommandResponse response;
        final Guild guild = this.channel.getGuild();
        final TrackScheduler scheduler = AudioUtil.getTrackScheduler(guild);

        final AudioTrack selectedTrack = playlist.getSelectedTrack();
        final AudioTrack track;
        if (!playlist.isSearchResult() && selectedTrack != null) {
            track = selectedTrack;
        } else if (playlist.isSearchResult()) {
            track = playlist.getTracks().get(0);
        } else {
            track = null;
        }

        if (track != null) {
            if (scheduler.isQueued(track)) {
                response = new CommandResponse("x",
                        "Sorry " + this.requester.getAsMention() + ", but that track is already in the queue.")
                                .setDeletionDelay(30, TimeUnit.SECONDS);
                response.sendResponse(this.channel);
                return;
            }

            final AudioTrackInfo info = track.getInfo();
            if (info.isStream) {
                response = new CommandResponse("x",
                        "Sorry " + this.requester.getAsMention() + ", but streams cannot be added to the queue.")
                                .setDeletionDelay(30, TimeUnit.SECONDS);
                response.sendResponse(this.channel);
                return;
            }

            if ((info.length >= 60000 && info.length <= 900000) || PermissionManager.isWhitelisted(this.requester)) {
                final RequestedTrack requestedTrack = new RequestedTrack(track, requester);
                MainThread.scheduleImmediately(new TrackAdditionTask(guild, requestedTrack));
                response = new CommandResponse("notes",
                        this.requester.getAsMention() + " added the following track to the queue:");
                response.attachEmbed(TrackUtil.trackInfoEmbed(requestedTrack));
                response.sendResponse(this.channel);
            } else {
                response = new CommandResponse("x",
                        "Sorry " + this.requester.getAsMention()
                                + ", but you can only request tracks between 1 and 15 minutes in length.")
                                        .setDeletionDelay(30, TimeUnit.SECONDS);
                response.sendResponse(this.channel);
            }
        } else {
            if (PermissionManager.isWhitelisted(this.requester)) {
                final LinkedList<RequestedTrack> tracksToAdd = new LinkedList<>();
                int count = 0;
                for (final AudioTrack playlistTrack : playlist.getTracks()) {
                    if (count++ != 250) {
                        if (!scheduler.isQueued(playlistTrack) && !playlistTrack.getInfo().isStream) {
                            tracksToAdd.add(new RequestedTrack(playlistTrack, requester));
                        }
                    } else {
                        break;
                    }
                }

                if (tracksToAdd.size() == 0) {
                    response = new CommandResponse("x",
                            "Sorry " + this.requester.getAsMention()
                                    + ", but none of the tracks in that playlist could be added to the queue.")
                                            .setDeletionDelay(30, TimeUnit.SECONDS);
                    response.sendResponse(this.channel);
                }

                MainThread.scheduleImmediately(new TrackAdditionTask(guild, tracksToAdd));
                response = new CommandResponse("notes",
                        this.requester.getAsMention() + " added the following tracks from a playlist to the queue:");
                response.attachEmbed(TrackUtil.trackListInfoEmbed(tracksToAdd, false));
                response.sendResponse(this.channel);
            } else {
                response = new CommandResponse("x",
                        "Sorry " + this.requester.getAsMention()
                                + ", but you are not allowed to add playlists to the queue.").setDeletionDelay(30,
                                        TimeUnit.SECONDS);
                response.sendResponse(this.channel);
            }
        }
    }

    @Override
    public final void noMatches() {
        final CommandResponse response = new CommandResponse("x",
                "Sorry " + this.requester.getAsMention() + ", but I was not able to find that track.")
                        .setDeletionDelay(30, TimeUnit.SECONDS);
        response.sendResponse(this.channel);
    }

    @Override
    public final void loadFailed(final FriendlyException exception) {
        Checks.notNull(exception, "Exception");

        final CommandResponse response;
        final String message = exception.getMessage();
        if (message.equals("Unknown file format.")) {
            response = new CommandResponse("x",
                    "Sorry " + this.requester.getAsMention() + ", but I do not recognize the format of that track.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        } else if (message.equals("The playlist is private.")) {
            response = new CommandResponse("x",
                    "Sorry " + this.requester.getAsMention() + ", but that playlist is private.").setDeletionDelay(30,
                            TimeUnit.SECONDS);
        } else {
            final Guild guild = this.channel.getGuild();
            logger.error("An error occurred for " + guild.getName() + " (" + guild.getId()
                    + ") while trying to load a track!", exception);
            response = new CommandResponse("sos",
                    "Sorry " + this.requester.getAsMention()
                            + ", but an error occurred while trying to get that track!").setDeletionDelay(30,
                                    TimeUnit.SECONDS);
        }
        response.sendResponse(this.channel);
    }
}