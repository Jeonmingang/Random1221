package com.minkang.ultimate.random.pack;

import org.bukkit.inventory.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class PackageDef {
    private final String name;
    private final List<ItemStack> items = new ArrayList<>();

    public PackageDef(String name) { this.name = name; }
    public String getName() { return name; }
    public List<ItemStack> getItems() { return items; }
    public void add(ItemStack it) { items.add(it); }
    public void clear() { items.clear(); }
}
