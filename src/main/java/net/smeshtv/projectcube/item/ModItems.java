package net.smeshtv.projectcube.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.smeshtv.projectcube.ProjectCube;

public class ModItems {
    // Регистрация предметов
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(ProjectCube.MODID);

    // Смартфон
    public static final DeferredItem<Item> SMARTPHONE =
            ITEMS.register("smartphone", SmartPhoneItem::new);
}