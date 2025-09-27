package com.minkang.ultimate.random.listener;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import com.minkang.ultimate.random.gui.SpinnerGUI;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Optional;

public class InteractListener implements Listener {
    private final Main plugin;
    public InteractListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        ItemStack hand = e.getPlayer().getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;

        // check against any roulette key item (compare type + display name + lore strictly)
        for (Roulette r : plugin.roulettes().all()) {
            ItemStack key = r.getTriggerKeyItem();
            if (key == null) continue;
            if (equalsStrict(hand, key)) {
                e.setCancelled(true);
                new SpinnerGUI(plugin, e.getPlayer(), r).open();
                boolean needConsume = plugin.getConfig().getBoolean("need-key", false);
                if (needConsume) {
                    hand.setAmount(hand.getAmount()-1);
                }
                return;
            }
        }
    }

    private boolean equalsStrict(ItemStack a, ItemStack b) {
        if (a.getType() != b.getType()) return false;
        ItemMeta am = a.getItemMeta();
        ItemMeta bm = b.getItemMeta();
        if (am == null && bm == null) return true;
        if (am == null || bm == null) return false;
        if (am.hasDisplayName() != bm.hasDisplayName()) return false;
        if (am.hasDisplayName() && !am.getDisplayName().equals(bm.getDisplayName())) return false;
        if (am.hasLore() != bm.hasLore()) return false;
        if (am.hasLore() && !am.getLore().equals(bm.getLore())) return false;
        return true;
    }
}
