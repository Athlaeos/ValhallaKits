package me.athlaeos.valhallakits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallakits.hooks.ValhallaHook;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KitManager {
    private static final Gson gson = new GsonBuilder()
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    private static final Map<String, Kit> registeredKits = new HashMap<>();

    public static void registerKitsFromFile(File f){
        Map<String, Kit> kits = new HashMap<>();
        if (f.exists()){
            try (BufferedReader recipesReader = new BufferedReader(new FileReader(f, StandardCharsets.UTF_8))){
                Kit[] collectedKits = (ValhallaKits.isValhallaHooked() ? ValhallaHook.getValhallaGson() : gson).fromJson(recipesReader, Kit[].class);
                for (Kit kit : collectedKits) if (kit != null) kits.put(kit.getName(), kit);
            } catch (IOException | JsonSyntaxException exception){
                ValhallaKits.getPlugin().getServer().getLogger().severe("Could not load recipes file " + f.getPath() + ", " + exception.getMessage());
            } catch (NoClassDefFoundError ignored){}
        } else {
            ValhallaKits.getPlugin().getServer().getLogger().warning("File " + f.getPath() + " does not exist!");
        }

        registeredKits.putAll(kits);
    }

    public static void saveKits(){
        File f = new File(ValhallaKits.getPlugin().getDataFolder(), "/kits.json");
        if (!f.exists()) ValhallaKits.getPlugin().save("kits.json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = (ValhallaKits.isValhallaHooked() ? ValhallaHook.getValhallaGson() : gson).toJsonTree(new ArrayList<>(registeredKits.values()), new TypeToken<ArrayList<Kit>>(){}.getType());
            (ValhallaKits.isValhallaHooked() ? ValhallaHook.getValhallaGson() : gson).toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaKits.getPlugin().getServer().getLogger().severe("Could not save recipes file kits.json, " + exception.getMessage());
        }
    }

    public static Map<String, Kit> getRegisteredKits() {
        return registeredKits;
    }
}
