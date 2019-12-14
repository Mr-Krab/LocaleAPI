package mr_krab.localeapi.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.translation.locale.Locales;

import mr_krab.localeapi.LocaleAPIMain;

public class LocaleAPI {

	private LocaleAPIMain plugin;
	private Map<String, Map<Locale, LocaleUtil> > pluginLocales;
	private Map<String, Map<Locale, HoconLocaleUtil> > pluginHoconLocales;
	private Map<String, Map<Locale, JsonLocaleUtil> > pluginJsonLocales;
	private Map<String, Map<Locale, YamlLocaleUtil> > pluginYamlLocales;
	private List<Locale> locales;
	private Locale defaultLocale;

	public LocaleAPI(LocaleAPIMain plugin) {
		this.plugin = plugin;
		pluginLocales = new HashMap<String, Map<Locale, LocaleUtil> >();
		pluginHoconLocales = new HashMap<String, Map<Locale, HoconLocaleUtil>>();
		pluginJsonLocales = new HashMap<String, Map<Locale,JsonLocaleUtil>>();
		pluginYamlLocales = new HashMap<String, Map<Locale,YamlLocaleUtil>>();
		locales = new ArrayList<Locale>();
		defaultLocale = Locale.forLanguageTag(plugin.getRootNode().getNode("DefaultLocale").getString("en-US"));
		generateLocales();
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
		return defaultLocale;
	}

	/**
	 * Generating a list of localizations.
	 */
	private void generateLocales() {
		for(Field field : Locales.class.getFields()) {
			try {
				if(field.get(field.getType()) instanceof Locale) {
					Locale locale = (Locale) field.get(field.getType());
					locales.add(locale);
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				plugin.getLogger().error("Error get locales {}", e.getMessage());
			}
		}
	}

	/**
	 * Getting ID from @Plugin annotation of the plugin main class.
	 * @param instancePlugin - the main class of the plugin.
	 * @return id of the plugin in String format.
	 */
	private String getPluginID(Object instancePlugin) {
		String pluginiD = "";
		for (Annotation annotation : instancePlugin.getClass().getDeclaredAnnotations()) {
			if(annotation instanceof Plugin) {
				pluginiD = ((Plugin) annotation).id();
			}
		}
		return pluginiD;
	}

	/**
	 * Saving all localization files(extension .properties) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void saveLocales(Object instancePlugin) {
		Map<Locale, LocaleUtil> locales = new HashMap<Locale, LocaleUtil>();
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(localeExist(pluginID, locale)) {
				locales.put(locale, new LocaleUtil(plugin, pluginID, locale.toLanguageTag()));
				try {
					locales.get(locale).init();
				} catch (IOException e) {
					plugin.getLogger().error("Error save locale {}", e.getMessage());
				}
			}
		}
		pluginLocales.put(pluginID, locales);
	}

	/**
	 * Saving all localization files(extension .properties) of the plugin.
	 * @param luginID - Plugin ID.
	 */
	public void saveLocales(String pluginID) {
		Map<Locale, LocaleUtil> locales = new HashMap<Locale, LocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(localeExist(pluginID, locale)) {
				locales.put(locale, new LocaleUtil(plugin, pluginID, locale.toLanguageTag()));
				try {
					locales.get(locale).init();
				} catch (IOException e) {
					plugin.getLogger().error("Error save locale {}", e.getMessage());
				}
			}
		}
		pluginLocales.put(pluginID, locales);
	}

