package com.loopers.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

    public static String invert9sComplement(String s) {
        char[] chars = new char[s.length()];

        for (int i = 0; i < chars.length; i++) {
            char c = s.charAt(i); // '0'..'9'
            chars[i] = (char) ('9' - (c - '0'));
        }

        return new String(chars);
    }

}
