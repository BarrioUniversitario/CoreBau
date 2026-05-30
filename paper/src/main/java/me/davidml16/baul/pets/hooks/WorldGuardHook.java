package me.davidml16.baul.pets.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardHook {

    private WorldGuard worldGuardPlugin;

    public void setupWorldGuard(Plugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardPlugin = WorldGuard.getInstance();
        }
    }

    public ApplicableRegionSet getRegionAt(Location location) {
        if (worldGuardPlugin == null) return null;
        World world = BukkitAdapter.adapt(location.getWorld());
        RegionManager regionManager = worldGuardPlugin.getPlatform().getRegionContainer().get(world);
        if (regionManager == null) return null;
        return regionManager.getApplicableRegions(
            BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ())
        );
    }

    public boolean isPlayerInRegion(Player player, String regionId) {
        Location loc = player.getLocation();
        ApplicableRegionSet regionSet = getRegionAt(loc);
        if (regionSet == null) return false;
        for (ProtectedRegion region : regionSet) {
            if (region.getId().equalsIgnoreCase(regionId)) {
                return true;
            }
        }
        return false;
    }
}
