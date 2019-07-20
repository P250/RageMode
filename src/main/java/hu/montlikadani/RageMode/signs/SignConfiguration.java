package hu.montlikadani.ragemode.signs;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import hu.montlikadani.ragemode.RageMode;

public class SignConfiguration {

	private static boolean inited = false;
	private static File yamlSignsFile;
	private static FileConfiguration signConfig;

	public static void initSignConfiguration() {
		if (inited)
			return;
		else
			inited = true;

		File file = new File(RageMode.getInstance().getFolder(), "signs.yml");
		FileConfiguration config = null;
		yamlSignsFile = file;

		if (!file.exists()) {
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			try {
				file.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
				RageMode.getInstance().throwMsg();
			}

			config = new YamlConfiguration();
			config.createSection("signs");

			try {
				config.save(file);
			} catch (IOException e2) {
				e2.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
		} else
			config = YamlConfiguration.loadConfiguration(file);

		signConfig = config;
		try {
			config.save(file);
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
	}

	public static File getYamlSignsFile() {
		return yamlSignsFile;
	}

	public static FileConfiguration getSignConfig() {
		return signConfig;
	}
}