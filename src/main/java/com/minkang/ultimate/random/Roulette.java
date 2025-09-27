
package com.minkang.ultimate.random;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Roulette {

    private final String key;
    private final List<RouletteEntry> entries = new ArrayList<>();
    private ItemStack triggerItem;

    public Roulette(String key){
        this.key = key;
    }

    public String getKey() { return key; }

    public List<RouletteEntry> getEntries() { return entries; }

    public int getTotalWeight(){
        int sum = 0;
        for (RouletteEntry e : entries) sum += Math.max(0, e.getWeight());
        return sum;
    }

    public RouletteEntry pickByWeight(){
        int total = getTotalWeight();
        if (total <= 0) return null;
        int rnd = new Random().nextInt(total) + 1;
        int acc = 0;
        for (RouletteEntry e : entries){
            acc += Math.max(0, e.getWeight());
            if (rnd <= acc) return e;
        }
        return null;
    }

    public ItemStack getTriggerItem() { return triggerItem; }
    public void setTriggerItem(ItemStack it){ this.triggerItem = it == null ? null : it.clone(); }
}
