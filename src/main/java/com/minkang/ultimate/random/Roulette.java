package com.minkang.ultimate.random;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Roulette {
    private final String key;
    private final List<RouletteEntry> entries = new ArrayList<>();
    private ItemStack triggerKeyItem; // nullable

    public Roulette(String key) {
        this.key = key;
    }

    public String getKey() { return key; }
    public List<RouletteEntry> getEntries() { return Collections.unmodifiableList(entries); }
    public void setEntries(List<RouletteEntry> newEntries) {
        entries.clear();
        if (newEntries != null) entries.addAll(newEntries);
    }
    public void addEntry(RouletteEntry e) { entries.add(e); }
    public void clear() { entries.clear(); }

    public Optional<RouletteEntry> spinOnce() {
        if (entries.isEmpty()) return Optional.empty();
        int total = 0;
        for (RouletteEntry e : entries) total += Math.max(1, e.getWeight());
        int r = ThreadLocalRandom.current().nextInt(total);
        int acc = 0;
        for (RouletteEntry e : entries) {
            acc += Math.max(1, e.getWeight());
            if (r < acc) return Optional.of(e);
        }
        return Optional.of(entries.get(entries.size() - 1));
    }

    public ItemStack getTriggerKeyItem() { return triggerKeyItem; }
    public void setTriggerKeyItem(ItemStack keyItem) { this.triggerKeyItem = keyItem; }

    public void save(ConfigurationSection sec) {
        sec.set("key", key);
        // entries
        List<Map<String, Object>> list = new ArrayList<>();
        for (RouletteEntry e : entries) {
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("item", e.getItem());
            m.put("weight", e.getWeight());
            list.add(m);
        }
        sec.set("entries", list);
        if (triggerKeyItem != null) sec.set("triggerKey", triggerKeyItem);
    }

    @SuppressWarnings("unchecked")
    public static Roulette load(String key, ConfigurationSection sec) {
        Roulette r = new Roulette(key);
        List<Map<?,?>> list = (List<Map<?,?>>) sec.getList("entries", Collections.emptyList());
        for (Map<?,?> m : list) {
            Object itemObj = m.get("item");
            Object weightObj = m.get("weight");
            if (itemObj instanceof ItemStack) {
                int w = 1;
                if (weightObj instanceof Number) w = ((Number) weightObj).intValue();
                r.addEntry(new RouletteEntry((ItemStack) itemObj, w));
            }
        }
        ItemStack trig = sec.getItemStack("triggerKey");
        if (trig != null) r.setTriggerKeyItem(trig);
        return r;
    }
}
