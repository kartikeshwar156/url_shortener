package com.project.url_shortener.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Base62CodecTest {

    @Test
    void encodesZeroWithPadding() {
        assertEquals("000000", Base62Codec.encode(0));
    }

    @Test
    void encodesSmallValuesWithMinimumLength() {
        assertEquals("000001", Base62Codec.encode(1));
        assertEquals("00000A", Base62Codec.encode(10));
    }

    @Test
    void distinctInputsProduceDistinctOutputs() {
        Set<String> codes = new HashSet<>();
        for (long i = 0; i < 10_000; i++) {
            String code = Base62Codec.encode(i);
            assertTrue(codes.add(code), "Collision at value " + i);
        }
    }

    @Test
    void rejectsNegativeValues() {
        assertThrows(IllegalArgumentException.class, () -> Base62Codec.encode(-1));
    }
}
