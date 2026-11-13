package com.k1ngv3nom.client;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class HudConfig {
	public static int x = 10;
	public static int y = 10;

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
		} catch (IOException | NumberFormatException ignored) {
		}
	}

	public static void save() {
		Properties props = new Properties();
		props.setProperty("x", String.valueOf(x));
		props.setProperty("y", String.valueOf(y));
		try (Writer writer = Files.newBufferedWriter(file())) {
			props.store(writer, "Paper Helper HUD position");
		} catch (IOException ignored) {
		}
	}
}
