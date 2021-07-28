package sawfowl.localeapi.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import sawfowl.localeapi.LocaleAPIMain;

public class HoconLocaleUtil extends AbstractLocaleUtil {

	private LocaleAPIMain plugin;
	private String pluginID;
	private String locale;
	private boolean thisIsDefault = false;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode localeNode;
	private Path path;

	public HoconLocaleUtil(LocaleAPIMain plugin, String pluginID, String locale) {
		this.plugin = plugin;
		this.pluginID = pluginID;
		this.locale = locale;
		thisIsDefault = locale.equals(Locales.DEFAULT.toLanguageTag());
		saveLocaleFile();
		path = plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".conf");
		configLoader = HoconConfigurationLoader.builder().defaultOptions(plugin.getConfigurationOptions()).path(path).build();
		reload();
	}

	@Override
	public void reload() {
		try {
			localeNode = configLoader.load();
		} catch (IOException e) {
			plugin.getLogger().error(e.getMessage());
		}
	}

	@Override
	public void saveLocaleNode() {
		try {
			configLoader.save(localeNode);
		} catch (IOException e) {
			plugin.getLogger().error(e.getMessage());
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
			plugin.getLogger().error(e.getLocalizedMessage());
		}
		return Arrays.asList("Error getting list of Strings " + getPathName(path));
	}

	@Override
	public boolean checkComponent(boolean json, Component component, @Nullable String comment, Object... path) {
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
				plugin.getLogger().error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListComponents(boolean json, List<Component> components, @Nullable String comment, Object... path) {
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
				plugin.getLogger().error(e.getLocalizedMessage());
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
				plugin.getLogger().error(e.getLocalizedMessage());
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
				plugin.getLogger().error(e.getLocalizedMessage());
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
		return plugin.getAPI().getPluginLocales(pluginID).get(Locales.DEFAULT);
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
	
	private void saveLocaleFile() {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".conf").toFile().exists()) {
			return;
		}
		Optional<PluginContainer> optPlugin = Sponge.pluginManager().plugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.assetManager().asset(optPlugin.get(), "lang/" + locale + ".conf");
			if(assetOpt.isPresent()) {
				Asset asset = assetOpt.get();
				try {
					if(!plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".conf").toFile().exists()) {
						asset.copyToDirectory(plugin.getConfigDir().resolve(pluginID));
						plugin.getLogger().info("Locale config saved");
					}
				} catch (IOException e) {
					plugin. getLogger().error("Failed to save locale config! " + e.getLocalizedMessage());
				}
			}
		}
	}

}