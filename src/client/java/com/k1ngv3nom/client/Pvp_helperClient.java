package com.k1ngv3nom.client;

import com.k1ngv3nom.Pvp_helper;
import com.mojang.brigadier.Command;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public class Pvp_helperClient implements ClientModInitializer {
	public static final int BOX_W = 120;
	public static final int BOX_H = 72;

	// An XP bottle gives 3-11 XP (about 7 on average). Mending repairs 2 durability per XP.
	private static final double AVG_XP_PER_BOTTLE = 7.0;

	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	@Override
	public void onInitializeClient() {
		HudConfig.load();
		FpsBoost.register();

		HudElementRegistry.addLast(Pvp_helper.id("armor_hud"), (graphics, deltaTracker) -> {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player == null || mc.options.hideGui || mc.screen instanceof HudEditScreen) return;
			drawArmor(graphics, HudConfig.x, HudConfig.y, false);
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("ph").executes(context -> {
				Minecraft mc = Minecraft.getInstance();
				mc.execute(() -> mc.setScreen(new HudEditScreen()));
				return Command.SINGLE_SUCCESS;
			})));
	}

	public static void drawArmor(GuiGraphics graphics, int x, int y, boolean preview) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null) return;

		boolean holdingBottle = mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)
			|| mc.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE);

		ItemStack bottleIcon = new ItemStack(Items.EXPERIENCE_BOTTLE);

		int row = 0;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = mc.player.getItemBySlot(slot);
			if (stack.isEmpty()) continue;

			int rowY = y + row * 18;
			graphics.renderItem(stack, x, rowY);

			if (stack.isDamageableItem()) {
				int max = stack.getMaxDamage();
				int damage = stack.getDamageValue();
				int percent = (max - damage) * 100 / max;

				int color = percent > 50 ? 0xFF55FF55 : percent > 25 ? 0xFFFFFF55 : 0xFFFF5555;
				graphics.drawString(mc.font, percent + "%", x + 20, rowY + 4, color);

				if ((holdingBottle || preview) && damage > 0 && hasMending(stack)) {
					int bottles = (int) Math.ceil(damage / 2.0 / AVG_XP_PER_BOTTLE);
					graphics.renderItem(bottleIcon, x + 50, rowY);
					graphics.drawString(mc.font, String.valueOf(bottles), x + 70, rowY + 4, 0xFF55FFFF);
				}
			}
			row++;
		}
	}

	private static boolean hasMending(ItemStack stack) {
		ItemEnchantments enchants = stack.get(DataComponents.ENCHANTMENTS);
		if (enchants == null) return false;
		for (Holder<Enchantment> holder : enchants.keySet()) {
			if (holder.is(Enchantments.MENDING)) return true;
		}
		return false;
	}
}
