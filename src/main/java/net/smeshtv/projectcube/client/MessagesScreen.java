package net.smeshtv.projectcube.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class MessagesScreen extends Screen {
    private Player player;

    public MessagesScreen() {
        super(Component.literal("Сообщения"));
        this.player = Minecraft.getInstance().player;
    }

    @Override
    protected void init() {
        super.clearWidgets();

        int centerX = this.width / 2;
        int buttonWidth = 150;
        int buttonHeight = 30;
        int startY = 100;
        int spacing = 40;

        // Кнопки меню
        addRenderableWidget(Button.builder(
                        Component.literal("📝 Новое сообщение"),
                        button -> player.sendSystemMessage(Component.literal("§aФункция в разработке")))
                .pos(centerX - buttonWidth/2, startY)
                .size(buttonWidth, buttonHeight)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("📥 Входящие"),
                        button -> player.sendSystemMessage(Component.literal("§aФункция в разработке")))
                .pos(centerX - buttonWidth/2, startY + spacing)
                .size(buttonWidth, buttonHeight)
                .build());

        addRenderableWidget(Button.builder(
                        Component.literal("💸 Перевод денег"),
                        button -> player.sendSystemMessage(Component.literal("§7Используйте команду: §f/wallet pay <игрок> <сумма>")))
                .pos(centerX - buttonWidth/2, startY + spacing * 2)
                .size(buttonWidth, buttonHeight)
                .build());

        // Кнопка выхода
        addRenderableWidget(Button.builder(
                        Component.literal("← Назад"),
                        button -> Minecraft.getInstance().setScreen(new PhoneScreen()))
                .pos(centerX - 50, this.height - 50)
                .size(100, 25)
                .build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Фон
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);

        // Заголовок
        guiGraphics.drawCenteredString(this.font, "💬 Сообщения", this.width / 2, 50, 0xFFFFFFFF);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}