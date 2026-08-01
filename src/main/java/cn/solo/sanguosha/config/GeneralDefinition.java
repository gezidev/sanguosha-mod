/*
 * Decompiled with CFR 0.152.
 */
package cn.solo.sanguosha.config;

import java.util.List;

public record GeneralDefinition(String id, String name, String kingdom, int health, String imageId, String imageFormat, List<Skill> skills) {
    public static final String CUSTOM_GROUP = "\u81ea\u5b9a\u4e49";

    public GeneralDefinition {
        if (imageId == null) {
            imageId = "";
        }
        if (imageFormat == null) {
            imageFormat = "";
        }
        if (skills == null) {
            skills = List.of();
        }
    }

    public GeneralDefinition(String id, String name, String kingdom, int health, String legacyImage, List<Skill> skills) {
        this(id, name, kingdom, health, legacyImage != null && legacyImage.matches("[a-f0-9]{64}") ? legacyImage : "", "", skills);
    }

    public boolean hasImage() {
        return this.imageId.matches("[a-f0-9]{64}") && ("png".equals(this.imageFormat) || "gif".equals(this.imageFormat));
    }

    public String category() {
        return this.id.startsWith("custom_") ? CUSTOM_GROUP : "\u6807";
    }

    public static GeneralDefinition custom(String id, String name, String kingdom, int health, String imageId, String imageFormat, List<Skill> skills) {
        return new GeneralDefinition((String)(id.startsWith("custom_") ? id : "custom_" + id), name, kingdom, health, imageId, imageFormat, skills);
    }

    public record Skill(String name, String description) {
    }
}

