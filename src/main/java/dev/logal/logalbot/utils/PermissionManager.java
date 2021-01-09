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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.utils.Checks;

public final class PermissionManager {
    private static final Logger logger = LoggerFactory.getLogger(PermissionManager.class);

    private PermissionManager() {
        // Static access only.
    }

    public static final boolean isWhitelisted(final Member member) {
        Checks.notNull(member, "Member");

        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }

        if (DataManager.getUserValue(member, "whitelisted") == null) {
            DataManager.setUserValue(member, "whitelisted", "false");
        }

        return DataManager.getUserValue(member, "whitelisted").equals("true");
    }

    public static final void addToWhitelist(final Member member) {
        Checks.notNull(member, "Member");

        DataManager.setUserValue(member, "whitelisted", "true");
        logger.info(member.getEffectiveName() + " (" + member.getUser().getId() + ") was added to the whitelist in "
                + member.getGuild().getName() + " (" + member.getGuild().getId() + ").");
    }

    public static final void removeFromWhitelist(final Member member) {
        Checks.notNull(member, "Member");

        DataManager.setUserValue(member, "whitelisted", "false");
        logger.info(member.getEffectiveName() + " (" + member.getUser().getId() + ") was removed from the whitelist in "
                + member.getGuild().getName() + " (" + member.getGuild().getId() + ").");
    }
}