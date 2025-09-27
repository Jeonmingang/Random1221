
package com.minkang.ultimate.random.listener;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.Roulette;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class InteractListener implements Listener {

    private final Main plugin;

    public InteractListener(Main plugin){ this.plugin = plugin; }

    @EventHandler
    public void onUseKey(PlayerInteractEvent e){
        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() == EquipmentSlot.OFF_HAND) return;
        Player p = e.getPlayer();
        ItemStack hand = p.getInventory().getItemInMainHand();
        if (hand == null || hand.getType() == Material.AIR) return;
        if (!hand.hasItemMeta()) return;
        String key = hand.getItemMeta().getPersistentDataContainer().get(plugin.getPdcKey(), PersistentDataType.STRING);
        if (key == null || key.isEmpty()) return;
        Roulette r = plugin.getManager().get(key);
        if (r == null) return;

        // consume one key if config requires
        boolean needKey = plugin.getConfig().getBoolean("need-key", false);
        if (needKey){
            int amt = hand.getAmount();
            if (amt <= 0){
                p.sendMessage(plugin.msg("need_key").replace("%key%", key));
                return;
            }
            hand.setAmount(amt - 1);
        }

        e.setCancelled(true);
        com.minkang.ultimate.random.gui.SpinnerGUI.start(plugin, p, r);
    }
}
