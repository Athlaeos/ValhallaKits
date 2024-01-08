package me.athlaeos.valhallakits;

import me.athlaeos.valhallakits.config.ConfigManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Collection;
import java.util.HashSet;

public class JoinListener implements Listener {
    private final Collection<String> joiningKits = new HashSet<>();

    public JoinListener(){
        joiningKits.addAll(ConfigManager.getInstance().getConfig("config.yml").get().getStringList("joining_kits"));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        for (String kitName : joiningKits){
            Kit kit = KitManager.getRegisteredKits().get(kitName);
            if (kit != null){
                if (KitCooldownManager.getInstance().isKitCooldownExpired(e.getPlayer(), kit)){
                    for (Kit.KitEntry entry : kit.getItems().values()){
                        entry.giveItem(e.getPlayer());
                    }
                    for (String cmd : kit.getCommands()){
                        ValhallaKits.getPlugin().getServer().dispatchCommand(ValhallaKits.getPlugin().getServer().getConsoleSender(), cmd.replace("%player%", e.getPlayer().getName()));
                    }
                    KitCooldownManager.getInstance().setKitCooldown(e.getPlayer(), kit);
                }
            }
        }
    }
}
