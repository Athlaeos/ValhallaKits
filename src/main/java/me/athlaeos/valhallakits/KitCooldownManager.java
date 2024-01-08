package me.athlaeos.valhallakits;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.util.HashMap;
import java.util.Map;

public class KitCooldownManager {
    private static KitCooldownManager manager = null;
    private static final NamespacedKey kitCooldownKey = new NamespacedKey(ValhallaKits.getPlugin(), "kit_cooldowns");

    public static KitCooldownManager getInstance(){
        if (manager == null) manager = new KitCooldownManager();
        return manager;
    }

    public boolean isKitCooldownExpired(Player p, Kit kit){
        long cooldown = getKitCooldown(p, kit);
        if (cooldown == -1) return false;
        if (kit.getCooldown() == 0) return true;
        return cooldown == 0;
    }

    /**
     * Returns the amount of milliseconds until the kit can be unlocked
     * If the player has never unlocked the kit, return 0
     * If the stored cooldown is -1, return -1, which means the kit is meant to be used once and has already been used
     * @param p the player to retrieve their kit cooldown from
     * @param kit the specific kit to check its cooldown. If the kit was not used yet, cooldown is 0
     * @return the remaining cooldown in milliseconds until the kit can be used again, 0 if available, or -1 if permanently unavailable
     */
    public long getKitCooldown(Player p, Kit kit){
        if (kit.getCooldown() == 0) return 0;
        if (p.getPersistentDataContainer().has(kitCooldownKey, PersistentDataType.STRING)){
            String storedValue = p.getPersistentDataContainer().get(kitCooldownKey, PersistentDataType.STRING);
            if (storedValue == null) return 0;
            String[] stringCooldowns = storedValue.split(";");
            for (String cooldown : stringCooldowns){
                String[] args = cooldown.split(":");
                if (args.length >= 2){
                    try {
                        if (kit.getName().equalsIgnoreCase(args[0])){
                            long expiredUntil = Long.parseLong(args[1]);
                            if (expiredUntil == -1) return -1;
                            return Math.max(0, expiredUntil - System.currentTimeMillis());
                        }
                    } catch (IllegalArgumentException ignored){
                    }
                }
            }
        }
        return 0;
    }

    private Map<String, CooldownInstance> getKitCooldowns(Player p){
        Map<String, CooldownInstance> requirements = new HashMap<>();
        if (p.getPersistentDataContainer().has(kitCooldownKey, PersistentDataType.STRING)){
            String storedValue = p.getPersistentDataContainer().get(kitCooldownKey, PersistentDataType.STRING);
            if (storedValue == null) return requirements;
            String[] stringCooldowns = storedValue.split(";");
            for (String cooldown : stringCooldowns){
                String[] args = cooldown.split(":");
                if (args.length >= 2){
                    try {
                        long cd = Long.parseLong(args[1]);
                        Kit kit = KitManager.getRegisteredKits().get(args[0]);
                        if (kit == null) continue;
                        requirements.put(kit.getName(), new CooldownInstance(kit.getName(), cd));
                    } catch (IllegalArgumentException ignored){
                    }
                }
            }
        }
        return requirements;
    }

    private void setKitCooldowns(Player p, Map<String, CooldownInstance> cooldowns){
        if (cooldowns.isEmpty()) {
            p.getPersistentDataContainer().remove(kitCooldownKey);
        } else {
            StringBuilder cooldownsString = new StringBuilder();
            boolean first = true;
            for (CooldownInstance cooldown : cooldowns.values()){
                if (cooldown.getExpiredUntil() >= 0){
                    if (cooldown.getExpiredUntil() <= System.currentTimeMillis()) continue;
                }
                if (first){
                    cooldownsString.append(cooldown.getKitName()).append(":").append(cooldown.getExpiredUntil());
                    first = false;
                } else {
                    cooldownsString.append(";").append(cooldown.getKitName()).append(":").append(cooldown.getExpiredUntil());
                }
            }
            p.getPersistentDataContainer().set(kitCooldownKey, PersistentDataType.STRING, cooldownsString.toString());
        }
    }

    /**
     * Applies a kit's cooldown
     * If the cooldown is -1, it is considered never obtainable again
     * @param p the player to set their cooldown
     * @param kit the kit
     */
    public void setKitCooldown(Player p, Kit kit){
        setKitCooldown(p, kit, kit.getCooldown());
    }

    public void setKitCooldown(Player p, Kit kit, long cooldown){
        CooldownInstance instance = new CooldownInstance(kit.getName(), cooldown == -1 ? -1 : System.currentTimeMillis() + cooldown);
        Map<String, CooldownInstance> existingCooldowns = getKitCooldowns(p);
        existingCooldowns.put(kit.getName(), instance);
        setKitCooldowns(p, existingCooldowns);
    }

    private static class CooldownInstance{
        private final long expiredUntil;
        private final String kitName;

        public CooldownInstance(String kitName, long expiredUntil){
            this.expiredUntil = expiredUntil;
            this.kitName = kitName;
        }

        public long getExpiredUntil() {
            return expiredUntil;
        }

        public String getKitName() {
            return kitName;
        }
    }
}
