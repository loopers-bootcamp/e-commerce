package com.loopers.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static String padStart(String s, int length, char pad) {
        if (length <= 0) {
            return "";
        }

        int originLength = s.length();
        int padCount = length - originLength;

        // No copy; fastest path
        if (padCount <= 0) {
            return s;
        }

        char[] chars = new char[length];
        Arrays.fill(chars, 0, padCount, pad);

        // Bulk copy
        s.getChars(0, originLength, chars, padCount);

        return new String(chars);
    }

    public static String invert9sComplement(String s) {
        char[] chars = s.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            chars[i] = (char) ('9' - (c - '0'));
        }

        return new String(chars);
    }

}
