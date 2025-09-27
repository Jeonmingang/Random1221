package com.minkang.ultimate.random.gui;

import com.minkang.ultimate.random.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpinnerProtector implements Listener {
    private static final Set<UUID> spinning = new HashSet<>();
    @SuppressWarnings("unused")
    private final Main plugin;

    public SpinnerProtector(Main plugin) {
        this.plugin = plugin;
    }

    public static void markSpinning(UUID id) { spinning.add(id); }
    public static void unmarkSpinning(UUID id) { spinning.remove(id); }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (spinning.contains(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (spinning.contains(e.getWhoClicked().getUniqueId())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        spinning.remove(e.getPlayer().getUniqueId());
    }
}
