/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.kyori.adventure.text.Component
 *  net.kyori.adventure.text.TextComponent
 *  net.kyori.adventure.text.format.TextDecoration
 *  net.kyori.adventure.text.minimessage.MiniMessage
 *  net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
 *  net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
 */
package com.fortunepillars.utils;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class TextUtil {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private TextUtil() {
    }

    public static Component parse(String message) {
        return MINI_MESSAGE.deserialize(message).decoration(TextDecoration.ITALIC, false);
    }

    public static Component parse(String message, String ... placeholders) {
        String replaced = TextUtil.replacePlaceholders(message, placeholders);
        return TextUtil.parse(replaced);
    }

    public static String replacePlaceholders(String message, String ... placeholders) {
        if (placeholders.length % 2 != 0) {
            return message;
        }
        for (int i = 0; i < placeholders.length; i += 2) {
            message = message.replace("{" + placeholders[i] + "}", placeholders[i + 1]);
        }
        return message;
    }

    public static String toPlainText(Component component) {
        return PLAIN.serialize(component);
    }

    public static String legacyToMiniMessage(String legacy) {
        TextComponent component = LEGACY.deserialize(legacy);
        return (String)MINI_MESSAGE.serialize((Component)component);
    }

    public static String stripFormatting(String message) {
        return TextUtil.toPlainText(TextUtil.parse(message));
    }

    public static String centerText(String text) {
        int CENTER_PX = 154;
        String plainText = TextUtil.stripFormatting(text);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : plainText.toCharArray()) {
            if (c == '\u00a7') {
                previousCode = true;
                continue;
            }
            if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }
            messagePxSize += TextUtil.getCharWidth(c, isBold);
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        int spaceLength = 4;
        StringBuilder sb = new StringBuilder();
        for (int compensated = 0; compensated < toCompensate; compensated += spaceLength) {
            sb.append(" ");
        }
        return String.valueOf(sb) + text;
    }

    private static int getCharWidth(char c, boolean bold) {
        int width;
        switch (c) {
            case '!': 
            case '\'': 
            case '.': 
            case ':': 
            case ';': 
            case 'i': 
            case '|': {
                width = 2;
                break;
            }
            case 'l': {
                width = 3;
                break;
            }
            case ' ': 
            case 'I': 
            case '[': 
            case ']': 
            case 't': {
                width = 4;
                break;
            }
            case '\"': 
            case '(': 
            case ')': 
            case '*': 
            case '<': 
            case '>': 
            case 'f': 
            case 'k': 
            case '{': 
            case '}': {
                width = 5;
                break;
            }
            default: {
                width = 6;
            }
        }
        if (bold && c != ' ') {
            ++width;
        }
        return width;
    }

    public static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 3) + "...";
    }

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : text.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
                continue;
            }
            if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            }
            result.append(Character.toLowerCase(c));
        }
        return result.toString();
    }

    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }

    public static String formatNumberShort(long number) {
        if (number < 1000L) {
            return String.valueOf(number);
        }
        if (number < 1000000L) {
            return String.format("%.1fK", (double)number / 1000.0);
        }
        if (number < 1000000000L) {
            return String.format("%.1fM", (double)number / 1000000.0);
        }
        return String.format("%.1fB", (double)number / 1.0E9);
    }

    public static String progressBar(double progress, int length, String filledColor, String emptyColor) {
        int filled = (int)Math.round(progress * (double)length);
        int empty = length - filled;
        return filledColor + "\u2588".repeat(Math.max(0, filled)) + emptyColor + "\u2591".repeat(Math.max(0, empty));
    }

    public static List<String> wrapText(String text, int maxLineLength) {
        ArrayList<String> lines = new ArrayList<String>();
        StringBuilder currentLine = new StringBuilder();
        for (String word : text.split(" ")) {
            if (currentLine.length() + word.length() + 1 > maxLineLength) {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder();
            }
            currentLine.append(word).append(" ");
        }
        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString().trim());
        }
        return lines;
    }
}

