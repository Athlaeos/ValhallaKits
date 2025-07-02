package me.athlaeos.valhallakits.hooks;

import me.athlaeos.valhallakits.Kit;
import me.athlaeos.valhallakits.KitCooldownManager;
import me.athlaeos.valhallakits.KitManager;
import me.athlaeos.valhallakits.Utils;
import me.athlaeos.valhallakits.config.ConfigManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KitsPlaceholderExpansion extends PlaceholderExpansion {
    private static final String kit_unclaimable = ConfigManager.getInstance().getConfig("config.yml").get().getString("kit_unclaimable", "");
    @Override
    public @NotNull String getAuthor() {
        return "Athlaeos";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "valhallakits";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player.isOnline()){
            if(params.startsWith("kit_cooldown_timestamp1_")){
                String kitName = params.replace("kit_cooldown_timestamp1_", "");
                Kit kit = KitManager.getRegisteredKits().get(kitName);
                if (kit == null) return null;
                long cooldown = KitCooldownManager.getInstance().getKitCooldown((Player) player, kit);
                return cooldown >= 0 ? Utils.toTimeStamp(cooldown, 1000) : kit_unclaimable;
            } else if(params.startsWith("kit_cooldown_timestamp2_")){
                String kitName = params.replace("kit_cooldown_timestamp2_", "");
                Kit kit = KitManager.getRegisteredKits().get(kitName);
                if (kit == null) return null;
                long cooldown = KitCooldownManager.getInstance().getKitCooldown((Player) player, kit);
                return cooldown >= 0 ? Utils.msToTimestamp(cooldown) : kit_unclaimable;
            }
        }
        return null; // Placeholder is unknown by the Expansion
    }


}
