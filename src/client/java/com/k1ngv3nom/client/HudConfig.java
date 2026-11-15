package com.k1ngv3nom.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class HudConfig {
	public static final String[] THEME_NAMES = {"Purple", "Cyan", "Green", "Red", "Gold", "Pink"};
	public static final int[] THEME_COLORS = {
		0xFF8A6BFF, 0xFF35D0FF, 0xFF4CE07A, 0xFFFF5470, 0xFFFFC043, 0xFFFF7AD9
	};

	public static int x = 10;
	public static int y = 10;
	public static boolean horizontal = false;
	public static int scale = 100;
	public static int theme = 0;
	public static boolean showPercent = true;
	public static boolean showTotems = true;
	public static boolean showBottles = true;
	public static boolean lowWarning = true;

	public static int accent() {
		return THEME_COLORS[Math.floorMod(theme, THEME_COLORS.length)];
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("paper_helper.properties");
	}

	public static void load() {
		Path path = file();
		if (!Files.exists(path)) return;
		try (Reader reader = Files.newBufferedReader(path)) {
			Properties props = new Properties();
			props.load(reader);
			x = Integer.parseInt(props.getProperty("x", "10"));
			y = Integer.parseInt(props.getProperty("y", "10"));
			horizontal = Boolean.parseBoolean(props.getProperty("horizontal", "false"));
			scale = Math.max(50, Math.min(200, Integer.parseInt(props.getProperty("scale", "100"))));
			theme = Math.floorMod(Integer.parseInt(props.getProperty("theme", "0")), THEME_COLORS.length);
			showPercent = Boolean.parseBoolean(props.getProperty("showPercent", "true"));
			showTotems = Boolean.parseBoolean(props.getProperty("showTotems", "true"));
			showBottles = Boolean.parseBoolean(props.getProperty("showBottles", "true"));
			lowWarning = Boolean.parseBoolean(props.getProperty("lowWarning", "true"));
		} catch (IOException | NumberFormatException ignored) {
		}
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("x", String.valueOf(x));
		props.setProperty("y", String.valueOf(y));
		props.setProperty("horizontal", String.valueOf(horizontal));
		props.setProperty("scale", String.valueOf(scale));
		props.setProperty("theme", String.valueOf(theme));
		props.setProperty("showPercent", String.valueOf(showPercent));
		props.setProperty("showTotems", String.valueOf(showTotems));
		props.setProperty("showBottles", String.valueOf(showBottles));
		props.setProperty("lowWarning", String.valueOf(lowWarning));
		try (Writer writer = Files.newBufferedWriter(file())) {
			props.store(writer, "Paper Helper settings");
		} catch (IOException ignored) {
		}
	}
}
