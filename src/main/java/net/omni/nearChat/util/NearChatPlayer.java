package net.omni.nearChat.util;

public class NearChatPlayer {
    private final String name;
    private boolean enabled = false;

    // TODO merge with PlayerManager

    public NearChatPlayer(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }
}
