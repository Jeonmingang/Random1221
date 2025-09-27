package com.minkang.ultimate.random.pack;

import com.minkang.ultimate.random.Main;
import com.minkang.ultimate.random.gui.PreviewGUI;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

public class PackageCommand implements CommandExecutor {
    private final Main plugin;
    public PackageCommand(Main plugin){ this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("Players only."); return true; }
        Player p = (Player)sender;

        if (args.length == 0) {
            help(p, label);
            return true;
        }
        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create": {
                if (args.length < 2) { p.sendMessage(c("&c사용법: /"+label+" create <이름>")); return true; }
                String name = args[1];
                plugin.packages().getOrCreate(name);
                plugin.packages().saveAll();
                p.sendMessage(c("&a패키지 생성: &e"+name));
                return true;
            }
            case "add": {
                if (args.length < 2) { p.sendMessage(c("&c사용법: /"+label+" add <이름>")); return true; }
                String name = args[1];
                ItemStack hand = p.getInventory().getItemInMainHand();
                if (hand==null || hand.getType()== Material.AIR) { p.sendMessage(c("&c손에 아이템을 들어주세요.")); return true; }
                plugin.packages().getOrCreate(name).add(hand.asOne());
                plugin.packages().saveAll();
                p.sendMessage(c("&a추가됨: &e"+name+" &7← "+hand.getType()+" x1"));
                return true;
            }
            case "clear": {
                if (args.length < 2) { p.sendMessage(c("&c사용법: /"+label+" clear <이름>")); return true; }
                String name = args[1];
                Optional<PackageDef> op = plugin.packages().get(name);
                if (op.isEmpty()) { p.sendMessage(c("&c없는 패키지: "+name)); return true; }
                op.get().clear();
                plugin.packages().saveAll();
                p.sendMessage(c("&a초기화됨: &e"+name));
                return true;
            }
            case "show": {
                if (args.length < 2) { p.sendMessage(c("&c사용법: /"+label+" show <이름>")); return true; }
                String name = args[1];
                Optional<PackageDef> op = plugin.packages().get(name);
                if (op.isEmpty()) { p.sendMessage(c("&c없는 패키지: "+name)); return true; }
                // Reuse preview GUI to list items
                PreviewGUI gui = new PreviewGUI(plugin, p, toRouletteLike(op.get()));
                gui.open();
                return true;
            }
            case "give": {
                if (args.length < 3) { p.sendMessage(c("&c사용법: /"+label+" give <이름> <플레이어> [횟수]")); return true; }
                String name = args[1];
                Player target = p.getServer().getPlayer(args[2]);
                if (target == null) { p.sendMessage(c("&c플레이어 오프라인: "+args[2])); return true; }
                int cnt = 1;
                if (args.length >= 4) { try { cnt = Integer.parseInt(args[3]); } catch (Exception ignored) {} }
                plugin.packages().giveTickets(name, target.getUniqueId(), cnt);
                plugin.packages().saveAll();
                p.sendMessage(c("&a지급됨: &e"+name+" &7x"+cnt+" → &e"+target.getName()));
                target.sendMessage(c("&a패키지 수령권 획득: &e"+name+" &7x"+cnt));
                return true;
            }
            case "claim": {
                if (args.length < 2) { p.sendMessage(c("&c사용법: /"+label+" claim <이름>")); return true; }
                String name = args[1];
                Optional<PackageDef> op = plugin.packages().get(name);
                if (op.isEmpty()) { p.sendMessage(c("&c없는 패키지: "+name)); return true; }
                UUID uid = p.getUniqueId();
                if (!plugin.packages().useTicket(name, uid)) {
                    p.sendMessage(c("&c수령권이 없습니다."));
                    return true;
                }
                for (ItemStack it : op.get().getItems()) {
                    p.getInventory().addItem(it.clone());
                }
                plugin.packages().saveAll();
                p.sendMessage(c("&a수령 완료: &e"+name));
                return true;
            }
            default:
                help(p, label);
                return true;
        }
    }

    private String c(String s){ return ChatColor.translateAlternateColorCodes('&', s); }

    private com.minkang.ultimate.random.Roulette toRouletteLike(PackageDef def) {
        // Build a temp roulette for preview listing
        com.minkang.ultimate.random.Roulette r = new com.minkang.ultimate.random.Roulette(def.getName());
        for (ItemStack it : def.getItems()) {
            r.addEntry(new com.minkang.ultimate.random.RouletteEntry(it.clone(), 1));
        }
        return r;
    }

    private void help(Player p, String label) {
        p.sendMessage(c("&6/"+label+" create <이름>&7: 패키지 생성"));
        p.sendMessage(c("&6/"+label+" add <이름>&7: 손 아이템 추가"));
        p.sendMessage(c("&6/"+label+" clear <이름>&7: 패키지 초기화"));
        p.sendMessage(c("&6/"+label+" give <이름> <플레이어> [횟수]&7: 수령권 지급"));
        p.sendMessage(c("&6/"+label+" show <이름>&7: 구성 보기"));
        p.sendMessage(c("&6/"+label+" claim <이름>&7: 수령"));
    }
}
