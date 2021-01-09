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

import dev.logal.logalbot.utils.CommandManager;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.utils.Checks;

public final class CommandExecutionTask implements Runnable {
    private final String[] command;
    private final Member executor;
    private final TextChannel channel;

    public CommandExecutionTask(final String[] command, final Member executor, final TextChannel channel) {
        Checks.noneNull(command, "Command");
        Checks.notNull(executor, "Executor");
        Checks.notNull(channel, "Channel");

        this.command = command;
        this.executor = executor;
        this.channel = channel;
    }

    @Override
    public final void run() {
        CommandManager.executeCommand(this.command, this.executor, this.channel);
    }
}