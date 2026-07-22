package com.project.url_shortener.util;

public final class Base62Codec {

    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int BASE = ALPHABET.length();
    private static final int MIN_LENGTH = 6;

    private Base62Codec() {
    }

    public static String encode(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        if (value == 0) {
            return pad(String.valueOf(ALPHABET.charAt(0)));
        }

        StringBuilder encoded = new StringBuilder();
        long current = value;
        while (current > 0) {
            encoded.append(ALPHABET.charAt((int) (current % BASE)));
            current /= BASE;
        }
        return pad(encoded.reverse().toString());
    }

    private static String pad(String encoded) {
        if (encoded.length() >= MIN_LENGTH) {
            return encoded;
        }
        return "0".repeat(MIN_LENGTH - encoded.length()) + encoded;
    }
}
