package me.davidml16.baul.pets.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MountManager {

    private final Map<UUID, UUID> playerMount;

    public MountManager() {
        this.playerMount = new HashMap<>();
    }

    public void mount(UUID playerUUID, UUID mountUUID) {
        playerMount.put(playerUUID, mountUUID);
    }

    public UUID getMount(UUID playerUUID) {
        return playerMount.get(playerUUID);
    }

    public boolean isMounted(UUID playerUUID) {
        return playerMount.containsKey(playerUUID);
    }

    public void unmount(UUID playerUUID) {
        playerMount.remove(playerUUID);
    }
}
