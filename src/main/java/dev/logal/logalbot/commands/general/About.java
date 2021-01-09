package dev.logal.logalbot.commands.general;

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

import dev.logal.logalbot.commands.Command;
import dev.logal.logalbot.commands.CommandResponse;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;

public final class About implements Command {
    @Override
    public CommandResponse execute(final String[] arguments, final Member executor, final TextChannel channel) {
        return new CommandResponse("wave", "Hello " + executor.getAsMention()
                + "! I'm LogalBot, a bot created by LogalDeveloper. https://logal.dev/");
    }
}