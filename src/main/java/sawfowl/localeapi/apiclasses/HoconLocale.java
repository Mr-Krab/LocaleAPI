package sawfowl.localeapi.apiclasses;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.serialize.SerializationException;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleReference;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

public class HoconLocale extends AbstractLocale {

	private ConfigurationLoader<CommentedConfigurationNode> configLoader;
	private ConfigurationReference<CommentedConfigurationNode> configurationReference;
	private ValueReference<LocaleReference, CommentedConfigurationNode> localeReference;
	private CommentedConfigurationNode localeNode;
	public HoconLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		super(localeService, logger, path, pluginID, locale);
		configLoader = SerializeOptions.createHoconConfigurationLoader(localeService.getItemStackSerializerVariant(pluginID)).path(this.path).build();
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

	@SuppressWarnings("unchecked")
	@Override
	public void reload() {
		try {
			localeNode = configLoader.load();
			if(localeReference != null && configurationReference != null) localeReference = (ValueReference<LocaleReference, CommentedConfigurationNode>) (configurationReference = configLoader.loadToReference()).referenceTo(localeReference.get().getClass());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public void saveLocaleNode() {
		try {
			if(localeReference != null && configurationReference != null) {
				configurationReference.save();
			} else configLoader.save(localeNode);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	public CommentedConfigurationNode getLocaleRootNode() {
		return localeReference == null ? localeNode : localeReference.node();
	}

	@Override
	public CommentedConfigurationNode getLocaleNode(Object... path) {
		return getLocaleRootNode().node(path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends LocaleReference> void setLocaleReference(Class<T> reference) throws SerializationException, ConfigurateException {
		if(reference == null) return;
		if(configLoader == null) configLoader = SerializeOptions.createHoconConfigurationLoader(localeService.getItemStackSerializerVariant(pluginID)).path(this.path).build();
		localeReference = (ValueReference<LocaleReference, CommentedConfigurationNode>) (configurationReference = configLoader.loadToReference()).referenceTo(reference);
		if(localeNode != null && !localeNode.empty()) localeReference.node().from(localeNode);
		localeNode = localeReference.node();
	}

	@Override
	public <T extends LocaleReference> void setLocaleReference(T reference) throws SerializationException, ConfigurateException {
		setLocaleReference(reference.getClass());
		localeReference.setAndSave(reference);
		localeNode = localeReference.node();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends LocaleReference> T asReference(Class<T> clazz) {
		if(localeReference == null)
			try {
				setLocaleReference(localeService.getDefaultReference(pluginID));
			} catch (ConfigurateException e) {
				e.printStackTrace();
			}
		return localeReference == null ? (thisIsDefault ? null : getDefaultLocale().asReference(clazz)) : (T) localeReference.get();
	}

}