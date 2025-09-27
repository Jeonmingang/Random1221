
package com.minkang.ultimate.random.pack;

import com.minkang.ultimate.random.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;

public class PackageClaimGUI implements Listener {

    private final Main plugin;
    public PackageClaimGUI(Main plugin){ this.plugin = plugin; }

    public static void open(Main plugin, Player p, PackageDef def, boolean claimMode){
        String title = plugin.getConfig().getString("titles.package-claim", "패키지: %name%").replace("%name%", def.getName());
        Inventory inv = Bukkit.createInventory(p, 54, ChatColor.translateAlternateColorCodes('&', title));
        int slot = 0;
        for (ItemStack it : def.getItems()){
            if (slot >= 45) break;
            inv.setItem(slot++, it.clone());
        }
        // Claim button
        ItemStack btn = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta m = btn.getItemMeta();
        if (m != null){
            m.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("package.claim-button-name", "&a[ 보상 수령 ]")));
            m.setLore(java.util.Arrays.asList(
                    ChatColor.translateAlternateColorCodes('&', "&7아래 보상 아이템이 지급됩니다.")
            ));
            btn.setItemMeta(m);
        }
        inv.setItem(49, btn);
        p.openInventory(inv);
        if (claimMode){
            p.sendMessage(plugin.color("&a보상 수령 GUI가 열렸습니다. 버튼을 클릭하세요."));
        } else {
            p.sendMessage(plugin.color("&a패키지 구성 미리보기입니다."));
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        String title = e.getView().getTitle();
        if (title == null) return;
        String plain = ChatColor.stripColor(title);
        if (plain == null || !plain.startsWith("패키지:")) return;

        if (e.getClickedInventory() != null && e.getClickedInventory().equals(e.getView().getTopInventory())){
            e.setCancelled(true);
            if (e.getRawSlot() == 49){
                Player p = (Player) e.getWhoClicked();
                String name = plain.replace("패키지:","").trim();
                PackageDef def = plugin.getPackageManager().get(name.toLowerCase());
                if (def == null){
                    p.closeInventory();
                    return;
                }
                // consume claim
                if (!plugin.getPackageManager().consumeClaim(name, p.getUniqueId())){
                    p.sendMessage(plugin.color("&c수령 가능한 권한(회수)이 없습니다."));
                    return;
                }
                // give items
                for (ItemStack it : def.getItems()){
                    ItemStack copy = it.clone();
                    java.util.Map<Integer, ItemStack> leftover = p.getInventory().addItem(copy);
                    for (ItemStack lf : leftover.values()){
                        if (lf != null) p.getWorld().dropItemNaturally(p.getLocation(), lf);
                    }
                }
                p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                p.sendMessage(plugin.color("&a보상을 수령했습니다: &e" + name));
                p.closeInventory();
            }
        }
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){
        // just viewing; nothing to do
    }
}
