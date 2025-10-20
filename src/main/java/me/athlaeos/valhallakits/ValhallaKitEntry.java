package me.athlaeos.valhallakits;


import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.DynamicItemModifier;
import me.athlaeos.valhallammo.crafting.dynamicitemmodifiers.ModifierContext;
import me.athlaeos.valhallammo.item.ItemBuilder;
import me.athlaeos.valhallammo.utility.ItemUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ValhallaKitEntry extends Kit.KitEntry {
    private List<DynamicItemModifier> modifiers;

    public ValhallaKitEntry(String id, ItemStack i, List<DynamicItemModifier> modifiers) {
        super(id, i);
        this.modifiers = modifiers;
    }

    public List<DynamicItemModifier> getModifiers() { return modifiers; }
    public void setModifiers(List<DynamicItemModifier> modifiers) {
        this.modifiers = modifiers;
        DynamicItemModifier.sortModifiers(this.modifiers);
    }

    @Override
    public void giveItem(Player p){
        ItemBuilder result = new ItemBuilder(getItem());
        DynamicItemModifier.modify(ModifierContext.builder(result)
                .crafter(p)
                .executeUsageMechanics()
                .validate()
                .get(), modifiers);
        ItemStack item = result.get();
        if (ItemUtils.isEmpty(item)) return;
        Utils.giveItem(p, item);
    }
}
