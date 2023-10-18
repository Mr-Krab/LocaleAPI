package sawfowl.localeapi.apiclasses;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

public class JsonLocale extends AbstractLocale {

	private GsonConfigurationLoader configLoader;
	private ConfigurationNode localeNode;
	public JsonLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		super(localeService, logger, path, pluginID, locale);
		configLoader = SerializeOptions.createJsonConfigurationLoader(localeService.getItemStackSerializerVariant(pluginID)).path(this.path).build();
		reload();
	}

	@Override
	ConfigTypes getType() {
		return ConfigTypes.JSON;
	}

	@Override
	void setComment(String comment, Object... path) {}

	@Override
	public void reload() {
		try {
			localeNode = configLoader.load();
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void saveLocaleNode() {
		freezeWatcher();
		try {
			configLoader.save(localeNode);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public ConfigurationNode getLocaleRootNode() {
		return localeNode;
	}

	@Override
	public ConfigurationNode getLocaleNode(Object... path) {
		return localeNode.node(path);
	}

}