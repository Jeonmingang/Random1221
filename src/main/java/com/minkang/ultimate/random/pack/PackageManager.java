
package com.minkang.ultimate.random.pack;

import com.minkang.ultimate.random.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PackageManager {

    private final Main plugin;
    private final File file;
    private final Map<String, PackageDef> map = new HashMap<>();
    private final Map<String, Map<UUID,Integer>> claims = new HashMap<>();

    public PackageManager(Main plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "packages.yml");
        load();
    }

    public void load(){
        map.clear();
        claims.clear();
        if (!file.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection root = y.getConfigurationSection("packages");
        if (root != null){
            for (String name : root.getKeys(false)){
                PackageDef def = new PackageDef(name);
                List<ItemStack> list = (List<ItemStack>) root.getList(name + ".items");
                if (list != null) def.setItems(list);
                map.put(name.toLowerCase(), def);
            }
        }
        ConfigurationSection croot = y.getConfigurationSection("claims");
        if (croot != null){
            for (String name : croot.getKeys(false)){
                ConfigurationSection cs = croot.getConfigurationSection(name);
                Map<UUID,Integer> m = new HashMap<>();
                if (cs != null){
                    for (String uuidStr : cs.getKeys(false)){
                        try {
                            UUID u = UUID.fromString(uuidStr);
                            int count = cs.getInt(uuidStr);
                            m.put(u, count);
                        } catch (IllegalArgumentException ignored){}
                    }
                }
                claims.put(name.toLowerCase(), m);
            }
        }
    }

    public void save(){
        YamlConfiguration y = new YamlConfiguration();
        ConfigurationSection root = y.createSection("packages");
        for (PackageDef def : map.values()){
            root.set(def.getName() + ".items", def.getItems());
        }
        ConfigurationSection croot = y.createSection("claims");
        for (Map.Entry<String, Map<UUID,Integer>> en : claims.entrySet()){
            ConfigurationSection cs = croot.createSection(en.getKey());
            for (Map.Entry<UUID,Integer> ce : en.getValue().entrySet()){
                cs.set(ce.getKey().toString(), ce.getValue());
            }
        }
        try { y.save(file); } catch (IOException ignored){}
    }

    public boolean exists(String name){ return map.containsKey(name.toLowerCase()); }
    public PackageDef create(String name){
        name = name.toLowerCase();
        PackageDef def = new PackageDef(name);
        map.put(name, def);
        save();
        return def;
    }
    public PackageDef get(String name){ return name == null ? null : map.get(name.toLowerCase()); }
    public boolean delete(String name){
        name = name.toLowerCase();
        PackageDef removed = map.remove(name);
        save();
        return removed != null;
    }

    public int getClaims(String name, UUID u){
        Map<UUID,Integer> m = claims.computeIfAbsent(name.toLowerCase(), k -> new HashMap<>());
        return m.getOrDefault(u, 0);
    }
    public void addClaims(String name, UUID u, int amount){
        Map<UUID,Integer> m = claims.computeIfAbsent(name.toLowerCase(), k -> new HashMap<>());
        m.put(u, Math.max(0, m.getOrDefault(u, 0) + amount));
        save();
    }
    public boolean consumeClaim(String name, UUID u){
        Map<UUID,Integer> m = claims.computeIfAbsent(name.toLowerCase(), k -> new HashMap<>());
        int left = m.getOrDefault(u, 0);
        if (left <= 0) return false;
        m.put(u, left - 1);
        save();
        return true;
    }

    public Collection<PackageDef> all(){ return map.values(); }
}
