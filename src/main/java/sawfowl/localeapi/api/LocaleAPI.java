package sawfowl.localeapi.api;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import sawfowl.localeapi.utils.HoconLocaleUtil;
import sawfowl.localeapi.utils.JsonLocaleUtil;
import sawfowl.localeapi.utils.LegacyLocaleUtil;
import sawfowl.localeapi.utils.AbstractLocaleUtil;
import sawfowl.localeapi.utils.YamlLocaleUtil;

public class LocaleAPI implements LocaleService {

	private Map<String, Map<Locale, AbstractLocaleUtil>> pluginLocales;
	private List<Locale> locales;
	private WatchThread watchThread;
	private Path configDirectory;
	private Logger logger;
	private ObjectMapper.Factory factory;
	private TypeSerializerCollection child;
	private ConfigurationOptions options;
	
	public WatchThread getWatchThread() {
		return watchThread;
	}

	public LocaleAPI(Logger logger, Path path) {
		this.logger = logger;
		configDirectory = path;
		factory = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build();
		child = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(factory).build();
		options = ConfigurationOptions.defaults().serializers(child);
		pluginLocales = new HashMap<String, Map<Locale, AbstractLocaleUtil>>();
		locales = new ArrayList<Locale>();
		generateLocalesList();
		watchThread = new WatchThread(this, logger, path);
		watchThread.start();
	}

	private void updateWatch(String pluginID) {
		watchThread.getWatchLocales().addPluginData(pluginID);
	}

	private void generateLocalesList() {
		for(Field field : Locales.class.getFields()) {
			try {
				if(field.get(field.getType()) instanceof Locale) locales.add((Locale) field.get(field.getType()));
			} catch (IllegalArgumentException | IllegalAccessException e) {
				logger.error("Error get locales {}" + e.getLocalizedMessage());
			}
		}
	}

	private String getPluginID(Object plugin) {
		for (Annotation annotation : plugin.getClass().getDeclaredAnnotations()) {
			if(annotation instanceof Plugin) return ((Plugin) annotation).value();
		}
		return "";
	}

