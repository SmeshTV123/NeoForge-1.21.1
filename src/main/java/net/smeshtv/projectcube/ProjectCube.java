package net.smeshtv.projectcube;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ProjectCube.MODID)
class ProjectCube {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "projectchaosecube";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ProjectCube(IEventBus modEventBus) {
        // Register the commonSetup method for mod-loading
        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class to respond directly to events.
        NeoForge.EVENT_BUS.register(this);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Если добавишь Config класс с ModConfigSpec — раскомментируй:
        // modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Здесь код для общей инициализации (common setup)
    }

    // Add items/blocks to creative tabs
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Пример: if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) { event.accept(YOUR_ITEM); }
    }

}