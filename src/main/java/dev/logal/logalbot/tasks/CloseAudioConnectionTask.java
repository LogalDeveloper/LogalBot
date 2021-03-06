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

import dev.logal.logalbot.utils.AudioUtil;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.utils.Checks;

public final class CloseAudioConnectionTask implements Runnable {
    private final Guild guild;

    public CloseAudioConnectionTask(final Guild guild) {
        Checks.notNull(guild, "Guild");

        this.guild = guild;
    }

    @Override
    public final void run() {
        AudioUtil.closeAudioConnection(guild);
    }
}