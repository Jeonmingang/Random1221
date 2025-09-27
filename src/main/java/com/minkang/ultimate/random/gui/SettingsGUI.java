package com.minkang.ultimate.random.gui;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import com.minkang.ultimate.random.RouletteEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SettingsGUI {
    private final Main plugin;
    private final Player player;
    private final Roulette roulette;

    public SettingsGUI(Main plugin, Player player, Roulette roulette) {
        this.plugin = plugin;
        this.player = player;
        this.roulette = roulette;
    }

    public void open() {
        // Simple read-only settings view to avoid runtime complexity.
        Inventory inv = Bukkit.createInventory(null, 54, color("&9[설정] "+roulette.getKey()));
        int i=0;
        for (RouletteEntry e : roulette.getEntries()) {
            if (i>=54) break;
            inv.setItem(i++, e.getItem());
        }
        // Add a placeholder anvil to indicate "drag item here to add"
        inv.setItem(53, new ItemStack(Material.ANVIL));
        player.openInventory(inv);
    }

    /** Utility used by external code to replace all entries (for compilation compatibility). */
    public static void applyEntries(Roulette r, List<RouletteEntry> entries) {
        if (entries == null) entries = new ArrayList<>();
        r.setEntries(entries);
        Main.get().roulettes().saveAll();
    }

    private static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
}
