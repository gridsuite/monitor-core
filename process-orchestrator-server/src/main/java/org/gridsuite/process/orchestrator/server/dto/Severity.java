package org.gridsuite.process.orchestrator.server.dto;

public enum Severity {
    UNKNOWN(0),
    TRACE(1),
    DEBUG(2),
    DETAIL(3),
    INFO(4),
    WARN(5),
    ERROR(6),
    FATAL(7);

    private final int level;

    Severity(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }

    public static Severity fromValue(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        try {
            return valueOf(value);
        } catch (final IllegalArgumentException | NullPointerException e) {
            return UNKNOWN;
        }
    }
}
