package dev.logal.logalbot.events;

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

import dev.logal.logalbot.MainThread;
import dev.logal.logalbot.tasks.ReactionCallbackExecutionTask;
import net.dv8tion.jda.core.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.utils.Checks;

public final class GuildMessageReactionAdd extends ListenerAdapter {
    @Override
    public final void onGuildMessageReactionAdd(final GuildMessageReactionAddEvent event) {
        Checks.notNull(event, "Event");

        if (!event.getUser().equals(event.getJDA().getSelfUser())) {
            MainThread.scheduleImmediately(new ReactionCallbackExecutionTask(event.getMessageIdLong(),
                    event.getChannel(), event.getMember(), event.getReactionEmote().getName()));
        }
    }
}