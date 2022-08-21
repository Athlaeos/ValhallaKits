package me.athlaeos.valhallakits;

import me.athlaeos.valhallakits.config.ConfigManager;
import me.athlaeos.valhallammo.crafting.DynamicItemModifierManager;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DuoArgDynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierPriority;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.TripleArgDynamicItemModifier;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;

import java.util.*;

public class KitManager {
    private static KitManager manager;

    private final Map<String, Kit> kits = new HashMap<>();

    public static KitManager getInstance(){
        if (manager == null) manager = new KitManager();
        return manager;
    }

    public void loadKits(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("kits.yml").get();
        ConfigurationSection kitsSection = config.getConfigurationSection("");
        if (kitsSection != null){
            for (String kitName : kitsSection.getKeys(false)){
                Material icon = Material.BOOK;
                try {
                    icon = Material.valueOf(config.getString(kitName + ".icon", "BOOK"));
                } catch (IllegalArgumentException ignored){
                    ValhallaKits.getPlugin().getServer().getLogger().warning("Icon for kit " + kitName + " is invalid, defaulted to BOOK");
                }
                int modelData = config.getInt(kitName + ".model_data", -1);
                long cooldown = config.getLong(kitName + ".cooldown");
                double price = config.getDouble(kitName + ".price");
                Collection<String> commands = new HashSet<>(config.getStringList(kitName + ".commands"));
                String description = config.getString(kitName + ".description", "");
                String displayName = config.getString(kitName + ".display_name", kitName);
                String permissionRequired = config.getString(kitName + ".permission_required");
                if (permissionRequired != null && ValhallaKits.getPlugin().getServer().getPluginManager().getPermission(permissionRequired) == null){
                    ValhallaKits.getPlugin().getServer().getPluginManager().addPermission(new Permission(permissionRequired));
                }
                Map<String, Kit.KitEntry> entries = new HashMap<>();
                ConfigurationSection itemSection = config.getConfigurationSection(kitName + ".contents");
                if (itemSection != null){
                    for (String i : itemSection.getKeys(false)){
                        ItemStack item = config.getItemStack(kitName + ".contents." + i + ".item");
                        if (Utils.isItemEmptyOrNull(item)) continue;
                        if (ValhallaKits.isValhallaHooked()){
                            List<DynamicItemModifier> modifiers = new ArrayList<>();
                            ConfigurationSection modifierSection = config.getConfigurationSection(kitName + ".contents." + i + ".modifiers");
                            if (modifierSection != null){
                                for (String mod : modifierSection.getKeys(false)){
                                    ModifierPriority priority = ModifierPriority.NEUTRAL;
                                    try {
                                        String stringPriority = config.getString(kitName + ".contents." + i + ".modifiers." + mod + ".priority");
                                        if (stringPriority == null) throw new IllegalArgumentException();
                                        priority = ModifierPriority.valueOf(stringPriority);
                                    } catch (IllegalArgumentException ignored){
                                    }
                                    double strength = config.getDouble(kitName + ".contents." + i + ".modifiers." + mod + ".strength");
                                    DynamicItemModifier modifier;
                                    if (Utils.doesPathExist(config, kitName + ".contents." + i + ".modifiers." + mod, "strength2")){
                                        double strength2 = config.getDouble(kitName + ".contents." + i + ".modifiers." + mod + ".strength2");
                                        if (Utils.doesPathExist(config, kitName + ".contents." + i + ".modifiers." + mod, "strength3")){
                                            double strength3 = config.getDouble(kitName + ".contents." + i + ".modifiers." + mod + ".strength3");
                                            modifier = DynamicItemModifierManager.getInstance().createModifier(mod, strength, strength2, strength3, priority);
                                        } else {
                                            modifier = DynamicItemModifierManager.getInstance().createModifier(mod, strength, strength2, priority);
                                        }
                                    } else {
                                        modifier = DynamicItemModifierManager.getInstance().createModifier(mod, strength, priority);
                                    }
                                    if (modifier != null){
                                        modifiers.add(modifier);
                                    }
                                }
                            }
                            entries.put(i, new Kit.ValhallaKitEntry(i, item, modifiers));
                        } else {
                            entries.put(i, new Kit.KitEntry(i, item));
                        }
                    }
                } else continue;
                Kit newKit = new Kit(kitName, cooldown, permissionRequired, price, displayName, description, commands, entries);
                newKit.setIcon(icon);
                newKit.setModelData(modelData);
                kits.put(kitName, newKit);
            }
        }
    }

    public void saveKits(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("kits.yml").get();
        ConfigurationSection section = config.getConfigurationSection("");
        if (section != null){
            for (String s : section.getKeys(false)){
                config.set(s, null);
            }
        }
        for (Kit kit : kits.values()){
            if (kit.getPermissionRequired() != null) config.set(kit.getName() + ".permission_required", kit.getPermissionRequired());
            config.set(kit.getName() + ".cooldown", kit.getCooldown());
            config.set(kit.getName() + ".price", kit.getPrice());
            config.set(kit.getName() + ".icon", kit.getIcon().toString());
            config.set(kit.getName() + ".model_data", kit.getModelData());
            config.set(kit.getName() + ".description", kit.getDescription());
            config.set(kit.getName() + ".display_name", kit.getDisplayName());
            config.set(kit.getName() + ".commands", new ArrayList<>(kit.getCommands()));
            for (Kit.KitEntry entry : kit.getItems().values()){
                config.set(kit.getName() + ".contents." + entry.getId() + ".item", entry.getItem());
                if (ValhallaKits.isValhallaHooked()){
                    if (entry instanceof Kit.ValhallaKitEntry){
                        for (DynamicItemModifier modifier : ((Kit.ValhallaKitEntry) entry).getModifiers()){
                            if (modifier instanceof TripleArgDynamicItemModifier){
                                config.set(kit.getName() + ".contents." + entry.getId() + ".modifiers." + modifier.getName() + ".strength3", Utils.round(((TripleArgDynamicItemModifier) modifier).getStrength3(), 6));
                            }
                            if (modifier instanceof DuoArgDynamicItemModifier){
                                config.set(kit.getName() + ".contents." + entry.getId() + ".modifiers." + modifier.getName() + ".strength2", Utils.round(((DuoArgDynamicItemModifier) modifier).getStrength2(), 6));
                            }
                            config.set(kit.getName() + ".contents." + entry.getId() + ".modifiers." + modifier.getName() + ".strength", Utils.round(modifier.getStrength(), 6));
                            config.set(kit.getName() + ".contents." + entry.getId() + ".modifiers." + modifier.getName() + ".priority", modifier.getPriority().toString());
                        }
                    }
                }
            }
        }
        ConfigManager.getInstance().saveConfig("kits.yml");
    }

    public Map<String, Kit> getKits() {
        return kits;
    }
}
