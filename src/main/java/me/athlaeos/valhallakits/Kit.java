package me.athlaeos.valhallakits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Map;

public class Kit implements Cloneable{
    private final String name;
    private Material icon = Material.BOOK;
    private int modelData = -1;
    private long cooldown; // if less than 0, kit can only be obtained once. if 0, kit can be used indefinitely. if more than 0, kit can be used every x milliseconds
    private String permissionRequired; // if null, no permission is required. if not, the given permission is required to access this kit
    private final Map<String, KitEntry> items;
    private final Collection<String> commands;
    private String displayName;
    private String description;
    private double price;

    public Kit(String name, long cooldown, String permissionRequired, double price, String displayName, String description, Collection<String> commands, Map<String, KitEntry> items){
        this.name = name;
        this.cooldown = cooldown;
        this.permissionRequired = permissionRequired;
        this.items = items;
        this.price = price;
        this.commands = commands;
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public void setModelData(int modelData) {
        this.modelData = modelData;
    }

    public int getModelData() {
        return modelData;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public Collection<String> getCommands() {
        return commands;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getCooldown() {
        return cooldown;
    }

    public String getPermissionRequired() {
        return permissionRequired;
    }

    public void setPermissionRequired(String permissionRequired) {
        this.permissionRequired = permissionRequired;
    }

    public void setCooldown(long cooldown) {
        this.cooldown = cooldown;
    }

    public Map<String, KitEntry> getItems() {
        return items;
    }

    @Override
    public Kit clone() {
        final Kit clone;
        try {
            clone = (Kit) super.clone();
        }
        catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Exception occurred calling ItemCraftingRecipe.clone()", ex);
        }
        return clone;
    }


    public static class KitEntry{
        private ItemStack item;
        private final String id;

        public KitEntry(String id, ItemStack i){
            this.id = id;
            this.item = i;
        }

        public String getId() {
            return id;
        }

        public ItemStack getItem() {
            return item;
        }

        public void setItem(ItemStack item) {
            this.item = item;
        }

        public void giveItem(Player p){
            Utils.giveItem(p, item.clone());
        }
    }
}
