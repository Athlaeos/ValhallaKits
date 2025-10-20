package me.athlaeos.valhallakits.menus;

import me.athlaeos.valhallakits.Kit;
import me.athlaeos.valhallakits.Utils;
import me.athlaeos.valhallakits.ValhallaKitEntry;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import me.athlaeos.valhallammo.gui.SetModifiersMenu;
import me.athlaeos.valhallammo.gui.implementations.DynamicModifierMenu;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class ValhallaKitEntryEditingMenu extends Menu implements SetModifiersMenu {
    private final Kit.KitEntry entry;
    private final Kit editingKit;
    private final ItemStack dynamicModifierButton = Utils.createItemStack(Material.BOOK, Utils.chat("&aDynamic Item Modifiers"), Arrays.asList(
            Utils.chat("&fExecutes a number of modifiers on"),
            Utils.chat("&fthe item before rewarding, allowing"),
            Utils.chat("&fthe item to have custom properties"),
            Utils.chat("&fdepending on the player's stats")
    ));
    private final ItemStack saveButton = Utils.createItemStack(Material.STRUCTURE_VOID, Utils.chat("&a&lSave Drop"), null);
    private final ItemStack deleteButton = Utils.createItemStack(Material.BARRIER, Utils.chat("&c&lDelete Drop"), null);
    private ItemStack drop;
    private List<DynamicItemModifier> currentModifiers;

    public ValhallaKitEntryEditingMenu(PlayerMenuUtility playerMenuUtility, Kit editingKit, Kit.KitEntry entry) {
        super(playerMenuUtility);
        // This menu can only occur if ValhallaMMO is hooked and therefore all kit entries are valhalla entries

        this.entry = entry;
        this.editingKit = editingKit;
        this.drop = entry.getItem();
        this.currentModifiers = ((ValhallaKitEntry) entry).getModifiers();
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Edit Drop");
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (e.getClickedInventory() instanceof PlayerInventory){
            e.setCancelled(false);
        }
        ItemStack clickedItem = e.getCurrentItem();
        if (!Utils.isItemEmptyOrNull(clickedItem)){
            if (clickedItem.equals(dynamicModifierButton)){
                playerMenuUtility.setPreviousMenu(this);
                new DynamicModifierMenu(playerMenuUtility, this).open();
                return;
            } else if (clickedItem.equals(deleteButton)){
                editingKit.getItems().remove(entry.getId());
                new KitEditingMenu(PlayerMenuUtilManager.getPlayerMenuUtility(playerMenuUtility.getOwner()), editingKit).open();
                return;
            } else if (clickedItem.equals(saveButton)){
                editingKit.getItems().put(entry.getId(), new ValhallaKitEntry(entry.getId(), drop, currentModifiers));
                new KitEditingMenu(PlayerMenuUtilManager.getPlayerMenuUtility(playerMenuUtility.getOwner()), editingKit).open();
                return;
            } else if (clickedItem.equals(drop)) {
                if (!Utils.isItemEmptyOrNull(e.getCursor())){
                    drop = e.getCursor().clone();
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
        for (int i = 0; i < getSlots(); i++){
            inventory.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
        }
        List<String> modifierButtonLore = new ArrayList<>();
        List<DynamicItemModifier> modifiers = new ArrayList<>(currentModifiers);
        modifiers.sort(Comparator.comparingInt((DynamicItemModifier a) -> a.getPriority().getPriorityRating()));
        for (DynamicItemModifier modifier : modifiers){
            modifierButtonLore.add(Utils.chat("&7- " + modifier.toString()));
        }
        ItemMeta modifierButtonMeta = dynamicModifierButton.getItemMeta();
        assert modifierButtonMeta != null;
        modifierButtonMeta.setLore(modifierButtonLore);
        dynamicModifierButton.setItemMeta(modifierButtonMeta);

        inventory.setItem(22, drop);
        inventory.setItem(24, dynamicModifierButton);
        inventory.setItem(36, deleteButton);
        inventory.setItem(44, saveButton);
    }

    @Override
    public void setResultModifiers(List<DynamicItemModifier> list) {
        this.currentModifiers = list;
    }

    @Override
    public List<DynamicItemModifier> getResultModifiers() {
        return currentModifiers;
    }
}
