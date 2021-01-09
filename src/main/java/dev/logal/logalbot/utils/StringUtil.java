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

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;

import net.dv8tion.jda.core.utils.Checks;

public final class StringUtil {
    private StringUtil() {
        // Static access only.
    }

    public static final String sanitize(final String string) {
        Checks.notNull(string, "String");

        return string.replaceAll("([_*`<@>~|])", "\\\\$1").replaceAll("[\r\n]", "");
    }

    public static final String sanitizeCodeBlock(final String string) {
        Checks.notNull(string, "String");

        return string.replaceAll("[`]", "'").replaceAll("[\r\n]", "");
    }

    public static final Emoji intToKeycapEmoji(final int number) {
        final String word;
        switch (number) {
            case 0:
                word = "zero";
                break;
            case 1:
                word = "one";
                break;
            case 2:
                word = "two";
                break;
            case 3:
                word = "three";
                break;
            case 4:
                word = "four";
                break;
            case 5:
                word = "five";
                break;
            case 6:
                word = "six";
                break;
            case 7:
                word = "seven";
                break;
            case 8:
                word = "eight";
                break;
            case 9:
                word = "nine";
                break;
            case 10:
                word = "keycap_ten";
                break;
            default:
                word = "hash";
                break;
        }

        return EmojiManager.getForAlias(word);
    }

    public static final String formatTime(final long milliseconds) {
        Checks.notNegative(milliseconds, "Milliseconds");

        final long second = (milliseconds / 1000) % 60;
        final long minute = (milliseconds / (1000 * 60)) % 60;
        final long hour = (milliseconds / (1000 * 60 * 60)) % 24;

        return String.format("%02d:%02d:%02d", hour, minute, second);
    }
}