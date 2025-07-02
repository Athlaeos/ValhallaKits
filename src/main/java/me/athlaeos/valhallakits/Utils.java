package me.athlaeos.valhallakits;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    public static String oldChat(String message) {
        return ChatColor.translateAlternateColorCodes('&', message + "");
    }
    static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String chat(String message) {
        char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuilder buffer = new StringBuilder(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return Utils.oldChat(matcher.appendTail(buffer).toString());
    }

    public static String getItemName(ItemStack i){
        String name;
        assert i.getItemMeta() != null;
        if (i.getItemMeta().hasDisplayName()){
            name = Utils.chat(i.getItemMeta().getDisplayName());
        } else if (i.getItemMeta().hasLocalizedName()){
            name = Utils.chat(i.getItemMeta().getLocalizedName());
        } else {
            name = i.getType().toString().toLowerCase().replace("_", " ");
        }
        return name;
    }

    public static String toPascalCase(String s){
        if (s == null) return null;
        if (s.length() == 0) return s;
        String allLowercase = s.toLowerCase();
        char c = allLowercase.charAt(0);
        return allLowercase.replaceFirst("" + c, "" + Character.toUpperCase(c));
    }

    public static Map<Integer, ArrayList<ItemStack>> paginateItemStackList(int pageSize, List<ItemStack> allEntries) {
        Map<Integer, ArrayList<ItemStack>> pages = new HashMap<>();
        int stepper = 0;

        for (int pageNumber = 0; pageNumber < Math.ceil((double)allEntries.size()/(double)pageSize); pageNumber++) {
            ArrayList<ItemStack> pageEntries = new ArrayList<>();
            for (int pageEntry = 0; pageEntry < pageSize && stepper < allEntries.size(); pageEntry++, stepper++) {
                pageEntries.add(allEntries.get(stepper));
            }
            pages.put(pageNumber, pageEntries);
        }
        return pages;
    }

    public static void giveItem(Player p, ItemStack i){
        Map<Integer, ItemStack> remainder = p.getInventory().addItem(i);
        if (!remainder.isEmpty()){
            for (ItemStack item : remainder.values()){
                Item drop = p.getWorld().dropItem(p.getLocation(), item);
                drop.setPickupDelay(0);
                drop.setOwner(p.getUniqueId());
                drop.setThrower(p.getUniqueId());
            }
        }
    }

    public static List<String> separateStringIntoLines(String string, int maxLength){
        List<String> lines = new ArrayList<>();
        String[] words = string.split(" ");
        if (words.length == 0) return lines;
        StringBuilder sentence = new StringBuilder();
        for (String s : words){
            if (sentence.length() + s.length() > maxLength || s.contains("-n")){
                s = s.replace("-n", "");
                lines.add(sentence.toString());
                String previousSentence = sentence.toString();
                sentence = new StringBuilder();
                sentence.append(me.athlaeos.valhallammo.utility.Utils.chat(org.bukkit.ChatColor.getLastColors(me.athlaeos.valhallammo.utility.Utils.chat(previousSentence)))).append(s);
            } else if (words[0].equals(s)){
                sentence.append(s);
            } else {
                sentence.append(" ").append(s);
            }
        }
        lines.add(sentence.toString());
        return lines;
    }

    public static String msToTimestamp(Long ms){
        Long timeLeft = ms;
        int days = (int)(ms/(1000*60*60*24));
        timeLeft %= 1000*60*60*24;
        int hours = (int)(ms/(1000*60*60));
        timeLeft %= 1000*60*60;
        int minutes = (int)(timeLeft/(1000*60));
        timeLeft %= 1000*60;
        int seconds = (int)(timeLeft/1000);
        timeLeft %= 1000;
        int millis = (int)(timeLeft/100);

        String returnString;
        if (days > 0){
            returnString = days + "d";
        } else if (hours > 0){
            returnString = hours + "h";
        } else if (minutes > 0) {
            returnString = minutes + "m";
        } else {
            returnString = seconds + "." + millis + "s";
        }

        return returnString;
    }

    public static String toTimeStamp(long ticks, long base){
        if (ticks == 0) return "0:00";
        if (ticks < 0) return "∞";
        int hours = (int) Math.floor(ticks / (3600D * base));
        ticks %= (base * 3600);
        int minutes = (int) Math.floor(ticks / (60D * base));
        ticks %= (base * 60);
        int seconds = (int) Math.floor(ticks / (double) base);
        if (hours > 0){
            if (seconds < 10){
                if (minutes < 10){
                    return String.format("%d:0%d:0%d", hours, minutes, seconds);
                } else {
                    return String.format("%d:%d:0%d", hours, minutes, seconds);
                }
            } else {
                if (minutes < 10){
                    return String.format("%d:0%d:%d", hours, minutes, seconds);
                } else {
                    return String.format("%d:%d:%d", hours, minutes, seconds);
                }
            }
        } else {
            if (seconds < 10){
                return String.format("%d:0%d", minutes, seconds);
            } else {
                return String.format("%d:%d", minutes, seconds);
            }
        }
    }

    public static boolean isItemEmptyOrNull(ItemStack i){
        if (i == null) return true;
        return i.getType().isAir();
    }

    public static double round(double value, int places) {
        if (places < 0) places = 2;

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static ItemStack createItemStack(Material material, String displayName, List<String> lore){
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(displayName);
        if (lore != null){
            List<String> coloredLore = new ArrayList<>();
            for (String l : lore){
                coloredLore.add(Utils.chat(l));
            }
            meta.setLore(coloredLore);
        }
        item.setItemMeta(meta);
        return item;
    }

    public static boolean doesPathExist(YamlConfiguration config, String root, String key){
        ConfigurationSection section = config.getConfigurationSection(root);
        if (section != null){
            return section.getKeys(false).contains(key);
        }
        return false;
    }
}
