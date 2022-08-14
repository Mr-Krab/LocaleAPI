package sawfowl.localeapi.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Cause;
import org.spongepowered.api.event.EventContext;
import org.spongepowered.api.event.EventContextKeys;
import org.spongepowered.api.event.impl.AbstractEvent;
import org.spongepowered.plugin.PluginContainer;

import sawfowl.localeapi.LocaleAPIMain;
import sawfowl.localeapi.event.LocaleEvent;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class WatchLocales {

	private LocaleService localeService;
	private Logger logger;
	private Path configDirectory;
	private WatchService watchService;
	private Map<String, Long> updated;
	private Cause cause;
	public WatchLocales(LocaleService localeService, Logger logger, Path path) {
		this.localeService = localeService;
		this.logger = logger;
		PluginContainer pluginContainer = LocaleAPIMain.getPluginContainer();
		cause = Cause.of(EventContext.builder().add(EventContextKeys.PLUGIN, pluginContainer).build(), pluginContainer);
		configDirectory = path;
		updated = new HashMap<String, Long>();
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		};
		checkAndClearUpdated();
	}

	void addPluginData(String pluginID) {
		if(updated.containsKey(pluginID)) return;
		try {
			updated.put(pluginID, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			configDirectory.resolve(pluginID).register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
			logger.info("[FileWatcher] Added tracking of localization files for plugin: " + pluginID);
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	void startWatch() {
		logger.info("[FileWatcher] File tracking has been launched.");
		WatchKey key;
		try {
			Thread.sleep(5000);
			while ((key = watchService.take()) != null) {
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
			logger.error(e.getLocalizedMessage());
		}
	}

	private void checkOnCreate(String pluginID, String fileName) {
		long oldTime = System.currentTimeMillis();
		for(Locale locale : localeService.getLocalesList()) {
			if(fileName.contains(locale.toLanguageTag())) {
				if(fileName.contains("tmp")) {
					return;
				}
				if((updated.containsKey(pluginID) && updated.get(pluginID) + 5 > oldTime) || localeService.getPluginLocales(pluginID).containsKey(locale)) return;
				for(ConfigTypes configType : ConfigTypes.values()) {
					String configTypeName = configType.toString();
					if(fileName.contains(configTypeName)) {
						int localesSizeBefore = localeService.getPluginLocales(pluginID).size();
						logger.info("[FileWatcher] New locale file found -> " + fileName + " for plugin \"" + pluginID + "\"! Loading...");
						localeService.createPluginLocale(pluginID, configType, locale);
						class CreateLocaleEvent extends AbstractEvent implements LocaleEvent.Create {

							@Override
							public String plugin() {
								return pluginID;
							}

							@Override
							public Locale getLocale() {
								return locale;
							}

							@Override
							public Cause cause() {
								return cause;
							}

							@Override
							public ConfigTypes configType() {
								return configType;
							}
							
						}
						postEvent(new CreateLocaleEvent());
						if(localesSizeBefore < localeService.getPluginLocales(pluginID).size()) {
							logger.info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
						} else {
							logger.error("[FileWatcher] Error loading file " + fileName + " for plugin \"" + pluginID + "\"!");
						}
					}
				}
				updated.put(pluginID + locale.toLanguageTag(), TimeUnit.MILLISECONDS.toSeconds(oldTime));
				break;
			}
		}
	}

	private void checkOnModify(String pluginID, String fileName) {
		long oldTime = System.currentTimeMillis();
		for(Locale locale : localeService.getLocalesList()) {
			if(updated.containsKey(pluginID + locale.toLanguageTag())) return;
			if(fileName.contains(locale.toLanguageTag()) && (fileName.contains("conf") || fileName.contains("yml") || fileName.contains("json") || fileName.contains("properties"))) {
				if(updated.containsKey(fileName)) 
				logger.info("[FileWatcher] Locale file " + fileName + " has been changed! Reloading...");
				localeService.getOrDefaultLocale(pluginID, locale).reload();
				updated.put(pluginID + locale.toLanguageTag(), TimeUnit.MILLISECONDS.toSeconds(oldTime));
				class ReloadLocaleEvent extends AbstractEvent implements LocaleEvent.Reload {

					@Override
					public String plugin() {
						return pluginID;
					}

					@Override
					public Locale getLocale() {
						return locale;
					}

					@Override
					public Cause cause() {
						return cause;
					}
					
				}
				postEvent(new ReloadLocaleEvent());
				break;
			}
		}
	}

	private void checkAndClearUpdated() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				for(;;) {
					if(!updated.isEmpty()) {
						long time = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
						Map<String, Long> cache = new HashMap<String, Long>();
						cache.putAll(updated);
						for(Entry<String, Long> entry : cache.entrySet()) {
							if(entry.getValue() + 5 < time && updated.containsKey(entry.getKey())) updated.remove(entry.getKey());
						}
						try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							logger.error(e.getLocalizedMessage());
						}
					}
				}
			}
		});
	}

	private void postEvent(LocaleEvent localeEvent) {
		Sponge.eventManager().post(localeEvent);
	}

}
