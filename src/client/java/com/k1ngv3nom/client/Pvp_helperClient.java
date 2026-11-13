package com.k1ngv3nom.client;

import com.k1ngv3nom.Pvp_helper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class Pvp_helperClient implements ClientModInitializer {
	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	private static final int HUD_X = 10;
	private static final int HUD_Y = 10;

	@Override
	public void onInitializeClient() {
		HudElementRegistry.addLast(Pvp_helper.id("armor_hud"), Pvp_helperClient::renderArmor);
	}

	private static void renderArmor(GuiGraphics graphics, DeltaTracker deltaTracker) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.options.hideGui) return;

		int row = 0;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = mc.player.getItemBySlot(slot);
			if (stack.isEmpty()) continue;

			int y = HUD_Y + row * 18;
			graphics.renderItem(stack, HUD_X, y);

			if (stack.isDamageableItem()) {
				int max = stack.getMaxDamage();
				int left = max - stack.getDamageValue();
				int percent = left * 100 / max;

				int color = percent > 50 ? 0xFF55FF55 : percent > 25 ? 0xFFFFFF55 : 0xFFFF5555;
				graphics.drawString(mc.font, percent + "%", HUD_X + 20, y + 4, color);
			}
			row++;
		}
	}
}
