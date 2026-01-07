package net.smeshtv.projectcube.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.smeshtv.projectcube.wallet.WalletData;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@ParametersAreNonnullByDefault
public class WalletScreen extends Screen {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 512;
    private static final float PHONE_SCALE = 1.0f;

    private static final int SCREEN_X = 28;
    private static final int SCREEN_Y = 80;
    private static final int SCREEN_WIDTH = 200;
    private static final int SCREEN_HEIGHT = 340;

    private static final ResourceLocation PHONE_BG = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/phone_background.png");

    private static final ResourceLocation COIN_ICON = ResourceLocation.fromNamespaceAndPath(
            "projectchaosecube", "textures/gui/coin.png");

    private float fadeIn = 0.0f;
    private float phoneScreenX, phoneScreenY;
    private WalletData walletData;

    private ScreenState currentState = ScreenState.MAIN;
    private String statusMessage = "";
    private long statusTimer = 0;

    private final ButtonRect backButton = new ButtonRect(SCREEN_X + 10, SCREEN_Y + 10, 80, 20, "← Назад");
    private final ButtonRect historyButton = new ButtonRect(SCREEN_X + SCREEN_WIDTH - 90, SCREEN_Y + 10, 80, 20, "История →");
    private final ButtonRect showIdButton = new ButtonRect(0, 0, 140, 30, "Показать ID");
    private final ButtonRect copyIdButton = new ButtonRect(0, 0, 120, 30, "Скопировать ID");

    enum ScreenState {
        MAIN, HISTORY, RECEIVE
    }

    static class ButtonRect {
        int x, y, width, height;
        String text;

        ButtonRect(int x, int y, int width, int height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }

        boolean contains(int checkX, int checkY) {
            return checkX >= x && checkX <= x + width &&
                    checkY >= y && checkY <= y + height;
        }

        void setPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public WalletScreen() {
        super(Component.literal("Кошелёк"));
    }

    public static void open() {
        Minecraft.getInstance().setScreen(new WalletScreen());
    }

    @Override
    protected void init() {
        super.clearWidgets();

        Player player = Minecraft.getInstance().player;
        if (player != null) {
            this.walletData = WalletData.get(player);
        }

        calculatePhonePosition();

        int centerX = SCREEN_X + SCREEN_WIDTH / 2;
        showIdButton.setPosition(centerX - 70, SCREEN_Y + 220);
        copyIdButton.setPosition(centerX - 60, SCREEN_Y + SCREEN_HEIGHT - 60);

        if (walletData == null) {
            statusMessage = "§cОшибка загрузки данных!";
            statusTimer = System.currentTimeMillis() + 3000;
        }
    }

    private void calculatePhonePosition() {
        float phoneWidth = TEXTURE_WIDTH * PHONE_SCALE;
        float phoneHeight = TEXTURE_HEIGHT * PHONE_SCALE;

        phoneScreenX = (this.width - phoneWidth) / 2;
        phoneScreenY = (this.height - phoneHeight) / 2;
    }

    @Override
    public void tick() {
        fadeIn = Mth.clamp(fadeIn + 0.1f, 0.0f, 1.0f);

        if (statusTimer > 0 && System.currentTimeMillis() > statusTimer) {
            statusMessage = "";
            statusTimer = 0;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xAA000000);
        renderPhone(guiGraphics);

        // УБИРАЕМ анимацию для элементов (они сразу появляются)
        renderWalletContent(guiGraphics, mouseX, mouseY);

        if (!statusMessage.isEmpty()) {
            renderStatusMessage(guiGraphics);
        }
    }

    private void renderPhone(GuiGraphics guiGraphics) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(phoneScreenX, phoneScreenY, 0);
        guiGraphics.pose().scale(PHONE_SCALE, PHONE_SCALE, 1.0f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, PHONE_BG);
        RenderSystem.texParameter(3553, 10241, 9728);
        RenderSystem.texParameter(3553, 10240, 9728);

        // Убираем анимацию - всегда полная непрозрачность
        guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.blit(PHONE_BG,
                0, 0,
                0, 0,
                TEXTURE_WIDTH, TEXTURE_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        guiGraphics.pose().popPose();
    }

