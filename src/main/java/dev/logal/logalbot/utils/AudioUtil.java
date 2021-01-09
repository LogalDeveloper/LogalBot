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

import java.util.HashMap;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.logal.logalbot.audio.AudioPlayerSendHandler;
import dev.logal.logalbot.audio.RequestedTrack;
import dev.logal.logalbot.audio.TrackLoadHandler;
import dev.logal.logalbot.audio.TrackScheduler;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.managers.AudioManager;
import net.dv8tion.jda.core.utils.Checks;

public final class AudioUtil {
    private static final Logger logger = LoggerFactory.getLogger(AudioUtil.class);

    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static final HashMap<Long, AudioPlayer> players = new HashMap<>();
    private static final HashMap<Long, TrackScheduler> schedulers = new HashMap<>();
    private static final HashMap<Long, RequestedTrack> loadedTracks = new HashMap<>();

    private AudioUtil() {
        // Static access only.
    }

    public static final void initializePlayerManager() {
        AudioSourceManagers.registerRemoteSources(playerManager);
    }

    public static final void initialize(final Guild guild) {
        Checks.notNull(guild, "Guild");

        players.put(guild.getIdLong(), playerManager.createPlayer());
        schedulers.put(guild.getIdLong(), new TrackScheduler(guild));
        players.get(guild.getIdLong()).addListener(schedulers.get(guild.getIdLong()));

        try {
            setVolume(guild, Integer.parseInt(DataManager.getGuildValue(guild, "defaultVolume")));
        } catch (final NumberFormatException exception) {
            setVolume(guild, 10);
        }

        getTrackScheduler(guild).setQueueLocked(false);
        setPausedState(guild, false);

        logger.info("Audio environment initialized for " + guild.getName() + " (" + guild.getId() + ").");
    }

    public static final void openAudioConnection(final VoiceChannel channel) {
        Checks.notNull(channel, "Channel");

        final Guild guild = channel.getGuild();
        final AudioManager audioManager = guild.getAudioManager();

        audioManager.setSendingHandler(new AudioPlayerSendHandler(players.get(guild.getIdLong())));
        audioManager.openAudioConnection(channel);
    }

    public static final void closeAudioConnection(final Guild guild) {
        Checks.notNull(guild, "Guild");

        guild.getAudioManager().closeAudioConnection();
    }

    public static final VoiceChannel getVoiceChannelConnectedTo(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return guild.getAudioManager().getConnectedChannel();
    }

    public static final boolean isAudioConnectionOpen(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return guild.getAudioManager().isConnected();
    }

    public static final void playTrack(final Guild guild, final RequestedTrack track) {
        Checks.notNull(guild, "Guild");
        Checks.notNull(track, "Requested Track");

        players.get(guild.getIdLong()).playTrack(track.getTrack());
        loadedTracks.put(guild.getIdLong(), track);
    }

    public static final void playNextTrack(final Guild guild) {
        Checks.notNull(guild, "Guild");

        final RequestedTrack track = schedulers.get(guild.getIdLong()).popFromQueue();
        if (track != null) {
            playTrack(guild, track);
        }
    }

    public static final void stopTrack(final Guild guild) {
        Checks.notNull(guild, "Guild");

        loadedTracks.remove(guild.getIdLong());
        players.get(guild.getIdLong()).stopTrack();
    }

    public static final boolean isTrackLoaded(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return !(getLoadedTrack(guild) == null);
    }

    public static final RequestedTrack getLoadedTrack(final Guild guild) {
        Checks.notNull(guild, "Guild");

        if (players.get(guild.getIdLong()).getPlayingTrack() == null) {
            loadedTracks.remove(guild.getIdLong());
            return null;
        } else {
            return loadedTracks.get(guild.getIdLong());
        }
    }

    public static final void setPausedState(final Guild guild, final boolean pausedState) {
        Checks.notNull(guild, "Guild");

        guild.getAudioManager().setSelfMuted(pausedState);
        players.get(guild.getIdLong()).setPaused(pausedState);

        if (pausedState) {
            logger.info("The audio player was paused in " + guild.getName() + " (" + guild.getId() + ").");
        } else {
            logger.info("The audio player was resumed in " + guild.getName() + " (" + guild.getId() + ").");
        }
    }

    public static final boolean isPlayerPaused(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return players.get(guild.getIdLong()).isPaused();
    }

    public static final void setVolume(final Guild guild, final int volume) {
        Checks.notNull(guild, "Guild");
        Checks.positive(volume, "Volume");

        players.get(guild.getIdLong()).setVolume(volume);
        logger.info("The audio player's volume was set to " + getVolume(guild) + "% in " + guild.getName() + " ("
                + guild.getId() + ").");
    }

    public static final int getVolume(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return players.get(guild.getIdLong()).getVolume();
    }

    public static final void findTrack(final String query, final Member requester, final TextChannel channel) {
        Checks.notEmpty(query, "Query");
        Checks.notNull(requester, "Requester");
        Checks.notNull(channel, "Channel");

        playerManager.loadItem(query, new TrackLoadHandler(requester, channel));
    }

    public static final TrackScheduler getTrackScheduler(final Guild guild) {
        Checks.notNull(guild, "Guild");

        return schedulers.get(guild.getIdLong());
    }
}