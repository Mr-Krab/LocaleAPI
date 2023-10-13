package sawfowl.localeapi.apiclasses;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;

public class YamlLocale extends AbstractLocale {

	private YamlConfigurationLoader configLoader;
	private CommentedConfigurationNode localeNode;
	public YamlLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		super(localeService, logger, path, pluginID, locale);
		configLoader = YamlConfigurationLoader.builder().defaultOptions(localeService.getConfigurationOptions()).path(this.path).nodeStyle(NodeStyle.BLOCK).build();
		reload();
	}

	@Override
	ConfigTypes getType() {
		return ConfigTypes.YAML;
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
		} catch (ConfigurateException e) {
			logger.error(e.getLocalizedMessage());
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