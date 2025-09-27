
package com.minkang.ultimate.random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RouletteManager {

    private final Main plugin;
    private final File dataFile;
    private final Map<String, Roulette> map = new HashMap<>();

    public RouletteManager(Main plugin){
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "roulettes.yml");
        load();
    }

    public void load(){
        map.clear();
        if (!dataFile.exists()) return;
        YamlConfiguration y = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection root = y.getConfigurationSection("roulettes");
        if (root == null) return;
        for (String key : root.getKeys(false)){
            ConfigurationSection sec = root.getConfigurationSection(key);
            if (sec == null) continue;
            Roulette r = new Roulette(key);
            List<Map<?,?>> list = sec.getMapList("entries");
            for (Map<?,?> m : list){
                ItemStack item = (ItemStack) m.get("item");
                int weight = (int) m.getOrDefault("weight", 1);
                if (item != null) r.getEntries().add(new RouletteEntry(item, weight));
            }
            ItemStack trig = sec.getItemStack("triggerItem");
            if (trig != null) r.setTriggerItem(trig);
            map.put(key.toLowerCase(), r);
        }
    }

    public void save(){
        YamlConfiguration y = new YamlConfiguration();
        ConfigurationSection root = y.createSection("roulettes");
        for (Roulette r : map.values()){
            ConfigurationSection sec = root.createSection(r.getKey());
            List<Map<String,Object>> list = new ArrayList<>();
            for (RouletteEntry e : r.getEntries()){
                Map<String,Object> m = new HashMap<>();
                m.put("item", e.getItem());
                m.put("weight", e.getWeight());
                list.add(m);
            }
            sec.set("entries", list);
            sec.set("triggerItem", r.getTriggerItem());
        }
        try { y.save(dataFile); } catch (IOException ignored) {}
    }

    public boolean exists(String key){ return map.containsKey(key.toLowerCase()); }
    public Roulette create(String key){
        key = key.toLowerCase();
        Roulette r = new Roulette(key);
        map.put(key, r);
        save();
        return r;
    }
    public boolean delete(String key){
        key = key.toLowerCase();
        Roulette removed = map.remove(key);
        save();
        return removed != null;
    }
    public Roulette get(String key){ return key == null ? null : map.get(key.toLowerCase()); }
    public Collection<Roulette> all(){ return map.values(); }
}
