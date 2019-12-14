package mr_krab.localeapi.utils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import mr_krab.localeapi.LocaleAPIMain;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;

public class JsonLocaleUtil {

	private LocaleAPIMain plugin;
    private String pluginID;
    private String locale;
	private GsonConfigurationLoader configLoader;
	private ConfigurationNode localeNode;

	public JsonLocaleUtil(LocaleAPIMain plugin, String pluginID, String locale) {
		this.plugin = plugin;
		this.pluginID = pluginID;
		this.locale = locale;
		saveLocaleFile();
		configLoader = GsonConfigurationLoader.builder().setPath(plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".json")).build();
		try {
			localeNode = configLoader.load();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Saving the configuration file of the selected localization.
	 */
	public void saveLocaleNode() {
		try {
			configLoader.save(localeNode);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Getting the configuration file of the selected localization.
	 */
	public ConfigurationNode getLocaleNode() {
		return localeNode;
	}

	private void saveLocaleFile() {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".json").toFile().exists()) {
	        return;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale + ".json");
			if(assetOpt.isPresent()) {
				Asset asset = assetOpt.get();
		        try {
					if(!plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".json").toFile().exists()) {
						asset.copyToDirectory(plugin.getConfigDir().resolve(pluginID));
						plugin.getLogger().info("Locale config saved");
					}
				} catch (IOException e) {
					plugin. getLogger().error("Failed to save locale config! ", e);
				}
			}
		}
	}

}
