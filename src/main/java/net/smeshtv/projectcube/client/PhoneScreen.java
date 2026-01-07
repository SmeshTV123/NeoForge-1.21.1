package net.smeshtv.projectcube.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.smeshtv.projectcube.wallet.WalletData;

import java.util.ArrayList;
import java.util.List;

public class PhoneScreen extends Screen {
    // Фон телефона
    private static final ResourceLocation PHONE_BG = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/phone_background.png");

    // Иконки приложений
    private static final ResourceLocation APP_WALLET = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/wallet.png");
    private static final ResourceLocation APP_MESSAGES = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/chat.png");
    private static final ResourceLocation APP_SETTINGS = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/settings.png");
    private static final ResourceLocation APP_CAMERA = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/camera.png");
    private static final ResourceLocation APP_PHOTOS = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/photos.png");
    private static final ResourceLocation APP_STORE = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/store.png");
    private static final ResourceLocation APP_NOTES = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/notes.png");
    private static final ResourceLocation APP_CLOCK = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/clock.png");
    private static final ResourceLocation APP_HAPS = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/haps.png");
    private static final ResourceLocation APP_CONSOLE = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/console.png");
    private static final ResourceLocation APP_HAGPOINT = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/apps/hagpoint.png");

    // Размеры телефона
    private static final int PHONE_WIDTH = 200;
    private static final int PHONE_HEIGHT = 400;

    // Область экрана
    private static final int SCREEN_X = 20;
    private static final int SCREEN_Y = 65;
    private static final int SCREEN_WIDTH = 160;
    private static final int SCREEN_HEIGHT = 295;

    private float phoneScreenX, phoneScreenY;
    private Player player;
    private WalletData walletData;
    private final List<AppButton> appButtons = new ArrayList<>();

    public PhoneScreen() {
        super(Component.literal(""));
        this.player = Minecraft.getInstance().player;
        if (this.player != null) {
            this.walletData = WalletData.get(player);
        }
    }

    @Override
    protected void init() {
        super.clearWidgets();
        calculatePhonePosition();
        appButtons.clear();
        createAppGrid();
    }

    private void calculatePhonePosition() {
        float phoneScale = Config.PHONE_SCALE;
        float phoneWidth = PHONE_WIDTH * phoneScale;
        float phoneHeight = PHONE_HEIGHT * phoneScale;

        phoneScreenX = (this.width - phoneWidth) / 2;
        phoneScreenY = (this.height - phoneHeight) / 2;
    }

    private void createAppGrid() {
        int screenStartX = (int) (phoneScreenX + SCREEN_X * Config.PHONE_SCALE);
        int screenStartY = (int) (phoneScreenY + SCREEN_Y * Config.PHONE_SCALE);

        int appSize = Config.APP_SIZE;
        int spacingX = Config.APP_SPACING_X;
        int spacingY = Config.APP_SPACING_Y;

        int cols = 3;
        int maxRows = 5;

        int gridWidth = cols * appSize + (cols - 1) * spacingX;
        int gridStartX = screenStartX + (int)((SCREEN_WIDTH * Config.PHONE_SCALE - gridWidth) / 2);

        String[] appNames = {
                "Кошелёк", "Сообщения", "Настройки",
                "Камера", "Фото", "Магазин",
                "Заметки", "Часы", "Haps",
                "Console", "Hagpoint"
        };

        ResourceLocation[] appIcons = {
                APP_WALLET, APP_MESSAGES, APP_SETTINGS,
                APP_CAMERA, APP_PHOTOS, APP_STORE,
                APP_NOTES, APP_CLOCK, APP_HAPS,
                APP_CONSOLE, APP_HAGPOINT
        };

        Runnable[] appActions = {
                () -> Minecraft.getInstance().setScreen(new WalletScreen()),
                () -> Minecraft.getInstance().setScreen(new MessagesScreen()),
                () -> sendMessage("§aНастройки в разработке"),
                () -> sendMessage("§aКамера в разработке"),
                () -> sendMessage("§aФото в разработке"),
                () -> sendMessage("§aМагазин в разработке"),
                () -> sendMessage("§aЗаметки в разработке"),
                () -> sendMessage("§aЧасы в разработке"),
                () -> sendMessage("§aHaps в разработке"),
                () -> sendMessage("§aConsole в разработке"),
                () -> sendMessage("§aHagpoint в разработке")
        };

        int appIndex = 0;
        for (int row = 0; row < maxRows; row++) {
            for (int col = 0; col < cols; col++) {
                if (appIndex >= appNames.length) break;

                int x = gridStartX + col * (appSize + spacingX);
                int y = screenStartY + row * (appSize + spacingY + 12);

                appButtons.add(new AppButton(
                        x, y,
                        appSize, appSize,
                        appNames[appIndex],
                        appIcons[appIndex],
                        appActions[appIndex]
                ));

                appIndex++;
            }
            if (appIndex >= appNames.length) break;
        }
    }

