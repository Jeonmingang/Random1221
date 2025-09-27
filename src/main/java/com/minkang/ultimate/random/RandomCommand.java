
package com.minkang.ultimate.random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;

public class RandomCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public RandomCommand(Main plugin) {
        this.plugin = plugin;
    }

    private boolean isAdmin(CommandSender s) {
        if (!(s instanceof Player)) return true;
        Player p = (Player) s;
        return p.isOp() || p.hasPermission("ultimate.random.admin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (a.length == 0) {
            sender.sendMessage(plugin.color("&e/random create <키>, add <키> <가중치>, del <키>, list <키>, preview <키>, spin <키>, setkey <키>"));
            return true;
        }
        String sub = a[0].toLowerCase();
        if (sub.equals("create")) {
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (a.length < 2) { sender.sendMessage(ChatColor.RED + "키를 입력하세요."); return true; }
            String key = a[1].toLowerCase();
            if (plugin.getManager().exists(key)) {
                sender.sendMessage(plugin.msg("exists").replace("%key%", key));
                return true;
            }
            plugin.getManager().create(key);
            sender.sendMessage(plugin.msg("created").replace("%key%", key));
            return true;
        }
        if (sub.equals("del") || sub.equals("delete")) {
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (a.length < 2) { sender.sendMessage(ChatColor.RED + "키를 입력하세요."); return true; }
            String key = a[1].toLowerCase();
            if (plugin.getManager().delete(key)) {
                sender.sendMessage(plugin.msg("deleted").replace("%key%", key));
            } else {
                sender.sendMessage(plugin.msg("not_found").replace("%key%", key));
            }
            return true;
        }
        if (sub.equals("add")) {
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용할 수 있습니다."); return true; }
            if (a.length < 3) { sender.sendMessage(ChatColor.RED + "/random add <키> <가중치>"); return true; }
            String key = a[1].toLowerCase();
            int weight;
            try { weight = Integer.parseInt(a[2]); } catch (Exception e){ sender.sendMessage("가중치는 정수입니다."); return true; }
            Roulette r = plugin.getManager().get(key);
            if (r == null) { sender.sendMessage(plugin.msg("not_found").replace("%key%", key)); return true; }
            Player p = (Player) sender;
            ItemStack inHand = p.getInventory().getItemInMainHand();
            if (inHand == null || inHand.getType() == Material.AIR) {
                sender.sendMessage(plugin.msg("need_item_in_hand"));
                return true;
            }
            ItemStack copy = inHand.clone();
            copy.setAmount(1);
            r.getEntries().add(new RouletteEntry(copy, Math.max(1, weight)));
            plugin.getManager().save();
            sender.sendMessage(plugin.color("&a추가됨: &f" + pretty(copy) + " &7x 가중치 " + weight));
            return true;
        }
        if (sub.equals("list")) {
            if (a.length < 2) { sender.sendMessage(ChatColor.RED + "키를 입력하세요."); return true; }
            String key = a[1].toLowerCase();
            Roulette r = plugin.getManager().get(key);
            if (r == null) { sender.sendMessage(plugin.msg("not_found").replace("%key%", key)); return true; }
            sender.sendMessage(plugin.color("&6[ " + key + " ] &7구성/가중치"));
            int total = r.getTotalWeight();
            int i = 1;
            for (RouletteEntry e : r.getEntries()) {
                double chance = total > 0 ? (100.0 * e.getWeight() / total) : 0.0;
                sender.sendMessage(plugin.color("&7" + (i++) + ". &f" + pretty(e.getItem()) + " &7- &e" + e.getWeight() + " &7(" + String.format(Locale.KOREA,"%.2f", chance) + "%)"));
            }
            return true;
        }
        if (sub.equals("preview")) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            if (a.length < 2) { sender.sendMessage(ChatColor.RED + "키"); return true; }
            String key = a[1].toLowerCase();
            Roulette r = plugin.getManager().get(key);
            if (r == null) { sender.sendMessage(plugin.msg("not_found").replace("%key%", key)); return true; }
            com.minkang.ultimate.random.gui.PreviewGUI.open(plugin, (Player) sender, r);
            return true;
        }
        if (sub.equals("spin") || sub.equals("draw")) {
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            if (a.length < 2) { sender.sendMessage(ChatColor.RED + "키"); return true; }
            String key = a[1].toLowerCase();
            Roulette r = plugin.getManager().get(key);
            if (r == null) { sender.sendMessage(plugin.msg("not_found").replace("%key%", key)); return true; }
            com.minkang.ultimate.random.gui.SpinnerGUI.start(plugin, (Player) sender, r);
            return true;
        }
        if (sub.equals("setkey")) {
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (!(sender instanceof Player)) { sender.sendMessage("플레이어만 사용"); return true; }
            if (a.length < 2) { sender.sendMessage(ChatColor.RED + "키"); return true; }
            String key = a[1].toLowerCase();
            Roulette r = plugin.getManager().get(key);
            if (r == null) { sender.sendMessage(plugin.msg("not_found").replace("%key%", key)); return true; }
            Player p = (Player) sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR) {
                sender.sendMessage(plugin.msg("need_item_in_hand"));
                return true;
            }
            // Mark trigger item with PDC
            ItemMeta m = hand.getItemMeta();
            if (m != null) {
                m.getPersistentDataContainer().set(plugin.getPdcKey(), org.bukkit.persistence.PersistentDataType.STRING, key);
                hand.setItemMeta(m);
            }
            r.setTriggerItem(hand.clone());
            plugin.getManager().save();
            sender.sendMessage(plugin.msg("set_item_bind").replace("%key%", key));
            return true;
        }
        sender.sendMessage(ChatColor.RED + "알 수 없는 하위명령어");
        return true;
    }

    private String pretty(ItemStack it){
        String name = it.hasItemMeta() && it.getItemMeta().hasDisplayName() ? it.getItemMeta().getDisplayName() : it.getType().name();
        return ChatColor.stripColor(name);
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] a) {
        List<String> keys = plugin.getManager().all().stream().map(Roulette::getKey).collect(Collectors.toList());
        if (a.length == 1) return Arrays.asList("create","add","del","list","preview","spin","setkey");
        if (a.length == 2 && Arrays.asList("add","del","list","preview","spin","setkey").contains(a[0].toLowerCase())) {
            return keys;
        }
        if (a.length == 3 && a[0].equalsIgnoreCase("add")) return Arrays.asList("1","2","5","10","25","50");
        return Collections.emptyList();
    }
}
