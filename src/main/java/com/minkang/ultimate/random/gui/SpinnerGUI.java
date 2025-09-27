
package com.minkang.ultimate.random.gui;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import com.minkang.ultimate.random.RouletteEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SpinnerGUI {

    public static void start(final Main plugin, final Player p, final Roulette r){
        final String title = plugin.getConfig().getString("titles.spinner","룰렛 뽑기: %key%").replace("%key%", r.getKey());
        final Inventory inv = Bukkit.createInventory(p, 27, ChatColor.translateAlternateColorCodes('&', title));

        final Material glassMat;
        try {
            glassMat = Material.valueOf(plugin.getConfig().getString("spinner.glass", "EMERALD_BLOCK"));
        } catch (IllegalArgumentException ex){
            throw new IllegalArgumentException("Invalid glass material in config.");
        }
        final ItemStack glass = new ItemStack(glassMat);
        ItemMeta gm = glass.getItemMeta();
        if (gm != null){
            gm.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("spinner.glass-name","&f")));
            glass.setItemMeta(gm);
        }
        for (int i=0;i<9;i++) inv.setItem(i, glass);
        for (int i=18;i<27;i++) inv.setItem(i, glass);

        if (plugin.getConfig().getBoolean("spinner.use-pointer-panes", true)){
            ItemStack pointer = new ItemStack(Material.HOPPER);
            ItemMeta pmeta = pointer.getItemMeta();
            if (pmeta != null){
                pmeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("spinner.pointer-name","&6▲ 중앙 슬롯 ▼")));
                pointer.setItemMeta(pmeta);
            }
            inv.setItem(4, pointer);
            inv.setItem(22, pointer);
        }

        // Build a working list duplicated by weight
        final List<ItemStack> weighted = new ArrayList<>();
        for (RouletteEntry e : r.getEntries()){
            int w = Math.max(1, e.getWeight());
            for (int i=0;i<w;i++) weighted.add(e.getItem().clone());
        }
        if (weighted.isEmpty()){
            p.sendMessage(plugin.msg("no_items"));
            p.closeInventory();
            return;
        }

        p.openInventory(inv);
        p.sendMessage(plugin.msg("draw_start"));

        final Random rng = new Random();
        final int[] cursor = new int[]{0};
        final int center = 13; // 3x9 inventory central slot

        new BukkitRunnable(){
            int ticks = 0;
            int totalTicks = 20 * 6; // ~6 seconds
            @Override
            public void run() {
                if (!p.getOpenInventory().getTopInventory().equals(inv)){
                    cancel();
                    return;
                }
                // pick next item
                ItemStack show = weighted.get(rng.nextInt(weighted.size())).clone();
                // glow if enabled
                if (plugin.getConfig().getBoolean("spinner.glow", true)){
                    ItemMeta m = show.getItemMeta();
                    if (m != null){
                        m.addEnchant(org.bukkit.enchantments.Enchantment.LUCK, 1, true);
                        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                        show.setItemMeta(m);
                    }
                }
                inv.setItem(center, show);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.6f);

                // slow down
                ticks++;
                int remain = totalTicks - ticks;
                if (remain <= 0){
                    cancel();
                    // winner
                    ItemStack win = show.clone();
                    // give item
                    java.util.Map<Integer, ItemStack> leftover = p.getInventory().addItem(win);
                    for (ItemStack lf : leftover.values()){
                        if (lf != null) p.getWorld().dropItemNaturally(p.getLocation(), lf);
                    }
                    String name = (win.hasItemMeta() && win.getItemMeta().hasDisplayName())
                            ? win.getItemMeta().getDisplayName() : win.getType().name();
                    p.sendMessage(plugin.msg("draw_win").replace("%item%", ChatColor.stripColor(name)));
                    p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                    p.closeInventory();
                    return;
                }
                long period = Math.max(1, remain / 3); // decelerate
                this.runTaskLater(plugin, period);
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }
}
