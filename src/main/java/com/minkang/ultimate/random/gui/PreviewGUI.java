package com.minkang.ultimate.random.gui;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import com.minkang.ultimate.random.RouletteEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class PreviewGUI {
    private final Main plugin;
    private final Player player;
    private final Roulette roulette;

    public PreviewGUI(Main plugin, Player player, Roulette roulette) {
        this.plugin = plugin;
        this.player = player;
        this.roulette = roulette;
    }

    public void open() {
        FileConfiguration cfg = plugin.getConfig();
        String title = color(cfg.getString("titles.preview","&aPreview: {key}").replace("{key}", roulette.getKey()));
        int size = 54;
        Inventory inv = Bukkit.createInventory(null, size, title);
        int total = roulette.getEntries().stream().mapToInt(RouletteEntry::getWeight).sum();
        int i = 0;
        for (RouletteEntry e : roulette.getEntries()) {
            if (i >= size) break;
            ItemStack it = e.getItem().clone();
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
                if (cfg.getBoolean("preview.show-weight", true)) {
                    double chance = total>0 ? (100.0*e.getWeight()/total) : 0.0;
                    String fmt = cfg.getString("preview.lore-format", "&7weight: &e{weight} &7p: &e{chance}%");
                    lore.add(color(fmt.replace("{weight}", String.valueOf(e.getWeight()))
                            .replace("{chance}", String.format("%.2f", chance))));
                }
                meta.setLore(lore);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
                it.setItemMeta(meta);
            }
            inv.setItem(i++, it);
        }
        player.openInventory(inv);
    }

    private static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
}
