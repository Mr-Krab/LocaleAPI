package sawfowl.localeapi.apiclasses;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;

public class HoconLocale extends AbstractLocale {

	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private CommentedConfigurationNode localeNode;
	public HoconLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		super(localeService, logger, path, pluginID, locale);
		configLoader = HoconConfigurationLoader.builder().emitJsonCompatible(true).emitComments(false).defaultOptions(SerializeOptions.CONFIGURATIO_NOPTIONS).path(this.path).build();
		reload();
	}

	@Override
	ConfigTypes getType() {
		return ConfigTypes.HOCON;
	}

	@Override
	void setComment(String comment, Object... path) {
		getLocaleNode(path).comment(comment);
	}

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
	public CommentedConfigurationNode getLocaleRootNode() {
		return localeNode;
	}

	@Override
	public CommentedConfigurationNode getLocaleNode(Object... path) {
		return localeNode.node(path);
	}

}