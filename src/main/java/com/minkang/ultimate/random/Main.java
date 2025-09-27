package com.minkang.ultimate.random;

import com.minkang.ultimate.random.gui.SpinnerProtector;
import com.minkang.ultimate.random.listener.InteractListener;
import com.minkang.ultimate.random.pack.PackageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static Main instance;
    private RouletteManager rouletteManager;
    private PackageManager packageManager;

    public static Main get() { return instance; }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.rouletteManager = new RouletteManager(this);
        this.packageManager = new PackageManager(this);

        getServer().getPluginManager().registerEvents(new SpinnerProtector(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);

        getCommand("random").setExecutor(new RandomCommand(this));
        getCommand("package").setExecutor(new com.minkang.ultimate.random.pack.PackageCommand(this));
        getLogger().info("UltimateRandomRoulette enabled.");
    }

    @Override
    public void onDisable() {
        rouletteManager.saveAll();
        packageManager.saveAll();
        getLogger().info("UltimateRandomRoulette disabled.");
    }

    public RouletteManager roulettes() { return rouletteManager; }
    public PackageManager packages() { return packageManager; }
}
