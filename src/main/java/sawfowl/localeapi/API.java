package sawfowl.localeapi;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.StoppedGameEvent;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.apiclasses.AbstractLocale;
import sawfowl.localeapi.apiclasses.HoconLocale;
import sawfowl.localeapi.apiclasses.JsonLocale;
import sawfowl.localeapi.apiclasses.LegacyLocale;
import sawfowl.localeapi.apiclasses.YamlLocale;
import sawfowl.localeapi.utils.WatchThread;

class API implements LocaleService {

	private Map<String, Map<Locale, PluginLocale>> pluginLocales;
	private List<Locale> locales;
	private WatchThread watchThread;
	private Path configDirectory;
	private Logger logger;
	private ObjectMapper.Factory factory;
	private TypeSerializerCollection child;
	private ConfigurationOptions options;
	private Locale system = Locale.getDefault();
	private boolean allowSystem = false;

	API(Logger logger, Path path) {
		this.logger = logger;
		configDirectory = path;
		factory = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build();
		child = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(factory).register(DataContainer.class, DATA_CONTAINER_SERIALIZER).build();
		options = ConfigurationOptions.defaults().serializers(child);
		pluginLocales = new HashMap<String, Map<Locale, PluginLocale>>();
		locales = EnumLocales.getLocales();
		watchThread = new WatchThread(this, logger, path);
		watchThread.start();
		allowSystem = locales.contains(system) || locales.stream().filter(locale -> (locale.toLanguageTag().equals(system.toLanguageTag()))).findFirst().isPresent();
		Sponge.eventManager().registerListeners(LocaleAPI.getPluginContainer(), this);
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

	public ConfigurationOptions getConfigurationOptions() {
		return options;
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
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		if(!pluginLocales.containsKey(pluginID)) pluginLocales.put(pluginID, new HashMap<Locale, PluginLocale>());
		for(Locale locale : this.locales) {
			saveAssets(pluginID, locale);
		}
		localesExist(pluginID);
		updateWatch(pluginID);
	}

	public PluginLocale createPluginLocale(PluginContainer plugin, ConfigTypes configType, Locale locale) {
		String pluginID = getPluginID(plugin);
		return createPluginLocale(pluginID, configType, locale);
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

	private static final TypeSerializer<DataContainer> DATA_CONTAINER_SERIALIZER = new TypeSerializer<DataContainer>() {

		@Override
		public DataContainer deserialize(Type type, ConfigurationNode node) throws SerializationException {
			DataContainer container = DataContainer.createNew();
			for (ConfigurationNode query : node.childrenMap().values()) {
				Map<List<String>, Object> values = findValue(query, new ArrayList<>());
				for (Map.Entry<List<String>, Object> entry : values.entrySet()) {
					DataQuery valueQuery = DataQuery.of(entry.getKey());
					container = container.set(valueQuery, entry.getValue());
				}
			}
			return container;
		}

		@Override
		public void serialize(Type type, @Nullable DataContainer obj, ConfigurationNode node) throws SerializationException {
			if (obj == null) {
				node.set(null);
				return;
			}
			for (DataQuery key : obj.keys(true)) {
				Optional<Object> opValue = obj.get(key);
				if (!opValue.isPresent()) {
					System.err.println("Skipping '" + key + "'. Could not read value");
					continue;
				}
				Object[] nodes = key.parts().stream().map(s -> (Object) s).toArray();
				Object value = opValue.get();

				node.node(nodes).node("value").set(value);
				node.node(nodes).node("type").set(value.getClass().getTypeName());
			}
		}

		private Map<List<String>, Object> findValue(ConfigurationNode node, List<String> path) {
			List<String> newPath = new ArrayList<>(path);
			Map<List<String>, Object> newMap = new HashMap<>();
			newPath.add(node.key().toString());
			if (node.node("type").isNull()) {
				for (ConfigurationNode child : node.childrenList()) {
					Map<List<String>, Object> returnedMap = findValue(child, newPath);
					newMap.putAll(returnedMap);
				}
				return newMap;
			}
			String type = node.node("type").getString();
			Class<?> clazz;
			try {
				clazz = Class.forName(type);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			try {
				Object value = node.node("value").get(clazz);
				newMap.put(newPath, value);
				return newMap;
			} catch (SerializationException e) {
				throw new RuntimeException(e);
			}
		}
	};

}
