package me.athlaeos.valhallakits.hooks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.hooks.PluginHook;
import me.athlaeos.valhallammo.persistence.GsonAdapter;
import me.athlaeos.valhallammo.persistence.ItemStackGSONAdapter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class ValhallaHook extends PluginHook {
    public ValhallaHook() {
        super("ValhallaMMO");
    }

    @Override
    public void whenPresent() {

    }

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DynamicItemModifier.class, new GsonAdapter<DynamicItemModifier>("MOD_TYPE"))
            .registerTypeHierarchyAdapter(ConfigurationSerializable.class, new ItemStackGSONAdapter())
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .enableComplexMapKeySerialization()
            .create();

    public static Gson getValhallaGson(){
        return gson;
    }
}
