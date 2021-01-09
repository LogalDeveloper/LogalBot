package dev.logal.logalbot.commands.fun;

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

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.vdurmont.emoji.EmojiManager;

import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import dev.logal.logalbot.utils.StringUtil;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class EightBall implements Command {
    private final ArrayList<String> responses = new ArrayList<>(20);
    private final Random rng = new Random();

    public EightBall() {
        responses.add("It is certain");
        responses.add("It is decidedly so");
        responses.add("Without a doubt");
        responses.add("Yes - definitely");
        responses.add("You may rely on it");
        responses.add("As I see it, yes");
        responses.add("Most likely");
        responses.add("Outlook good");
        responses.add("Yes");
        responses.add("Signs point to yes");

        responses.add("Reply hazy, try again");
        responses.add("Ask again later");
        responses.add("Better not tell you now");
        responses.add("Cannot predict now");
        responses.add("Concentrate and ask again");

        responses.add("Don't count on it");
        responses.add("My reply is no");
        responses.add("My sources say no");
        responses.add("Outlook not so good");
        responses.add("Very doubtful");
    }

    @Override
    public final CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        if (arguments.length == 0) {
            return new CommandResponse("x",
                    "Sorry, " + executor.getAsMention() + ", but you need to supply a question for the Magic 8 Ball.")
                            .setDeletionDelay(30, TimeUnit.SECONDS);
        }

        final String question = StringUtil.sanitizeCodeBlock(String.join(" ", arguments));

        return new CommandResponse("question",
                executor.getAsMention() + " asked the Magic 8 Ball: `" + question + "`\n"
                        + EmojiManager.getForAlias("8ball").getUnicode() + " The Magic 8 Ball responds: *"
                        + responses.get(rng.nextInt(responses.size())) + "*.");
    }
}