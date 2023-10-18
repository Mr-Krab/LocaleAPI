package sawfowl.localeapi.api;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.spongepowered.plugin.PluginContainer;

public interface LocaleService {

	/*
	 * Getting the system locale.<br>
	 * If Sponge does not support your system locale, the default locale for Sponge will be selected.
	 */
	public Locale getSystemOrDefaultLocale();

	/**
	 * List of all localizations of the game.
	 * 
	 */
	public List<Locale> getLocalesList();

	/**
	 * The default locale. Used in a localization map.
	 */
	public Locale getDefaultLocale();

	/**
	 * Getting a map of plugin localizations with Sponge config files. <br>
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public Map<Locale, PluginLocale> getPluginLocales(PluginContainer plugin);

	/**
	 * Getting a map of plugin localizations with Sponge config files. <br>
	 * 
	 * @param pluginID - Plugin ID.
	 */
	public Map<Locale, PluginLocale> getPluginLocales(String pluginID);

	/**
	 * Get plugin localization with Sponge config file. <br> <br>
	 * Note that getting the ConfigurationNode object in the <b>'*.properties'</b> configuration is not possible.<br>
	 * Methods for getting this object will return null.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @param locale - Selected localization. If the selected localization is not found, the default localization will be returned.
	 */
	public PluginLocale getOrDefaultLocale(PluginContainer plugin, Locale locale);

	/**
	 * Get plugin localization with Sponge config file. <br> <br>
	 * Note that getting the ConfigurationNode object in the <b>'*.properties'</b> configuration is not possible.<br>
	 * Methods for getting this object will return null.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param locale - Selected localization. If the selected localization is not found, the default localization will be returned.
	 */
	public PluginLocale getOrDefaultLocale(String pluginID, Locale locale);

	/**
	 * Save plugin locales from assets.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 */
	public void saveAssetLocales(PluginContainer plugin);

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
	public PluginLocale createPluginLocale(PluginContainer plugin, ConfigTypes configType, Locale locale);

	/**
	 * Creating a plugin localization file.
	 * 
	 * @param pluginID - Plugin ID.
	 * @param configType - Selected config type. See enum class 'ConfigTypes'.
	 */
	public PluginLocale createPluginLocale(String pluginID, ConfigTypes configType, Locale locale);

	/**
	 * Load plugin locales if exists.
	 * 
	 * @param plugin - A class annotated with '@Plugin'.
	 * @return true if loaded.
	 */
	public boolean localesExist(PluginContainer plugin);

	/**
	 * Load plugin locales if exists.
	 * 
	 * @param pluginID - Plugin ID.
	 * @return true if loaded.
	 */
	public boolean localesExist(String pluginID);

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>1</b> - All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.<br>
	 * <b>2</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>3</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public void setItemStackSerializerVariant(PluginContainer container, int variant) throws Exception ;

	/**
	 * Getting the type number of the serialization type of an items.
	 */
	public int getItemStackSerializerVariant(PluginContainer container);

	/**
	 * Getting the type number of the serialization type of an items.
	 */
	public int getItemStackSerializerVariant(String pluginID);

}
