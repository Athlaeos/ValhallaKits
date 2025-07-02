package me.athlaeos.valhallakits;

import me.athlaeos.valhallakits.config.ConfigUpdater;
import me.athlaeos.valhallakits.hooks.KitsPlaceholderExpansion;
import me.athlaeos.valhallakits.hooks.VaultHook;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public final class ValhallaKits extends JavaPlugin {

    private static ValhallaKits plugin;
    private static boolean valhallaHooked = false;
    private static boolean vaultHooked = false;
    private static VaultHook vaultHook;
    @Override
    public void onEnable() {
        plugin = this;
        valhallaHooked = getServer().getPluginManager().isPluginEnabled("ValhallaMMO");
        vaultHooked = getServer().getPluginManager().isPluginEnabled("Vault");
        if (vaultHooked) vaultHook = new VaultHook();
        if (vaultHooked && vaultHook.getEcon() == null) vaultHooked = false;
        // Plugin startup logic
        new KitsCommand();
        new KitCommand();
        saveAndUpdateConfig("config.yml");
        saveConfig("kits.yml");

        getServer().getPluginManager().registerEvents(new JoinListener(), this);

        KitManager.registerKitsFromFile(new File(getDataFolder(), "/kits.json"));
        if(getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new KitsPlaceholderExpansion().register();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        KitManager.saveKits();
    }

    public static VaultHook getVaultHook() {
        return vaultHook;
    }

    public static ValhallaKits getPlugin() {
        return plugin;
    }

    public static boolean isValhallaHooked() {
        return valhallaHooked;
    }

    public static boolean isVaultHooked() {
        return vaultHooked;
    }

    private void saveAndUpdateConfig(String config){
        saveConfig(config);
        updateConfig(config);
    }

    public void saveConfig(String name){
        File config = new File(this.getDataFolder(), name);
        if (!config.exists()){
            this.saveResource(name, false);
        }
    }

    private void updateConfig(String name){
        File configFile = new File(getDataFolder(), name);
        try {
            ConfigUpdater.update(plugin, name, configFile, new ArrayList<>());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