	private void saveAssets(String pluginID, Locale locale) {
		if(pluginID == null || pluginID.isEmpty()) {
			logger.error("Plugin can not be null or noname(\"\")");
			return;
		}
		Optional<PluginContainer> optPluginContainer = Sponge.pluginManager().plugin(pluginID);
		if(optPluginContainer.isPresent()) {
			PluginContainer pluginContainer = optPluginContainer.get();
			for(ConfigTypes configType : ConfigTypes.values()) {
				String configTypeName = configType.toString();
				pluginContainer.openResource(URI.create(File.separator + "assets" + File.separator + pluginID + File.separator + "lang" + File.separator + locale.toLanguageTag() + configTypeName)).ifPresent(inputStream -> {
					File localeFile = configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + configTypeName).toFile();
					if(!localeFile.exists()) {
						try {
							Files.copy(inputStream, localeFile.toPath());
							logger.info("Locale config " + locale.toLanguageTag() + configTypeName + " for plugin \"" + pluginID + "\" has been saved");
						} catch (IOException e) {
							logger.error(e.getLocalizedMessage());
						}
					}
				});
			}
		} else {
			logger.error("Could not find PluginContainer for plugin " + pluginID);
		}
	}

	private void addPluginLocale(String pluginID, Locale locale, AbstractLocaleUtil localeUtil) {
		if(!pluginLocales.get(pluginID).containsKey(locale)) pluginLocales.get(pluginID).put(locale, localeUtil);
	}

	/**
	 * The method returns options for the Sponge configuration files.<br>
	 * These options disable serialization of objects not marked by the <b>@Setting</b> annotation.
	 * 
	 */
	public ConfigurationOptions getConfigurationOptions() {
		return options;
	}

	/**
	 * List of all localizations of the game.
	 * 
	 */
	public List<Locale> getLocalesList() {
		return locales;
	}

	/**
	 * The default location. Used in a localization map.
	 */
	public Locale getDefaultLocale() {
		return Locales.DEFAULT;
	}

	/**
	 * Getting a map of plugin localizations with Sponge config files. <br>
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public Map<Locale, AbstractLocaleUtil> getPluginLocales(Object plugin) {
		return getPluginLocales(getPluginID(plugin));
	}

	/**
	 * Getting a map of plugin localizations with Sponge config files. <br>
	 * 
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, AbstractLocaleUtil> getPluginLocales(String pluginID) {
		if(pluginID == null || pluginID.isEmpty()) {
			logger.error("Plugin can not be null or noname(\"\")");
			return null;
		}
		return pluginLocales.containsKey(pluginID) ? pluginLocales.get(pluginID) : new HashMap<Locale, AbstractLocaleUtil>();
	}

	/**
	 * Get plugin localization with Sponge config file. <br> <br>
	 * Note that getting the ConfigurationNode object in the <b>'*.properties'</b> configuration is not possible.<br>
	 * Methods for getting this object will return null.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @param locale - Selected localization. If the selected localization is not found, the default localization will be returned.
	 */
	public AbstractLocaleUtil getOrDefaultLocale(Object plugin, Locale locale) {
		return getOrDefaultLocale(getPluginID(plugin), locale);
	}

	/**
	 * Get plugin localization with Sponge config file. <br> <br>
	 * Note that getting the ConfigurationNode object in the <b>'*.properties'</b> configuration is not possible.<br>
	 * Methods for getting this object will return null.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param locale - Selected localization. If the selected localization is not found, the default localization will be returned.
	 */
	public AbstractLocaleUtil getOrDefaultLocale(String pluginID, Locale locale) {
		if(pluginID == null || pluginID.isEmpty()) {
			logger.error("Plugin can not be null or noname(\"\")");
			return null;
		}
		return getPluginLocales(pluginID).getOrDefault(locale, pluginLocales.get(pluginID).get(Locales.DEFAULT));
	}

	/**
	 * Save plugin locales from assets.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public void saveAssetLocales(Object plugin) {
		String pluginID = getPluginID(plugin);
		saveAssetLocales(pluginID);
	}

	/**
	 * Save plugin locales from assets.
	 * 
	 * @param pluginID - Plugin ID.
	 */
	public void saveAssetLocales(String pluginID) {
		if(pluginID == null || pluginID.isEmpty()) {
			logger.error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(this.configDirectory + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, AbstractLocaleUtil>());
		for(Locale locale : this.locales) {
			saveAssets(pluginID, locale);
		}
		localesExist(pluginID);
		updateWatch(pluginID);
	}

	/**
	 * Creating a plugin localization file.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 */
	public AbstractLocaleUtil createPluginLocale(Object plugin, ConfigTypes configType, Locale locale) {
		String pluginID = getPluginID(plugin);
		return createPluginLocale(pluginID, configType, locale);
	}

	/**
	 * Creating a plugin localization file.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 */
	public AbstractLocaleUtil createPluginLocale(String pluginID, ConfigTypes configType, Locale locale) {
		if(pluginID == null || pluginID.isEmpty()) {
			this.logger.error("Plugin can not be null or noname(\"\")");
			return null;
		}
		if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, AbstractLocaleUtil>());
		if(getPluginLocales(pluginID).containsKey(locale)) return getPluginLocales(pluginID).get(locale);
		if(configType.equals(ConfigTypes.HOCON)) {
			addPluginLocale(pluginID, locale, new HoconLocaleUtil(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
		} else if(configType.equals(ConfigTypes.JSON)) {
			addPluginLocale(pluginID, locale, new JsonLocaleUtil(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
		} else if(configType.equals(ConfigTypes.YAML)) {
			addPluginLocale(pluginID, locale, new YamlLocaleUtil(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
		} else if(configType.equals(ConfigTypes.PROPERTIES)) {
			addPluginLocale(pluginID, locale, new LegacyLocaleUtil(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
		}
		updateWatch(pluginID);
		return getPluginLocales(pluginID).get(locale);
	}

	/**
	 * Load plugin locales if exists.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public boolean localesExist(Object plugin) {
		return localesExist(getPluginID(plugin));
	}

	/**
	 * Load plugin locales if exists.
	 * 
	 * @param pluginID - Plugin ID.
	 */
	public boolean localesExist(String pluginID) {
		if(pluginID == null || pluginID.isEmpty()) {
			this.logger.error("Plugin can not be null or noname(\"\")");
			return false;
		}
		for(Locale locale : locales) {
			if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf").toFile().exists() && HoconConfigurationLoader.builder().defaultOptions(getConfigurationOptions()).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf")).build().canLoad()) {
				createPluginLocale(pluginID, ConfigTypes.HOCON, locale);
			} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".json").toFile().exists() && GsonConfigurationLoader.builder().defaultOptions(getConfigurationOptions()).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".json")).build().canLoad()) {
				createPluginLocale(pluginID, ConfigTypes.JSON, locale);
			} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml").toFile().exists() && YamlConfigurationLoader.builder().defaultOptions(getConfigurationOptions()).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml")).build().canLoad()) {
				createPluginLocale(pluginID, ConfigTypes.YAML, locale);
			} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".properties").toFile().exists()) {
				createPluginLocale(pluginID, ConfigTypes.PROPERTIES, locale);
			}
		}
		return pluginLocales.containsKey(pluginID) && pluginLocales.get(pluginID).containsKey(Locales.DEFAULT);
	}

}
