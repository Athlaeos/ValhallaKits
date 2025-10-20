package me.athlaeos.valhallakits;

import me.athlaeos.valhallakits.config.ConfigManager;
import me.athlaeos.valhallakits.menus.KitEditingMenu;
import me.athlaeos.valhallakits.menus.KitSelectionMenu;
import me.athlaeos.valhallakits.menus.PlayerMenuUtilManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.stream.Collectors;

public class KitsCommand implements TabExecutor {
    private final String error_kit_not_found;
    private final String status_kit_received;
    private final String status_kit_given;
    private final String error_player_not_found;
    private final String error_no_permission;
    private final String status_permission_changed;
    private final String status_permission_removed;
    private final String status_cooldown_changed;
    private final String status_cooldown_removed;
    private final String error_invalid_number;
    private final String error_kit_already_exists;
    private final String status_kit_created;
    private final String status_kit_cooldown_reset;
    private final String status_kit_command_added;
    private final String status_kit_command_removed;
    private final String status_kit_commands_cleared;
    private final String warning_kit_command_doesnt_exist;
    private final String status_kit_price_set;
    private final String status_kit_price_removed;
    private final String warning_vault_not_hooked;
    private final String status_model_data_removed;
    private final String status_model_data_changed;
    private final String status_icon_removed;
    private final String status_icon_changed;
    private final String error_invalid_material;
    private final String status_display_name_removed;
    private final String status_display_name_changed;
    private final String status_description_removed;
    private final String status_description_changed;

