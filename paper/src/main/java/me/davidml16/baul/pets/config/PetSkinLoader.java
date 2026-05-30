package me.davidml16.baul.pets.config;

import me.davidml16.baul.Main;
import me.davidml16.baul.pets.pet.PetSkin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetSkinLoader {
    private final Main plugin;
    private final File skinsFolder;
    private final Map<String, List<PetSkin>> skins = new HashMap<>();

    public PetSkinLoader(Main plugin) {
        this.plugin = plugin;
        skinsFolder = new File(plugin.getDataFolder(), "skins");
        createSkinsFolder();
    }

    private void createSkinsFolder() {
        if (!skinsFolder.exists()) {
            skinsFolder.mkdirs();
            saveDefaultSkin();
        }
    }

    private void saveDefaultSkin() {
        File exampleSkin = new File(skinsFolder, "golden_dragon.yml");
        if (!exampleSkin.exists()) {
            plugin.saveResource("skins/ender_dragon.yml", false);
        }
    }

    public void loadSkins() {
        skins.clear();
        File[] files = skinsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String id = config.getString("id");
            String name = config.getString("name");
            String petId = config.getString("pet");
            String texture = config.getString("texture");

            if (id == null || name == null || petId == null || texture == null) {
                plugin.getLogger().warning("Invalid skin configuration in " + file.getName());
                continue;
            }

            PetSkin skin = new PetSkin(id, name, petId, texture);
            skins.computeIfAbsent(petId, k -> new ArrayList<>()).add(skin);
        }
    }

    public List<PetSkin> getPetSkins(String petId) {
        return skins.getOrDefault(petId, new ArrayList<>());
    }

    public PetSkin getSkinById(String skinId) {
        for (List<PetSkin> skinList : skins.values()) {
            for (PetSkin skin : skinList) {
                if (skin.id().equals(skinId)) {
                    return skin;
                }
            }
        }
        return null;
    }

    public List<PetSkin> getAllSkins() {
        List<PetSkin> allSkins = new ArrayList<>();
        for (List<PetSkin> skinList : skins.values()) {
            allSkins.addAll(skinList);
        }
        return allSkins;
    }

    public List<PetSkin> getSkinsForPet(String petId) {
        return getPetSkins(petId);
    }
}
