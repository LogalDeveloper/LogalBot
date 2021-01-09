package dev.logal.logalbot.commands;

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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.tasks.MessageDeleteTask;
import dev.logal.logalbot.tasks.ReactionCallbackExpireTask;
import dev.logal.logalbot.utils.ReactionCallbackManager;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

public final class CommandResponse {
    private final Emoji emoji;
    private final String response;
    private final LinkedHashMap<Emoji, ReactionCallback> callbacks = new LinkedHashMap<>();
    private MessageEmbed responseEmbed;
    private User callbacksTarget;

    private long deletionDelay, expireDelay = 0;
    private TimeUnit deletionDelayUnit, expireDelayUnit;

    public CommandResponse(final String emoji, final String response) {
        Checks.notEmpty(emoji, "Emoji");
        Checks.notEmpty(response, "Response");

        this.emoji = EmojiManager.getForAlias(emoji);
        Checks.notNull(this.emoji, "Valid Emoji");
        this.response = response;
    }

    public final CommandResponse attachEmbed(final MessageEmbed embed) {
        Checks.notNull(embed, "Embed");

        this.responseEmbed = embed;
        return this;
    }

    public final CommandResponse setDeletionDelay(final long delay, final TimeUnit unit) {
        Checks.notNegative(delay, "Delay");
        Checks.notNull(unit, "Unit");

        this.deletionDelay = delay;
        this.deletionDelayUnit = unit;
        return this;
    }

    public final CommandResponse addReactionCallback(final String emoji, final ReactionCallback callback) {
        Checks.notEmpty(emoji, "Emoji");
        Checks.notNull(callback, "Callback");

        this.callbacks.put(EmojiManager.getForAlias(emoji), callback);
        return this;
    }

    public final CommandResponse setReactionCallbackTarget(final Member member) {
        Checks.notNull(member, "Member");

        this.callbacksTarget = member.getUser();
        return this;
    }

    public final CommandResponse setReactionCallbackExpireDelay(final long delay, final TimeUnit unit) {
        Checks.notNegative(delay, "Delay");
        Checks.notNull(unit, "Unit");

        this.expireDelay = delay;
        this.expireDelayUnit = unit;
        return this;
    }

    public final void sendResponse(final TextChannel channel) {
        Checks.notNull(channel, "Channel");

        final MessageBuilder builder = new MessageBuilder();
        builder.setContent(this.emoji.getUnicode() + " " + this.response);

        if (this.responseEmbed != null) {
            builder.setEmbed(this.responseEmbed);
        }

        channel.sendMessage(builder.build()).queue(this::handleResponseCreation);
    }

    private final void handleResponseCreation(final Message message) {
        if ((this.deletionDelay != 0) && (this.deletionDelayUnit != null)) {
            MainThread.scheduleLater(new MessageDeleteTask(message), this.deletionDelay, this.deletionDelayUnit);
        } else if ((this.expireDelay != 0) && (this.expireDelayUnit != null)) {
            MainThread.scheduleLater(new ReactionCallbackExpireTask(message), this.expireDelay, this.expireDelayUnit);
        }

        for (final Map.Entry<Emoji, ReactionCallback> callback : callbacks.entrySet()) {
            ReactionCallbackManager.registerCallback(message, callback.getKey(), callback.getValue());
        }

        if (callbacksTarget != null) {
            ReactionCallbackManager.setCallbackTarget(message, this.callbacksTarget);
        }
    }
}