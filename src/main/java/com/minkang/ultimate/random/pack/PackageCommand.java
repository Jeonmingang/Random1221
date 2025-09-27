
package com.minkang.ultimate.random.pack;

import com.minkang.ultimate.random.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PackageCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public PackageCommand(Main plugin){ this.plugin = plugin; }

    private boolean isAdmin(CommandSender s) {
        if (!(s instanceof Player)) return true;
        Player p = (Player) s;
        return p.isOp() || p.hasPermission("ultimate.random.admin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] a) {
        if (a.length == 0){
            sender.sendMessage(plugin.color("&e/package create <이름>, add <이름>, clear <이름>, give <이름> <플레이어> [갯수], show <이름>, claim <이름>"));
            return true;
        }
        String sub = a[0].toLowerCase();
        if (sub.equals("create")){
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (a.length < 2){ sender.sendMessage(ChatColor.RED + "이름을 입력"); return true; }
            String name = a[1].toLowerCase();
            if (plugin.getPackageManager().exists(name)){ sender.sendMessage(ChatColor.RED + "이미 존재합니다."); return true; }
            plugin.getPackageManager().create(name);
            sender.sendMessage(plugin.color("&a생성됨: &e" + name));
            return true;
        }
        if (sub.equals("add")){
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용"); return true; }
            if (a.length < 2){ sender.sendMessage(ChatColor.RED + "이름을 입력"); return true; }
            String name = a[1].toLowerCase();
            PackageDef def = plugin.getPackageManager().get(name);
            if (def == null){ sender.sendMessage(ChatColor.RED + "없음: " + name); return true; }
            Player p = (Player) sender;
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType() == Material.AIR){ sender.sendMessage(ChatColor.RED + "손에 든 아이템이 없습니다."); return true; }
            ItemStack copy = hand.clone();
            copy.setAmount(Math.max(1, Math.min(copy.getAmount(), 64)));
            def.getItems().add(copy);
            plugin.getPackageManager().save();
            sender.sendMessage(plugin.color("&a추가됨: &f" + pretty(copy)));
            return true;
        }
        if (sub.equals("clear")){
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (a.length < 2){ sender.sendMessage("이름"); return true; }
            String name = a[1].toLowerCase();
            PackageDef def = plugin.getPackageManager().get(name);
            if (def == null){ sender.sendMessage(ChatColor.RED + "없음"); return true; }
            def.getItems().clear();
            plugin.getPackageManager().save();
            sender.sendMessage(plugin.color("&a초기화 완료"));
            return true;
        }
        if (sub.equals("give")){
            if (!isAdmin(sender)) { sender.sendMessage(ChatColor.RED + "권한이 없습니다."); return true; }
            if (a.length < 3){ sender.sendMessage("/package give <이름> <플레이어> [갯수]"); return true; }
            String name = a[1].toLowerCase();
            Player t = Bukkit.getPlayerExact(a[2]);
            if (t == null){ sender.sendMessage(ChatColor.RED + "플레이어 오프라인"); return true; }
            int count = 1;
            if (a.length >= 4){ try{ count = Math.max(1, Integer.parseInt(a[3])); }catch(Exception ignored){} }
            plugin.getPackageManager().addClaims(name, t.getUniqueId(), count);
            sender.sendMessage(plugin.color("&a부여됨: &e" + t.getName() + " &7← " + count + "회"));
            t.sendMessage(plugin.color("&a패키지 수령권이 " + count + "회 지급되었습니다: &e" + name));
            return true;
        }
        if (sub.equals("show")){
            if (!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용"); return true; }
            if (a.length < 2){ sender.sendMessage("이름"); return true; }
            String name = a[1].toLowerCase();
            PackageDef def = plugin.getPackageManager().get(name);
            if (def == null){ sender.sendMessage(ChatColor.RED + "없음"); return true; }
            PackageClaimGUI.open(plugin, (Player) sender, def, false);
            return true;
        }
        if (sub.equals("claim")){
            if (!(sender instanceof Player)){ sender.sendMessage("플레이어만 사용"); return true; }
            if (a.length < 2){ sender.sendMessage("이름"); return true; }
            String name = a[1].toLowerCase();
            PackageDef def = plugin.getPackageManager().get(name);
            if (def == null){ sender.sendMessage(ChatColor.RED + "없음"); return true; }
            PackageClaimGUI.open(plugin, (Player) sender, def, true);
            return true;
        }
        sender.sendMessage(ChatColor.RED + "알 수 없는 하위명령어");
        return true;
    }

    private String pretty(ItemStack it){
        String name = it.hasItemMeta() && it.getItemMeta().hasDisplayName() ? it.getItemMeta().getDisplayName() : it.getType().name();
        return ChatColor.stripColor(name) + " x" + it.getAmount();
    }

    @Override
    public List<String> onTabComplete(CommandSender s, Command cmd, String alias, String[] a) {
        List<String> names = plugin.getPackageManager().all().stream().map(PackageDef::getName).collect(Collectors.toList());
        if (a.length == 1) return Arrays.asList("create","add","clear","give","show","claim");
        if (a.length == 2 && Arrays.asList("add","clear","show","claim").contains(a[0].toLowerCase())) return names;
        if (a.length == 3 && a[0].equalsIgnoreCase("give")) return null;
        return Collections.emptyList();
    }
}
