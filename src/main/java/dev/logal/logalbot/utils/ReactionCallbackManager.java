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

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import dev.logal.logalbot.commands.ReactionCallback;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;

public final class ReactionCallbackManager {
    private static final HashMap<Long, HashMap<Emoji, ReactionCallback>> callbackDictionary = new HashMap<>();
    private static final HashMap<Long, Long> targetDictionary = new HashMap<>();

    private ReactionCallbackManager() {
        // Static access only.
    }

    public static final void registerCallback(final Message message, final Emoji emoji,
            final ReactionCallback callback) {
        Checks.notNull(message, "Message");
        Checks.notNull(emoji, "Emoji");
        Checks.notNull(callback, "Callback");

        if (!callbackDictionary.containsKey(message.getIdLong())) {
            callbackDictionary.put(message.getIdLong(), new HashMap<>());
        }

        callbackDictionary.get(message.getIdLong()).put(emoji, callback);
        message.addReaction(emoji.getUnicode()).queue();
    }

    public static final void setCallbackTarget(final Message message, final User user) {
        Checks.notNull(message, "Messsage");
        Checks.notNull(user, "User");

        targetDictionary.put(message.getIdLong(), user.getIdLong());
    }

    public static final void unregisterMessage(final Message message, final boolean delete) {
        Checks.notNull(message, "Message");

        if (delete) {
            message.delete().queue();
        } else {
            if (callbackDictionary.containsKey(message.getIdLong())) {
                message.addReaction(EmojiManager.getForAlias("no_entry").getUnicode()).queue();
            }
        }

        callbackDictionary.remove(message.getIdLong());
        targetDictionary.remove(message.getIdLong());
    }

    public static final void executeCallback(final long messageID, final TextChannel channel, final Member reactor,
            final String emoji) {
        Checks.notNegative(messageID, "Message ID");
        Checks.notNull(channel, "Channel");
        Checks.notNull(reactor, "Reactor");
        Checks.notEmpty(emoji, "Emoji");

        channel.getMessageById(messageID).queue((message) -> {
            if (callbackDictionary.containsKey(messageID)) {
                if (targetDictionary.containsKey(messageID)
                        && !targetDictionary.get(messageID).equals(reactor.getUser().getIdLong())) {
                    return;
                }

                final Emoji parsedEmoji = EmojiManager.getByUnicode(emoji);
                if (callbackDictionary.get(messageID).containsKey(parsedEmoji)) {
                    callbackDictionary.get(messageID).get(parsedEmoji).run(reactor, message);
                }
            }
        });
    }
}