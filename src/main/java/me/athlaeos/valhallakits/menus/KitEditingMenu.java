package me.athlaeos.valhallakits.menus;

import me.athlaeos.valhallakits.*;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.gui.PlayerMenuUtilManager;
import me.athlaeos.valhallammo.utility.StringUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.stream.Collectors;

public class KitEditingMenu extends Menu {
    private final NamespacedKey entryKey = new NamespacedKey(ValhallaKits.getPlugin(), "entry_key");

    private final Kit kit;
    private final ItemStack returnToMenuButton = Utils.createItemStack(Material.WRITABLE_BOOK, Utils.chat("&7&lReturn to menu"), null);
    private final ItemStack saveButton = Utils.createItemStack(Material.STRUCTURE_VOID, Utils.chat("&a&lSave Changes"), null);
    private final ItemStack deleteButton = Utils.createItemStack(Material.BARRIER, Utils.chat("&c&lDelete Kit"), null);
    private final ItemStack newEntryButton = Utils.createItemStack(Material.GREEN_STAINED_GLASS_PANE, Utils.chat("&aAdd"), !ValhallaKits.isValhallaHooked() ?
            Arrays.asList(Utils.chat("&7Drag item from your inventory"), Utils.chat("&7here to add it to the kit")) :
            Arrays.asList(Utils.chat("&7Drag item from your inventory"), Utils.chat("&7here to add it to the kit without modifiers"), Utils.chat("&7or click regularly to add modifiers also")));

    public KitEditingMenu(PlayerMenuUtility playerMenuUtility, Kit kit) {
        super(playerMenuUtility);
        this.kit = kit.clone();
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Editing Kit " + kit.getName());
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getClickedInventory() instanceof PlayerInventory){
            e.setCancelled(false);
        }
        ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem != null){
            if (clickedItem.equals(returnToMenuButton)) {
                new KitSelectionMenu(playerMenuUtility).open();
                return;
            } else if (clickedItem.equals(newEntryButton)){
                String name = UUID.randomUUID().toString();
                for (int i = 0; i < 10; i++){
                    if (kit.getItems().containsKey(name)){
                        name = UUID.randomUUID().toString();
                    } else {
                        break;
                    }
                }
                if (ValhallaKits.isValhallaHooked()){
                    if (!Utils.isItemEmptyOrNull(e.getCursor())){
                        kit.getItems().put(name, new ValhallaKitEntry(name, e.getCursor().clone(), new ArrayList<>()));
                    } else {
                        new ValhallaKitEntryEditingMenu(me.athlaeos.valhallammo.gui.PlayerMenuUtilManager.getPlayerMenuUtility(playerMenuUtility.getOwner()), kit, new ValhallaKitEntry(name, Utils.createItemStack(Material.WOODEN_SWORD, Utils.chat("&r&fPlace your own custom kit item here :)"), null), new ArrayList<>())).open();
                    }
                } else {
                    if (!Utils.isItemEmptyOrNull(e.getCursor())){
                        kit.getItems().put(name, new Kit.KitEntry(name, e.getCursor().clone()));
                    }
                }
            } else if (clickedItem.equals(saveButton)){
                KitManager.getRegisteredKits().put(kit.getName(), kit);
                new KitSelectionMenu(playerMenuUtility).open();
                return;
            } else if (clickedItem.equals(deleteButton)){
                KitManager.getRegisteredKits().remove(kit.getName());
                new KitSelectionMenu(playerMenuUtility).open();
                return;
            } else {
                ItemMeta meta = clickedItem.getItemMeta();
                assert meta != null;
                if (meta.getPersistentDataContainer().has(entryKey, PersistentDataType.STRING)){
                    String value = meta.getPersistentDataContainer().get(entryKey, PersistentDataType.STRING);
                    Kit.KitEntry entry = kit.getItems().get(value);
                    if (entry == null){
                        playerMenuUtility.getOwner().sendMessage(Utils.chat("&cItem has been removed"));
                    } else {
                        if (ValhallaKits.isValhallaHooked()){
                            new ValhallaKitEntryEditingMenu(PlayerMenuUtilManager.getPlayerMenuUtility(playerMenuUtility.getOwner()), kit, entry).open();
                        } else {
                            kit.getItems().remove(value);
                        }
                    }
                }
            }
        }
        setMenuItems();
    }

    @Override
    public void handleMenu(InventoryDragEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void setMenuItems() {
        inventory.clear();

        List<ItemStack> entryButtons = new ArrayList<>();
        for (Kit.KitEntry entry : kit.getItems().values().stream().limit(45).collect(Collectors.toSet())){
            ItemStack button = entry.getItem().clone();
            ItemMeta buttonMeta = button.getItemMeta();
            if (buttonMeta != null){
                List<String> buttonLore = buttonMeta.getLore() != null ? buttonMeta.getLore() : new ArrayList<>();
                if (ValhallaKits.isValhallaHooked()){
                    buttonLore.add(Utils.chat("&8&m                                 "));
                    buttonLore.add(Utils.chat("&fModifiers:"));
                    ValhallaKitEntry asValhallaEntry = entry instanceof ValhallaKitEntry ? (ValhallaKitEntry) entry : new ValhallaKitEntry(entry.getId(), entry.getItem(), new ArrayList<>());
                    for (DynamicItemModifier modifier : asValhallaEntry.getModifiers()){
                        buttonLore.addAll(StringUtils.separateStringIntoLines(Utils.chat("&f> " + modifier.getActiveDescription()), 40));
                    }
                }

                buttonMeta.setLore(buttonLore);
                buttonMeta.getPersistentDataContainer().set(entryKey, PersistentDataType.STRING, entry.getId());
                button.setItemMeta(buttonMeta);

                entryButtons.add(button);
            }
        }
        if (entryButtons.size() < 45){
            entryButtons.add(newEntryButton);
        }
        inventory.addItem(entryButtons.toArray(new ItemStack[]{}));
        inventory.setItem(45, deleteButton);
        inventory.setItem(49, returnToMenuButton);
        inventory.setItem(53, saveButton);
    }
}
