package sawfowl.localeapi.apiclasses;

import java.io.IOException;
import java.nio.file.Path;

import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.reference.ConfigurationReference;
import org.spongepowered.configurate.reference.ValueReference;
import org.spongepowered.configurate.serialize.SerializationException;

import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleReference;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.Logger;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

public class JsonLocale extends AbstractLocale {

	private GsonConfigurationLoader configLoader;
	private ValueReference<LocaleReference, BasicConfigurationNode> localeReference;
	private ConfigurationReference<BasicConfigurationNode> configurationReference;
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

	@SuppressWarnings("unchecked")
	@Override
	public void reload() {
		try {
			localeNode = configLoader.load();
			if(localeReference != null && configurationReference != null) localeReference = (ValueReference<LocaleReference, BasicConfigurationNode>) (configurationReference = configLoader.loadToReference()).referenceTo(localeReference.get().getClass());
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
	public ConfigurationNode getLocaleRootNode() {
		return localeReference == null ? localeNode : localeReference.node();
	}

	@Override
	public ConfigurationNode getLocaleNode(Object... path) {
		return getLocaleRootNode().node(path);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends LocaleReference> void setLocaleReference(Class<T> reference) throws SerializationException, ConfigurateException {
		if(reference == null) return;
		if(configLoader == null) configLoader = SerializeOptions.createJsonConfigurationLoader(localeService.getItemStackSerializerVariant(pluginID)).path(this.path).build();
		localeReference = (ValueReference<LocaleReference, BasicConfigurationNode>) (configurationReference = configLoader.loadToReference()).referenceTo(reference);
		if(localeNode != null && !localeNode.empty()) localeReference.node().from(localeNode);
		localeNode = localeReference.node();
		if(addIfNotExist(asReference(reference), new Object[] {})) configLoader.save(localeNode);
	}

	@Override
	public <T extends LocaleReference> void setLocaleReference(T reference) throws SerializationException, ConfigurateException {
		setLocaleReference(reference.getClass());
		localeReference.setAndSave(reference);
		localeNode = localeReference.node();
		if(addIfNotExist(reference, new Object[] {})) configLoader.save(localeNode);
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