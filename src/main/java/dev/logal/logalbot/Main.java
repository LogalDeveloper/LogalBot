package dev.logal.logalbot;

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

import javax.security.auth.login.LoginException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.logal.logalbot.commands.administration.*;
import dev.logal.logalbot.commands.audio.*;
import dev.logal.logalbot.commands.fun.*;
import dev.logal.logalbot.commands.general.*;
import dev.logal.logalbot.events.*;
import dev.logal.logalbot.utils.AudioUtil;
import dev.logal.logalbot.utils.CommandManager;
import dev.logal.logalbot.utils.DataManager;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;

public final class Main implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Main.class);

	private static final String token = System.getenv("TOKEN");
	private static JDA jda;

	private Main() {
		// Static access only.
	}

	public static final void main(final String[] arguments) {
		MainThread.scheduleImmediately(new Main());
	}

	@Override
	public final void run() {
		logger.info("Beginning setup of LogalBot...");

		logger.info("Verifying connection to Redis...");
		try {
			DataManager.verifyConnection();
		} catch (final Throwable exception) {
			logger.error("An error occurred while attempting to verify the connection to Redis!", exception);
			System.exit(1);
		}

		logger.info("Running any needed schema migrations...");
		try {
			DataManager.runMigrations();
		} catch (final Throwable exception) {
			logger.error("An error occurred while attempting to migrate the database!", exception);
			System.exit(1);
		}

		logger.info("Attempting to log into Discord...");
		try {
			final JDABuilder builder = new JDABuilder(AccountType.BOT);
			builder.setAutoReconnect(true);
			builder.setAudioEnabled(true);
			builder.setToken(token);
			builder.addEventListener(new GuildReady());
			jda = builder.build().awaitReady();
		} catch (final LoginException exception) {
			logger.error("The token specified is not valid.");
			System.exit(1);
		} catch (final Throwable exception) {
			logger.error("An error occurred while attempting to set up JDA!", exception);
			System.exit(1);
		}
		logger.info("Successfully logged into Discord as bot user '" + jda.getSelfUser().getName() + "'.");

		logger.info("Initializing...");
		AudioUtil.initializePlayerManager();

		jda.addEventListener(new GuildVoiceLeave());
		jda.addEventListener(new GuildVoiceMove());
		jda.addEventListener(new GuildMessageReactionAdd());

		// General Commands
		CommandManager.registerCommand("about", new About(), false);
		CommandManager.registerCommand("help", new Help(), false);

		// Fun Commands
		CommandManager.registerCommand("dice", new Dice(), false);
		CommandManager.registerCommandAlias("die", "dice");
		CommandManager.registerCommandAlias("random", "dice");
		CommandManager.registerCommandAlias("rng", "dice");
		CommandManager.registerCommandAlias("roll", "dice");
		CommandManager.registerCommand("8ball", new EightBall(), false);

		// Audio Commands
		CommandManager.registerCommand("forceskip", new ForceSkip(), true);
		CommandManager.registerCommandAlias("fs", "forceskip");
		CommandManager.registerCommandAlias("fskip", "forceskip");
		CommandManager.registerCommand("lock", new Lock(), true);
		CommandManager.registerCommandAlias("l", "lock");
		CommandManager.registerCommand("nowplaying", new NowPlaying(), false);
		CommandManager.registerCommandAlias("np", "nowplaying");
		CommandManager.registerCommand("pause", new Pause(), true);
		CommandManager.registerCommand("play", new Play(), false);
		CommandManager.registerCommandAlias("p", "play");
		CommandManager.registerCommandAlias("pl", "play");
		CommandManager.registerCommandAlias("add", "play");
		CommandManager.registerCommandAlias("a", "play");
		CommandManager.registerCommand("queue", new Queue(), false);
		CommandManager.registerCommandAlias("q", "queue");
		CommandManager.registerCommand("remove", new Remove(), true);
		CommandManager.registerCommandAlias("r", "remove");
		CommandManager.registerCommandAlias("x", "remove");
		CommandManager.registerCommandAlias("rem", "remove");
		CommandManager.registerCommandAlias("rm", "remove");
		CommandManager.registerCommand("reset", new Reset(), true);
		CommandManager.registerCommandAlias("rst", "reset");
		CommandManager.registerCommand("skip", new Skip(), false);
		CommandManager.registerCommandAlias("s", "skip");
		CommandManager.registerCommand("volume", new Volume(), true);
		CommandManager.registerCommandAlias("v", "volume");
		CommandManager.registerCommandAlias("vol", "volume");
		CommandManager.registerCommand("shuffle", new Shuffle(), true);
		CommandManager.registerCommandAlias("shuf", "shuffle");
		CommandManager.registerCommandAlias("shuff", "shuffle");
		CommandManager.registerCommandAlias("shfl", "shuffle");

		// Administration Commands
		CommandManager.registerCommand("whitelist", new Whitelist(), true);
		CommandManager.registerCommandAlias("wl", "whitelist");
		CommandManager.registerCommand("settings", new Settings(), true);
		CommandManager.registerCommandAlias("set", "settings");
		CommandManager.registerCommandAlias("setting", "settings");
		CommandManager.registerCommandAlias("configure", "settings");
		CommandManager.registerCommandAlias("config", "settings");
		CommandManager.registerCommandAlias("conf", "settings");

		logger.info("Everything seems to be ready! Enabling command listener...");
		jda.addEventListener(new GuildMessageReceived());
		logger.info("Initialization complete!");
	}
}