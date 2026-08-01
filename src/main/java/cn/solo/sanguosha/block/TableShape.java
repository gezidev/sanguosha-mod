/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.StringRepresentable
 */
package cn.solo.sanguosha.block;

import net.minecraft.util.StringRepresentable;

public enum TableShape implements StringRepresentable
{
    SINGLE("single"),
    END("end"),
    CENTER("center"),
    CORNER("corner"),
    FALLBACK("fallback");

    private final String serializedName;

    private TableShape(String serializedName) {
        this.serializedName = serializedName;
    }

    public String m_7912_() {
        return this.serializedName;
    }
}

