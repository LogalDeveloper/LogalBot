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

import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.Checks;
import redis.clients.jedis.Jedis;

public final class DataManager {
    private static final Logger logger = LoggerFactory.getLogger(DataManager.class);

    private static final String host = System.getenv("REDIS_HOST");
    private static final String password = System.getenv("REDIS_PASSWORD");
    private static final String databaseNumber = System.getenv("REDIS_DATABASE");
    private static Jedis jedis = new Jedis();

    private DataManager() {
        // Static access only.
    }

    public static final void verifyConnection() {
        if (!jedis.isConnected()) {
            jedis = new Jedis(host);

            if (password != null) {
                jedis.auth(password);
            }

            if (databaseNumber != null) {
                final int num = Integer.parseInt(databaseNumber);
                jedis.select(num);
            }
        }
    }

    public static final void runMigrations() {
        if (jedis.get("schemaVersion") == null) {
            logger.info("Migrating schema to version 1...");
            jedis.set("schemaVersion", "1");
            logger.info("Migration to schema version 1 complete.");
        }
    }

    public static final String getUserValue(final Member member, final String key) {
        Checks.notNull(member, "Member");
        Checks.notEmpty(key, "Key");

        return jedis.get("g" + member.getGuild().getId() + ":u" + member.getUser().getId() + ":" + key);
    }

    public static final void setUserValue(final Member member, final String key, final String value) {
        Checks.notNull(member, "Member");
        Checks.notEmpty(key, "Key");
        Checks.notEmpty(value, "Value");

        jedis.set("g" + member.getGuild().getId() + ":u" + member.getUser().getId() + ":" + key, value);
    }

    public static final void deleteUserKey(final Member member, final String key) {
        Checks.notNull(member, "Member");
        Checks.notEmpty(key, "Key");

        jedis.del("g" + member.getGuild().getId() + ":u" + member.getUser().getId() + ":" + key);
    }

    public static final String getGlobalUserValue(final User user, final String key) {
        Checks.notNull(user, "User");
        Checks.notEmpty(key, "Key");

        return jedis.get("u" + user.getId() + ":" + key);
    }

    public static final void setGlobalUserValue(final User user, final String key, final String value) {
        Checks.notNull(user, "User");
        Checks.notEmpty(key, "Key");
        Checks.notEmpty(value, "Value");

        jedis.set("u" + user.getId() + ":" + key, value);
    }

    public static final void deleteGlobalUserKey(final User user, final String key) {
        Checks.notNull(user, "User");
        Checks.notEmpty(key, "Key");

        jedis.del("u" + user.getId() + ":" + key);
    }

    public static final String getGuildValue(final Guild guild, final String key) {
        Checks.notNull(guild, "Guild");
        Checks.notEmpty(key, "Key");

        return jedis.get("g" + guild.getId() + ":" + key);
    }

    public static final void setGuildValue(final Guild guild, final String key, final String value) {
        Checks.notNull(guild, "Guild");
        Checks.notEmpty(key, "Key");
        Checks.notEmpty(value, "Value");

        jedis.set("g" + guild.getId() + ":" + key, value);
    }

    public static final void deleteGuildKey(final Guild guild, final String key) {
        Checks.notNull(guild, "Guild");
        Checks.notEmpty(key, "Key");

        jedis.del("g" + guild.getId() + ":" + key);
    }
}