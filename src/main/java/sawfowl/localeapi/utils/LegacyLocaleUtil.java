package sawfowl.localeapi.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurationNode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.api.LocaleService;

/**
 * Most likely, this is the final version of the class..  <br>
 * Okay, what is this? This is an assistant class that will help you understand your language packs. <br>
 * Those. Instead of using yaml/hocon files that are dependent on the read encoding of the file, 
 * you can safely write your locales in the .properties file without 
 * worrying about encoding the file at all. <br>
 * http://java-properties-editor.com/ - program for editing ;) <br>
 * <br>
 * Class rewritten for use in plugins written on Sponge API,
 * here, the methods from the above api are used. But it is not worth 
 * it to rewrite / delete a couple of points for other applications.
 *
 * @author Dereku
 * @update SawFowl
 */
public class LegacyLocaleUtil extends AbstractLocaleUtil {

	private LocaleService localeService;
	private Logger logger;
	private Path path;
	private final Properties locale = new Properties();
	private File localeFile;
	private String loc;
	private String pluginID;
	private FileWriter fileWriter;
	private boolean thisIsDefault = false;

	public LegacyLocaleUtil(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		this.localeService = localeService;
		this.logger = logger;
		this.path = path;
		this.pluginID = pluginID;
		this.loc = locale;
		thisIsDefault = locale.equals(Locales.DEFAULT.toLanguageTag());
		try {
			init();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	public void reload() {
		try {
			init();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void saveLocaleNode() {
		try {
			if(fileWriter == null) fileWriter = new FileWriter(localeFile);
			locale.store(fileWriter, null);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	public ConfigurationNode getLocaleRootNode() {
		logger.error("In this type of configuration(Properties), it is not possible to obtain the ConfigurationNode object.");
		return null;
	}

	@Override
	public ConfigurationNode getLocaleNode(Object... path) {
		logger.error("In this type of configuration(Properties), it is not possible to obtain the ConfigurationNode object.");
		return null;
	}

	@Override
	public Component getComponent(boolean json, Object... path) {
		String key = getKey(path);
		if(thisIsDefault && !containsKey(key)) return deserializeLegacy("Path " + getPathName(path) + "(PropertiesKey " + key + ") not exist!");
		if(json) {
			return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getComponent(json, path) : deserializeJson(getString(key));
		}
		return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getComponent(json, path) :  deserializeLegacy(getString(key));
	}

	@Override
	public List<Component> getListComponents(boolean json, Object... path) {
		String key = getKey(path);
		if(thisIsDefault && !containsKey(key)) return Arrays.asList(deserializeLegacy("Path " + getPathName(path) + "(PropertiesKey " + key + ") not exist!"));
		if(json) return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getListComponents(json, path) : getListStrings(key).stream().map(this::deserializeJson).collect(Collectors.toList());
		return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getListComponents(json, path) : getListStrings(key).stream().map(this::deserializeLegacy).collect(Collectors.toList());
	}

	@Override
	public String getString(Object... path) {
		String key = getKey(path);
		if(thisIsDefault && !containsKey(key)) return "Path " + getPathName(path) + "(PropertiesKey " + key + ") not exist!";
		return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getString(path) : getString(key);
	}

	@Override
	public List<String> getListStrings(Object... path) {
		String key = getKey(path);
		if(thisIsDefault && !containsKey(key)) return Arrays.asList("Path " + getPathName(path) + "(PropertiesKey " + key + ") not exist!");
		return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getListStrings(path) : getListStrings(key);
	}

	@Override
	public boolean checkComponent(boolean json, Component component, String comment, Object... path) {
		String key = getKey(path);
		if(!containsKey(key)) {
			if(json) {
				locale.setProperty(key, serializeJson(component));
			} else {
				locale.setProperty(key, serializeLegacy(component));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path) {
		String key = getKey(path);
		String value = "";
		int componentsSize = components.size();
		List<String> strings = json ? components.stream().map(this::serializeJson).collect(Collectors.toList()) : components.stream().map(this::serializeLegacy).collect(Collectors.toList());
		for(String string : strings) {
			if(componentsSize > 1) {
				value = value + string + "%LINE_SEPARATOR%";
				componentsSize--;
			} else {
				value = value + string;
			}
		}
		if(!containsKey(key)) {
			locale.setProperty(key, value);
			return true;
		}
		return false;
	}

	@Override
	public boolean checkString(String string, String comment, Object... path) {
		String key = getKey(path);
		if(!containsKey(key)) {
			locale.setProperty(key, string);
			return true;
		}
		return false;
	}

	@Override
	public boolean checkListStrings(List<String> strings, String comment, Object... path) {
		String key = getKey(path);
		String value = "";
		int stringsSize = strings.size();
		for(String string : strings) {
			if(stringsSize > 1) {
				value = value + string + "%LINE_SEPARATOR%";
				stringsSize--;
			} else {
				value = value + string;
			}
		}
		if(!containsKey(key)) {
			locale.setProperty(key, value);
			return true;
		}
		return false;
	}

	private void init() throws IOException {
		this.locale.clear();
		String loc = this.loc;
		this.localeFile = new File(path.toFile(), File.separator + pluginID + File.separator + loc + ".properties");
		if (localeFile.exists()) {
			try (FileReader fr = new FileReader(this.localeFile)) {
				this.locale.load(fr);
			} catch (Exception ex) {
				logger.error("Failed to load " + loc + " locale!" + ex.getLocalizedMessage());
			}
		}
	}

	private List<String> getListStrings(final String key) {
		List<String> list = new ArrayList<String>();
		String out = this.locale.getProperty(key);
		if (out == null) {
			logger.error("&§4PropertiesKey \"" + key + "\" not found!");
			return list;
		}
		String spliter = "\n";
		if(out.contains("%LINE_SEPARATOR%")) {
			spliter = "%LINE_SEPARATOR%";
		}
		String[] subStr = out.split(spliter);
		for(int i = 0; i < subStr.length; i++) {
			list.add(subStr[i]);
		}
		return list;
	}

	private String getString(final String key) {
		String out = this.locale.getProperty(key);
		if (out == null) {
			return "&§4PropertiesKey \"" + key + "\" not found!";
		}
		return out;
	}

	private boolean containsKey(String key) {
		return locale.containsKey(key);
	}

	private String getKey(Object... path) {
		String key = "";
		int pathSize = path.length;
		for(Object object : path) {
			if(pathSize > 1) {
				key = key + object + ".";
				pathSize--;
			} else {
				key = key + object;
			}
		}
		return key;
	}

	private String getPathName(Object... path) {
		String name = "[";
		int pathSize = path.length;
		for(Object object : path) {
			if(pathSize > 1) {
				name = name + object + ", ";
				pathSize--;
			} else {
				name = name + object;
			}
		}
		return name + "]";
	}

	private Component deserializeJson(String string) {
		return GsonComponentSerializer.gson().deserialize(string);
	}

	private Component deserializeLegacy(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	private String serializeJson(Component component) {
		return GsonComponentSerializer.gson().serialize(component);
	}

	private String serializeLegacy(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

	private AbstractLocaleUtil getDefaultLocale() {
		return localeService.getPluginLocales(pluginID).get(Locales.DEFAULT);
	}
	
}