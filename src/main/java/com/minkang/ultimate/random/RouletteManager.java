package com.minkang.ultimate.random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RouletteManager {
    private final Main plugin;
    private final Map<String, Roulette> map = new HashMap<>();
    private final File file;
    private FileConfiguration data;

    public RouletteManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "roulettes.yml");
        loadAll();
    }

    public Roulette getOrCreate(String key) {
        return map.computeIfAbsent(key.toLowerCase(Locale.ROOT), Roulette::new);
    }

    public Optional<Roulette> get(String key) {
        return Optional.ofNullable(map.get(key.toLowerCase(Locale.ROOT)));
    }

    public Collection<Roulette> all() { return Collections.unmodifiableCollection(map.values()); }

    public void loadAll() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create roulettes.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        map.clear();
        ConfigurationSection root = data.getConfigurationSection("roulettes");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                ConfigurationSection sec = root.getConfigurationSection(key);
                if (sec != null) {
                    Roulette r = Roulette.load(key, sec);
                    map.put(key.toLowerCase(Locale.ROOT), r);
                }
            }
        }
    }

    public void saveAll() {
        if (data == null) data = new YamlConfiguration();
        data.set("roulettes", null); // clear
        ConfigurationSection root = data.createSection("roulettes");
        for (Roulette r : map.values()) {
            ConfigurationSection sec = root.createSection(r.getKey());
            r.save(sec);
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save roulettes.yml: " + e.getMessage());
        }
    }
}
