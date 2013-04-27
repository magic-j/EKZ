package com.magic_j.pluginhelpers;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RegionHelper {
	
	private static WorldGuardPlugin worldGuard = null;

	public static void init(WorldGuardPlugin wg) {
		if (wg != null) {
			worldGuard = wg;
		}
	}	
	
	public static ProtectedRegion getCurrentRegion(Player p) {
		final RegionManager mgr = worldGuard.getGlobalRegionManager().get(p.getWorld());	
		LocalPlayer localPlayer = worldGuard.wrapPlayer(p);
		final Vector pt = localPlayer.getPosition();
        final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
        if (set.size() == 0) {
        	return null;
        }
		return set.iterator().next();
	}

	public static String getCurrentRegionName(Player player) {
		ProtectedRegion region = getCurrentRegion(player);
		if (region == null) {
			return "";
		}
		return region.getId();
	}

	public static boolean isOwnerOf(Player currentPlayer, ProtectedRegion region) {
		DefaultDomain owners = region.getOwners();
		return owners.contains(currentPlayer.getName());
	}

	public static String getshopMembersByRegionName(String rgName, World world) {
		ProtectedRegion shopRegion = getRegionByName(rgName, world);
		if (shopRegion == null) {
			return null;
		}
		DefaultDomain members = shopRegion.getMembers();
		return members.toUserFriendlyString();
	}


	public static ProtectedRegion getBlockRegion(Block block) {
		final RegionManager mgr = worldGuard.getGlobalRegionManager().get(block.getWorld());	
		final Vector pt = new Vector(block.getX(), block.getY(), block.getZ());
		final ApplicableRegionSet set = mgr.getApplicableRegions(pt);
        if (set.size() == 0) {
        	return null;
        }
		return set.iterator().next();
	}

	public static ProtectedRegion getRegionByName(String rgName, World world) {
		final RegionManager mgr = worldGuard.getGlobalRegionManager().get(world);	
		return mgr.getRegion(rgName);
	}

}
