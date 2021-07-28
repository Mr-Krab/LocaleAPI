package sawfowl.localeapi.utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.plugin.PluginContainer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.LocaleAPIMain;

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

	private final Properties locale = new Properties();
	private File localeFile;
	private String loc;
	private String pluginID;
	private FileWriter fileWriter;
	private boolean thisIsDefault = false;

	LocaleAPIMain plugin;
	public LegacyLocaleUtil(LocaleAPIMain plugin, String pluginID, String locale) {
		this.plugin = plugin;
		this.pluginID = pluginID;
		this.loc = locale;
		thisIsDefault = locale.equals(Locales.DEFAULT.toLanguageTag());
		try {
			init();
		} catch (IOException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
	}

	@Override
	public void reload() {
		try {
			init();
		} catch (IOException e) {
			plugin.getLogger().error(e.getMessage());
		}
	}

	@Override
	public void saveLocaleNode() {
		try {
			if(fileWriter == null) fileWriter = new FileWriter(localeFile);
			locale.store(fileWriter, null);
		} catch (IOException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
	}

	@Override
	public ConfigurationNode getLocaleRootNode() {
		plugin.getLogger().error("In this type of configuration(Properties), it is not possible to obtain the ConfigurationNode object.");
		return null;
	}

	@Override
	public ConfigurationNode getLocaleNode(Object... path) {
		plugin.getLogger().error("In this type of configuration(Properties), it is not possible to obtain the ConfigurationNode object.");
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
		this.localeFile = new File(plugin.getConfigFile(), File.separator + pluginID + File.separator + loc + ".properties");
		if (this.saveLocale(pluginID, loc)) {
			try (FileReader fr = new FileReader(this.localeFile)) {
				this.locale.load(fr);
			} catch (Exception ex) {
				plugin.getLogger().error("Failed to load " + loc + " locale!" + ex.getLocalizedMessage());
			}
		}
	}

	private List<String> getListStrings(final String key) {
		List<String> list = new ArrayList<String>();
		String out = this.locale.getProperty(key);
		if (out == null) {
			plugin.getLogger().error("&§4PropertiesKey \"" + key + "\" not found!");
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
		return plugin.getAPI().getPluginLocales(pluginID).get(Locales.DEFAULT);
	}

	private boolean saveLocale(final String pluginID, final String name) {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + name + ".properties").toFile().exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.pluginManager().plugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.assetManager().asset(optPlugin.get(), "lang/" + name + ".properties");
			if(assetOpt.isPresent()) {
				Asset asset = assetOpt.get();
				try {
					if(!plugin.getConfigDir().resolve(pluginID + File.separator + name + ".properties").toFile().exists()) {
						asset.copyToDirectory(plugin.getConfigDir().resolve(pluginID));
						plugin.getLogger().info("Locale config saved");
					}
					return true;
				} catch (IOException e) {
					plugin. getLogger().error("Failed to save locale config! " + e.getLocalizedMessage());
				}
			}
		}
		return false;
	}
	
}