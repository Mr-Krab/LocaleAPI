package sawfowl.localeapi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.StartedEngineEvent;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.LocaleReference;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.apiclasses.AbstractLocale;
import sawfowl.localeapi.apiclasses.HoconLocale;
import sawfowl.localeapi.apiclasses.JsonLocale;
import sawfowl.localeapi.apiclasses.LegacyLocale;
import sawfowl.localeapi.apiclasses.YamlLocale;
import sawfowl.localeapi.utils.WatchThread;

public class ImplementAPI {

	private static LocaleService service;

	public static LocaleService getLocaleService() {
		return service;
	}

	API create(Logger logger, Path path) {
		return new API(logger, path);
	}

	class API implements LocaleService {

		private Map<String, Map<Locale, PluginLocale>> pluginLocales;
		private Map<String, Integer> stackSerializers;
		private Map<String, Class<? extends LocaleReference>> defaultReferences;
		private List<Locale> locales;
		private WatchThread watchThread;
		private final Path configDirectory;
		private final Logger logger;
		private Locale system = Locale.getDefault();
		private boolean allowSystem = false;

		API(Logger logger, Path path) {
			setInstaice();
			this.logger = logger;
			configDirectory = path;
			pluginLocales = new HashMap<String, Map<Locale, PluginLocale>>();
			stackSerializers = new HashMap<String, Integer>();
			defaultReferences = new HashMap<String, Class<? extends LocaleReference>>();
			locales = EnumLocales.getLocales();
			watchThread = new WatchThread(this, logger, path);
			allowSystem = locales.contains(system) || locales.stream().filter(locale -> (locale.toLanguageTag().equals(system.toLanguageTag()))).findFirst().isPresent();
			Sponge.eventManager().registerListeners(LocaleAPI.getPluginContainer(), this);
		}

		private void setInstaice() {
			service = this;
		}

		private void updateWatch(String pluginID) {
			watchThread.getWatchLocales().addPluginData(pluginID);
		}

		private String getPluginID(PluginContainer plugin) {
			return plugin.metadata().id();
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

		private void addPluginLocale(String pluginID, Locale locale, AbstractLocale localeUtil) {
			if(!pluginLocales.get(pluginID).containsKey(locale)) pluginLocales.get(pluginID).put(locale, localeUtil);
		}

		public Locale getSystemOrDefaultLocale() {
			return allowSystem ? system : getDefaultLocale();
		}

		public List<Locale> getLocalesList() {
			return locales;
		}

		public Locale getDefaultLocale() {
			return Locales.DEFAULT;
		}

		public Map<Locale, PluginLocale> getPluginLocales(PluginContainer plugin) {
			return getPluginLocales(getPluginID(plugin));
		}

		public Map<Locale, PluginLocale> getPluginLocales(String pluginID) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return null;
			}
			return pluginLocales.containsKey(pluginID) ? pluginLocales.get(pluginID) : new HashMap<Locale, PluginLocale>();
		}

		public PluginLocale getOrDefaultLocale(PluginContainer plugin, Locale locale) {
			return getOrDefaultLocale(getPluginID(plugin), locale);
		}