    private void renderWalletContent(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (walletData == null) {
            // Показываем сообщение об ошибке
            renderCenteredText(guiGraphics, "§cНет данных кошелька",
                    SCREEN_X + SCREEN_WIDTH / 2, SCREEN_Y + 100);
            return;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(phoneScreenX, phoneScreenY, 0);
        guiGraphics.pose().scale(PHONE_SCALE, PHONE_SCALE, 1.0f);

        int virtualMouseX = (int)((mouseX - phoneScreenX) / PHONE_SCALE);
        int virtualMouseY = (int)((mouseY - phoneScreenY) / PHONE_SCALE);

        switch (currentState) {
            case MAIN -> {
                renderMainScreen(guiGraphics, virtualMouseX, virtualMouseY);
                renderButton(guiGraphics, backButton, virtualMouseX, virtualMouseY);
                renderButton(guiGraphics, historyButton, virtualMouseX, virtualMouseY);
                renderButton(guiGraphics, showIdButton, virtualMouseX, virtualMouseY);
            }
            case HISTORY -> {
                renderHistoryScreen(guiGraphics);
                renderButton(guiGraphics, backButton, virtualMouseX, virtualMouseY);
            }
            case RECEIVE -> {
                renderReceiveScreen(guiGraphics);
                renderButton(guiGraphics, backButton, virtualMouseX, virtualMouseY);
                renderButton(guiGraphics, copyIdButton, virtualMouseX, virtualMouseY);
            }
        }

        guiGraphics.pose().popPose();
    }

    private void renderButton(GuiGraphics guiGraphics, ButtonRect button, int mouseX, int mouseY) {
        boolean hovered = button.contains(mouseX, mouseY);
        int bgColor = hovered ? 0xFF4488FF : 0xFF555555;

        guiGraphics.fill(button.x, button.y,
                button.x + button.width, button.y + button.height,
                bgColor);

        guiGraphics.renderOutline(button.x, button.y,
                button.width, button.height,
                0xFF000000);

        int textWidth = font.width(button.text);
        int textX = button.x + (button.width - textWidth) / 2;
        int textY = button.y + (button.height - 8) / 2;

        renderTextWithOutline(guiGraphics, button.text, textX, textY, 0xFFFFFFFF, 0xFF000000);
    }

    private void renderMainScreen(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int centerX = SCREEN_X + SCREEN_WIDTH / 2;
        int y = SCREEN_Y + 50;

        // Иконка монеты
        try {
            RenderSystem.setShaderTexture(0, COIN_ICON);
            int coinSize = 80;
            guiGraphics.blit(COIN_ICON,
                    centerX - coinSize/2, y,
                    0, 0,
                    coinSize, coinSize,
                    coinSize, coinSize);
        } catch (Exception e) {
            guiGraphics.fill(centerX - 40, y, centerX + 40, y + 80, 0xFF00AA00);
            guiGraphics.renderOutline(centerX - 40, y, 80, 80, 0xFF008800);
            renderCenteredTextWithOutline(guiGraphics, "💎", centerX, y + 30, 0xFFFFFFFF, 0xFF000000);
        }

        y += 90;

        // Баланс с обводкой и БОЛЬШИМ ШРИФТОМ
        String balanceText = formatNumber(walletData.getBalance());

        guiGraphics.pose().pushPose();
        float balanceScale = 1.3f;
        int balanceWidth = (int)(font.width(balanceText) * balanceScale);
        int balanceX = centerX - balanceWidth / 2;
        int balanceY = y;

        guiGraphics.pose().translate(balanceX, balanceY, 0);
        guiGraphics.pose().scale(balanceScale, balanceScale, 1);

        renderTextWithOutline(guiGraphics, balanceText, 0, 0, 0xFFFFFFFF, 0xFF000000);

        guiGraphics.pose().popPose();

        y += (int)(25 * balanceScale);

        // "монет" с обводкой
        renderCenteredTextWithOutline(guiGraphics, "монет", centerX, y, 0xFFAAAAAA, 0xFF000000);

        y += 40;

        // ID кошелька
        String idText = "ID: " + walletData.getWalletId();
        renderCenteredTextWithOutline(guiGraphics, idText, centerX, y, 0xFF666666, 0xFF000000);

        // УБРАЛИ историю транзакций - теперь только кнопка "История →"
    }

    private void renderHistoryScreen(GuiGraphics guiGraphics) {
        int y = SCREEN_Y + 50;

        renderCenteredTextWithOutline(guiGraphics, "История транзакций:", SCREEN_X + SCREEN_WIDTH / 2, y, 0xFFAAAAAA, 0xFF000000);

        y += 30;

        List<WalletData.Transaction> transactions = walletData.getTransactions(15);
        if (transactions.isEmpty()) {
            renderCenteredTextWithOutline(guiGraphics, "Нет операций", SCREEN_X + SCREEN_WIDTH / 2, y, 0xFF888888, 0xFF000000);
        } else {
            for (int i = transactions.size() - 1; i >= 0; i--) {
                WalletData.Transaction t = transactions.get(i);
                if (y > SCREEN_Y + SCREEN_HEIGHT - 40) break;

                String time = t.getFormattedTime();
                String amount = (t.amount >= 0 ? "§a+" : "§c") + t.amount;

                renderTextWithOutline(guiGraphics, "• " + time + " " + amount, SCREEN_X + 20, y, 0xFF000000, 0xFF888888);

                String details = "  " + truncate(t.description, 25) + " | Итог: " + formatNumber(t.newBalance);
                renderTextWithOutline(guiGraphics, "§7" + details, SCREEN_X + 30, y + 12, 0xFF000000, 0xFF888888);

                y += 30;
            }
        }
    }

    private void renderReceiveScreen(GuiGraphics guiGraphics) {
        int centerX = SCREEN_X + SCREEN_WIDTH / 2;
        int y = SCREEN_Y + 70;

        renderCenteredTextWithOutline(guiGraphics, "Ваш ID кошелька", centerX, y, 0xFFFFFFFF, 0xFF000000);

        y += 30;

        String walletId = walletData.getWalletId();
        int idWidth = font.width(walletId);

        guiGraphics.fill(centerX - idWidth/2 - 20, y - 10,
                centerX + idWidth/2 + 20, y + 20,
                0xFF222222);
        guiGraphics.renderOutline(centerX - idWidth/2 - 20, y - 10,
                idWidth + 40, 30,
                0xFF666666);

        renderCenteredTextWithOutline(guiGraphics, walletId, centerX, y, 0xFFFFFFFF, 0xFF000000);

        y += 50;

        renderCenteredTextWithOutline(guiGraphics, "Передайте этот код отправителю", centerX, y, 0xFFAAAAAA, 0xFF000000);

        y += 20;

        renderCenteredTextWithOutline(guiGraphics, "Используйте команду: /wallet pay", centerX, y, 0xFF00AA00, 0xFF000000);
    }

    // Вспомогательные методы для текста
    private void renderTextWithOutline(GuiGraphics guiGraphics, String text, int x, int y, int textColor, int outlineColor) {
        String cleanText = text.replaceAll("§.", "");
        guiGraphics.drawString(font, cleanText, x - 1, y, outlineColor, false);
        guiGraphics.drawString(font, cleanText, x + 1, y, outlineColor, false);
        guiGraphics.drawString(font, cleanText, x, y - 1, outlineColor, false);
        guiGraphics.drawString(font, cleanText, x, y + 1, outlineColor, false);

        // Рисуем цветной текст
        String[] parts = text.split("(?=§)");
        int currentX = x;
        for (String part : parts) {
            if (part.startsWith("§")) {
                String content = part.substring(2);
                guiGraphics.drawString(font, content, currentX, y, textColor, false);
                currentX += font.width(content);
            } else {
                guiGraphics.drawString(font, part, currentX, y, textColor, false);
                currentX += font.width(part);
            }
        }
    }

    private void renderCenteredTextWithOutline(GuiGraphics guiGraphics, String text, int centerX, int y, int textColor, int outlineColor) {
        String cleanText = text.replaceAll("§.", "");
        int textWidth = font.width(cleanText);
        int x = centerX - textWidth / 2;
        renderTextWithOutline(guiGraphics, text, x, y, textColor, outlineColor);
    }

    private void renderCenteredText(GuiGraphics guiGraphics, String text, int centerX, int y) {
        String cleanText = text.replaceAll("§.", "");
        int textWidth = font.width(cleanText);
        guiGraphics.drawString(font, text, centerX - textWidth / 2, y, 0xFFFFFFFF, false);
    }

    private void renderStatusMessage(GuiGraphics guiGraphics) {
        if (statusMessage.isEmpty()) return;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(phoneScreenX, phoneScreenY, 0);
        guiGraphics.pose().scale(PHONE_SCALE, PHONE_SCALE, 1.0f);

        int centerX = SCREEN_X + SCREEN_WIDTH / 2;
        int y = SCREEN_Y + SCREEN_HEIGHT - 40;

        String cleanMsg = statusMessage.replaceAll("§.", "");
        int msgWidth = font.width(cleanMsg);

        guiGraphics.fill(centerX - msgWidth/2 - 15, y - 8,
                centerX + msgWidth/2 + 15, y + 18,
                0xCC333333);

        renderCenteredText(guiGraphics, statusMessage, centerX, y);

        guiGraphics.pose().popPose();
    }

    private String formatNumber(long number) {
        return String.format("%,d", number).replace(",", " ");
    }

    private String truncate(String text, int maxLength) {
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int virtualMouseX = (int)((mouseX - phoneScreenX) / PHONE_SCALE);
        int virtualMouseY = (int)((mouseY - phoneScreenY) / PHONE_SCALE);

        if (backButton.contains(virtualMouseX, virtualMouseY)) {
            handleBack();
            return true;
        }

        switch (currentState) {
            case MAIN -> {
                if (historyButton.contains(virtualMouseX, virtualMouseY)) {
                    currentState = ScreenState.HISTORY;
                    return true;
                }
                if (showIdButton.contains(virtualMouseX, virtualMouseY)) {
                    currentState = ScreenState.RECEIVE;
                    return true;
                }
            }
            case RECEIVE -> {
                if (copyIdButton.contains(virtualMouseX, virtualMouseY)) {
                    // Копируем ID в буфер обмена
                    Minecraft.getInstance().keyboardHandler.setClipboard(walletData.getWalletId());
                    showStatus("§aID скопирован!");
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void handleBack() {
        if (currentState == ScreenState.MAIN) {
            Minecraft.getInstance().setScreen(new PhoneScreen());
        } else {
            currentState = ScreenState.MAIN;
        }
    }

    private void showStatus(String message) {
        statusMessage = message;
        statusTimer = System.currentTimeMillis() + 2000;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            handleBack();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}