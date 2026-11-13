package com.k1ngv3nom.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudEditScreen extends Screen {
	private static final int STEP = 4;
	private boolean wasPressed = false;
	private boolean moving = false;

	public HudEditScreen() {
		super(Component.literal("Paper Helper"));
	}

	@Override
	protected void init() {
		int rowY = height - 66;
		int startX = width / 2 - 70;
		addRenderableWidget(Button.builder(Component.literal("<"), b -> nudge(-STEP, 0))
			.bounds(startX, rowY, 30, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">"), b -> nudge(STEP, 0))
			.bounds(startX + 36, rowY, 30, 20).build());
		addRenderableWidget(Button.builder(Component.literal("^"), b -> nudge(0, -STEP))
			.bounds(startX + 72, rowY, 30, 20).build());
		addRenderableWidget(Button.builder(Component.literal("v"), b -> nudge(0, STEP))
			.bounds(startX + 108, rowY, 30, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(width / 2 - 105, height - 40, 100, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
			HudConfig.x = 10;
			HudConfig.y = 10;
		}).bounds(width / 2 + 5, height - 40, 100, 20).build());
	}

	private int clampX(int value) {
		return Math.max(0, Math.min(width - Pvp_helperClient.BOX_W, value));
	}

	private int clampY(int value) {
		return Math.max(0, Math.min(height - Pvp_helperClient.BOX_H, value));
	}

	private void nudge(int dx, int dy) {
		HudConfig.x = clampX(HudConfig.x + dx);
		HudConfig.y = clampY(HudConfig.y + dy);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);

		boolean pressed = Minecraft.getInstance().mouseHandler.isLeftPressed();
		if (pressed && !wasPressed) {
			moving = mouseY < height - 70;
		}
		if (!pressed) moving = false;

		if (moving) {
			HudConfig.x = clampX(mouseX - Pvp_helperClient.BOX_W / 2);
			HudConfig.y = clampY(mouseY - Pvp_helperClient.BOX_H / 2);
		}
		wasPressed = pressed;

		int w = Pvp_helperClient.BOX_W;
		int h = Pvp_helperClient.BOX_H;
		graphics.fill(HudConfig.x - 2, HudConfig.y - 2, HudConfig.x + w, HudConfig.y + h, 0x80000000);
		Pvp_helperClient.drawArmor(graphics, HudConfig.x, HudConfig.y, true);

		graphics.drawCenteredString(font, "Tap anywhere to move the armor", width / 2, 20, 0xFFFFFFFF);
		graphics.drawCenteredString(font, "Use the arrows to fine tune", width / 2, 34, 0xFFAAAAAA);
	}

	@Override
	public void onClose() {
		HudConfig.save();
		super.onClose();
	}
}