	/**
	 * Reload all localization files(extension .properties) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void reloadLocales(Object instancePlugin) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginLocales.remove(pluginID);
		saveLocales(pluginID);
	}

	/**
	 * Reload all localization files(extension .properties) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void reloadLocales(String pluginID) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginLocales.remove(pluginID);
		saveLocales(pluginID);
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
	public boolean localeLoaded(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginLocales.containsKey(pluginID)) {
			return pluginLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 * @return
	 */
	public boolean localeLoaded(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginLocales.containsKey(pluginID)) {
			return pluginLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Checking for the presence of a localization file.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
    public boolean localeExist(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".properties");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".properties");
			return assetOpt.isPresent();
		}
		return false;
    }

    /**
     * Checking for the presence of a localization file.
     * @param pluginID - Plugin ID.
     * @param locale - localization.
     */
    public boolean localeExist(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".properties");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".properties");
			return assetOpt.isPresent();
		}
		return false;
    }

	/**
	 * Getting the plugin localization map.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public Map<Locale, LocaleUtil> getLocalesMap(Object instancePlugin) {
		return pluginLocales.get(getPluginID(instancePlugin));	
	}

	/**
	 * Getting the plugin localization map.
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, LocaleUtil> getLocalesMap(String pluginID) {
		return pluginLocales.get(pluginID);	
	}

	/**
	 * Getting the localization.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public LocaleUtil getLocale(Object instancePlugin, Locale locale) {
		return getLocalesMap(instancePlugin).get(locale);
	}

	/**
	 * Getting the localization.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public LocaleUtil getLocale(String pluginID, Locale locale) {
		return getLocalesMap(pluginID).get(locale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public LocaleUtil getOrDefaultLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getLocalesMap(pluginID).get(locale);
		}
		return getLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public LocaleUtil getOrDefaultLocale(String pluginID, Locale locale) {
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getLocalesMap(pluginID).get(locale);
		}
		return getLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public LocaleUtil getDefaultLocale(Object instancePlugin) {
		return getLocalesMap(instancePlugin).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param pluginID - Plugin ID.
	 */
	public LocaleUtil getDefaultLocale(String pluginID) {
		return getLocalesMap(pluginID).get(defaultLocale);
	}
	
	
// HOCON


	/**
	 * Saving all localization files(extension .conf) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void saveHoconLocales(Object instancePlugin) {
		Map<Locale, HoconLocaleUtil> locales = new HashMap<Locale, HoconLocaleUtil>();
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(hoconLocaleExist(pluginID, locale)) {
				locales.put(locale, new HoconLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
			}
		}
		pluginHoconLocales.put(pluginID, locales);
	}

	/**
	 * Saving all localization files(extension .conf) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void saveHoconLocales(String pluginID) {
		Map<Locale, HoconLocaleUtil> locales = new HashMap<Locale, HoconLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(hoconLocaleExist(pluginID, locale)) {
				locales.put(locale, new HoconLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
			}
		}
		pluginHoconLocales.put(pluginID, locales);
	}

	/**
	 * 
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public void createHoconLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf").toFile().exists()) {
			plugin.getLogger().warn("The localization file(" + locale.toLanguageTag() + ".conf) already exists.");
	        return;
		}
		Map<Locale, HoconLocaleUtil> locales = new HashMap<Locale, HoconLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		locales.put(locale, new HoconLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
		locales.get(locale).saveLocaleNode();
		pluginHoconLocales.put(pluginID, locales);
	}

	/**
	 * 
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public void createHoconLocale(String pluginID, Locale locale) {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale.toLanguageTag() + ".conf").toFile().exists()) {
			plugin.getLogger().warn("The localization file(" + locale.toLanguageTag() + ".conf) already exists.");
	        return;
		}
		Map<Locale, HoconLocaleUtil> locales = new HashMap<Locale, HoconLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		locales.put(locale, new HoconLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
		locales.get(locale).saveLocaleNode();
		pluginHoconLocales.put(pluginID, locales);
	}

	/**
	 * Reload all localization files(extension .conf) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void reloadHoconLocales(Object instancePlugin) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginHoconLocales.remove(pluginID);
		saveHoconLocales(pluginID);
	}

	/**
	 * Reload all localization files(extension .conf) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void reloadHoconLocales(String pluginID) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginHoconLocales.remove(pluginID);
		saveHoconLocales(pluginID);
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
	public boolean hoconLocaleLoaded(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginHoconLocales.containsKey(pluginID)) {
			return pluginHoconLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 * @return
	 */
	public boolean hoconLocaleLoaded(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginHoconLocales.containsKey(pluginID)) {
			return pluginHoconLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Checking for the presence of a localization file.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
    public boolean hoconLocaleExist(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".conf");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".conf");
			return assetOpt.isPresent();
		}
		return false;
    }

    /**
     * Checking for the presence of a localization file.
     * @param pluginID - Plugin ID.
     * @param locale - localization.
     */
    public boolean hoconLocaleExist(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".conf");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".conf");
			return assetOpt.isPresent();
		}
		return false;
    }

	/**
	 * Getting the plugin localization map.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public Map<Locale, HoconLocaleUtil> getHoconLocalesMap(Object instancePlugin) {
		return pluginHoconLocales.get(getPluginID(instancePlugin));	
	}

	/**
	 * Getting the plugin localization map.
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, HoconLocaleUtil> getHoconLocalesMap(String pluginID) {
		return pluginHoconLocales.get(pluginID);	
	}

	/**
	 * Getting the localization.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public HoconLocaleUtil getHoconLocale(Object instancePlugin, Locale locale) {
		return getHoconLocalesMap(getPluginID(instancePlugin)).get(locale);
	}

	/**
	 * Getting the localization.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public HoconLocaleUtil getHoconLocale(String pluginID, Locale locale) {
		return getHoconLocalesMap(pluginID).get(locale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public HoconLocaleUtil getOrDefaultHoconLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getHoconLocalesMap(pluginID).get(locale);
		}
		return getHoconLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public HoconLocaleUtil getOrDefaultHoconLocale(String pluginID, Locale locale) {
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getHoconLocalesMap(pluginID).get(locale);
		}
		return getHoconLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public HoconLocaleUtil getDefaultHoconLocale(Object instancePlugin) {
		return getHoconLocalesMap(getPluginID(instancePlugin)).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param pluginID - Plugin ID.
	 */
	public HoconLocaleUtil getDefaultHoconLocale(String pluginID) {
		return getHoconLocalesMap(pluginID).get(defaultLocale);
	}


