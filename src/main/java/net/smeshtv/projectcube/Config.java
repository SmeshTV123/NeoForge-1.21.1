package net.smeshtv.projectcube;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // Существующие настройки
    public static final ModConfigSpec.BooleanValue LOG_DIRT_BLOCK = BUILDER
            .comment("Whether to log the dirt block on common setup")
            .define("logDirtBlock", true);

    public static final ModConfigSpec.IntValue MAGIC_NUMBER = BUILDER
            .comment("A magic number")
            .defineInRange("magicNumber", 42, 0, Integer.MAX_VALUE);

    // НАСТРОЙКИ ТЕЛЕФОНА
    public static final ModConfigSpec.DoubleValue PHONE_SCALE = BUILDER
            .comment("Масштаб телефона (0.3-3.0)")
            .defineInRange("phone.scale", 1.0, 0.3, 3.0);

    public static final ModConfigSpec.IntValue APP_SIZE = BUILDER
            .comment("Размер иконок приложений (32-96)")
            .defineInRange("phone.appSize", 64, 32, 96);

    public static final ModConfigSpec.DoubleValue STATUS_TEXT_SCALE = BUILDER
            .comment("Масштаб текста статус-бара")
            .defineInRange("phone.statusTextScale", 1.0, 0.3, 2.0);

    public static final ModConfigSpec.IntValue STATUS_BAR_Y = BUILDER
            .comment("Позиция статус-бара по Y")
            .defineInRange("phone.statusBarY", 20, 0, 50);

    static final ModConfigSpec SPEC = BUILDER.build();
}