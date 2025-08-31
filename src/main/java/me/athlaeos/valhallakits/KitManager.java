package me.athlaeos.valhallakits;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import me.athlaeos.valhallammo.ValhallaMMO;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class KitManager {
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
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
                Kit[] collectedKits = gson.fromJson(recipesReader, Kit[].class);
                for (Kit kit : collectedKits) if (kit != null) kits.put(kit.getName(), kit);
            } catch (IOException | JsonSyntaxException exception){
                ValhallaMMO.logSevere("Could not load recipes file " + f.getPath() + ", " + exception.getMessage());
            } catch (NoClassDefFoundError ignored){}
        } else {
            ValhallaMMO.logWarning("File " + f.getPath() + " does not exist!");
        }

        registeredKits.putAll(kits);
    }

    public static void saveKits(){
        File f = new File(ValhallaKits.getPlugin().getDataFolder(), "/kits.json");
        if (!f.exists()) ValhallaKits.getPlugin().save("kits.json");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(f, StandardCharsets.UTF_8))){
            JsonElement element = gson.toJsonTree(new ArrayList<>(registeredKits.values()), new TypeToken<ArrayList<Kit>>(){}.getType());
            gson.toJson(element, writer);
            writer.flush();
        } catch (IOException | JsonSyntaxException exception){
            ValhallaMMO.logSevere("Could not save recipes file kits.json, " + exception.getMessage());
        }
    }

    public static Map<String, Kit> getRegisteredKits() {
        return registeredKits;
    }
}
