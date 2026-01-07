package net.smeshtv.projectcube;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.smeshtv.projectcube.commands.WalletCommands;
import net.smeshtv.projectcube.commands.PhoneCommands;
import net.smeshtv.projectcube.item.ModItems;
import net.smeshtv.projectcube.wallet.WalletData;

@Mod(ProjectCube.MODID)
public class ProjectCube {
    public static final String MODID = "projectchaosecube";

    public ProjectCube(IEventBus modEventBus) {
        // Регистрация предметов
        ModItems.ITEMS.register(modEventBus);

        // Регистрация данных кошелька
        WalletData.ATTACHMENTS.register(modEventBus);

        // События
        modEventBus.addListener(this::addCreative);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.SMARTPHONE.get().getDefaultInstance());
        }
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        WalletCommands.register(event.getDispatcher());
        PhoneCommands.register(event.getDispatcher());
    }
}