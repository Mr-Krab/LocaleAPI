package sawfowl.localeapi.apiclasses;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.TextUtils;

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
public class LegacyLocale extends AbstractLocale {

	private final Properties locale = new Properties();
	private File localeFile;
	private String loc;
	private FileWriter fileWriter;

	public LegacyLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		super(localeService, logger, path, pluginID, locale);
		this.loc = locale;
		try {
			init();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	@Override
	ConfigTypes getType() {
		return ConfigTypes.PROPERTIES;
	}

	@Override
	void setComment(String comment, Object... path) {}

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
		freezeWatcher();
		try {
			if(fileWriter == null) fileWriter = new FileWriter(localeFile);
			locale.store(fileWriter, "");
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
	public Component getComponent(Object... path) {
		String key = getKey(path);
		if(thisIsDefault && !containsKey(key)) return TextUtils.deserializeLegacy("&cPath " + getPathName(path) + "(PropertiesKey " + key + ") not exist!");
		return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getComponent(path) :  TextUtils.deserialize(getString(key));
	}

	@Override
	public List<Component> getListComponents(Object... path) {
		String key = getKey(path);
		if(thisIsDefault && !containsKey(key)) return Arrays.asList(TextUtils.deserializeLegacy("Path " + getPathName(path) + "(PropertiesKey " + key + ") not exist!"));
		return !containsKey(key) && !thisIsDefault ? getDefaultLocale().getListComponents(path) : getListStrings(key).stream().map(TextUtils::deserialize).toList();
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
			locale.setProperty(key, json ? TextUtils.serializeJson(component) : TextUtils.serializeLegacy(component));
			return true;
		}
		return false;
	}

	@Override
	public boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path) {
		String key = getKey(path);
		String value = "";
		int componentsSize = components.size();
		List<String> strings = json ? components.stream().map(TextUtils::serializeJson).toList() : components.stream().map(TextUtils::serializeLegacy).toList();
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
		this.localeFile = path.toFile();
		if (!localeFile.exists()) localeFile.createNewFile();
		try (FileReader fr = new FileReader(this.localeFile)) {
			this.locale.load(fr);
		} catch (Exception ex) {
			logger.error("Failed to load " + loc + " locale!" + ex.getLocalizedMessage());
		}
	}

	private List<String> getListStrings(final String key) {
		String out = this.locale.getProperty(key);
		if (out == null) {
			logger.error("&cPropertiesKey \"" + key + "\" not found!");
			return new ArrayList<String>();
		}
		String spliter = "\n";
		if(out.contains("%LINE_SEPARATOR%")) {
			spliter = "%LINE_SEPARATOR%";
		}
		String[] subStr = out.split(spliter);
		return Stream.of(subStr).toList();
	}

	private String getString(final String key) {
		String out = this.locale.getProperty(key);
		if (out == null) {
			return "&cPropertiesKey \"" + key + "\" not found!";
		}
		return out;
	}

	private boolean containsKey(String key) {
		return locale.containsKey(key);
	}

	private String getKey(Object... path) {
		/*String key = "";
		int pathSize = path.length;
		for(Object object : path) {
			if(pathSize > 1) {
				key = key + object + ".";
				pathSize--;
			} else {
				key = key + object;
			}
		}*/
		return String.join(".", Stream.of(path).map(Object::toString).toArray(String[]::new));
	}
	
}