package com.k1ngv3nom.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HudEditScreen extends Screen {
	private static final int PANEL_W = 196;
	private static final int PANEL_H = 176;
	private static final int ROW = 23;

	private final long openedAt = System.currentTimeMillis();
	private boolean wasPressed = false;
	private boolean moving = false;
	private Button sizeButton;

	public HudEditScreen() {
		super(Component.literal("Paper Helper"));
	}

	private int panelX() {
		return width - PANEL_W - 10;
	}

	private int panelY() {
		return Math.max(8, height / 2 - PANEL_H / 2);
	}

	private boolean overPanel(int mouseX, int mouseY) {
		return mouseX >= panelX() - 2 && mouseX <= panelX() + PANEL_W + 2
			&& mouseY >= panelY() - 2 && mouseY <= panelY() + PANEL_H + 2;
	}

	private static Component toggleLabel(String name, boolean on) {
		return Component.literal(name + ": ")
			.append(Component.literal(on ? "ON" : "OFF").withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
	}

	private static Component layoutLabel() {
		return Component.literal("Layout: " + (HudConfig.horizontal ? "Horizontal" : "Vertical"));
	}

	private static Component themeLabel() {
		return Component.literal("Theme: " + HudConfig.THEME_NAMES[HudConfig.theme]);
	}

	private static Component sizeLabel() {
		return Component.literal("Size: " + HudConfig.scale + "%");
	}

	@Override
	protected void init() {
		int px = panelX();
		int py = panelY();
		int top = py + 28;

		addRenderableWidget(Button.builder(layoutLabel(), b -> {
			HudConfig.horizontal = !HudConfig.horizontal;
			b.setMessage(layoutLabel());
			nudge(0, 0);
		}).bounds(px + 10, top, 176, 20).build());

		addRenderableWidget(Button.builder(Component.literal("-"), b -> changeScale(-10))
			.bounds(px + 10, top + ROW, 40, 20).build());
		sizeButton = addRenderableWidget(Button.builder(sizeLabel(), b -> {
			HudConfig.scale = 100;
			refreshSize();
		}).bounds(px + 54, top + ROW, 88, 20).build());
		addRenderableWidget(Button.builder(Component.literal("+"), b -> changeScale(10))
			.bounds(px + 146, top + ROW, 40, 20).build());

		addRenderableWidget(Button.builder(themeLabel(), b -> {
			HudConfig.theme = (HudConfig.theme + 1) % HudConfig.THEME_COLORS.length;
			b.setMessage(themeLabel());
		}).bounds(px + 10, top + ROW * 2, 176, 20).build());

		addRenderableWidget(Button.builder(toggleLabel("Percent", HudConfig.showPercent), b -> {
			HudConfig.showPercent = !HudConfig.showPercent;
			b.setMessage(toggleLabel("Percent", HudConfig.showPercent));
		}).bounds(px + 10, top + ROW * 3, 86, 20).build());
		addRenderableWidget(Button.builder(toggleLabel("Totems", HudConfig.showTotems), b -> {
			HudConfig.showTotems = !HudConfig.showTotems;
			b.setMessage(toggleLabel("Totems", HudConfig.showTotems));
			nudge(0, 0);
		}).bounds(px + 100, top + ROW * 3, 86, 20).build());

		addRenderableWidget(Button.builder(toggleLabel("Bottles", HudConfig.showBottles), b -> {
			HudConfig.showBottles = !HudConfig.showBottles;
			b.setMessage(toggleLabel("Bottles", HudConfig.showBottles));
		}).bounds(px + 10, top + ROW * 4, 86, 20).build());
		addRenderableWidget(Button.builder(toggleLabel("Warning", HudConfig.lowWarning), b -> {
			HudConfig.lowWarning = !HudConfig.lowWarning;
			b.setMessage(toggleLabel("Warning", HudConfig.lowWarning));
		}).bounds(px + 100, top + ROW * 4, 86, 20).build());

		addRenderableWidget(Button.builder(Component.literal("Reset"), b -> {
			HudConfig.x = 10;
			HudConfig.y = 10;
			HudConfig.scale = 100;
			refreshSize();
		}).bounds(px + 10, top + ROW * 5, 86, 20).build());
		addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
			.bounds(px + 100, top + ROW * 5, 86, 20).build());
	}

	private void changeScale(int delta) {
		HudConfig.scale = Math.max(50, Math.min(200, HudConfig.scale + delta));
		refreshSize();
	}

	private void refreshSize() {
		sizeButton.setMessage(sizeLabel());
		nudge(0, 0);
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
		int accent = HudConfig.accent();

		float fade = Math.min(1f, (System.currentTimeMillis() - openedAt) / 220f);
		graphics.fill(0, 0, width, height, ((int) (0xA0 * fade)) << 24);

		String hint = "Click or drag anywhere to move your HUD";
		int tw = font.width(hint);
		int hx1 = width / 2 - tw / 2 - 10;
		int hx2 = width / 2 + tw / 2 + 10;
		graphics.fill(hx1 - 1, 9, hx2 + 1, 29, accent);
		graphics.fill(hx1, 10, hx2, 28, 0xF0121218);
		graphics.drawCenteredString(font, hint, width / 2, 15, 0xFFFFFFFF);

		int hx = HudConfig.x;
		int hy = HudConfig.y;
		int w = Pvp_helperClient.boxWidth();
		int h = Pvp_helperClient.boxHeight();
		graphics.fill(hx - 5, hy - 5, hx + w + 3, hy + h + 3, accent);
		graphics.fill(hx - 4, hy - 4, hx + w + 2, hy + h + 2, 0xF0121218);
		Pvp_helperClient.drawArmor(graphics, hx, hy, true);
		graphics.drawString(font, hx + ", " + hy, hx, hy + h + 7, 0xFF9A9AA8);

		int px = panelX();
		int py = panelY();
		graphics.fill(px + 2, py + 3, px + PANEL_W + 2, py + PANEL_H + 3, 0x70000000);
		graphics.fill(px - 1, py - 1, px + PANEL_W + 1, py + PANEL_H + 1, accent);
		graphics.fill(px, py, px + PANEL_W, py + PANEL_H, 0xF0121218);
		graphics.fillGradient(px, py, px + PANEL_W, py + 19, (accent & 0x00FFFFFF) | 0x55000000, 0x00000000);
		graphics.drawCenteredString(font,
			Component.literal("PAPER HELPER").withStyle(ChatFormatting.BOLD),
			px + PANEL_W / 2, py + 6, 0xFFFFFFFF);
		graphics.fill(px + 10, py + 19, px + PANEL_W - 10, py + 20, accent);
	}

	@Override
	public void onClose() {
		HudConfig.save();
		super.onClose();
	}
}
