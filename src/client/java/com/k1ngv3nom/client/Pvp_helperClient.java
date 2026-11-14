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
	private static final int CELL_W = 42;

	// An XP bottle gives 3-11 XP (about 7 on average). Mending repairs 2 durability per XP.
	private static final double AVG_XP_PER_BOTTLE = 7.0;

	private static final EquipmentSlot[] SLOTS = {
		EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
	};

	public static int boxWidth() {
		return HudConfig.horizontal ? CELL_W * 4 : 120;
	}

	public static int boxHeight() {
		return HudConfig.horizontal ? 46 : 72;
	}

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

		boolean horizontal = HudConfig.horizontal;
		boolean holdingBottle = mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)
			|| mc.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE);

		ItemStack bottleIcon = new ItemStack(Items.EXPERIENCE_BOTTLE);

		int index = 0;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = mc.player.getItemBySlot(slot);
			if (stack.isEmpty()) continue;

			int px = horizontal ? x + index * CELL_W : x;
			int py = horizontal ? y : y + index * 18;

			int iconX = horizontal ? px + 4 : px;
			int textX = horizontal ? px : px + 20;
			int textY = horizontal ? py + 18 : py + 4;
			int bottleX = horizontal ? px : px + 50;
			int bottleY = horizontal ? py + 28 : py;
			int numX = bottleX + (horizontal ? 18 : 20);

			graphics.renderItem(stack, iconX, py);

			if (stack.isDamageableItem()) {
				int max = stack.getMaxDamage();
				int damage = stack.getDamageValue();
				int percent = (max - damage) * 100 / max;

				int color = percent > 50 ? 0xFF55FF55 : percent > 25 ? 0xFFFFFF55 : 0xFFFF5555;
				graphics.drawString(mc.font, percent + "%", textX, textY, color);

				if ((holdingBottle || preview) && damage > 0 && hasMending(stack)) {
					int bottles = (int) Math.ceil(damage / 2.0 / AVG_XP_PER_BOTTLE);
					graphics.renderItem(bottleIcon, bottleX, bottleY);
					graphics.drawString(mc.font, String.valueOf(bottles), numX, bottleY + 4, 0xFF55FFFF);
				}
			}
			index++;
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
