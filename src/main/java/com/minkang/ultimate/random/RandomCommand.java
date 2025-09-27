package com.minkang.ultimate.random;

import com.minkang.ultimate.random.gui.PreviewGUI;
import com.minkang.ultimate.random.gui.SpinnerGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomCommand implements CommandExecutor {
    private final Main plugin;
    public RandomCommand(Main plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        Player p = (Player) sender;

        if (args.length == 0) {
            help(p, label);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create": {
                if (args.length < 2) { p.sendMessage(color("&c사용법: /"+label+" create <키>")); return true; }
                String key = args[1];
                plugin.roulettes().getOrCreate(key);
                plugin.roulettes().saveAll();
                p.sendMessage(color("&a룰렛 생성: &e"+key));
                return true;
            }
            case "add": {
                if (args.length < 3) { p.sendMessage(color("&c사용법: /"+label+" add <키> <가중치>")); return true; }
                String key = args[1];
                int weight;
                try { weight = Integer.parseInt(args[2]); } catch (NumberFormatException e) { p.sendMessage(color("&c가중치는 정수여야 합니다.")); return true; }
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand==null || hand.getType()== Material.AIR) { p.sendMessage(color("&c손에 아이템을 들어주세요.")); return true; }
                Roulette r = plugin.roulettes().getOrCreate(key);
                ItemStack single = hand.asOne();
                r.addEntry(new RouletteEntry(single, weight));
                plugin.roulettes().saveAll();
                p.sendMessage(color("&a추가됨: &e"+key+" &7← "+single.getType()+" x1, weight="+weight));
                return true;
            }
            case "list": {
                if (args.length < 2) { p.sendMessage(color("&c사용법: /"+label+" list <키>")); return true; }
                String key = args[1];
                Optional<Roulette> or = plugin.roulettes().get(key);
                if (or.isEmpty()) { p.sendMessage(color("&c없는 키: "+key)); return true; }
                Roulette r = or.get();
                p.sendMessage(color("&6[룰렛 "+key+"]"));
                int i=1;
                int total = r.getEntries().stream().mapToInt(RouletteEntry::getWeight).sum();
                for (RouletteEntry e : r.getEntries()) {
                    double chance = total>0 ? (100.0*e.getWeight()/total) : 0.0;
                    p.sendMessage(color("&7"+(i++)+". &f"+e.getItem().getType()+" &ex1 &7| weight=&e"+e.getWeight()+" &7| p=&e"+String.format("%.2f",chance)+"%"));
                }
                return true;
            }
            case "preview": {
                if (args.length < 2) { p.sendMessage(color("&c사용법: /"+label+" preview <키>")); return true; }
                String key = args[1];
                Optional<Roulette> or = plugin.roulettes().get(key);
                if (or.isEmpty()) { p.sendMessage(color("&c없는 키: "+key)); return true; }
                new PreviewGUI(plugin, p, or.get()).open();
                return true;
            }
            case "spin": {
                if (args.length < 2) { p.sendMessage(color("&c사용법: /"+label+" spin <키>")); return true; }
                String key = args[1];
                Optional<Roulette> or = plugin.roulettes().get(key);
                if (or.isEmpty()) { p.sendMessage(color("&c없는 키: "+key)); return true; }
                new SpinnerGUI(plugin, p, or.get()).open();
                return true;
            }
            case "setkey": {
                if (args.length < 2) { p.sendMessage(color("&c사용법: /"+label+" setkey <키>")); return true; }
                String key = args[1];
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand == null || hand.getType()==Material.AIR) { p.sendMessage(color("&c손에 아이템을 들어주세요.")); return true; }
                Roulette r = plugin.roulettes().getOrCreate(key);
                r.setTriggerKeyItem(hand.asOne());
                plugin.roulettes().saveAll();
                p.sendMessage(color("&a키 아이템 설정됨: &e"+key+" &7← "+hand.getType()));
                return true;
            }
            default:
                help(p, label);
                return true;
        }
    }

    private void help(Player p, String label) {
        p.sendMessage(color("&6/"+label+" create <키>&7: 룰렛 생성"));
        p.sendMessage(color("&6/"+label+" add <키> <가중치>&7: 손 아이템 추가"));
        p.sendMessage(color("&6/"+label+" list <키>&7: 구성 보기"));
        p.sendMessage(color("&6/"+label+" preview <키>&7: 미리보기 GUI"));
        p.sendMessage(color("&6/"+label+" spin <키>&7: 스피너 GUI"));
        p.sendMessage(color("&6/"+label+" setkey <키>&7: 손 아이템을 키 아이템으로 설정"));
    }

    private static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s); }
}
