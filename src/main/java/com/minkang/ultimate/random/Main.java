
package com.minkang.ultimate.random;

import com.minkang.ultimate.random.gui.PreviewGUI;
import com.minkang.ultimate.random.gui.SpinnerGUI;
import com.minkang.ultimate.random.gui.SpinnerProtector;
import com.minkang.ultimate.random.listener.InteractListener;
import com.minkang.ultimate.random.pack.PackageClaimGUI;
import com.minkang.ultimate.random.pack.PackageManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private RouletteManager manager;
    private PackageManager packageManager;

    private NamespacedKey pdcKey;     // roulette key on trigger item
    private NamespacedKey pkgPdcKey;  // package name on package key

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.manager = new RouletteManager(this);
        this.packageManager = new PackageManager(this);
        this.pdcKey = new NamespacedKey(this, "urr_roulette_key");
        this.pkgPdcKey = new NamespacedKey(this, "urr_package_name");

        // Commands
        PluginCommand random = getCommand("random");
        if (random != null) {
            RandomCommand rc = new RandomCommand(this);
            random.setExecutor(rc);
            random.setTabCompleter(rc);
        }
        PluginCommand pack = getCommand("package");
        if (pack != null) {
            com.minkang.ultimate.random.pack.PackageCommand pc =
                    new com.minkang.ultimate.random.pack.PackageCommand(this);
            pack.setExecutor(pc);
            pack.setTabCompleter(pc);
        }

        // Listeners (GUIs + triggers)
        Bukkit.getPluginManager().registerEvents(new PreviewGUI(this), this);
        Bukkit.getPluginManager().registerEvents(new SpinnerProtector(this), this);
        Bukkit.getPluginManager().registerEvents(new InteractListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PackageClaimGUI(this), this);
        getLogger().info("UltimateRandomRoulette v" + getDescription().getVersion() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.save();
        if (packageManager != null) packageManager.save();
    }

    public RouletteManager getManager() {
        return manager;
    }

    public PackageManager getPackageManager() {
        return packageManager;
    }

    public NamespacedKey getPdcKey() {
        return pdcKey;
    }

    public NamespacedKey getPkgPdcKey() {
        return pkgPdcKey;
    }

    public String msg(String path) {
        String prefix = getConfig().getString("messages.prefix", "");
        String s = getConfig().getString("messages." + path, path);
        return ChatColor.translateAlternateColorCodes('&', prefix + s);
    }

    public String color(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
