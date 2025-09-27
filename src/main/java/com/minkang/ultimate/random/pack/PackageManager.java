package com.minkang.ultimate.random.pack;

import com.minkang.ultimate.random.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PackageManager {
    private final Main plugin;
    private final Map<String, PackageDef> map = new HashMap<>();
    private final Map<String, Map<UUID, Integer>> claims = new HashMap<>();
    private final File file;
    private FileConfiguration data;

    public PackageManager(Main plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "packages.yml");
        loadAll();
    }

    public PackageDef getOrCreate(String name){
        return map.computeIfAbsent(name.toLowerCase(Locale.ROOT), PackageDef::new);
    }
    public Optional<PackageDef> get(String name){
        return Optional.ofNullable(map.get(name.toLowerCase(Locale.ROOT)));
    }

    public int getTickets(String name, UUID uid){
        return claims.getOrDefault(name.toLowerCase(Locale.ROOT), Collections.emptyMap()).getOrDefault(uid, 0);
    }
    public void giveTickets(String name, UUID uid, int cnt){
        String k = name.toLowerCase(Locale.ROOT);
        claims.computeIfAbsent(k, s -> new HashMap<>()).merge(uid, Math.max(1,cnt), Integer::sum);
    }
    public boolean useTicket(String name, UUID uid){
        String k = name.toLowerCase(Locale.ROOT);
        Map<UUID,Integer> m = claims.get(k);
        if (m == null) return false;
        int cur = m.getOrDefault(uid, 0);
        if (cur <= 0) return false;
        m.put(uid, cur-1);
        return true;
    }

    public void loadAll() {
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create packages.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
        map.clear();
        claims.clear();
        ConfigurationSection root = data.getConfigurationSection("packages");
        if (root != null) {
            for (String key : root.getKeys(false)) {
                ConfigurationSection sec = root.getConfigurationSection(key);
                if (sec == null) continue;
                PackageDef def = new PackageDef(key);
                List<ItemStack> items = (List<ItemStack>) sec.getList("items", Collections.emptyList());
                if (items != null) def.getItems().addAll(items);
                map.put(key.toLowerCase(Locale.ROOT), def);
            }
        }
        ConfigurationSection croot = data.getConfigurationSection("claims");
        if (croot != null) {
            for (String pkg : croot.getKeys(false)) {
                Map<UUID,Integer> m = new HashMap<>();
                for (String uid : croot.getConfigurationSection(pkg).getKeys(false)) {
                    int val = croot.getConfigurationSection(pkg).getInt(uid, 0);
                    try { m.put(UUID.fromString(uid), val); } catch (IllegalArgumentException ignored) {}
                }
                claims.put(pkg.toLowerCase(Locale.ROOT), m);
            }
        }
    }

    public void saveAll() {
        if (data == null) data = new YamlConfiguration();
        data.set("packages", null);
        data.set("claims", null);
        ConfigurationSection root = data.createSection("packages");
        for (PackageDef def : map.values()) {
            ConfigurationSection sec = root.createSection(def.getName());
            sec.set("items", def.getItems());
        }
        ConfigurationSection croot = data.createSection("claims");
        for (Map.Entry<String, Map<UUID, Integer>> e : claims.entrySet()) {
            ConfigurationSection sec = croot.createSection(e.getKey());
            for (Map.Entry<UUID,Integer> in : e.getValue().entrySet()) {
                sec.set(in.getKey().toString(), in.getValue());
            }
        }
        try {
            data.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save packages.yml: " + e.getMessage());
        }
    }
}
