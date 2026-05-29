package me.davidml16.baul.cosmetics;

import me.davidml16.baul.Main;
import me.davidml16.baul.enums.CraftType;
import me.davidml16.baul.objects.CraftIngredient;
import me.davidml16.baul.objects.Profile;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Parallel to {@link me.davidml16.baul.handlers.CubeletCraftingHandler} but for
 * cosmetics. Each recipe maps a cosmetic id (e.g. "trail_rainbow") to a list of
 * {@link CraftIngredient}s (POINTS / CUBELET / MONEY) the player must spend to
 * unlock that cosmetic.
 *
 * Stores recipes in {@code plugins/Baul/cosmetic-crafting.yml}. Generates a
 * default file with one example recipe if missing.
 */
public class CosmeticCraftingHandler {

    private final Main main;
    private File file;
    private YamlConfiguration config;
    private final Map<String, List<CraftIngredient>> recipes = new LinkedHashMap<>();

    public CosmeticCraftingHandler(Main main) {
        this.main = main;
    }

    public Map<String, List<CraftIngredient>> getRecipes() {
        return Collections.unmodifiableMap(recipes);
    }

    public List<CraftIngredient> getRecipe(String cosmeticId) {
        return recipes.get(cosmeticId == null ? null : cosmeticId.toLowerCase());
    }

    public boolean hasRecipe(String cosmeticId) {
        return cosmeticId != null && recipes.containsKey(cosmeticId.toLowerCase());
    }

    public void load() {
        recipes.clear();

        if (!main.getDataFolder().exists()) main.getDataFolder().mkdirs();
        // Same unified crafting.yml that CubeletCraftingHandler reads.
        // It's extracted from the bundled resource on first run (by whichever
        // handler runs first; both are idempotent via saveResource(false)).
        file = new File(main.getDataFolder(), "crafting.yml");
        if (!file.exists()) main.saveResource("crafting.yml", false);
        config = YamlConfiguration.loadConfiguration(file);

        if (!config.contains("cosmetic-crafting.crafts")) {
            config.set("cosmetic-crafting.crafts", new ArrayList<>());
            save();
        }

        Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eLoading cosmetic crafts:"));

        if (config.getConfigurationSection("cosmetic-crafting.crafts") == null) return;
        for (String cosmeticId : config.getConfigurationSection("cosmetic-crafting.crafts").getKeys(false)) {
            List<CraftIngredient> ingredients = new ArrayList<>();
            if (config.getConfigurationSection("cosmetic-crafting.crafts." + cosmeticId + ".ingredients") == null) {
                continue;
            }
            for (String idx : config.getConfigurationSection("cosmetic-crafting.crafts." + cosmeticId + ".ingredients").getKeys(false)) {
                String typeName = config.getString("cosmetic-crafting.crafts." + cosmeticId + ".ingredients." + idx + ".type", "points");
                int amount = config.getInt("cosmetic-crafting.crafts." + cosmeticId + ".ingredients." + idx + ".amount", 0);
                if (amount <= 0) continue;

                CraftType ct;
                try {
                    ct = CraftType.valueOf(typeName.toUpperCase());
                } catch (IllegalArgumentException ex) {
                    main.getLogger().warning("Unknown craft type '" + typeName + "' in recipe '" + cosmeticId + "', skipping");
                    continue;
                }

                if (ct == CraftType.MONEY) {
                    if (!main.getEconomyHandler().isLoaded()) continue;
                    ingredients.add(new CraftIngredient(cosmeticId, CraftType.MONEY, amount));
                } else if (ct == CraftType.CUBELET) {
                    String name = config.getString("cosmetic-crafting.crafts." + cosmeticId + ".ingredients." + idx + ".name");
                    if (name == null || !main.getCubeletTypesHandler().getTypes().containsKey(name)) continue;
                    ingredients.add(new CraftIngredient(cosmeticId, CraftType.CUBELET, name, amount));
                } else {
                    ingredients.add(new CraftIngredient(cosmeticId, CraftType.POINTS, amount));
                }
            }
            recipes.put(cosmeticId.toLowerCase(), ingredients);
        }

        Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format(
                recipes.isEmpty()
                        ? "    &cNo cosmetic crafts loaded."
                        : "    &b" + recipes.size() + " &acosmetic crafts loaded!"));
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean hasIngredients(Player player, List<CraftIngredient> ingredients) {
        for (CraftIngredient i : ingredients) {
            if (!hasIngredient(player, i)) return false;
        }
        return true;
    }

    public boolean hasIngredient(Player player, CraftIngredient ingredient) {
        if (ingredient.getCraftType() == CraftType.CUBELET) {
            long amt = main.getPlayerDataHandler().getData(player).getCubelets().stream()
                    .filter(c -> c.getType().equalsIgnoreCase(ingredient.getName())).count();
            return amt >= ingredient.getAmount();
        } else if (ingredient.getCraftType() == CraftType.MONEY) {
            return main.getEconomyHandler().getBalance(player) >= ingredient.getAmount();
        } else if (ingredient.getCraftType() == CraftType.POINTS) {
            return main.getPlayerDataHandler().getData(player).getLootPoints() >= ingredient.getAmount();
        }
        return false;
    }

    public void consumeIngredients(Player player, List<CraftIngredient> ingredients) {
        for (CraftIngredient i : ingredients) {
            if (i.getCraftType() == CraftType.CUBELET) {
                try {
                    main.getTransactionHandler().removeCubelet(player.getUniqueId(), i.getName(), i.getAmount());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else if (i.getCraftType() == CraftType.MONEY) {
                main.getEconomyHandler().removeBalance(player, i.getAmount());
            } else if (i.getCraftType() == CraftType.POINTS) {
                Profile p = main.getPlayerDataHandler().getData(player);
                p.setLootPoints(p.getLootPoints() - i.getAmount());
                main.getDatabaseHandler().setPlayerLootPoints(player.getUniqueId(), p.getLootPoints());
                if (main.getSyncManager() != null) main.getSyncManager().syncPointsChange(player.getUniqueId());
            }
        }
    }
}
