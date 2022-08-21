package me.athlaeos.valhallakits.hooks;

import me.athlaeos.valhallakits.ValhallaKits;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private Economy econ = null;

    public VaultHook(){
        if (!setupEconomy() ) {
            ValhallaKits.getPlugin().getServer().getLogger().severe("Vault hook disabled due to no economy plugin being found");
        }
    }

    private boolean setupEconomy() {
        if (ValhallaKits.getPlugin().getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = ValhallaKits.getPlugin().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }

    public Economy getEcon() {
        return econ;
    }
}
