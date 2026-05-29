package me.davidml16.baul.handlers;

import me.davidml16.baul.Main;
import me.davidml16.baul.utils.Utils;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class LanguageHandler {

	private String language = null;

	private File file;
	private FileConfiguration config;

	private HashMap<String, String> messages;
	private HashMap<String, List<String>> messageList;

	private Main main;

	public LanguageHandler(Main main, String language) {
		this.main = main;
		new File(main.getDataFolder().toString() + "/language").mkdirs();
		loadLanguage("en");
		loadLanguage("es");
		loadLanguage("ru");
		this.language = checkLanguage(language);
		this.messages = new HashMap<String, String>();
		this.messageList = new HashMap<String, List<String>>();
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public FileConfiguration getConfig() { return config; }

	public String getPrefix() {
		return me.davidml16.baul.utils.Colorize.format(messages.get("Prefix"));
	}

	public String getRawMessage(String message) {
		if (!messages.containsKey(message)) return "";
		return messages.get(message)
				.replace("<center>", "%center%")
				.replace("</center>", "")
				.replaceAll("%prefix%", Matcher.quoteReplacement(messages.getOrDefault("Prefix", "")));
	}

	public List<String> getRawMessageList(String message) {
		List<String> lines = new ArrayList<>();
		if (!messageList.containsKey(message)) return lines;
		for (String line : messageList.get(message)) {
			lines.add(line
					.replace("<center>", "%center%")
					.replace("</center>", "")
					.replaceAll("%prefix%", Matcher.quoteReplacement(messages.getOrDefault("Prefix", ""))));
		}
		return lines;
	}

	public String getMessage(String message) {
		return me.davidml16.baul.utils.Colorize.format(getRawMessage(message));
	}

	public boolean isEmptyMessage(String message) {
		if(!messages.containsKey(message)) return true;
		return messages.get(message).isEmpty();
	}

	public List<String> getMessageList(String message) {
		List<String> lines = new ArrayList<>();
		for (String raw : getRawMessageList(message)) {
			lines.add(me.davidml16.baul.utils.Colorize.format(raw));
		}
		return lines;
	}

	public String checkLanguage(String lang) {
		File f = new File("plugins/Baul/language/messages_" + lang + ".yml");
		if(f.exists())
			return lang;
		return "es";
	}

	public void pushMessages() {
		Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format(""));
		Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format("  &eLoading language:"));

		file = new File("plugins/Baul/language/messages_" + language + ".yml");
		config = YamlConfiguration.loadConfiguration(file);

		for(String key : config.getKeys(true)) {
			if (!(config.get(key) instanceof MemorySection)) {
				if(config.get(key) instanceof ArrayList)
					messageList.put(key, config.getStringList(key));
				else
					messages.put(key, config.getString(key));
			}
		}

		Main.log.sendMessage(me.davidml16.baul.utils.Colorize.format("    &a'" + language + "' loaded!"));
	}

	public void loadLanguage(String lang) {
		File file = new File(main.getDataFolder() + "/language/messages_" + lang + ".yml");
		if (!file.exists() || file.length() == 0) {
			main.saveResource("language/messages_" + lang + ".yml", true);
		}

		YamlConfiguration cfg;
		try {
			cfg = YamlConfiguration.loadConfiguration(file);
			cfg.getKeys(true);
		} catch (Exception e) {
			main.saveResource("language/messages_" + lang + ".yml", true);
			cfg = YamlConfiguration.loadConfiguration(file);
		}
		InputStreamReader input = new InputStreamReader(main.getResource("language/messages_" + lang + ".yml"));
		FileConfiguration data = YamlConfiguration.loadConfiguration(input);

		Map<String, Object> msgDefaults = new LinkedHashMap<String, Object>();
		for(String key : data.getKeys(true)) {
			if(!(data.get(key) instanceof MemorySection)) {
				msgDefaults.put(key, data.get(key));
			}
		}

		boolean needsSave = false;
		for (String key : msgDefaults.keySet()) {
			Object oldValue = cfg.isSet(key) ? cfg.get(key) : null;
			Object newValue = msgDefaults.get(key);
			if (oldValue == null || !oldValue.equals(newValue)) {
				cfg.set(key, newValue);
				needsSave = true;
			}
		}

		for(String key : cfg.getKeys(true)) {
			if(!(cfg.get(key) instanceof MemorySection)) {
				if (!data.isSet(key)) {
					cfg.set(key, null);
					needsSave = true;
				}
			}
		}

		int newSize = Math.max(cfg.getStringList("Holograms.Reward.New.Me").size(), cfg.getStringList("Holograms.Reward.New.Other").size());
		int duplicateSize = cfg.getStringList("Holograms.Reward.Duplicate").size();

		List<String> newLinesMe = new ArrayList<>();
		for(int i = 0; i < (duplicateSize - newSize); i++)
			newLinesMe.add("");
		newLinesMe.addAll(cfg.getStringList("Holograms.Reward.New.Me"));
		cfg.set("Holograms.Reward.New.Me", newLinesMe);

		List<String> newLinesOther = new ArrayList<>();
		for(int i = 0; i < (duplicateSize - newSize); i++)
			newLinesOther.add("");
		newLinesOther.addAll(cfg.getStringList("Holograms.Reward.New.Other"));
		cfg.set("Holograms.Reward.New.Other", newLinesOther);

		if (needsSave) {
			writeConfigSafely(cfg, file);
		}
	}

	private void writeConfigSafely(YamlConfiguration cfg, File file) {
		DumperOptions options = new DumperOptions();
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.DOUBLE_QUOTED);
		options.setIndent(2);
		options.setPrettyFlow(false);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setSplitLines(false);
		Yaml yaml = new Yaml(options);

		Map<String, Object> root = new LinkedHashMap<>();
		for (String key : cfg.getKeys(false)) {
			root.put(key, sectionToMap(cfg, key));
		}

		try (FileWriter fw = new FileWriter(file)) {
			yaml.dump(root, fw);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private Object sectionToMap(FileConfiguration cfg, String key) {
		if (cfg.isConfigurationSection(key)) {
			Map<String, Object> map = new LinkedHashMap<>();
			for (String sub : cfg.getConfigurationSection(key).getKeys(false)) {
				map.put(sub, sectionToMap(cfg, key + "." + sub));
			}
			return map;
		} else if (cfg.isList(key)) {
			List<Object> list = new ArrayList<>();
			for (Object item : cfg.getList(key)) {
				if (item instanceof String) {
					list.add(item);
				} else {
					list.add(item.toString());
				}
			}
			return list;
		} else {
			return cfg.get(key);
		}
	}

}