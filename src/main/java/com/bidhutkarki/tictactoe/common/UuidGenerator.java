package com.bidhutkarki.tictactoe.common;

import com.fasterxml.uuid.Generators;

public final class UuidGenerator {

    private UuidGenerator() {
    }

    /** Generates a time-ordered UUIDv7 rendered as 32 hex chars without dashes. */
    public static String newId() {
        return Generators.timeBasedEpochGenerator().generate().toString().replace("-", "");
    }
}
