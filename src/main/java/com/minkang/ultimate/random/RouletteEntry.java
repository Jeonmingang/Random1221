
package com.minkang.ultimate.random;

import org.bukkit.inventory.ItemStack;

public class RouletteEntry {
    private ItemStack item;
    private int weight;

    public RouletteEntry(ItemStack item, int weight){
        this.item = item == null ? null : item.clone();
        this.weight = Math.max(1, weight);
    }

    public ItemStack getItem() { return item == null ? null : item.clone(); }
    public void setItem(ItemStack item) { this.item = item == null ? null : item.clone(); }

    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = Math.max(1, weight); }
}
