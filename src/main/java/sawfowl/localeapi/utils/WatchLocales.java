package sawfowl.localeapi.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPI;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.api.EnumLocales;
import sawfowl.localeapi.api.FileUtils;
import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.PluginLocale;
import sawfowl.localeapi.api.event.LocaleEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class WatchLocales {

	private LocaleService localeService;
	private Logger logger;
	private Path configDirectory;
	private WatchService watchService;
	private Cause cause;
	private PluginContainer pluginContainer;
	private Set<UpdateInfo> updateInfo = new HashSet<UpdateInfo>();
	private Set<String> registered = new HashSet<String>();
	private boolean freeze = true;
	public WatchLocales(LocaleService localeService, Logger logger, Path path) {
		this.localeService = localeService;
		this.logger = logger;
		pluginContainer = LocaleAPI.getPluginContainer();
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, pluginContainer).build(), pluginContainer);
		configDirectory = path;
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		};
		Sponge.asyncScheduler().submit(Task.builder().plugin(pluginContainer).interval(1, TimeUnit.SECONDS).execute(() -> {
			updateInfo.forEach(update -> {
				if(update.update) {
					if(update.create) {
						create(update);
					} else reload(update);
				} else update.update = true;
			});
			updateInfo.removeIf(update -> update.updated);
		}).build());
	}

	public void addPluginData(String pluginID) {
		if(!configDirectory.resolve(pluginID).toFile().exists()) {
			if(registered.contains(pluginID)) registered.remove(pluginID);
			configDirectory.resolve(pluginID).toFile().mkdir();
		}
		if(registered.contains(pluginID)) return;
		registered.add(pluginID);
		try {
			configDirectory.resolve(pluginID).register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
			logger.info("[FileWatcher] Added tracking of localization files for plugin: " + pluginID);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	public void unfreeze() {
		freeze = false;
	}

	void startWatch() {
		if(freeze) return;
		WatchKey key;
		try {
			//Thread.sleep(5000);
			while(watchService != null && (key = watchService.take()) != null) {
				for(WatchEvent<?> event : key.pollEvents()) {
					String pluginID = key.watchable().toString().replace(configDirectory.toString() + File.separator, "");
					String fileName = event.context().toString();
					if(event.kind() == ENTRY_CREATE) {
						checkOnCreate(pluginID, fileName);
					} else if(event.kind() == ENTRY_MODIFY) {
						checkOnModify(pluginID, fileName);
					}
				}
				if(!key.reset()) {
					break;
				}
			}
		} catch (InterruptedException e) {
			if(watchService != null) logger.error(e.getLocalizedMessage());
		}
	}

	void stopWatch() {
		freeze = true;
		watchService = null;
	}

	private void checkOnCreate(String pluginID, String fileName) {
		if(fileName.contains("tmp")) return;
		long oldTime = System.currentTimeMillis();
		updateInfo.removeIf(info -> info.plugin.equals(pluginID) && info.fileName.equals(fileName));
		String configTypeName = FileUtils.getExtension(fileName);
		ConfigTypes configType = ConfigTypes.find("." + configTypeName);
		if(configType == ConfigTypes.UNKNOWN) return;
		String localeName = fileName.split("." + configTypeName)[0];
		if(configType == ConfigTypes.UNKNOWN || !EnumLocales.exist(localeName)) return;
		Locale locale = EnumLocales.find(localeName);
		if(configType == ConfigTypes.PROPERTIES && localeService.getDefaultReference(pluginID) != null) {
			logger.warn("[FileWatcher] The \"" + fileName + "\" localization for the \"" + pluginID + "\" plugin cannot be loaded, because the plugin uses \"Reference\" localization classes.");
			return;
		}
		int localesSizeBefore = localeService.getPluginLocales(pluginID).size();
		if(localesSizeBefore == 0) return;
		PluginLocale localeconfig = localeService.createPluginLocale(pluginID, configType, locale);
		updateInfo.add(new UpdateInfo(pluginID, fileName, oldTime, localeconfig, locale, true));
	}

	private void checkOnModify(String pluginID, String fileName) {
		if(fileName.contains("tmp")) return;
		long oldTime = System.currentTimeMillis();
		updateInfo.removeIf(info -> info.plugin.equals(pluginID) && info.fileName.equals(fileName));
		String configTypeName = FileUtils.getExtension(fileName);
		ConfigTypes configType = ConfigTypes.find("." + configTypeName);
		String localeName = fileName.split("." + configTypeName)[0];
		if(configType == ConfigTypes.UNKNOWN || !EnumLocales.exist(localeName)) return;
		Locale locale = EnumLocales.find(localeName);
		PluginLocale localeconfig = localeService.getOrDefaultLocale(pluginID, locale);
		updateInfo.add(new UpdateInfo(pluginID, fileName, oldTime, localeconfig, locale, false));
	}

	private void create(UpdateInfo info) {
		long oldTime = System.currentTimeMillis();
		postEvent(create(info.plugin, info.locale, info.localeconfig, FileUtils.getExtension(info.fileName)));
		if(localeService.getPluginLocales(info.plugin).size() > 0) {
			logger.info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
		} else {
			logger.error("[FileWatcher] Error loading file \"" + info.fileName + "\" for plugin \"" + info.plugin + "\"!");
		}
		info.updated = true;
	}

	private void reload(UpdateInfo info) {
		logger.info("[FileWatcher] Locale file \"" + info.fileName + "\" for plugin \"" + info.plugin + "\" has been changed! Reloading...");
		info.localeconfig.reload();
		postEvent(reload(info.plugin, info.locale, info.localeconfig));
		info.updated = true;
	}

	private void postEvent(LocaleEvent localeEvent) {
		Sponge.eventManager().post(localeEvent);
	}

	private LocaleEvent.Create create(String plugin, Locale locale, PluginLocale pluginLocale, String configType) {
		return new LocaleEvent.Create() {

			@Override
			public String plugin() {
				return plugin;
			}

			@Override
			public Locale getLocale() {
				return locale;
			}

			@Override
			public PluginLocale getLocaleConfig() {
				return pluginLocale;
			}

			@Override
			public Cause cause() {
				return cause;
			}

			@Override
			public String configType() {
				return configType;
			}};
	}

	private LocaleEvent.Reload reload(String plugin, Locale locale, PluginLocale pluginLocale) {
		return new LocaleEvent.Reload() {
			
			@Override
			public Cause cause() {
				return cause;
			}
			
			@Override
			public String plugin() {
				return plugin;
			}
			
			@Override
			public PluginLocale getLocaleConfig() {
				return pluginLocale;
			}
			
			@Override
			public Locale getLocale() {
				return locale;
			}
		};
	}

	private class UpdateInfo {

		String plugin;
		String fileName;
		long time;
		PluginLocale localeconfig;
		Locale locale;
		boolean create;
		boolean update = false;
		boolean updated = false;
		public UpdateInfo(String plugin, String fileName, long time, PluginLocale localeconfig, Locale locale, boolean create) {
			this.plugin = plugin;
			this.fileName = fileName;
			this.time = time;
			this.localeconfig = localeconfig;
			this.locale = locale;
			this.create = create;
		}

		@Override
		public int hashCode() {
			return Objects.hash(fileName, plugin, time);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || getClass() != obj.getClass()) return false;
			if (this == obj) return true;
			UpdateInfo other = (UpdateInfo) obj;
			return Objects.equals(fileName, other.fileName) && Objects.equals(plugin, other.plugin) && time == other.time;
		}

	}

}
