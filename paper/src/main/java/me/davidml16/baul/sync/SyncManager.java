package me.davidml16.baul.sync;

import me.davidml16.baul.Main;
import me.davidml16.baul.menus.player.CubeletsMenu;
import me.davidml16.baul.menus.player.LootHistoryMenu;
import me.davidml16.baul.menus.player.crafting.CraftingMenu;
import me.davidml16.baul.menus.player.gifts.GiftCubeletMenu;
import me.davidml16.baul.menus.player.gifts.GiftMenu;
import me.davidml16.baul.objects.CubeletMachine;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.UUID;

public class SyncManager implements PluginMessageListener {

    public static final String CHANNEL = "baul:sync";

    private final Main main;
    private final String serverName;
    private final boolean enabled;

    public SyncManager(Main main) {
        this.main = main;
        this.enabled = main.getConfig().getBoolean("Sync.Enabled", false);
        this.serverName = main.getConfig().getString("Sync.ServerName", "lobby1");

        if (enabled) {
            main.getServer().getMessenger().registerOutgoingPluginChannel(main, CHANNEL);
            main.getServer().getMessenger().registerIncomingPluginChannel(main, CHANNEL, this);
            main.getLogger().info("Sync enabled! Server name: " + serverName);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getServerName() {
        return serverName;
    }

    public void syncCubeletGive(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("CUBELET_GIVE", playerUuid);
    }

    public void syncCubeletRedeem(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("CUBELET_REDEEM", playerUuid);
    }

    public void syncPointsChange(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("POINTS_CHANGE", playerUuid);
    }

    public void syncInvalidateCache(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("CACHE_INVALIDATE", playerUuid);
    }

    public void syncMachineBusy(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("MACHINE_BUSY", playerUuid);
    }

    public void syncMachineFree(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("MACHINE_FREE", playerUuid);
    }

    public void syncCosmeticUnlock(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("COSMETIC_UNLOCK", playerUuid);
    }

    public void syncCosmeticEquip(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("COSMETIC_EQUIP", playerUuid);
    }

    public void syncCosmeticCooldown(UUID playerUuid) {
        if (!enabled) return;
        sendMessage("COSMETIC_COOLDOWN", playerUuid);
    }

    private void sendMessage(String action, UUID playerUuid) {
        sendMessage(action, playerUuid, null);
    }

    private void sendMessage(String action, UUID playerUuid, String extraData) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeUTF(action);
            out.writeUTF(playerUuid.toString());
            out.writeUTF(serverName);
            out.writeLong(System.currentTimeMillis());
            if (extraData != null) {
                out.writeUTF(extraData);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        main.getServer().sendPluginMessage(main, CHANNEL, baos.toByteArray());
    }

    public void syncMachineBusy(UUID playerUuid, CubeletMachine machine) {
        if (!enabled) return;
        Location loc = machine.getLocation();
        sendMessage("MACHINE_BUSY", playerUuid,
                loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ());
    }

    public void syncMachineFree(UUID playerUuid, CubeletMachine machine) {
        if (!enabled) return;
        Location loc = machine.getOriginalLocation();
        sendMessage("MACHINE_FREE", playerUuid,
                loc.getWorld().getName() + ";" + loc.getBlockX() + ";" + loc.getBlockY() + ";" + loc.getBlockZ());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(CHANNEL)) return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        try {
            String action = in.readUTF();
            String uuidStr = in.readUTF();
            String sourceServer = in.readUTF();
            long timestamp = in.readLong();

            String extraData = in.available() > 0 ? in.readUTF() : null;

            if (sourceServer.equals(serverName)) return;

            long age = System.currentTimeMillis() - timestamp;
            if (age > 5000) return;

            final UUID targetUuid = UUID.fromString(uuidStr);
            final String extraDataFinal = extraData;

            Bukkit.getScheduler().runTask(main, () -> handleSync(action, targetUuid, extraDataFinal));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleSync(String action, UUID playerUuid, String extraData) {
        switch (action) {
            case "MACHINE_BUSY":
                if (extraData != null) handleMachineSync(extraData, true);
                break;
            case "MACHINE_FREE":
                if (extraData != null) handleMachineSync(extraData, false);
                break;
            case "CUBELET_GIVE":
            case "CUBELET_REDEEM":
            case "POINTS_CHANGE":
            case "CACHE_INVALIDATE":
                invalidatePlayerData(playerUuid);
                break;
            case "COSMETIC_UNLOCK":
            case "COSMETIC_EQUIP":
                invalidateCosmetics(playerUuid);
                break;
            case "COSMETIC_COOLDOWN":
                invalidateCooldowns(playerUuid);
                break;
        }
    }

    private void invalidateCooldowns(UUID playerUuid) {
        Player target = Bukkit.getPlayer(playerUuid);
        if (target == null || !target.isOnline()) return;
        if (main.getEmoteCooldowns() != null) main.getEmoteCooldowns().loadForPlayer(playerUuid);
    }

    private void invalidateCosmetics(UUID playerUuid) {
        Player target = Bukkit.getPlayer(playerUuid);
        if (target == null || !target.isOnline()) return;
        if (main.getCosmeticRegistry() == null || !main.getCosmeticRegistry().isEnabled()) return;

        main.getDatabaseHandler().getOwnedCosmetics(playerUuid).thenAccept(owned ->
            Bukkit.getScheduler().runTask(main, () -> main.getPlayerDataHandler().getData(target).setOwnedCosmetics(owned)));
        main.getDatabaseHandler().getEquippedCosmetics(playerUuid).thenAccept(equipped ->
            Bukkit.getScheduler().runTask(main, () -> main.getPlayerDataHandler().getData(target).setEquippedCosmetics(equipped)));
    }

    private void handleMachineSync(String locationData, boolean busy) {
        String[] parts = locationData.split(";");
        if (parts.length < 4) return;

        String worldName = parts[0];
        int x, y, z;
        try {
            x = Integer.parseInt(parts[1]);
            y = Integer.parseInt(parts[2]);
            z = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            return;
        }

        org.bukkit.World world = Bukkit.getWorld(worldName);
        if (world == null) return;

        Location loc = new Location(world, x, y, z);
        CubeletMachine machine = main.getCubeletMachineHandler().getMachineByLocation(loc);
        if (machine == null) return;

        if (busy) {
            machine.setState(me.davidml16.baul.enums.CubeletBoxState.ANIMATION);
        } else {
            machine.setState(me.davidml16.baul.enums.CubeletBoxState.EMPTY);
        }
    }

    private void invalidatePlayerData(UUID playerUuid) {
        Player target = Bukkit.getPlayer(playerUuid);
        if (target == null || !target.isOnline()) return;

        main.getPlayerDataHandler().getData(target).getCubelets().clear();
        main.getDatabaseHandler().getCubelets(playerUuid).thenAccept(cubelets -> {
            Bukkit.getScheduler().runTask(main, () -> {
                main.getPlayerDataHandler().getData(target).getCubelets().addAll(cubelets);
                main.getDatabaseHandler().getPlayerLootPoints(playerUuid, points -> {
                    main.getPlayerDataHandler().getData(target).setLootPoints(points);
                    main.getMenuHandler().reloadAllMenus(target, CubeletsMenu.class);
                    main.getMenuHandler().reloadAllMenus(target, CraftingMenu.class);
                    main.getMenuHandler().reloadAllMenus(target, GiftMenu.class);
                    main.getMenuHandler().reloadAllMenus(target, GiftCubeletMenu.class);
                    main.getMenuHandler().reloadAllMenus(target, LootHistoryMenu.class);
                    main.getHologramImplementation().reloadHolograms(target);
                });
            });
        });
    }

    public void shutdown() {
        if (enabled) {
            main.getServer().getMessenger().unregisterOutgoingPluginChannel(main, CHANNEL);
            main.getServer().getMessenger().unregisterIncomingPluginChannel(main, CHANNEL);
        }
    }
}
