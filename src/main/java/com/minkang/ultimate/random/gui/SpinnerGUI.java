package com.minkang.ultimate.random.gui;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import com.minkang.ultimate.random.RouletteEntry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class SpinnerGUI {
    private final Main plugin;
    private final Player player;
    private final Roulette roulette;
    private Inventory inv;

    public SpinnerGUI(Main plugin, Player player, Roulette roulette) {
        this.plugin = plugin;
        this.player = player;
        this.roulette = roulette;
    }

    public void open() {
        FileConfiguration cfg = plugin.getConfig();
        String title = color(cfg.getString("titles.spinner","&6Spinner: {key}").replace("{key}", roulette.getKey()));
        int size = Math.max(9, Math.min(54, cfg.getInt("spinner.size", 27)));
        inv = Bukkit.createInventory(null, size, title);

        // frame + pointer
        Material border = Material.matchMaterial(cfg.getString("spinner.border-material", "BLACK_STAINED_GLASS_PANE"));
        Material pointer = Material.matchMaterial(cfg.getString("spinner.pointer-material", "RED_STAINED_GLASS_PANE"));
        ItemStack filler = new ItemStack(border == null ? Material.BLACK_STAINED_GLASS_PANE : border);
        ItemMeta fm = filler.getItemMeta();
        if (fm != null) { fm.setDisplayName(" "); filler.setItemMeta(fm); }
        for (int i=0;i<size;i++) inv.setItem(i, filler);

        int pointerSlot = Math.min(size-1, Math.max(0, cfg.getInt("spinner.pointer-slot", size/2)));
        ItemStack ptr = new ItemStack(pointer == null ? Material.RED_STAINED_GLASS_PANE : pointer);
        ItemMeta pm = ptr.getItemMeta();
        if (pm != null) { pm.setDisplayName(color("&e▼")); ptr.setItemMeta(pm); }
        inv.setItem(pointerSlot, ptr);

        player.openInventory(inv);
        SpinnerProtector.markSpinning(player.getUniqueId());

        // create trail items (to simulate spinning)
        List<ItemStack> trail = new ArrayList<>();
        for (RouletteEntry e : roulette.getEntries()) {
            ItemStack it = e.getItem().clone();
            ItemMeta meta = it.getItemMeta();
            if (meta != null) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                it.setItemMeta(meta);
            }
            trail.add(it);
        }
        if (trail.isEmpty()) {
            player.closeInventory();
            player.sendMessage(color("&c구성 아이템이 없습니다."));
            SpinnerProtector.unmarkSpinning(player.getUniqueId());
            return;
        }

        int minTicks = Math.max(20, cfg.getInt("spinner.spin-ticks-min", 40));
        int maxTicks = Math.max(minTicks, cfg.getInt("spinner.spin-ticks-max", 80));
        int ticks = new Random().nextInt(maxTicks - minTicks + 1) + minTicks;

        Sound sStart = parseSound(cfg.getString("spinner.sound.start","UI_BUTTON_CLICK"), Sound.UI_BUTTON_CLICK);
        Sound sEnd = parseSound(cfg.getString("spinner.sound.end","UI_TOAST_CHALLENGE_COMPLETE"), Sound.UI_TOAST_CHALLENGE_COMPLETE);

        player.playSound(player.getLocation(), sStart, 1f, 1f);

        new BukkitRunnable() {
            int t = 0;
            int idx = 0;
            @Override
            public void run() {
                if (t >= ticks || !player.isOnline() || !player.getOpenInventory().getTopInventory().equals(inv)) {
                    this.cancel();
                    // result
                    Optional<RouletteEntry> res = roulette.spinOnce();
                    if (res.isPresent()) {
                        ItemStack got = res.get().getItem().clone();
                        player.getInventory().addItem(got);
                        player.playSound(player.getLocation(), sEnd, 1f, 1f);
                        player.sendMessage(color("&a당첨: &e"+got.getType()));
                    } else {
                        player.sendMessage(color("&c실패: 구성 없음"));
                    }
                    SpinnerProtector.unmarkSpinning(player.getUniqueId());
                    player.closeInventory();
                    return;
                }
                // show next trail item under the pointer
                int pointerSlot = Math.min(inv.getSize()-1, Math.max(0, cfg.getInt("spinner.pointer-slot", inv.getSize()/2)));
                inv.setItem(pointerSlot == 0 ? 0 : pointerSlot - 1, trail.get(idx));
                idx = (idx + 1) % trail.size();
                t++;
            }
        }.runTaskTimer(plugin, 2L, 2L);
    }

    private Sound parseSound(String name, Sound def) {
        try { return Sound.valueOf(name.toUpperCase()); } catch (Exception e) { return def; }
    }

    private static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
}
