package me.athlaeos.valhallakits.menus;

import me.athlaeos.valhallakits.Kit;
import me.athlaeos.valhallakits.KitManager;
import me.athlaeos.valhallakits.Utils;
import me.athlaeos.valhallakits.ValhallaKits;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class KitSelectionMenu extends Menu {
    private final NamespacedKey kitKey = new NamespacedKey(ValhallaKits.getPlugin(), "kit_key");
    private int pageNumber = 1;
    private final ItemStack nextPageButton = Utils.createItemStack(Material.ARROW, Utils.chat("&fNext Page"), null);
    private final ItemStack previousPageButton = Utils.createItemStack(Material.ARROW, Utils.chat("&fPrevious Page"), null);

    public KitSelectionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&8Kit Overview");
    }

    @Override
    public int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        e.setCancelled(true);
        if (Utils.isItemEmptyOrNull(e.getCurrentItem())) return;
        assert e.getCurrentItem().getItemMeta() != null;
        if (e.getCurrentItem().equals(previousPageButton)){
            pageNumber--;
        } else if (e.getCurrentItem().equals(nextPageButton)){
            pageNumber++;
        } else if (e.getCurrentItem().getItemMeta().getPersistentDataContainer().has(kitKey, PersistentDataType.STRING)){
            String kit = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
            Kit chosenKit = KitManager.getRegisteredKits().get(kit);
            if (chosenKit != null){
                new KitEditingMenu(playerMenuUtility, chosenKit).open();
                return;
            } else {
                playerMenuUtility.getOwner().sendMessage(Utils.chat("&cKit no longer exists"));
            }
            e.getWhoClicked().closeInventory();
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

        List<ItemStack> kitButtons = new ArrayList<>();
        for (Kit kit : KitManager.getRegisteredKits().values()){
            ItemStack button = new ItemStack(kit.getIcon());
            ItemMeta buttonMeta = button.getItemMeta();
            if (buttonMeta != null){
                List<String> buttonLore = new ArrayList<>(Utils.separateStringIntoLines(Utils.chat(kit.getDescription()), 40));
                buttonLore.add(Utils.chat("&fCooldown: &e" + (kit.getCooldown() < 0 ? "Single Use" : Utils.toTimeStamp(kit.getCooldown(), 1000))));
                buttonLore.add(Utils.chat("&fRequires permission: &e" + (kit.getPermissionRequired() == null ? "None" : kit.getPermissionRequired())));
                buttonLore.add(Utils.chat("&fCosts: &e$" + String.format("%,.2f", kit.getPrice())));
                if (kit.getCommands().isEmpty()){
                    buttonLore.add(Utils.chat("&fKit executes no commands"));
                } else {
                    buttonLore.add(Utils.chat("&fCommands:"));
                    for (String cmd : kit.getCommands()){
                        buttonLore.add(Utils.chat(String.format("&f- &e%s", (cmd.length() > 30 ? cmd.substring(0, 30) + "..." : cmd))));
                    }
                }
                if (kit.getItems().isEmpty()){
                    buttonLore.add(Utils.chat("&fKit is currently empty"));
                } else {
                    buttonLore.add(Utils.chat("&fContents:"));
                    for (Kit.KitEntry reward : kit.getItems().values()){
                        buttonLore.add(Utils.chat(String.format("&f- &e%s &fx%d", Utils.getItemName(reward.getItem()), reward.getItem().getAmount())));
                    }
                }

                buttonMeta.setDisplayName(Utils.chat("&r" + kit.getDisplayName()));

                buttonMeta.setLore(buttonLore);
                buttonMeta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit.getName());
                button.setItemMeta(buttonMeta);

                kitButtons.add(button);
            }
        }

        kitButtons.sort(Comparator.comparing(ItemStack::getType).thenComparing(item -> ChatColor.stripColor(Utils.getItemName(item))));

        if (kitButtons.size() >= 45){
            Map<Integer, ArrayList<ItemStack>> pages = Utils.paginateItemStackList(45, kitButtons);
            if (pageNumber > pages.size()){
                pageNumber = pages.size();
            } else if (pageNumber < 1){
                pageNumber = 1;
            }
            for (ItemStack button : pages.get(pageNumber - 1)){
                inventory.addItem(button);
            }
            if (pageNumber < pages.size()){
                inventory.setItem(53, nextPageButton);
            }
            if (pageNumber > 1){
                inventory.setItem(45, previousPageButton);
            }
        } else {
            inventory.addItem(kitButtons.toArray(new ItemStack[]{}));
        }
    }
}