// JSON


	/**
	 * Saving all localization files(extension .json) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void saveJsonLocales(Object instancePlugin) {
		Map<Locale, JsonLocaleUtil> locales = new HashMap<Locale, JsonLocaleUtil>();
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(jsonLocaleExist(pluginID, locale)) {
				locales.put(locale, new JsonLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
			}
		}
		pluginJsonLocales.put(pluginID, locales);
	}

	/**
	 * Saving all localization files(extension .conf) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void saveJsonLocales(String pluginID) {
		Map<Locale, JsonLocaleUtil> locales = new HashMap<Locale, JsonLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(jsonLocaleExist(pluginID, locale)) {
				locales.put(locale, new JsonLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
			}
		}
		pluginJsonLocales.put(pluginID, locales);
	}

	/**
	 * 
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public void createJsonLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale.toLanguageTag() + ".json").toFile().exists()) {
			plugin.getLogger().warn("The localization file(" + locale.toLanguageTag() + ".json) already exists.");
	        return;
		}
		Map<Locale, JsonLocaleUtil> locales = new HashMap<Locale, JsonLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		locales.put(locale, new JsonLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
		locales.get(locale).saveLocaleNode();
		pluginJsonLocales.put(pluginID, locales);
	}

	/**
	 * 
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public void createJsonLocale(String pluginID, Locale locale) {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale.toLanguageTag() + ".json").toFile().exists()) {
			plugin.getLogger().warn("The localization file(" + locale.toLanguageTag() + ".json) already exists.");
	        return;
		}
		Map<Locale, JsonLocaleUtil> locales = new HashMap<Locale, JsonLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		locales.put(locale, new JsonLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
		locales.get(locale).saveLocaleNode();
		pluginJsonLocales.put(pluginID, locales);
	}

	/**
	 * Reload all localization files(extension .conf) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void reloadJsonLocales(Object instancePlugin) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginJsonLocales.remove(pluginID);
		saveJsonLocales(pluginID);
	}

	/**
	 * Reload all localization files(extension .conf) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void reloadJsonLocales(String pluginID) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginJsonLocales.remove(pluginID);
		saveJsonLocales(pluginID);
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
	public boolean jsonLocaleLoaded(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginJsonLocales.containsKey(pluginID)) {
			return pluginJsonLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 * @return
	 */
	public boolean jsonLocaleLoaded(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginJsonLocales.containsKey(pluginID)) {
			return pluginJsonLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Checking for the presence of a localization file.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
    public boolean jsonLocaleExist(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".json");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".json");
			return assetOpt.isPresent();
		}
		return false;
    }

    /**
     * Checking for the presence of a localization file.
     * @param pluginID - Plugin ID.
     * @param locale - localization.
     */
    public boolean jsonLocaleExist(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".json");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".json");
			return assetOpt.isPresent();
		}
		return false;
    }

	/**
	 * Getting the plugin localization map.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public Map<Locale, JsonLocaleUtil> getJsonLocalesMap(Object instancePlugin) {
		return pluginJsonLocales.get(getPluginID(instancePlugin));	
	}

	/**
	 * Getting the plugin localization map.
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, JsonLocaleUtil> getJsonLocalesMap(String pluginID) {
		return pluginJsonLocales.get(pluginID);	
	}

	/**
	 * Getting the localization.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public JsonLocaleUtil getJsonLocale(Object instancePlugin, Locale locale) {
		return getJsonLocalesMap(getPluginID(instancePlugin)).get(locale);
	}

	/**
	 * Getting the localization.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public JsonLocaleUtil getJsonLocale(String pluginID, Locale locale) {
		return getJsonLocalesMap(pluginID).get(locale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public JsonLocaleUtil getOrDefaultJsonLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getJsonLocalesMap(pluginID).get(locale);
		}
		return getJsonLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public JsonLocaleUtil getOrDefaultJsonLocale(String pluginID, Locale locale) {
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getJsonLocalesMap(pluginID).get(locale);
		}
		return getJsonLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public JsonLocaleUtil getDefaultJsonLocale(Object instancePlugin) {
		return getJsonLocalesMap(getPluginID(instancePlugin)).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param pluginID - Plugin ID.
	 */
	public JsonLocaleUtil getDefaultJsonLocale(String pluginID) {
		return getJsonLocalesMap(pluginID).get(defaultLocale);
	}



