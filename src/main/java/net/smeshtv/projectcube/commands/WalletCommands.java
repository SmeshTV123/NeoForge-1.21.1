package net.smeshtv.projectcube.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.smeshtv.projectcube.wallet.WalletData;

public class WalletCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("wallet")
                .requires(source -> source.hasPermission(0))

                // Баланс (только для себя)
                .then(Commands.literal("balance")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            WalletData data = WalletData.get(player);
                            String walletId = data.getWalletId();

                            // Только отправителю
                            context.getSource().sendSuccess(
                                    () -> Component.literal("§6═══════ Кошелёк ═══════\n" +
                                            "§aБаланс: §e" + formatNumber(data.getBalance()) + "\n" +
                                            "§aID кошелька: §e" + walletId + "\n" +
                                            "§7Используйте: §f/wallet pay <игрок> <сумма>"),
                                    false
                            );
                            return 1;
                        })
                        // Админ может посмотреть чужой баланс
                        .then(Commands.argument("player", EntityArgument.player())
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                    WalletData data = WalletData.get(target);
                                    String walletId = data.getWalletId();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("§6═══ Кошелёк игрока ═══\n" +
                                                    "§aИгрок: §e" + target.getName().getString() + "\n" +
                                                    "§aБаланс: §e" + formatNumber(data.getBalance()) + "\n" +
                                                    "§aID кошелька: §e" + walletId),
                                            true
                                    );
                                    return 1;
                                })
                        )
                )

                // Перевод игроку (приватный)
                .then(Commands.literal("pay")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer sender = context.getSource().getPlayer();
                                            ServerPlayer receiver = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");

                                            if (sender == receiver) {
                                                context.getSource().sendFailure(Component.literal("§cНельзя перевести самому себе"));
                                                return 0;
                                            }

                                            WalletData senderData = WalletData.get(sender);
                                            WalletData receiverData = WalletData.get(receiver);

                                            if (senderData.transferTo(receiverData, amount, sender.getName().getString())) {
                                                // Только отправителю
                                                context.getSource().sendSuccess(
                                                        () -> Component.literal("§a✅ Перевод выполнен!\n" +
                                                                "§7Получатель: §f" + receiver.getName().getString() + "\n" +
                                                                "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                "§7Ваш новый баланс: §f" + formatNumber(senderData.getBalance())),
                                                        false
                                                );

                                                // Только получателю
                                                receiver.sendSystemMessage(
                                                        Component.literal("§a📬 Получен перевод!\n" +
                                                                "§7От: §f" + sender.getName().getString() + "\n" +
                                                                "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                "§7Ваш новый баланс: §f" + formatNumber(receiverData.getBalance()))
                                                );
                                                return 1;
                                            } else {
                                                context.getSource().sendFailure(
                                                        Component.literal("§c❌ Недостаточно средств")
                                                );
                                                return 0;
                                            }
                                        })
                                )
                        )
                )

                // Админ: добавить деньги игроку
                .then(Commands.literal("add")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            String adminName = context.getSource().getTextName();

                                            WalletData data = WalletData.get(target);
                                            if (data.addBalance(amount, "Админ: " + adminName)) {
                                                context.getSource().sendSuccess(
                                                        () -> Component.literal("§a✅ Деньги добавлены!\n" +
                                                                "§7Игрок: §f" + target.getName().getString() + "\n" +
                                                                "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                "§7Новый баланс: §f" + formatNumber(data.getBalance())),
                                                        true
                                                );

                                                target.sendSystemMessage(
                                                        Component.literal("§a💸 Зачисление средств\n" +
                                                                "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                "§7От: §fАдминистрация\n" +
                                                                "§7Ваш баланс: §f" + formatNumber(data.getBalance()))
                                                );
                                                return 1;
                                            }
                                            return 0;
                                        })
                                )
                        )
                )

                // Админ: от чужого имени (fake transaction)
                .then(Commands.literal("fake")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("fromPlayer", EntityArgument.player())
                                .then(Commands.argument("toPlayer", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes(context -> {
                                                    ServerPlayer fromPlayer = EntityArgument.getPlayer(context, "fromPlayer");
                                                    ServerPlayer toPlayer = EntityArgument.getPlayer(context, "toPlayer");
                                                    long amount = LongArgumentType.getLong(context, "amount");
                                                    String adminName = context.getSource().getTextName();

                                                    WalletData fromData = WalletData.get(fromPlayer);
                                                    WalletData toData = WalletData.get(toPlayer);

                                                    if (fromData.transferTo(toData, amount, "Админ от имени: " + fromPlayer.getName().getString())) {
                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal("§a✅ Фейковый перевод выполнен!\n" +
                                                                        "§7От имени: §f" + fromPlayer.getName().getString() + "\n" +
                                                                        "§7Получатель: §f" + toPlayer.getName().getString() + "\n" +
                                                                        "§7Сумма: §f" + formatNumber(amount)),
                                                                true
                                                        );

                                                        // Отправляем сообщения игрокам
                                                        fromPlayer.sendSystemMessage(
                                                                Component.literal("§c📤 Перевод выполнен\n" +
                                                                        "§7Получатель: §f" + toPlayer.getName().getString() + "\n" +
                                                                        "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                        "§7Ваш баланс: §f" + formatNumber(fromData.getBalance()))
                                                        );

                                                        toPlayer.sendSystemMessage(
                                                                Component.literal("§a📬 Получен перевод!\n" +
                                                                        "§7От: §f" + fromPlayer.getName().getString() + "\n" +
                                                                        "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                        "§7Ваш баланс: §f" + formatNumber(toData.getBalance()))
                                                        );
                                                        return 1;
                                                    } else {
                                                        context.getSource().sendFailure(
                                                                Component.literal("§c❌ У отправителя недостаточно средств")
                                                        );
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                        )
                )

                // Админ: создать виртуального игрока и добавить деньги
                .then(Commands.literal("virtual")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("virtualName", StringArgumentType.string())
                                .then(Commands.argument("toPlayer", EntityArgument.player())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                                .executes(context -> {
                                                    String virtualName = StringArgumentType.getString(context, "virtualName");
                                                    ServerPlayer toPlayer = EntityArgument.getPlayer(context, "toPlayer");
                                                    long amount = LongArgumentType.getLong(context, "amount");
                                                    String adminName = context.getSource().getTextName();

                                                    WalletData toData = WalletData.get(toPlayer);

                                                    // Просто добавляем деньги с пометкой "виртуальный"
                                                    if (toData.addBalance(amount, "Виртуальный: " + virtualName)) {
                                                        context.getSource().sendSuccess(
                                                                () -> Component.literal("§a✅ Виртуальный перевод!\n" +
                                                                        "§7От: §f" + virtualName + " §8(виртуальный)\n" +
                                                                        "§7Кому: §f" + toPlayer.getName().getString() + "\n" +
                                                                        "§7Сумма: §f" + formatNumber(amount)),
                                                                true
                                                        );

                                                        toPlayer.sendSystemMessage(
                                                                Component.literal("§a💸 Получены средства\n" +
                                                                        "§7От: §f" + virtualName + "\n" +
                                                                        "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                        "§7Ваш баланс: §f" + formatNumber(toData.getBalance()))
                                                        );
                                                        return 1;
                                                    }
                                                    return 0;
                                                })
                                        )
                                )
                        )
                )

                // Админ: снять деньги
                .then(Commands.literal("remove")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(1))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            String adminName = context.getSource().getTextName();

                                            WalletData data = WalletData.get(target);
                                            if (data.removeBalance(amount, "Снятие админом: " + adminName)) {
                                                context.getSource().sendSuccess(
                                                        () -> Component.literal("§c✅ Деньги сняты!\n" +
                                                                "§7Игрок: §f" + target.getName().getString() + "\n" +
                                                                "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                "§7Новый баланс: §f" + formatNumber(data.getBalance())),
                                                        true
                                                );

                                                target.sendSystemMessage(
                                                        Component.literal("§c📥 Списание средств\n" +
                                                                "§7Сумма: §f" + formatNumber(amount) + "\n" +
                                                                "§7Причина: §fАдминистрация\n" +
                                                                "§7Ваш баланс: §f" + formatNumber(data.getBalance()))
                                                );
                                                return 1;
                                            } else {
                                                context.getSource().sendFailure(
                                                        Component.literal("§c❌ Недостаточно средств у игрока")
                                                );
                                                return 0;
                                            }
                                        })
                                )
                        )
                )

                // Админ: установить точный баланс
                .then(Commands.literal("set")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                        .executes(context -> {
                                            ServerPlayer target = EntityArgument.getPlayer(context, "player");
                                            long amount = LongArgumentType.getLong(context, "amount");
                                            String adminName = context.getSource().getTextName();

                                            WalletData data = WalletData.get(target);
                                            long oldBalance = data.getBalance();
                                            long difference = amount - oldBalance;

                                            if (difference > 0) {
                                                data.addBalance(difference, "Установка баланса админом: " + adminName);
                                            } else if (difference < 0) {
                                                data.removeBalance(Math.abs(difference), "Установка баланса админом: " + adminName);
                                            }

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§a✅ Баланс установлен!\n" +
                                                            "§7Игрок: §f" + target.getName().getString() + "\n" +
                                                            "§7Старый баланс: §f" + formatNumber(oldBalance) + "\n" +
                                                            "§7Новый баланс: §f" + formatNumber(amount)),
                                                    true
                                            );

                                            target.sendSystemMessage(
                                                    Component.literal("§6⚖️ Баланс изменён\n" +
                                                            "§7Новый баланс: §f" + formatNumber(amount) + "\n" +
                                                            "§7Причина: §fАдминистрация")
                                            );
                                            return 1;
                                        })
                                )
                        )
                )

                // Показать ID своего кошелька
                .then(Commands.literal("id")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            WalletData data = WalletData.get(player);
                            String walletId = data.getWalletId();
                            String recoveryCode = data.getRecoveryCode();

                            context.getSource().sendSuccess(
                                    () -> Component.literal("§a📋 Данные вашего кошелька:\n" +
                                            "§7ID кошелька: §f" + walletId + "\n" +
                                            "§7Код восстановления: §f" + recoveryCode + "\n" +
                                            "§7────────────────────\n" +
                                            "§7Для перевода используйте:\n" +
                                            "§f/wallet pay <игрок> <сумма>\n" +
                                            "§7Сохраните код восстановления!"),
                                    false
                            );
                            return 1;
                        })
                )

                // История транзакций
                .then(Commands.literal("history")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayer();
                            if (player == null) return 0;

                            WalletData data = WalletData.get(player);
                            var transactions = data.getTransactions(10);

                            if (transactions.isEmpty()) {
                                context.getSource().sendSuccess(
                                        () -> Component.literal("§7📜 История транзакций пуста"),
                                        false
                                );
                            } else {
                                StringBuilder sb = new StringBuilder();
                                sb.append("§6══════ История ══════\n");

                                for (int i = transactions.size() - 1; i >= 0; i--) {
                                    var t = transactions.get(i);
                                    String time = t.getFormattedTime();
                                    String amount = t.amount >= 0 ? "§a+" + formatNumber(t.amount) : "§c" + formatNumber(t.amount);
                                    String desc = t.description.length() > 20 ? t.description.substring(0, 20) + "..." : t.description;

                                    sb.append("§7").append(time).append(" ")
                                            .append(amount).append(" §8| §7")
                                            .append(desc).append("\n");
                                }

                                sb.append("§6─────────────────────");
                                context.getSource().sendSuccess(() -> Component.literal(sb.toString()), false);
                            }
                            return 1;
                        })
                )

                // Восстановить кошелёк по коду (команда для админа)
                .then(Commands.literal("recover")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("recoveryCode", StringArgumentType.string())
                                        .executes(context -> {
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            String recoveryCode = StringArgumentType.getString(context, "recoveryCode");

                                            // Эта команда пока заглушка - в будущем реализуем восстановление
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("§7Функция восстановления в разработке"),
                                                    true
                                            );
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private static String formatNumber(long number) {
        return String.format("%,d", number).replace(",", " ");
    }
}