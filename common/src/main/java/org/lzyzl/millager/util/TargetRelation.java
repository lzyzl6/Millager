package org.lzyzl.millager.util;

import java.util.Locale;

public enum TargetRelation {
    HOSTILE("hostile"),
    NEUTRAL("neutral"),
    FRIENDLY("friendly");

    private final String serializedName;

    TargetRelation(String serializedName) {
        this.serializedName = serializedName;
    }

    public String serializedName() {
        return this.serializedName;
    }

    public static TargetRelation fromSerializedName(String name) {
        if (name == null) return null;
        for (TargetRelation relation : values()) {
            if (relation.serializedName.equals(name.toLowerCase(Locale.ROOT))) return relation;
        }
        return null;
    }
}
