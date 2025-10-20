package me.athlaeos.valhallakits;

import me.athlaeos.valhallakits.config.ConfigManager;
import me.athlaeos.valhallakits.menus.PlayerKitSelectionMenu;
import me.athlaeos.valhallakits.menus.PlayerMenuUtilManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class KitCommand implements TabExecutor {
    private final String error_kit_not_found;
    private final String status_kit_received;
    private final String error_no_permission;
    private final String warning_kit_cooldown;
    private final String warning_kit_already_unlocked;
    private final String warning_kit_too_expensive;

    public KitCommand(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("config.yml").get();
        error_kit_not_found = config.getString("error_kit_not_found");
        status_kit_received = config.getString("status_kit_received");
        error_no_permission = config.getString("error_no_permission");
        warning_kit_cooldown = config.getString("warning_kit_cooldown");
        warning_kit_already_unlocked = config.getString("warning_kit_already_unlocked");
        warning_kit_too_expensive = config.getString("warning_kit_too_expensive");

        PluginCommand cmd = ValhallaKits.getPlugin().getCommand("valhallakit");
        if (cmd == null) return;
        cmd.setExecutor(this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Utils.chat("&cOnly players may execute this command"));
            return true;
        }
        if (args.length >= 1){
            Kit kit = KitManager.getRegisteredKits().get(args[0]);
            if (kit == null){
                sender.sendMessage(Utils.chat(error_kit_not_found));
                return true;
            }
            if (kit.getPermissionRequired() != null){
                if (!(sender.hasPermission("valhallakits.allkits") || sender.hasPermission(kit.getPermissionRequired()))){
                    sender.sendMessage(Utils.chat(error_no_permission));
                    return true;
                }
            }
            if (kit.getPrice() > 0){
                if (ValhallaKits.isVaultHooked()){
                    Economy e = ValhallaKits.getVaultHook().getEcon();
                    if (e.getBalance((OfflinePlayer) sender) < kit.getPrice()){
                        sender.sendMessage(Utils.chat(warning_kit_too_expensive));
                        return true;
                    } else {
                        EconomyResponse response = e.withdrawPlayer((OfflinePlayer) sender, kit.getPrice());
                        if (!response.transactionSuccess()){
                            sender.sendMessage(Utils.chat("&cSomething went wrong with the transaction"));
                            return true;
                        }
                    }
                }
            }
            if (sender.hasPermission("valhallakits.allkits") || KitCooldownManager.getInstance().isKitCooldownExpired((Player) sender, kit)){
                for (Kit.KitEntry entry : kit.getItems().values()){
                    entry.giveItem((Player) sender);
                }
                for (String cmd : kit.getCommands()){
                    ValhallaKits.getPlugin().getServer().dispatchCommand(ValhallaKits.getPlugin().getServer().getConsoleSender(), cmd.replace("%player%", sender.getName()));
                }
                KitCooldownManager.getInstance().setKitCooldown((Player) sender, kit);
                sender.sendMessage(Utils.chat(status_kit_received));
            } else {
                long cooldown = KitCooldownManager.getInstance().getKitCooldown((Player) sender, kit);
                if (cooldown == -1){
                    sender.sendMessage(Utils.chat(warning_kit_already_unlocked));
                } else {
                    sender.sendMessage(Utils.chat(warning_kit_cooldown.replace("%cooldown%", Utils.toTimeStamp(cooldown, 1000))));
                }
            }
        } else {
            new PlayerKitSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) sender)).open();
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            return new ArrayList<>(KitManager.getRegisteredKits().keySet());
        }
        return null;
    }
}
