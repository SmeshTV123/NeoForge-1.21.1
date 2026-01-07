package net.smeshtv.projectcube.item;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.smeshtv.projectcube.client.PhoneScreen;
import org.jetbrains.annotations.NotNull;

public class SmartPhoneItem extends Item {
    public SmartPhoneItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new PhoneScreen());
        }

        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}