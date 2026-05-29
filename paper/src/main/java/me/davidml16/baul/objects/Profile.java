package me.davidml16.baul.objects;

import me.davidml16.baul.Main;
import me.davidml16.baul.objects.loothistory.LootHistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Profile {

	private Main main;

	private UUID uuid;

	private CubeletMachine boxOpened;

	private List<Cubelet> cubelets;

	private List<LootHistory> lootHistory;

	private long lootPoints;

	private String orderBy;

	private String animation;

	private Set<String> ownedCosmetics;

	private Map<String, String> equippedCosmetics;

	private boolean cosmeticsVisible;

	// Adaptive luck: count of opens since the last high-rarity (< 10% chance) reward.
	// Resets to 0 when one is hit. In-memory only (resets on rejoin).
	private int unluckyOpens;

	public Profile(Main main, UUID uuid) {
		this.main = main;
		this.uuid = uuid;
		this.cubelets = new ArrayList<>();
		this.lootHistory = new ArrayList<>();
		this.boxOpened = null;
		this.orderBy = "date";
		this.lootPoints = 0;
		this.animation = "animation2";
		this.ownedCosmetics = new HashSet<>();
		this.equippedCosmetics = new HashMap<>();
		this.cosmeticsVisible = true;
		this.unluckyOpens = 0;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public List<Cubelet> getCubelets() { return cubelets; }

	public void setCubelets(List<Cubelet> cubelets) { this.cubelets = cubelets; }

	public List<LootHistory> getLootHistory() {
		return lootHistory;
	}

	public void setLootHistory(List<LootHistory> lootHistory) {
		this.lootHistory = lootHistory;
	}

	public CubeletMachine getBoxOpened() { return boxOpened; }

	public void setBoxOpened(CubeletMachine boxOpened) { this.boxOpened = boxOpened; }

	public String getOrderBy() { return orderBy; }

	public void setOrderBy(String orderBy) { this.orderBy = orderBy; }

	public long getLootPoints() { return lootPoints; }

	public void setLootPoints(long lootPoints) { this.lootPoints = lootPoints; }

	public String getAnimation() { return animation; }

	public void setAnimation(String animation) { this.animation = animation; }

	public Set<String> getOwnedCosmetics() { return ownedCosmetics; }

	public void setOwnedCosmetics(Set<String> ownedCosmetics) {
		this.ownedCosmetics = ownedCosmetics != null ? ownedCosmetics : new HashSet<>();
	}

	public boolean ownsCosmetic(String cosmeticId) {
		return cosmeticId != null && ownedCosmetics.contains(cosmeticId);
	}

	public Map<String, String> getEquippedCosmetics() { return equippedCosmetics; }

	public void setEquippedCosmetics(Map<String, String> equippedCosmetics) {
		this.equippedCosmetics = equippedCosmetics != null ? equippedCosmetics : new HashMap<>();
	}

	public String getEquipped(String categoryId) {
		return categoryId == null ? null : equippedCosmetics.get(categoryId);
	}

	public boolean isCosmeticsVisible() { return cosmeticsVisible; }

	public void setCosmeticsVisible(boolean cosmeticsVisible) { this.cosmeticsVisible = cosmeticsVisible; }

	public int getUnluckyOpens() { return unluckyOpens; }

	public void setUnluckyOpens(int unluckyOpens) { this.unluckyOpens = Math.max(0, unluckyOpens); }

	@Override
	public String toString() {
		return "Profile{" +
				"main=" + main +
				", uuid=" + uuid +
				", boxOpened=" + boxOpened +
				", cubelets=" + cubelets +
				", orderBy='" + orderBy + '\'' +
				'}';
	}

}
