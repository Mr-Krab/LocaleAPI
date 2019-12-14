package mr_krab.localeapi.utils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.plugin.PluginContainer;

import mr_krab.localeapi.LocaleAPIMain;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

public class HoconLocaleUtil {

	private LocaleAPIMain plugin;
    private String pluginID;
    private String locale;
	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode localeNode;
	
	public HoconLocaleUtil(LocaleAPIMain plugin, String pluginID, String locale) {
		this.plugin = plugin;
		this.pluginID = pluginID;
		this.locale = locale;
		saveLocaleFile();
		configLoader = HoconConfigurationLoader.builder().setPath(plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".conf")).build();
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
	public CommentedConfigurationNode getLocaleNode() {
		return localeNode;
	}
	
	private void saveLocaleFile() {
		if(plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".conf").toFile().exists()) {
	        return;
		}
		Optional<PluginContainer> optPlugin = Sponge.getPluginManager().getPlugin(pluginID);
		if(optPlugin.isPresent()) {
			Optional<Asset> assetOpt = Sponge.getAssetManager().getAsset(optPlugin.get(), "lang/" + locale + ".conf");
			if(assetOpt.isPresent()) {
				Asset asset = assetOpt.get();
		        try {
					if(!plugin.getConfigDir().resolve(pluginID + File.separator + locale + ".conf").toFile().exists()) {
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
