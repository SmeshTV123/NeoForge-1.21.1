package net.smeshtv.projectcube.notifications;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.*;

// УБРАЛИ @EventBusSubscriber - пока нет методов для подписки на события
public class NotificationSystem {

    private static final Map<UUID, List<Notification>> playerNotifications = new HashMap<>();

    public static class Notification {
        public final String title;
        public final String message;
        public final long timestamp;

        public Notification(String title, String message) {
            this.title = title;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static void sendNotification(Player player, String title, String message) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("§6[" + title + "] §f" + message));

            // Сохраняем уведомление для истории
            playerNotifications.computeIfAbsent(player.getUUID(), k -> new ArrayList<>())
                    .add(new Notification(title, message));
        }
    }

    public static void openNotificationScreen(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            List<Notification> notifications = playerNotifications.getOrDefault(
                    player.getUUID(),
                    Collections.emptyList()
            );

            StringBuilder sb = new StringBuilder("§b=== Уведомления ===\n");
            if (notifications.isEmpty()) {
                sb.append("§7Нет уведомлений\n");
            } else {
                for (int i = 0; i < Math.min(10, notifications.size()); i++) {
                    Notification n = notifications.get(i);
                    sb.append("§6").append(n.title)
                            .append("§7: §f").append(n.message)
                            .append("\n");
                }
            }
            sb.append("§b===================");

            serverPlayer.sendSystemMessage(Component.literal(sb.toString()));
        }
    }

    // Временное отключение - включи когда будут нужны события
    /*
    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        playerNotifications.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        long currentTime = System.currentTimeMillis();
        playerNotifications.values().forEach(list ->
            list.removeIf(n -> currentTime - n.timestamp > 3600000)
        );
    }
    */
}