		public PluginLocale getOrDefaultLocale(String pluginID, Locale locale) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return null;
			}
			return getPluginLocales(pluginID).containsKey(locale) ? getPluginLocales(pluginID).get(locale) : getPluginLocales(pluginID).get(Locales.DEFAULT);
		}

		public void saveAssetLocales(PluginContainer plugin) {
			String pluginID = getPluginID(plugin);
			saveAssetLocales(pluginID);
		}

		public void saveAssetLocales(String pluginID) {
			if(pluginID == null || pluginID.isEmpty()) {
				logger.error("Plugin can not be null or noname(\"\")");
				return;
			}
			File localePath = new File(this.configDirectory + File.separator + pluginID);
			if(!localePath.exists()) localePath.mkdir();
			if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, PluginLocale>());
			for(Locale locale : this.locales) saveAssets(pluginID, locale);
			localesExist(pluginID);
			updateWatch(pluginID);
		}

		public PluginLocale createPluginLocale(PluginContainer plugin, ConfigTypes configType, Locale locale) {
			return createPluginLocale(getPluginID(plugin), configType, locale);
		}

		public PluginLocale createPluginLocale(String pluginID, ConfigTypes configType, Locale locale) {
			if(pluginID == null || pluginID.isEmpty()) {
				this.logger.error("Plugin can not be null or noname(\"\")");
				return null;
			}
			if(!configDirectory.resolve(pluginID).toFile().exists()) configDirectory.resolve(pluginID).toFile().mkdir();
			if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, PluginLocale>());
			if(getPluginLocales(pluginID).containsKey(locale)) return getPluginLocales(pluginID).get(locale);
			if(configType.equals(ConfigTypes.HOCON)) {
				addPluginLocale(pluginID, locale, new HoconLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			} else if(configType.equals(ConfigTypes.JSON)) {
				addPluginLocale(pluginID, locale, new JsonLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			} else if(configType.equals(ConfigTypes.YAML)) {
				addPluginLocale(pluginID, locale, new YamlLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			} else if(configType.equals(ConfigTypes.PROPERTIES)) {
				addPluginLocale(pluginID, locale, new LegacyLocale(this, logger, configDirectory, pluginID, locale.toLanguageTag()));
			}
			updateWatch(pluginID);
			return getPluginLocales(pluginID).get(locale);
		}

		public boolean localesExist(PluginContainer plugin) {
			return localesExist(getPluginID(plugin));
		}

		public boolean localesExist(String pluginID) {
			if(pluginID == null || pluginID.isEmpty()) {
				this.logger.error("Plugin can not be null or noname(\"\")");
				return false;
			}
			for(Locale locale : locales) {
				if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf").toFile().exists() && SerializeOptions.createHoconConfigurationLoader(getItemStackSerializerVariant(pluginID)).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf")).build().canLoad()) {
					createPluginLocale(pluginID, ConfigTypes.HOCON, locale);
				} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".json").toFile().exists() && 
						SerializeOptions.createJsonConfigurationLoader(
								getItemStackSerializerVariant(pluginID))
						.path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".json")).build().canLoad()) {
					createPluginLocale(pluginID, ConfigTypes.JSON, locale);
				} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml").toFile().exists() && SerializeOptions.createYamlConfigurationLoader(getItemStackSerializerVariant(pluginID)).path(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml")).build().canLoad()) {
					createPluginLocale(pluginID, ConfigTypes.YAML, locale);
				} else if(configDirectory.resolve(pluginID + File.separator + locale.toLanguageTag() + ".properties").toFile().exists()) {
					createPluginLocale(pluginID, ConfigTypes.PROPERTIES, locale);
				}
			}
			return pluginLocales.containsKey(pluginID) && pluginLocales.get(pluginID).containsKey(Locales.DEFAULT);
		}

		@Listener(order = Order.LAST)
		public void onCompleteLoad(StartedEngineEvent<Server> event) {
			watchThread.start();
		}

		@Listener
		public void stopWatch(StoppedGameEvent event) {
			if(event == null) return;
			watchThread.stopWatch();
		}

		@Override
		public void setItemStackSerializerVariant(PluginContainer container, int variant) throws Exception {
			if(variant < 1 || variant > 3) throw new IllegalStateException("The value must not be less than 1 or greater than 3.");
			if(stackSerializers.containsKey(container.metadata().id())) stackSerializers.remove(container.metadata().id());
			stackSerializers.put(container.metadata().id(), variant);
		}

		@Override
		public int getItemStackSerializerVariant(PluginContainer container) {
			return getItemStackSerializerVariant(container.metadata().id());
		}

		public int getItemStackSerializerVariant(String plugin) {
			return stackSerializers.getOrDefault(plugin, 1);
		}

		@Override
		public <T extends LocaleReference> void setDefaultReference(PluginContainer container, Class<T> defaultReference) {
			if(defaultReferences.containsKey(container.metadata().id())) defaultReferences.remove(container.metadata().id());
			defaultReferences.put(container.metadata().id(), defaultReference);
		}

		@Override
		public Class<? extends LocaleReference> getDefaultReference(PluginContainer container) {
			return getDefaultReference(container.metadata().id());
		}

		@Override
		public Class<? extends LocaleReference> getDefaultReference(String pluginID) {
			return defaultReferences.containsKey(pluginID) ? defaultReferences.get(pluginID) : null;
		}

	}

}
