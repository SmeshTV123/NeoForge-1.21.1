package net.smeshtv.projectcube.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.smeshtv.projectcube.client.PhoneScreen;

public class PhoneCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("phone")
                .requires(source -> source.hasPermission(2))

                .then(Commands.literal("scale")
                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0.3f, 3.0f))
                                .executes(context -> {
                                    float scale = FloatArgumentType.getFloat(context, "scale");
                                    PhoneScreen.Config.PHONE_SCALE = scale;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aМасштаб телефона: §e" + scale),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("appsize")
                        .then(Commands.argument("size", IntegerArgumentType.integer(32, 80)) // Увеличен максимальный размер
                                .executes(context -> {
                                    int size = IntegerArgumentType.getInteger(context, "size");
                                    PhoneScreen.Config.APP_SIZE = size;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aРазмер иконок: §e" + size),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("statustext")
                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0.3f, 2.0f))
                                .executes(context -> {
                                    float scale = FloatArgumentType.getFloat(context, "scale");
                                    PhoneScreen.Config.STATUS_TEXT_SCALE = scale;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aМасштаб текста статус-бара: §e" + scale),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("apptext")
                        .then(Commands.argument("scale", FloatArgumentType.floatArg(0.3f, 2.0f))
                                .executes(context -> {
                                    float scale = FloatArgumentType.getFloat(context, "scale");
                                    PhoneScreen.Config.APP_TEXT_SCALE = scale;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aМасштаб текста иконок: §e" + scale),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                // ЗАМЕНИЛИ на новые команды:
                .then(Commands.literal("spacingx")
                        .then(Commands.argument("spacing", IntegerArgumentType.integer(2, 20))
                                .executes(context -> {
                                    int spacing = IntegerArgumentType.getInteger(context, "spacing");
                                    PhoneScreen.Config.APP_SPACING_X = spacing;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aОтступ между иконками X: §e" + spacing),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("spacingy")
                        .then(Commands.argument("spacing", IntegerArgumentType.integer(2, 20))
                                .executes(context -> {
                                    int spacing = IntegerArgumentType.getInteger(context, "spacing");
                                    PhoneScreen.Config.APP_SPACING_Y = spacing;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aОтступ между рядами Y: §e" + spacing),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("debug")
                        .then(Commands.literal("on")
                                .executes(context -> {
                                    PhoneScreen.Config.DEBUG_MODE = true;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§aРежим отладки включен"),
                                            true
                                    );
                                    return 1;
                                })
                        )
                        .then(Commands.literal("off")
                                .executes(context -> {
                                    PhoneScreen.Config.DEBUG_MODE = false;
                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§cРежим отладки выключен"),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                .then(Commands.literal("reset")
                        .executes(context -> {
                            PhoneScreen.Config.resetToDefaults();
                            context.getSource().sendSuccess(
                                    () -> Component.literal("§aНастройки сброшены. Размер иконок: §e48"),
                                    true
                            );
                            return 1;
                        })
                )

                

                .then(Commands.literal("info")
                        .executes(context -> {
                            context.getSource().sendSuccess(
                                    () -> Component.literal("§b=== Настройки телефона ===\n" +
                                            "§aМасштаб телефона: §e" + PhoneScreen.Config.PHONE_SCALE + "\n" +
                                            "§aРазмер иконок: §e" + PhoneScreen.Config.APP_SIZE + "\n" +
                                            "§aМасштаб текста статус-бара: §e" + PhoneScreen.Config.STATUS_TEXT_SCALE + "\n" +
                                            "§aМасштаб текста иконок: §e" + PhoneScreen.Config.APP_TEXT_SCALE + "\n" +
                                            "§aОтступ X: §e" + PhoneScreen.Config.APP_SPACING_X + "\n" +
                                            "§aОтступ Y: §e" + PhoneScreen.Config.APP_SPACING_Y + "\n" +
                                            "§aРежим отладки: §e" + (PhoneScreen.Config.DEBUG_MODE ? "Вкл" : "Выкл")),
                                    false
                            );
                            return 1;
                        })
                )
        );
    }
}