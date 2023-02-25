package sawfowl.localeapi.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;

public class HoconLocaleUtil extends AbstractLocaleUtil {

	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode localeNode;
	public HoconLocaleUtil(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		super(localeService, logger, path, pluginID, locale, ConfigTypes.HOCON.toString());
		configLoader = HoconConfigurationLoader.builder().defaultOptions(localeService.getConfigurationOptions()).path(this.path).build();
		reload();
	}

	@Override
	public void reload() {
		try {
			localeNode = configLoader.load();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void saveLocaleNode() {
		freezeWatcher();
		try {
			configLoader.save(localeNode);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public CommentedConfigurationNode getLocaleRootNode() {
		return localeNode;
	}

	@Override
	public CommentedConfigurationNode getLocaleNode(Object... path) {
		return localeNode.node(path);
	}

	@Override
	public Component getComponent(boolean json, Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return deserializeLegacy("Path " + getPathName(path) + " not exist!");
		if(json) return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getComponent(json, path) : deserializeJson(getString(path));
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getComponent(json, path) : deserializeLegacy(getString(path));
	}

	@Override
	public List<Component> getListComponents(boolean json, Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return Arrays.asList(deserializeLegacy("Path " + getPathName(path) + " not exist!"));
		if(json) return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListComponents(json, path) : getListStrings(path).stream().map(this::deserializeJson).collect(Collectors.toList());
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListComponents(json, path) : getListStrings(path).stream().map(this::deserializeLegacy).collect(Collectors.toList());
	}

	@Override
	public String getString(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return "Path " + getPathName(path) + " not exist!";
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getString(path) : getLocaleNode(path).getString();
	}

	@Override
	public List<String> getListStrings(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return Arrays.asList("Path " + getPathName(path) + " not exist!");
		try {
			return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListStrings(path) : getLocaleNode(path).getList(String.class);
		} catch (SerializationException e) {
			logger.error(e.getLocalizedMessage());
		}
		return Arrays.asList("Error getting list of Strings " + getPathName(path));
	}

	@Override
	public boolean checkComponent(boolean json, Component component, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				if(json) {
					getLocaleNode(path).set(serializeJson(component));
				} else {
					getLocaleNode(path).set(serializeLegacy(component));
				}
				if(comment != null) {
					getLocaleNode(path).comment(comment);
				}
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				if(json) {
					getLocaleNode(path).setList(String.class, components.stream().map(this::serializeJson).collect(Collectors.toList()));
				} else {
					getLocaleNode(path).setList(String.class, components.stream().map(this::serializeLegacy).collect(Collectors.toList()));
				}
				if(comment != null) {
					getLocaleNode(path).comment(comment);
				}
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkString(String string, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				getLocaleNode(path).set(string);
				if(comment != null) {
					getLocaleNode(path).comment(comment);
				}
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListStrings(List<String> strings, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				getLocaleNode(path).setList(String.class, strings);
				if(comment != null) {
					getLocaleNode(path).comment(comment);
				}
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
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

}