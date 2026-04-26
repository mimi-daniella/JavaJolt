package com.daniella.util;

import java.util.List;

public final class AvatarCatalog {

    private static final List<String> AVATARS = List.of(
            "/images/avatars/orangehead.png",
            "/images/avatars/bobgirl.png",
            "/images/avatars/boy1.png",
            "/images/avatars/man2.png",
            "/images/avatars/afrogirl.png",
            "/images/avatars/animal.png");

    private AvatarCatalog() {
    }

    public static List<String> all() {
        return AVATARS;
    }

    public static String defaultAvatarFor(String seed) {
        if (seed == null || seed.isBlank()) {
            return AVATARS.get(0);
        }
        int index = Math.floorMod(seed.toLowerCase().hashCode(), AVATARS.size());
        return AVATARS.get(index);
    }

    public static boolean isSupported(String avatarPath) {
        return AVATARS.contains(avatarPath);
    }
}
