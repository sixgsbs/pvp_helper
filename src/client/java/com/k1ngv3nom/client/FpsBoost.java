package com.k1ngv3nom.client;

import com.mojang.brigadier.Command;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

public class FpsBoost {
	// Players closer than this (in blocks) are always drawn, even if standing still.
	private static final double NEAR_DISTANCE = 12.0;

	public static boolean enabled = true;

	public static void register() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
			dispatcher.register(ClientCommandManager.literal("ph")
				.then(ClientCommandManager.literal("fps").executes(context -> {
					enabled = !enabled;
					context.getSource().sendFeedback(
						Component.literal("Paper Helper FPS boost: " + (enabled ? "ON" : "OFF")));
					return Command.SINGLE_SUCCESS;
				}))));
	}

	public static boolean shouldHide(Entity entity) {
		if (!enabled) return false;
		if (!(entity instanceof RemotePlayer)) return false;

		LocalPlayer self = Minecraft.getInstance().player;
		if (self == null) return false;
		if (entity.distanceToSqr(self) < NEAR_DISTANCE * NEAR_DISTANCE) return false;

		double dx = entity.getX() - entity.xo;
		double dy = entity.getY() - entity.yo;
		double dz = entity.getZ() - entity.zo;
		return dx * dx + dy * dy + dz * dz < 1.0E-4;
	}
}
