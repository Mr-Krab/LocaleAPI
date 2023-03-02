package sawfowl.localeapi.api;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
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
import sawfowl.localeapi.LocaleAPIMain;
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
	private Locale system = Locale.getDefault();
	private boolean allowSystem = false;
	
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
		locales = EnumLocales.getLocales();
		watchThread = new WatchThread(this, logger, path);
		watchThread.start();
		allowSystem = locales.contains(system) || locales.stream().filter(locale -> (locale.toLanguageTag().equals(system.toLanguageTag()))).findFirst().isPresent();
		Sponge.eventManager().registerListeners(LocaleAPIMain.getPluginContainer(), this);
	}

	private void updateWatch(String pluginID) {
		watchThread.getWatchLocales().addPluginData(pluginID);
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

	public Locale getSystemOrDefaultLocale() {
		return allowSystem ? system : getDefaultLocale();
	}

	public ConfigurationOptions getConfigurationOptions() {
		return options;
	}

	public List<Locale> getLocalesList() {
		return locales;
	}

	public Locale getDefaultLocale() {
		return Locales.DEFAULT;
	}

	public Map<Locale, AbstractLocaleUtil> getPluginLocales(Object plugin) {
		return getPluginLocales(getPluginID(plugin));
	}

	public Map<Locale, AbstractLocaleUtil> getPluginLocales(String pluginID) {
		if(pluginID == null || pluginID.isEmpty()) {
			logger.error("Plugin can not be null or noname(\"\")");
			return null;
		}
		return pluginLocales.containsKey(pluginID) ? pluginLocales.get(pluginID) : new HashMap<Locale, AbstractLocaleUtil>();
	}

	public AbstractLocaleUtil getOrDefaultLocale(Object plugin, Locale locale) {
		return getOrDefaultLocale(getPluginID(plugin), locale);
	}

	public AbstractLocaleUtil getOrDefaultLocale(String pluginID, Locale locale) {
		if(pluginID == null || pluginID.isEmpty()) {
			logger.error("Plugin can not be null or noname(\"\")");
			return null;
		}
		return getPluginLocales(pluginID).containsKey(locale) ? getPluginLocales(pluginID).get(locale) : getPluginLocales(pluginID).get(Locales.DEFAULT);
	}

	public void saveAssetLocales(Object plugin) {
		String pluginID = getPluginID(plugin);
		saveAssetLocales(pluginID);
	}

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

	public AbstractLocaleUtil createPluginLocale(Object plugin, ConfigTypes configType, Locale locale) {
		String pluginID = getPluginID(plugin);
		return createPluginLocale(pluginID, configType, locale);
	}

	public AbstractLocaleUtil createPluginLocale(String pluginID, ConfigTypes configType, Locale locale) {
		if(pluginID == null || pluginID.isEmpty()) {
			this.logger.error("Plugin can not be null or noname(\"\")");
			return null;
		}
		if(!configDirectory.resolve(pluginID).toFile().exists()) configDirectory.resolve(pluginID).toFile().mkdir();
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

	public boolean localesExist(Object plugin) {
		return localesExist(getPluginID(plugin));
	}

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

	@Listener
	public void stopWatch(StoppedGameEvent event) {
		if(event == null) return;
		watchThread.stopWatch();
	}

}