// YAML



	/**
	 * Saving all localization files(extension .yml) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void saveYamlLocales(Object instancePlugin) {
		Map<Locale, YamlLocaleUtil> locales = new HashMap<Locale, YamlLocaleUtil>();
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(yamlLocaleExist(pluginID, locale)) {
				locales.put(locale, new YamlLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
			}
		}
		pluginYamlLocales.put(pluginID, locales);
	}

	/**
	 * Saving all localization files(extension .yml) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void saveYamlLocales(String pluginID) {
		Map<Locale, YamlLocaleUtil> locales = new HashMap<Locale, YamlLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		for(Locale locale : this.locales) {
			if(yamlLocaleExist(pluginID, locale)) {
				locales.put(locale, new YamlLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
			}
		}
		pluginYamlLocales.put(pluginID, locales);
	}

	/**
	 * 
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public void createYamlLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml").toFile().exists()) {
			plugin.getLogger().warn("The localization file(" + locale.toLanguageTag() + ".yml) already exists.");
	        return;
		}
		Map<Locale, YamlLocaleUtil> locales = new HashMap<Locale, YamlLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		locales.put(locale, new YamlLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
		locales.get(locale).saveLocaleNode();
		pluginYamlLocales.put(pluginID, locales);
	}

	/**
	 * 
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public void createYamlLocale(String pluginID, Locale locale) {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale.toLanguageTag() + ".yml").toFile().exists()) {
			plugin.getLogger().warn("The localization file(" + locale.toLanguageTag() + ".yml) already exists.");
	        return;
		}
		Map<Locale, YamlLocaleUtil> locales = new HashMap<Locale, YamlLocaleUtil>();
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		File localePath = new File(plugin.getConfigDir() + File.separator + pluginID);
		if(!localePath.exists()) {
			localePath.mkdir();
		}
		locales.put(locale, new YamlLocaleUtil(plugin, pluginID, locale.toLanguageTag()));
		locales.get(locale).saveLocaleNode();
		pluginYamlLocales.put(pluginID, locales);
	}

	/**
	 * Reload all localization files(extension .conf) of the plugin.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public void reloadYamlLocales(Object instancePlugin) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginYamlLocales.remove(pluginID);
		saveYamlLocales(pluginID);
	}

	/**
	 * Reload all localization files(extension .conf) of the plugin.
	 * @param pluginID - Plugin ID.
	 */
	public void reloadYamlLocales(String pluginID) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return;
		}
		pluginYamlLocales.remove(pluginID);
		saveYamlLocales(pluginID);
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
	public boolean yamlLocaleLoaded(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginYamlLocales.containsKey(pluginID)) {
			return pluginYamlLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Check if localization is loaded into memory.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 * @return
	 */
	public boolean yamlLocaleLoaded(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		if(pluginYamlLocales.containsKey(pluginID)) {
			return pluginYamlLocales.get(pluginID).containsKey(locale);
		}
		return false;
	}

	/**
	 * Checking for the presence of a localization file.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 * @return
	 */
    public boolean yamlLocaleExist(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".yml");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".yml");
			return assetOpt.isPresent();
		}
		return false;
    }

    /**
     * Checking for the presence of a localization file.
     * @param pluginID - Plugin ID.
     * @param locale - localization.
     */
    public boolean yamlLocaleExist(String pluginID, Locale locale) {
		if(pluginID.isEmpty()) {
			plugin.getLogger().error("Plugin can not be null or noname(\"\")");
			return false;
		}
		File file = new File(plugin.getConfigFile() + File.separator + pluginID + File.separator + locale.toLanguageTag() + ".yml");
		if(file.exists()) {
			return true;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale.toLanguageTag() + ".yml");
			return assetOpt.isPresent();
		}
		return false;
    }

	/**
	 * Getting the plugin localization map.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public Map<Locale, YamlLocaleUtil> getYamlLocalesMap(Object instancePlugin) {
		return pluginYamlLocales.get(getPluginID(instancePlugin));	
	}

	/**
	 * Getting the plugin localization map.
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, YamlLocaleUtil> getYamlLocalesMap(String pluginID) {
		return pluginYamlLocales.get(pluginID);	
	}

	/**
	 * Getting the localization.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public YamlLocaleUtil getYamlLocale(Object instancePlugin, Locale locale) {
		return getYamlLocalesMap(getPluginID(instancePlugin)).get(locale);
	}

	/**
	 * Getting the localization.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public YamlLocaleUtil getYamlLocale(String pluginID, Locale locale) {
		return getYamlLocalesMap(pluginID).get(locale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param instancePlugin - the main class of the plugin.
	 * @param locale - localization.
	 */
	public YamlLocaleUtil getOrDefaultYamlLocale(Object instancePlugin, Locale locale) {
		String pluginID = getPluginID(instancePlugin);
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getYamlLocalesMap(pluginID).get(locale);
		}
		return getYamlLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the localization. If there is no necessary localization, then default localization will be called.
	 * @param pluginID - Plugin ID.
	 * @param locale - localization.
	 */
	public YamlLocaleUtil getOrDefaultYamlLocale(String pluginID, Locale locale) {
		if(getLocalesMap(pluginID).containsKey(locale)) {
			return getYamlLocalesMap(pluginID).get(locale);
		}
		return getYamlLocalesMap(pluginID).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param instancePlugin - the main class of the plugin.
	 */
	public YamlLocaleUtil getDefaultYamlLocale(Object instancePlugin) {
		return getYamlLocalesMap(getPluginID(instancePlugin)).get(defaultLocale);
	}

	/**
	 * Getting the default localization.
	 * @param pluginID - Plugin ID.
	 */
	public YamlLocaleUtil getDefaultYamlLocale(String pluginID) {
		return getYamlLocalesMap(pluginID).get(defaultLocale);
	}

}