    private void sendMessage(String message) {
        if (player != null) {
            player.sendSystemMessage(Component.literal(message));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        renderPhone(guiGraphics);
        renderStatusBar(guiGraphics);
        renderApps(guiGraphics, mouseX, mouseY);

        if (Config.DEBUG_MODE) {
            renderDebugInfo(guiGraphics);
        }
    }

    private void renderPhone(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(phoneScreenX, phoneScreenY, 0);
        guiGraphics.pose().scale(Config.PHONE_SCALE, Config.PHONE_SCALE, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.setShaderTexture(0, PHONE_BG);

        guiGraphics.blit(PHONE_BG,
                0, 0,
                0, 0,
                PHONE_WIDTH, PHONE_HEIGHT,
                PHONE_WIDTH, PHONE_HEIGHT);

        guiGraphics.pose().popPose();
    }

    private void renderStatusBar(GuiGraphics guiGraphics) {
        int statusWidth = (int) (PHONE_WIDTH * Config.PHONE_SCALE);

        // Время - БОЛЬШЕ и НИЖЕ
        String time = getMinecraftTime();
        int timeWidth = font.width(time);
        int timeX = (int)(phoneScreenX + (statusWidth - timeWidth) / 2);
        int timeY = (int)(phoneScreenY + 35); // Было 32 - НИЖЕ

        // Увеличиваем время
        guiGraphics.pose().pushPose();
        float timeScale = 1.1f; // БОЛЬШЕ
        int scaledTimeWidth = (int)(timeWidth * timeScale);
        int scaledTimeX = (int)(phoneScreenX + (statusWidth - scaledTimeWidth) / 2);
        int scaledTimeY = timeY;

        guiGraphics.pose().translate(scaledTimeX, scaledTimeY, 0);
        guiGraphics.pose().scale(timeScale, timeScale, 1);
        renderTextWithOutline(guiGraphics, time, 0, 0, 0xFFFFFFFF, 0xFF000000);
        guiGraphics.pose().popPose();

        // Батарея - ПРАВЕЕ (меньше число = правее)
        String battery = "92%";
        int batteryX = (int)(phoneScreenX + statusWidth - 42); // Было 48 - ПРАВЕЕ
        int batteryY = (int)(phoneScreenY + 12);

        renderTextWithOutline(guiGraphics, "⚡" + battery, batteryX, batteryY, 0xFF00FF00, 0xFF000000);

        // Сеть - ЛЕВЕЕ (меньше число = левее)
        int networkX = (int)(phoneScreenX + 18); // Было 25 - ЛЕВЕЕ
        int networkY = (int)(phoneScreenY + 12);

        renderTextWithOutline(guiGraphics, "📶 4G", networkX, networkY, 0xFFFFFFFF, 0xFF000000);
    }

    private String getMinecraftTime() {
        if (Minecraft.getInstance().level == null) {
            return "12:00";
        }

        long timeOfDay = Minecraft.getInstance().level.getDayTime() % 24000;
        int hours = (int)((timeOfDay / 1000 + 6) % 24);
        int minutes = (int)((timeOfDay % 1000) * 60 / 1000);

        return String.format("%02d:%02d", hours, minutes);
    }

    private void renderTextWithOutline(GuiGraphics guiGraphics, String text, int x, int y, int textColor, int outlineColor) {
        guiGraphics.drawString(font, text, x - 1, y, outlineColor, false);
        guiGraphics.drawString(font, text, x + 1, y, outlineColor, false);
        guiGraphics.drawString(font, text, x, y - 1, outlineColor, false);
        guiGraphics.drawString(font, text, x, y + 1, outlineColor, false);
        guiGraphics.drawString(font, text, x, y, textColor, false);
    }

    private void renderApps(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        for (AppButton app : appButtons) {
            app.render(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderDebugInfo(GuiGraphics guiGraphics) {
        guiGraphics.renderOutline(
                (int)phoneScreenX, (int)phoneScreenY,
                (int)(PHONE_WIDTH * Config.PHONE_SCALE),
                (int)(PHONE_HEIGHT * Config.PHONE_SCALE),
                0xFFFF0000
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (AppButton app : appButtons) {
            if (app.isMouseOver(mouseX, mouseY)) {
                app.onClick();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private String formatNumber(long number) {
        return String.format("%,d", number).replace(",", " ");
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // Класс для кнопок приложений (без квадратиков, только иконки)
    private class AppButton {
        private final int x, y, width, height;
        private final String text;
        private final ResourceLocation icon;
        private final Runnable onClick;

        public AppButton(int x, int y, int width, int height, String text,
                         ResourceLocation icon, Runnable onClick) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
            this.icon = icon;
            this.onClick = onClick;
        }

        public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
            boolean hovered = isMouseOver(mouseX, mouseY);

            // Эффект наведения: легкий белый оверлей
            if (hovered) {
                guiGraphics.fill(x - 2, y - 2, x + width + 2, y + height + 2, 0x30FFFFFF);
            }

            // Иконка
            if (icon != null) {
                try {
                    RenderSystem.setShaderTexture(0, icon);

                    // При наведении иконка немного светлее
                    if (hovered) {
                        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                        int iconSize = width;
                        guiGraphics.blit(icon,
                                x, y,
                                0, 0,
                                iconSize, iconSize,
                                iconSize, iconSize);

                        // Добавляем белый оверлей для эффекта свечения
                        guiGraphics.fill(x, y, x + width, y + height, 0x40FFFFFF);
                    } else {
                        // Обычное состояние
                        guiGraphics.setColor(0.9f, 0.9f, 0.9f, 1.0f);
                        int iconSize = width;
                        guiGraphics.blit(icon,
                                x, y,
                                0, 0,
                                iconSize, iconSize,
                                iconSize, iconSize);
                        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                    }
                } catch (Exception e) {
                    // Fallback: первая буква названия
                    String iconText = text.substring(0, 1);
                    int textWidth = font.width(iconText);
                    int textX = x + (width - textWidth) / 2;
                    int textY = y + (height - 8) / 2;

                    if (hovered) {
                        guiGraphics.fill(x, y, x + width, y + height, 0x40FFFFFF);
                    }

                    guiGraphics.drawString(font, iconText, textX, textY, 0xFFFFFFFF);
                }
            }

            // Название приложения (под иконкой)
            if (!text.isEmpty()) {
                guiGraphics.pose().pushPose();
                float scale = Config.APP_TEXT_SCALE;
                int textWidth = (int)(font.width(text) * scale);
                int textX = x + (width - textWidth) / 2;
                int textY = y + height + 1;

                guiGraphics.pose().translate(textX, textY, 0);
                guiGraphics.pose().scale(scale, scale, 1);

                // Обводка для лучшей видимости
                guiGraphics.drawString(font, text, 1, 0, 0xFF000000);
                guiGraphics.drawString(font, text, -1, 0, 0xFF000000);
                guiGraphics.drawString(font, text, 0, 1, 0xFF000000);
                guiGraphics.drawString(font, text, 0, -1, 0xFF000000);
                guiGraphics.drawString(font, text, 0, 0, 0xFFFFFFFF);

                guiGraphics.pose().popPose();
            }
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            // Немного увеличиваем область клика для удобства
            return mouseX >= x - 3 && mouseX <= x + width + 3 &&
                    mouseY >= y - 3 && mouseY <= y + height + 3;
        }

        public void onClick() {
            try {
                onClick.run();
            } catch (Exception e) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.sendSystemMessage(
                            Component.literal("§cОшибка при запуске приложения"));
                }
            }
        }
    }

    // Конфигурация
    public static class Config {
        public static float PHONE_SCALE = 1.0f;
        public static int APP_SIZE = 48;
        public static float STATUS_TEXT_SCALE = 1.0f;
        public static float APP_TEXT_SCALE = 0.7f;
        public static int APP_SPACING_X = 10;
        public static int APP_SPACING_Y = 10;
        public static boolean DEBUG_MODE = false;

        // Настройки позиций статус-бара
        public static int TIME_Y_POSITION = 32;
        public static int BATTERY_X_OFFSET = 48;
        public static int BATTERY_Y_POSITION = 12;
        public static int NETWORK_X_OFFSET = 25; // Правее
        public static int NETWORK_Y_POSITION = 12;

        public static void resetToDefaults() {
            PHONE_SCALE = 1.0f;
            APP_SIZE = 48;
            STATUS_TEXT_SCALE = 1.0f;
            APP_TEXT_SCALE = 0.7f;
            APP_SPACING_X = 10;
            APP_SPACING_Y = 10;
            DEBUG_MODE = false;
            TIME_Y_POSITION = 32;
            BATTERY_X_OFFSET = 48;
            BATTERY_Y_POSITION = 12;
            NETWORK_X_OFFSET = 25;
            NETWORK_Y_POSITION = 12;
        }
    }
}