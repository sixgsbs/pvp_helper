package com.k1ngv3nom.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudEditScreen extends Screen {
	private static final int STEP = 4;
	private static final int PANEL_W = 220;
	private static final int PANEL_H = 98;

	private boolean wasPressed = false;
	private boolean moving = false;

	public HudEditScreen() {
		super(Component.literal("Paper Helper"));
	}

	private int panelX() {
		return width / 2 - PANEL_W / 2;
	}

	private int panelY() {
		return height - PANEL_H - 8;
	}

	private boolean overPanel(int mouseX, int mouseY) {
		return mouseX >= panelX() - 2 && mouseX <= panelX() + PANEL_W + 2
			&& mouseY >= panelY() - 2 && mouseY <= panelY() + PANEL_H + 2;
	}

	private Component layoutLabel() {
		return Component.literal("Layout: " + (HudConfig.horizontal ? "Horizontal" : "Vertical"));
	}

	@Override
	protected void init() {
		int px = panelX();
		int py = panelY();

		addRenderableWidget(Button.builder(layoutLabel(), b -> {
			HudConfig.horizontal = !HudConfig.horizontal;
			b.setMessage(layoutLabel());
			nudge(0, 0);
		}).bounds(px + 10, py + 26, 200, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Left"), b -> nudge(-STEP, 0))
			.bounds(px + 10, py + 50, 47, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Right"), b -> nudge(STEP, 0))
			.bounds(px + 61, py + 50, 47, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Up"), b -> nudge(0, -STEP))
			.bounds(px + 112, py + 50, 47, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Down"), b -> nudge(0, STEP))
			.bounds(px + 163, py + 50, 47, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
			HudConfig.x = 10;
			HudConfig.y = 10;
		}).bounds(px + 10, py + 74, 98, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(px + 112, py + 74, 98, 20).build());
	}

	private int clampX(int value) {
		return Math.max(0, Math.min(width - Pvp_helperClient.boxWidth(), value));
	}

	private int clampY(int value) {
		return Math.max(0, Math.min(height - Pvp_helperClient.boxHeight(), value));
	}

	private void nudge(int dx, int dy) {
		HudConfig.x = clampX(HudConfig.x + dx);
		HudConfig.y = clampY(HudConfig.y + dy);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		boolean pressed = Minecraft.getInstance().mouseHandler.isLeftPressed();
		if (pressed && !wasPressed) {
			moving = !overPanel(mouseX, mouseY);
		}
		if (!pressed) moving = false;

		if (moving) {
			HudConfig.x = clampX(mouseX - Pvp_helperClient.boxWidth() / 2);
			HudConfig.y = clampY(mouseY - Pvp_helperClient.boxHeight() / 2);
		}
		wasPressed = pressed;

		super.render(graphics, mouseX, mouseY, partialTick);
	}

	@Override
	public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.fill(0, 0, width, height, 0x90000000);

		String hint = "Tap anywhere or drag to move your armor";
		int tw = font.width(hint);
		graphics.fill(width / 2 - tw / 2 - 8, 10, width / 2 + tw / 2 + 8, 28, 0xB0000000);
		graphics.drawCenteredString(font, hint, width / 2, 15, 0xFFFFFFFF);

		int x = HudConfig.x;
		int y = HudConfig.y;
		int w = Pvp_helperClient.boxWidth();
		int h = Pvp_helperClient.boxHeight();
		graphics.fill(x - 4, y - 4, x + w + 2, y + h + 2, 0xFF8A6BFF);
		graphics.fill(x - 3, y - 3, x + w + 1, y + h + 1, 0xF0141418);
		Pvp_helperClient.drawArmor(graphics, x, y, true);

		int px = panelX();
		int py = panelY();
		graphics.fill(px - 1, py - 1, px + PANEL_W + 1, py + PANEL_H + 1, 0xFF3A3A4A);
		graphics.fill(px, py, px + PANEL_W, py + PANEL_H, 0xF0141418);
		graphics.drawCenteredString(font, "Paper Helper", width / 2, py + 7, 0xFFFFFFFF);
		graphics.fill(px + 10, py + 19, px + PANEL_W - 10, py + 20, 0xFF8A6BFF);
	}

	@Override
	public void onClose() {
		HudConfig.save();
		super.onClose();
	}
}
