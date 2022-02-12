package sawfowl.localeapi.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.configurate.ConfigurationOptions;

import sawfowl.localeapi.utils.AbstractLocaleUtil;

public interface LocaleService {

	/**
	 * The method returns options for the Sponge configuration files.<br>
	 * These options disable serialization of objects not marked by the <b>@Setting</b> annotation.
	 * 
	 */
	public ConfigurationOptions getConfigurationOptions();

	/**
	 * List of all localizations of the game.
	 * 
	 */
	public List<Locale> getLocalesList();

	/**
	 * The default location. Used in a localization map.
	 */
	public Locale getDefaultLocale();

	/**
	 * Getting a map of plugin localizations with Sponge config files. <br>
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public Map<Locale, AbstractLocaleUtil> getPluginLocales(Object plugin);

	/**
	 * Getting a map of plugin localizations with Sponge config files. <br>
	 * 
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, AbstractLocaleUtil> getPluginLocales(String pluginID);

	/**
	 * Get plugin localization with Sponge config file. <br> <br>
	 * Note that getting the ConfigurationNode object in the <b>'*.properties'</b> configuration is not possible.<br>
	 * Methods for getting this object will return null.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @param locale - Selected localization. If the selected localization is not found, the default localization will be returned.
	 */
	public AbstractLocaleUtil getOrDefaultLocale(Object plugin, Locale locale);

	/**
	 * Get plugin localization with Sponge config file. <br> <br>
	 * Note that getting the ConfigurationNode object in the <b>'*.properties'</b> configuration is not possible.<br>
	 * Methods for getting this object will return null.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param locale - Selected localization. If the selected localization is not found, the default localization will be returned.
	 */
	public AbstractLocaleUtil getOrDefaultLocale(String pluginID, Locale locale);

	/**
	 * Save plugin locales from assets.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public void saveAssetLocales(Object plugin);

	/**
	 * Save plugin locales from assets.
	 * 
	 * @param pluginID - Plugin ID.
	 */
	public void saveAssetLocales(String pluginID);

	/**
	 * Creating a plugin localization file.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 */
	public AbstractLocaleUtil createPluginLocale(Object plugin, ConfigTypes configType, Locale locale);

	/**
	 * Creating a plugin localization file.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 */
	public AbstractLocaleUtil createPluginLocale(String pluginID, ConfigTypes configType, Locale locale);

	/**
	 * Load plugin locales if exists.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 * @return 
	 */
	public boolean localesExist(Object plugin);

	/**
	 * Load plugin locales if exists.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 * @return 
	 */
	public boolean localesExist(String pluginID);

}
