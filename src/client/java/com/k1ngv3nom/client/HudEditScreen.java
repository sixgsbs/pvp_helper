package com.k1ngv3nom.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudEditScreen extends Screen {
	private boolean dragging = false;
	private boolean wasPressed = false;
	private int offsetX;
	private int offsetY;

	public HudEditScreen() {
		super(Component.literal("Paper Helper"));
	}

	@Override
	protected void init() {
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(width / 2 - 105, height - 40, 100, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
			HudConfig.x = 10;
			HudConfig.y = 10;
		}).bounds(width / 2 + 5, height - 40, 100, 20).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		int w = Pvp_helperClient.BOX_W;
		int h = Pvp_helperClient.BOX_H;
		boolean pressed = Minecraft.getInstance().mouseHandler.isLeftPressed();

		boolean inBox = mouseX >= HudConfig.x && mouseX <= HudConfig.x + w
			&& mouseY >= HudConfig.y && mouseY <= HudConfig.y + h;

		if (pressed && !wasPressed && inBox) {
			dragging = true;
			offsetX = mouseX - HudConfig.x;
			offsetY = mouseY - HudConfig.y;
		}
		if (!pressed) dragging = false;

		if (dragging) {
			HudConfig.x = Math.max(0, Math.min(width - w, mouseX - offsetX));
			HudConfig.y = Math.max(0, Math.min(height - h, mouseY - offsetY));
		}
		wasPressed = pressed;

		graphics.fill(HudConfig.x - 2, HudConfig.y - 2, HudConfig.x + w, HudConfig.y + h, 0x80000000);
		Pvp_helperClient.drawArmor(graphics, HudConfig.x, HudConfig.y, true);
		graphics.drawCenteredString(font, "Drag the armor to move it", width / 2, 20, 0xFFFFFFFF);
	}

	@Override
	public void onClose() {
		HudConfig.save();
		super.onClose();
	}
}
