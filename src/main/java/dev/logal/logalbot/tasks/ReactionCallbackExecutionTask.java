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

import dev.logal.logalbot.utils.ReactionCallbackManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.Checks;

public final class ReactionCallbackExecutionTask implements Runnable {
    private final long messageID;
    private final TextChannel channel;
    private final Member executor;
    private final String emoji;

    public ReactionCallbackExecutionTask(final long messageID, final TextChannel channel, final Member executor,
            final String emoji) {
        Checks.notNegative(messageID, "Message ID");
        Checks.notNull(channel, "Channel");
        Checks.notNull(executor, "Executor");
        Checks.notEmpty(emoji, "Emoji");

        this.messageID = messageID;
        this.channel = channel;
        this.executor = executor;
        this.emoji = emoji;
    }

    @Override
    public void run() {
        ReactionCallbackManager.executeCallback(this.messageID, this.channel, this.executor, this.emoji);
    }
}