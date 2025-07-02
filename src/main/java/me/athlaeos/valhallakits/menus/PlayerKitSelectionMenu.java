package me.athlaeos.valhallakits.menus;

import me.athlaeos.valhallakits.*;
import me.athlaeos.valhallakits.config.ConfigManager;
import me.athlaeos.valhallammo.gui.Menu;
import me.athlaeos.valhallammo.gui.PlayerMenuUtility;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class PlayerKitSelectionMenu extends Menu {
    private final List<String> kit_format = ConfigManager.getInstance().getConfig("config.yml").get().getStringList("kit_format");
    private final String kit_claimable = ConfigManager.getInstance().getConfig("config.yml").get().getString("kit_claimable", "");
    private final String kit_unclaimable = ConfigManager.getInstance().getConfig("config.yml").get().getString("kit_unclaimable", "");
    private final String kit_free = ConfigManager.getInstance().getConfig("config.yml").get().getString("kit_free", "");
    private final String error_kit_not_found = ConfigManager.getInstance().getConfig("config.yml").get().getString("error_kit_not_found", "");
    private final String status_kit_received = ConfigManager.getInstance().getConfig("config.yml").get().getString("status_kit_received", "");
    private final String error_no_permission = ConfigManager.getInstance().getConfig("config.yml").get().getString("error_no_permission", "");
    private final String warning_kit_cooldown = ConfigManager.getInstance().getConfig("config.yml").get().getString("warning_kit_cooldown", "");
    private final String warning_kit_already_unlocked = ConfigManager.getInstance().getConfig("config.yml").get().getString("warning_kit_already_unlocked", "");
    private final String warning_kit_too_expensive = ConfigManager.getInstance().getConfig("config.yml").get().getString("warning_kit_too_expensive", "");
    private final String kit_menu_title  = ConfigManager.getInstance().getConfig("config.yml").get().getString("kit_menu_title", "");

    private final NamespacedKey kitKey = new NamespacedKey(ValhallaKits.getPlugin(), "kit_key");
    private int pageNumber = 1;
    private final ItemStack nextPageButton = Utils.createItemStack(Material.ARROW, Utils.chat("&fNext Page"), null);
    private final ItemStack previousPageButton = Utils.createItemStack(Material.ARROW, Utils.chat("&fPrevious Page"), null);

    public PlayerKitSelectionMenu(PlayerMenuUtility playerMenuUtility) {
        super(playerMenuUtility);
    }

    @Override
    public String getMenuName() {
        return Utils.chat(kit_menu_title);
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
            String kitName = e.getCurrentItem().getItemMeta().getPersistentDataContainer().get(kitKey, PersistentDataType.STRING);
            Player who = playerMenuUtility.getOwner();
            Kit kit = KitManager.getRegisteredKits().get(kitName);
            if (kit != null){
                if (kit.getPermissionRequired() != null){
                    if (!(who.hasPermission("valhallakits.allkits") || who.hasPermission(kit.getPermissionRequired()))){
                        who.sendMessage(Utils.chat(error_no_permission));
                        setMenuItems();
                        return;
                    }
                }
                if (kit.getPrice() > 0){
                    if (ValhallaKits.isVaultHooked()){
                        Economy econ = ValhallaKits.getVaultHook().getEcon();
                        if (econ.getBalance(who) < kit.getPrice()){
                            who.sendMessage(Utils.chat(warning_kit_too_expensive));
                            setMenuItems();
                            return;
                        } else {
                            EconomyResponse response = econ.withdrawPlayer(who, kit.getPrice());
                            if (!response.transactionSuccess()){
                                who.sendMessage(Utils.chat("&cSomething went wrong with the transaction"));
                                setMenuItems();
                                return;
                            }
                        }
                    }
                }
                if (who.hasPermission("valhallakits.allkits") || KitCooldownManager.getInstance().isKitCooldownExpired(who, kit)){
                    for (Kit.KitEntry entry : kit.getItems().values()){
                        entry.giveItem(who);
                    }
                    for (String cmd : kit.getCommands()){
                        ValhallaKits.getPlugin().getServer().dispatchCommand(ValhallaKits.getPlugin().getServer().getConsoleSender(), cmd.replace("%player%", who.getName()));
                    }
                    KitCooldownManager.getInstance().setKitCooldown(who, kit);
                    who.sendMessage(Utils.chat(status_kit_received));
                } else {
                    long cooldown = KitCooldownManager.getInstance().getKitCooldown(who, kit);
                    if (cooldown == -1){
                        who.sendMessage(Utils.chat(warning_kit_already_unlocked));
                    } else {
                        assert warning_kit_cooldown != null;
                        who.sendMessage(Utils.chat(warning_kit_cooldown.replace("%cooldown%", Utils.toTimeStamp(cooldown, 1000))));
                    }
                }
                return;
            } else {
                playerMenuUtility.getOwner().sendMessage(Utils.chat(error_kit_not_found));
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
        Player who = playerMenuUtility.getOwner();

        List<ItemStack> kitButtons = new ArrayList<>();
        for (Kit kit : KitManager.getRegisteredKits().values()){
            if (kit.getPermissionRequired() == null || who.hasPermission(kit.getPermissionRequired()) || who.hasPermission("valhallakits.allkits")){
                ItemStack button = new ItemStack(kit.getIcon());
                ItemMeta buttonMeta = button.getItemMeta();
                if (buttonMeta != null){
                    buttonMeta.setDisplayName(Utils.chat("&r" + kit.getDisplayName()));
                    List<String> buttonLore = new ArrayList<>();
                    for (String formatLine : kit_format){
                        if (formatLine.contains("%description%")){
                            if (!kit.getDescription().isEmpty()){
                                buttonLore.addAll(Utils.separateStringIntoLines(Utils.chat(kit.getDescription()), 40));
                            }
                        } else if (formatLine.contains("%contents%")){
                            if (!kit.getItems().isEmpty()){
                                for (Kit.KitEntry reward : kit.getItems().values()){
                                    String baseLine = formatLine.replace("%contents%", String.format("%s x%d", Utils.getItemName(reward.getItem()), reward.getItem().getAmount()));
                                    buttonLore.add(Utils.chat(baseLine));
                                }
                            }
                        } else {
                            assert kit_unclaimable != null;
                            assert kit_claimable != null;
                            assert kit_free != null;
                            long cooldown = KitCooldownManager.getInstance().getKitCooldown(who, kit);
                            buttonLore.add(Utils.chat(formatLine
                                    .replace("%cooldown%", who.hasPermission("valhallakits.allkits") ? kit_claimable : (cooldown == -1 ? kit_unclaimable : (cooldown == 0 ? kit_claimable : Utils.msToTimestamp(cooldown))))
                                    .replace("%price%", kit.getPrice() <= 0 ? kit_free : String.format("%,.2f", kit.getPrice()))));
                        }
                    }

                    buttonMeta.setLore(buttonLore);
                    buttonMeta.getPersistentDataContainer().set(kitKey, PersistentDataType.STRING, kit.getName());
                    if (kit.getModelData() >= 0){
                        buttonMeta.setCustomModelData(kit.getModelData());
                    }
                    button.setItemMeta(buttonMeta);

                    kitButtons.add(button);
                }
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
