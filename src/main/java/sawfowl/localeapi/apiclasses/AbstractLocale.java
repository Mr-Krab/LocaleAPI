package sawfowl.localeapi.apiclasses;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.locale.Locales;
import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;

public abstract class AbstractLocale implements PluginLocale {


	final LocaleService localeService;
	final Logger logger;
	final String pluginID;
	final boolean thisIsDefault;
	final Path path;
	final String locale;
	public AbstractLocale(LocaleService localeService, Logger logger, Path path, String pluginID, String locale) {
		this.localeService = localeService;
		this.logger = logger;
		this.path = path.resolve(pluginID + File.separator + locale + getType().toString());
		this.pluginID = pluginID;
		this.locale = locale;
		thisIsDefault = locale.equals(Locales.DEFAULT.toLanguageTag());
	}

	abstract ConfigTypes getType();

	abstract void setComment(String comment, Object... path);

	@Override
	public Component getComponent(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return TextUtils.deserializeLegacy("&cPath " + getPathName(path) + " not exist!");
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getComponent(path) : TextUtils.deserialize(getString(path));
	}

	@Override
	public Text getText(Object... path) {
		return Text.of(getString(path));
	}

	@Override
	public List<Text> getTexts(Object... path) {
		return getListStrings(path).stream().map(Text::of).toList();
	}

	@Override
	public List<Component> getListComponents(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return Arrays.asList(TextUtils.deserializeLegacy("&cPath " + getPathName(path) + " not exist!"));
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListComponents(path) : getListStrings(path).stream().map(TextUtils::deserialize).toList();
	}

	@Override
	public String getString(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return "Path " + getPathName(path) + " not exist!";
		return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getString(path) : getLocaleNode(path).getString();
	}

	@Override
	public List<String> getListStrings(Object... path) {
		if(thisIsDefault && getLocaleNode(path).virtual()) return Arrays.asList("Path " + getPathName(path) + " not exist!");
		try {
			return getLocaleNode(path).virtual() && !thisIsDefault ? getDefaultLocale().getListStrings(path) : getLocaleNode(path).getList(String.class);
		} catch (SerializationException e) {
			logger.error(e.getLocalizedMessage());
		}
		return Arrays.asList("Error getting list of Strings " + getPathName(path));
	}

	@Override
	public boolean checkComponent(boolean json, Component component, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				if(json) {
					getLocaleNode(path).set(TextUtils.serializeJson(component));
				} else getLocaleNode(path).set(TextUtils.serializeLegacy(component));
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListComponents(boolean json, List<Component> components, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				if(json) {
					getLocaleNode(path).setList(String.class, components.stream().map(TextUtils::serializeJson).toList());
				} else getLocaleNode(path).setList(String.class, components.stream().map(TextUtils::serializeLegacy).toList());
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkString(String string, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				getLocaleNode(path).set(string);
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	@Override
	public boolean checkListStrings(List<String> strings, String comment, Object... path) {
		if(getLocaleNode(path).empty()) {
			try {
				getLocaleNode(path).setList(String.class, strings);
				if(comment != null) setComment(comment, path);
				return true;
			} catch (SerializationException e) {
				logger.error(e.getLocalizedMessage());
			}
		}
		return false;
	}

	protected void freezeWatcher() {
		getUpdated().put(pluginID + locale, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
		Sponge.asyncScheduler().submit(Task.builder().plugin(LocaleAPI.getPluginContainer()).delay(3, TimeUnit.SECONDS).execute(() -> {
			if(getUpdated().containsKey(pluginID + locale)) getUpdated().remove(pluginID + locale);
		}).build());
	}

	protected Map<String, Long> getUpdated() {
		return ((LocaleAPI) LocaleAPI.getPluginContainer().instance()).getUpdated();
	}

	protected PluginLocale getDefaultLocale() {
		return localeService.getPluginLocales(pluginID).get(Locales.DEFAULT);
	}

	protected String getPathName(Object... path) {
		/*String name = "[";
		int pathSize = path.length;
		for(Object object : path) {
			if(pathSize > 1) {
				name = name + object + ", ";
				pathSize--;
			} else {
				name = name + object;
			}
		}*/
		return "[" + String.join(", ", Stream.of(path).toArray(String[]::new)) + "]";
	}

}