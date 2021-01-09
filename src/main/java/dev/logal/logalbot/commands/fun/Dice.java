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

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class Dice implements Command {
    private final SecureRandom rng = new SecureRandom();

    @Override
    public CommandResponse execute(String[] arguments, Member executor, TextChannel channel) {
        if (arguments.length == 0) {
            return new CommandResponse("game_die",
                    executor.getAsMention() + ", the dice rolled **" + (rng.nextInt(6) + 1) + "**.");
        } else {
            final int maximumRange;
            try {
                maximumRange = Integer.parseInt(arguments[0]);
            } catch (final NumberFormatException exception) {
                return new CommandResponse("x",
                        "Sorry " + executor.getAsMention() + ", but the maximum range must be an integer.")
                                .setDeletionDelay(30, TimeUnit.SECONDS);
            }

            if (maximumRange < 1) {
                return new CommandResponse("x",
                        "Sorry " + executor.getAsMention() + ", but the maximum range must be at least 1.")
                                .setDeletionDelay(30, TimeUnit.SECONDS);
            }

            return new CommandResponse("game_die",
                    executor.getAsMention() + ", the dice rolled **" + (rng.nextInt(maximumRange) + 1) + "**.");
        }
    }
}