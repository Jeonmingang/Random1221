
package com.minkang.ultimate.random.pack;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PackageDef {

    private final String name;
    private final List<ItemStack> items = new ArrayList<>();

    public PackageDef(String name){ this.name = name; }

    public String getName(){ return name; }

    public List<ItemStack> getItems(){ return items; }

    public void setItems(List<ItemStack> list){
        items.clear();
        if (list != null){
            for (ItemStack it : list){
                if (it != null && it.getType() != org.bukkit.Material.AIR){
                    ItemStack copy = it.clone();
                    copy.setAmount(Math.max(1, Math.min(copy.getAmount(), 64)));
                    items.add(copy);
                }
            }
        }
    }
}