    public KitsCommand(){
        YamlConfiguration config = ConfigManager.getInstance().getConfig("config.yml").get();
        error_kit_not_found = config.getString("error_kit_not_found");
        status_kit_received = config.getString("status_kit_received");
        status_kit_given = config.getString("status_kit_given");
        error_player_not_found = config.getString("error_player_not_found");
        error_no_permission = config.getString("error_no_permission");
        status_permission_changed = config.getString("status_permission_changed");
        status_permission_removed = config.getString("status_permission_removed");
        status_cooldown_changed = config.getString("status_cooldown_changed");
        status_cooldown_removed = config.getString("status_cooldown_removed");
        error_invalid_number = config.getString("error_invalid_number");
        error_kit_already_exists = config.getString("error_kit_already_exists");
        status_kit_created = config.getString("status_kit_created");
        status_kit_cooldown_reset = config.getString("status_kit_cooldown_reset");
        status_kit_command_added = config.getString("status_kit_command_added");
        status_kit_command_removed = config.getString("status_kit_command_removed");
        status_kit_commands_cleared = config.getString("status_kit_commands_cleared");
        warning_kit_command_doesnt_exist = config.getString("warning_kit_command_doesnt_exist");
        status_kit_price_set = config.getString("status_kit_price_set");
        status_kit_price_removed = config.getString("status_kit_price_removed");
        warning_vault_not_hooked = config.getString("warning_vault_not_hooked");
        status_model_data_removed = config.getString("status_model_data_removed");
        status_model_data_changed = config.getString("status_model_data_changed");
        status_icon_removed = config.getString("status_icon_removed");
        status_icon_changed = config.getString("status_icon_changed");
        error_invalid_material = config.getString("error_invalid_material");
        status_display_name_removed = config.getString("status_display_name_removed");
        status_display_name_changed = config.getString("status_display_name_changed");
        status_description_removed = config.getString("status_description_removed");
        status_description_changed = config.getString("status_description_changed");

        PluginCommand cmd = ValhallaKits.getPlugin().getCommand("valhallakits");
        if (cmd == null) return;
        cmd.setExecutor(this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("valhallakits.managekits")){
            sender.sendMessage(Utils.chat(error_no_permission));
            return true;
        }
        if (args.length > 1){
            if (args[0].equalsIgnoreCase("resetcooldown")){ // /kits resetcooldown <kit> <player>
                Player target;
                if (args.length == 2){
                    if (sender instanceof Player){
                        target = (Player) sender;
                    } else {
                        sender.sendMessage(Utils.chat("&cYou must be a player to be able to reset your kit cooldowns"));
                        return true;
                    }
                } else {
                    target = ValhallaKits.getPlugin().getServer().getPlayer(args[2]);
                }
                if (target == null){
                    sender.sendMessage(Utils.chat(error_player_not_found));
                    return true;
                }
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                KitCooldownManager.getInstance().setKitCooldown(target, kit, 0);
                sender.sendMessage(Utils.chat(status_kit_cooldown_reset));
                return true;
            } else if (args[0].equalsIgnoreCase("give")){ // /kits give <kit> <player>
                Player target;
                if (args.length == 2){
                    if (sender instanceof Player){
                        target = (Player) sender;
                    } else {
                        sender.sendMessage(Utils.chat("&cYou must be a player to be able to receive a kit"));
                        return true;
                    }
                } else {
                    target = ValhallaKits.getPlugin().getServer().getPlayer(args[2]);
                }
                if (target == null){
                    sender.sendMessage(Utils.chat(error_player_not_found));
                    return true;
                }
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                for (Kit.KitEntry entry : kit.getItems().values()){
                    entry.giveItem(target);
                }
                for (String cmd : kit.getCommands()){
                    ValhallaKits.getPlugin().getServer().dispatchCommand(ValhallaKits.getPlugin().getServer().getConsoleSender(), cmd.replace("%player%", target.getName()));
                }
                target.sendMessage(Utils.chat(status_kit_received));
                if (sender instanceof Player){
                    sender.sendMessage(Utils.chat(status_kit_given));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("setpermission")){ // /kits setpermission <kit> <permission>
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    kit.setPermissionRequired(args[2]);
                    if (ValhallaKits.getPlugin().getServer().getPluginManager().getPermission(args[2]) == null){
                        ValhallaKits.getPlugin().getServer().getPluginManager().addPermission(new Permission(args[2]));
                    }
                    sender.sendMessage(Utils.chat(status_permission_changed.replace("%permission%", args[2])));
                } else {
                    kit.setPermissionRequired(null);
                    sender.sendMessage(Utils.chat(status_permission_removed));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("setcooldown")){ // /kits setcooldown <kit> <cooldown in milliseconds>
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    long cooldown;
                    try {
                        cooldown = Long.parseLong(args[2].replace("w", "").replace("d", "").replace("h", "")
                                .replace("m", "").replace("s", ""));
                    } catch (IllegalArgumentException ignored){
                        sender.sendMessage(Utils.chat(error_invalid_number));
                        return true;
                    }
                    if (args[2].endsWith("w")){
                        cooldown *= (7 * 24 * 60 * 60);
                    } else if (args[2].endsWith("d")){
                        cooldown *= (24 * 60 * 60);
                    } else if (args[2].endsWith("h")){
                        cooldown *= (60 * 60);
                    } else if (args[2].endsWith("m")){
                        cooldown *= (60);
                    }
                    kit.setCooldown(Math.max(0, cooldown * 1000));
                    sender.sendMessage(Utils.chat(status_cooldown_changed.replace("%cooldown%", Utils.msToTimestamp(cooldown * 1000))));
                } else {
                    kit.setCooldown(-1);
                    sender.sendMessage(Utils.chat(status_cooldown_removed));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("setmodeldata")){ // /kits setmodeldata <kit> <model data>
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    try {
                        int modelData = Integer.parseInt(args[2]);
                        kit.setModelData(Math.max(-1, modelData));
                        sender.sendMessage(Utils.chat(status_model_data_changed.replace("%model_data%", "" + modelData)));
                    } catch (IllegalArgumentException ignored){
                        sender.sendMessage(Utils.chat(error_invalid_number));
                    }
                } else {
                    kit.setModelData(-1);
                    sender.sendMessage(Utils.chat(status_model_data_removed));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("seticon")){ // /kits seticon <kit> <icon>
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    try {
                        Material icon = Material.valueOf(args[2].toUpperCase());
                        kit.setIcon(icon);
                        sender.sendMessage(Utils.chat(status_icon_changed.replace("%icon%", icon.toString())));
                    } catch (IllegalArgumentException ignored){
                        sender.sendMessage(Utils.chat(error_invalid_material));
                    }
                } else {
                    kit.setIcon(Material.BOOK);
                    sender.sendMessage(Utils.chat(status_icon_removed));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("setprice")){ // /kits setprice <kit> <cost>
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    try {
                        double price = Double.parseDouble(args[2]);
                        kit.setPrice(Math.max(0, price));
                        sender.sendMessage(Utils.chat(status_kit_price_set.replace("%price%", String.format("%,.2f", price))));
                    } catch (IllegalArgumentException ignored){
                        sender.sendMessage(Utils.chat(error_invalid_number));
                    }
                } else {
                    kit.setPrice(0);
                    sender.sendMessage(Utils.chat(status_kit_price_removed));
                }
                if (!ValhallaKits.isVaultHooked()){
                    sender.sendMessage(Utils.chat(warning_vault_not_hooked));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("create")){ // /kits create <kitname>
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit != null){
                    sender.sendMessage(Utils.chat(error_kit_already_exists));
                    return true;
                }
                Kit newKit = new Kit(args[1], -1, "valhallakits.allkits", 0, args[1], "", new HashSet<>(), new HashMap<>());
                KitManager.getRegisteredKits().put(newKit.getName(), newKit);
                if (sender instanceof Player){
                    new KitEditingMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) sender), newKit).open();
                    sender.sendMessage(Utils.chat(status_kit_created));
                } else {
                    sender.sendMessage(Utils.chat("&cKit is created, but since you're not a player you can't edit it. Use &4/kits edit &cingame to edit your kit"));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("addcommand")){
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    kit.getCommands().add(cmd);
                    sender.sendMessage(Utils.chat(status_kit_command_added.replace("%command%", cmd)));
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("setdisplayname")){
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    String displayName = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    kit.setDisplayName(displayName);
                    sender.sendMessage(Utils.chat(status_display_name_changed.replace("%displayname%", displayName)));
                } else {
                    kit.setDisplayName(kit.getName());
                    sender.sendMessage(Utils.chat(status_display_name_removed.replace("%displayname%", kit.getName())));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("setdescription")){
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    String description = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    kit.setDescription(description);
                    sender.sendMessage(Utils.chat(status_description_changed.replace("%description%", description)));
                } else {
                    kit.setDescription("");
                    sender.sendMessage(Utils.chat(status_description_removed));
                }
                return true;
            } else if (args[0].equalsIgnoreCase("removecommand")){
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                if (args.length > 2){
                    String cmd = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                    boolean removed = false;
                    for (String entry : kit.getCommands()){
                        if (entry.trim().replace(" ", "").equals(cmd.trim().replace(" ", ""))){
                            kit.getCommands().remove(entry);
                            removed = true;
                            sender.sendMessage(Utils.chat(status_kit_command_removed.replace("%command%", entry)));
                            break;
                        }
                    }
                    if (!removed){
                        sender.sendMessage(Utils.chat(warning_kit_command_doesnt_exist));
                    }
                    return true;
                }
            } else if (args[0].equalsIgnoreCase("clearcommands")){
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null){
                    sender.sendMessage(Utils.chat(error_kit_not_found));
                    return true;
                }
                kit.getCommands().clear();
                sender.sendMessage(Utils.chat(status_kit_commands_cleared));
                return true;
            }
        } else {
            if (args.length == 1){
                if (args[0].equalsIgnoreCase("edit")){ // /kits edit
                    if (sender instanceof Player){
                        new KitSelectionMenu(PlayerMenuUtilManager.getPlayerMenuUtility((Player) sender)).open();
                    } else {
                        sender.sendMessage(Utils.chat("&cYou must be a player to edit kits"));
                    }
                    return true;
                }
            }
        }
        sender.sendMessage(Utils.chat("&c/kits <option> <kit> <arg>>"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1){
            return Arrays.asList("create", "edit", "setdisplayname", "setdescription", "seticon", "setmodeldata", "setprice", "setcooldown", "setpermission", "give", "resetcooldown", "addcommand", "removecommand", "clearcommands");
        }
        if (args.length == 2){
            if (Arrays.asList("give", "setprice", "setdisplayname", "setdescription", "seticon", "setmodeldata", "setpermission", "setcooldown", "resetcooldown", "addcommand", "removecommand", "clearcommands").contains(args[0])){
                return new ArrayList<>(KitManager.getRegisteredKits().keySet());
            }
        }
        if (args.length == 3){
            if (args[0].equalsIgnoreCase("setpermission")){
                return Collections.singletonList("valhallakits.");
            } else if (args[0].equalsIgnoreCase("setcooldown")){
                List<String> validSuffixes = Arrays.asList("s", "m", "h", "d", "w");
                if (!(args[2].endsWith("w") || args[2].endsWith("d") || args[2].endsWith("h") || args[2].endsWith("m") || args[2].endsWith("s"))){
                    return validSuffixes.stream().map(s -> args[2] + s).collect(Collectors.toList());
                }
            } else if (args[0].equalsIgnoreCase("removecommand")){
                Kit kit = KitManager.getRegisteredKits().get(args[1]);
                if (kit == null) return Collections.singletonList("invalid_kit");
                return kit.getCommands().stream().map(String::trim).map(s -> s.replace(" ", "")).distinct().collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("seticon")){
                return Arrays.stream(Material.values()).map(Material::toString).map(String::toLowerCase).collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("setmodeldata")){
                return Collections.singletonList("<model_data>");
            }
        }
        return null;
    }
}
