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
import net.minecraft.world.entity.player.Inventory;
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

	private static float scale() {
		return HudConfig.scale / 100f;
	}

	private static int rowCount() {
		return 4 + (HudConfig.showTotems ? 1 : 0);
	}

	public static int boxWidth() {
		int base = HudConfig.horizontal ? CELL_W * rowCount() : 120;
		return Math.round(base * scale());
	}

	public static int boxHeight() {
		int base = HudConfig.horizontal ? 46 : rowCount() * 18;
		return Math.round(base * scale());
	}

	@Override
	public void onInitializeClient() {
		HudConfig.load();

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

		graphics.pose().pushMatrix();
		graphics.pose().translate((float) x, (float) y);
		graphics.pose().scale(scale(), scale());
		drawContents(graphics, mc, preview);
		graphics.pose().popMatrix();
	}

	private static void drawContents(GuiGraphics graphics, Minecraft mc, boolean preview) {
		boolean horizontal = HudConfig.horizontal;
		boolean showPercent = HudConfig.showPercent;
		boolean holdingBottle = mc.player.getMainHandItem().is(Items.EXPERIENCE_BOTTLE)
			|| mc.player.getOffhandItem().is(Items.EXPERIENCE_BOTTLE);
		boolean bottlesOn = HudConfig.showBottles && (holdingBottle || preview);
		boolean blinkOn = (System.currentTimeMillis() / 350) % 2 == 0;

		ItemStack bottleIcon = new ItemStack(Items.EXPERIENCE_BOTTLE);

		int index = 0;
		for (EquipmentSlot slot : SLOTS) {
			ItemStack stack = mc.player.getItemBySlot(slot);
			if (stack.isEmpty()) continue;

			int px = horizontal ? index * CELL_W : 0;
			int py = horizontal ? 0 : index * 18;

			graphics.renderItem(stack, horizontal ? px + 4 : px, py);

			if (stack.isDamageableItem()) {
				int max = stack.getMaxDamage();
				int damage = stack.getDamageValue();
				int percent = (max - damage) * 100 / max;

				if (showPercent) {
					int color = percent > 50 ? 0xFF55FF55 : percent > 25 ? 0xFFFFFF55 : 0xFFFF5555;
					if (HudConfig.lowWarning && percent <= 20 && blinkOn) color = 0xFFFFFFFF;
					graphics.drawString(mc.font, percent + "%",
						horizontal ? px : px + 20, horizontal ? py + 18 : py + 4, color);
				}

				if (bottlesOn && damage > 0 && hasMending(stack)) {
					int bottles = (int) Math.ceil(damage / 2.0 / AVG_XP_PER_BOTTLE);
					int bottleX = horizontal ? px : px + (showPercent ? 50 : 20);
					int bottleY = horizontal ? py + (showPercent ? 28 : 18) : py;
					graphics.renderItem(bottleIcon, bottleX, bottleY);
					graphics.drawString(mc.font, String.valueOf(bottles),
						bottleX + (horizontal ? 18 : 20), bottleY + 4, 0xFF55FFFF);
				}
			}
			index++;
		}

		if (HudConfig.showTotems) {
			int totems = countTotems(mc);
			int tx = horizontal ? index * CELL_W : 0;
			int ty = horizontal ? 0 : index * 18;
			graphics.renderItem(new ItemStack(Items.TOTEM_OF_UNDYING), horizontal ? tx + 4 : tx, ty);
			int totemColor = totems > 1 ? 0xFFFFFFFF : totems == 1 ? 0xFFFFFF55 : 0xFFFF5555;
			graphics.drawString(mc.font, String.valueOf(totems),
				horizontal ? tx + 4 : tx + 20, horizontal ? ty + 18 : ty + 4, totemColor);
		}
	}

	private static int countTotems(Minecraft mc) {
		int total = 0;
		Inventory inventory = mc.player.getInventory();
		for (int i = 0; i < inventory.getContainerSize(); i++) {
			ItemStack s = inventory.getItem(i);
			if (s.is(Items.TOTEM_OF_UNDYING)) total += s.getCount();
		}
		return total;
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
