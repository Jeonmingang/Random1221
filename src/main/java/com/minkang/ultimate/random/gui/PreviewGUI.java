
package com.minkang.ultimate.random.gui;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import com.minkang.ultimate.random.RouletteEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class PreviewGUI implements Listener {

    private final Main plugin;

    public PreviewGUI(Main plugin){ this.plugin = plugin; }

    public static void open(Main plugin, org.bukkit.entity.Player p, Roulette r){
        String title = plugin.getConfig().getString("titles.preview","구성 미리보기: %key%").replace("%key%", r.getKey());
        Inventory inv = Bukkit.createInventory(p, 54, ChatColor.translateAlternateColorCodes('&', title));
        boolean showWeight = plugin.getConfig().getBoolean("preview.show-weight", true);
        boolean showChance = plugin.getConfig().getBoolean("preview.show-chance", true);
        String weightFmt = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.weight-format","&7가중치: &e%weight%"));
        String chanceFmt = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("preview.chance-format","&7확률: &e%chance%%"));
        List<String> removeTags = plugin.getConfig().getStringList("preview.remove-tags");
        int total = r.getTotalWeight();
        DecimalFormat df = new DecimalFormat("#.##");
        int slot = 0;
        for (RouletteEntry e : r.getEntries()){
            if (slot >= 54) break;
            ItemStack it = e.getItem().clone();
            ItemMeta m = it.getItemMeta();
            if (m != null){
                List<String> lore = m.hasLore() ? m.getLore() : new ArrayList<>();
                if (lore == null) lore = new ArrayList<>();
                if (removeTags != null && !removeTags.isEmpty()){
                    List<String> filtered = new ArrayList<>();
                    for (String line : lore){
                        String plain = ChatColor.stripColor(line);
                        boolean banned = false;
                        for (String tag : removeTags){
                            if (plain != null && plain.contains(tag)) { banned = true; break; }
                        }
                        if (!banned) filtered.add(line);
                    }
                    lore = filtered;
                }
                if (showWeight) lore.add(weightFmt.replace("%weight%", String.valueOf(e.getWeight())));
                if (showChance) {
                    double c = total > 0 ? (e.getWeight() * 100.0 / total) : 0.0;
                    lore.add(chanceFmt.replace("%chance%", df.format(c)));
                }
                m.setLore(lore);
                it.setItemMeta(m);
            }
            inv.setItem(slot++, it);
        }
        p.openInventory(inv);
        p.sendMessage(plugin.msg("open_preview").replace("%key%", r.getKey()));
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        String title = e.getView().getTitle();
        if (title == null) return;
        String plain = ChatColor.stripColor(title);
        if (plain == null) return;
        if (plain.startsWith("구성 미리보기:") || plain.contains("미리보기")){
            if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())){
                e.setCancelled(true);
            }
        }
    }
}
