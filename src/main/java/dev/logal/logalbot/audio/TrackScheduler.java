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

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.tasks.CloseAudioConnectionTask;
import dev.logal.logalbot.tasks.OpenAudioConnectionTask;
import dev.logal.logalbot.tasks.PlayNextTrackTask;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.DataManager;
import dev.logal.logalbot.utils.PermissionManager;
import dev.logal.logalbot.utils.SkipTracker;
import dev.logal.logalbot.utils.VoiceChannelUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.utils.Checks;

public final class TrackScheduler extends AudioEventAdapter {
    private static final Logger logger = LoggerFactory.getLogger(TrackScheduler.class);

    private final Guild guild;
    private final LinkedList<RequestedTrack> queue = new LinkedList<>();
    private boolean queueLocked = false;
    private ScheduledFuture<?> closeAudioConnectionTask;

    public TrackScheduler(final Guild guild) {
        Checks.notNull(guild, "Guild");

        this.guild = guild;
    }

    public final void addToQueue(final RequestedTrack requestedTrack) {
        Checks.notNull(requestedTrack, "Requested Track");

        final Member requester = requestedTrack.getRequester();
        if (this.queueLocked && !PermissionManager.isWhitelisted(requester)) {
            return;
        }

        if (this.isQueueFull()) {
            return;
        }

        final User user = requester.getUser();
        final AudioTrack track = requestedTrack.getTrack();
        logger.info(user.getName() + " (" + user.getId() + ") added '" + track.getInfo().title + "' to the queue in "
                + this.guild.getName() + " (" + this.guild.getId() + ").");

        final VoiceChannel targetChannel = VoiceChannelUtil.getVoiceChannelMemberIsConnectedTo(requester);
        if (!AudioUtil.isAudioConnectionOpen(this.guild)
                || targetChannel != AudioUtil.getVoiceChannelConnectedTo(this.guild)) {
            MainThread.scheduleImmediately(
                    new OpenAudioConnectionTask(VoiceChannelUtil.getVoiceChannelMemberIsConnectedTo(requester)));
        }

        if (!AudioUtil.isTrackLoaded(this.guild) && this.queue.size() == 0) {
            MainThread.scheduleImmediately(new PlayNextTrackTask(this.guild));
        }
        this.queue.add(requestedTrack);
    }

    public final void removeFromQueue(final int index) {
        Checks.notNegative(index, "Index");

        logger.info("Track '" + queue.remove(index).getTrack().getInfo().title + "' has been removed from the queue in "
                + this.guild.getName() + " (" + this.guild.getId() + ").");
    }

    public final boolean isQueued(final AudioTrack track) {
        Checks.notNull(track, "Track");

        for (final RequestedTrack queuedTrack : queue) {
            if (track.getInfo().identifier.equals(queuedTrack.getTrack().getInfo().identifier)) {
                return true;
            }
        }
        return false;
    }

    public final boolean isQueueFull() {
        return this.queue.size() >= 250;
    }

    public final boolean isQueueEmpty() {
        return this.queue.isEmpty();
    }

    public final boolean isQueueLocked() {
        return this.queueLocked;
    }

    public final void setQueueLocked(final boolean locked) {
        this.queueLocked = locked;
    }

    public final void clearQueue() {
        this.queue.clear();
    }

    public final void shuffleQueue() {
        Collections.shuffle(this.queue);
    }

    public final RequestedTrack popFromQueue() {
        return this.queue.remove(0);
    }

    public final int occupiedSlotCount(Member member) {
        Checks.notNull(member, "Member");

        int count = 0;
        for (RequestedTrack requestedTrack : this.queue) {
            if (requestedTrack.getRequester().equals(member)) {
                count++;
            }
        }
        return count;
    }

    public final LinkedList<RequestedTrack> getQueue() {
        return this.queue;
    }

    public final void skipCurrentTrack() {
        if (AudioUtil.isTrackLoaded(this.guild)) {
            logger.info("Track '" + AudioUtil.getLoadedTrack(this.guild).getTrack().getInfo().title + "' in "
                    + this.guild.getName() + " (" + this.guild.getId() + ") been skipped.");
            AudioUtil.stopTrack(this.guild);
        }
    }

    @Override
    public final void onTrackStart(final AudioPlayer player, final AudioTrack track) {
        Checks.notNull(player, "Player");
        Checks.notNull(track, "Track");

        logger.info("Track '" + track.getInfo().title + "' in " + this.guild.getName() + " (" + this.guild.getId()
                + ") has started.");
        if (this.closeAudioConnectionTask != null && !this.closeAudioConnectionTask.isDone()) {
            logger.info("A track has started in " + this.guild.getName() + " (" + this.guild.getId()
                    + "). Cancelling scheduled disconnect.");
            this.closeAudioConnectionTask.cancel(true);
        }
        SkipTracker.resetVotes(this.guild);
    }

    @Override
    public final void onTrackEnd(final AudioPlayer player, final AudioTrack track,
            final AudioTrackEndReason endReason) {
        Checks.notNull(player, "Player");
        Checks.notNull(track, "Track");
        Checks.notNull(endReason, "End reason");

        logger.info("Track '" + track.getInfo().title + "' in " + this.guild.getName() + " (" + this.guild.getId()
                + ") has stopped.");
        if ((endReason.mayStartNext || endReason == AudioTrackEndReason.STOPPED) && this.queue.size() >= 1) {
            MainThread.scheduleImmediately(new PlayNextTrackTask(this.guild));
        } else {
            try {
                AudioUtil.setVolume(this.guild, Integer.parseInt(DataManager.getGuildValue(guild, "defaultVolume")));
            } catch (final NumberFormatException exception) {
                AudioUtil.setVolume(this.guild, 10);
            }
            this.queueLocked = false;
            AudioUtil.setPausedState(this.guild, false);
            final VoiceChannel currentChannel = AudioUtil.getVoiceChannelConnectedTo(this.guild);
            logger.info("Disconnecting from " + currentChannel.getName() + " (" + currentChannel.getId() + ") in "
                    + this.guild.getName() + " (" + this.guild.getId() + ") in 1 minute...");
            this.closeAudioConnectionTask = MainThread.scheduleLater(new CloseAudioConnectionTask(this.guild), 1,
                    TimeUnit.MINUTES);
        }
        SkipTracker.resetVotes(this.guild);
    }
}