package sawfowl.localeapi.api;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import sawfowl.localeapi.LocaleAPIMain;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

class WatchLocales {

	LocaleAPIMain plugin;
	public WatchLocales(LocaleAPIMain instance, LocaleAPI localeAPI) {
		plugin = instance;
		this.localeAPI = localeAPI;
		updated = new HashMap<String, Long>();
		try {
			watchService = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		};
		checkAndClearUpdated();
	}

	private WatchService watchService;
	private LocaleAPI localeAPI;
	private Map<String, Long> updated;

	void addPluginData(String pluginID) {
		try {
			updated.put(pluginID, TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()));
			plugin.getConfigDir().resolve(pluginID).register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
			plugin.getLogger().info("[FileWatcher] Added tracking of localization files for plugin: " + pluginID);
		} catch (IOException e) {
			plugin.getLogger().error(e.getLocalizedMessage());
		}
	}

	void startWatch() {
		plugin.getLogger().info("[FileWatcher] File tracking has been launched.");
		WatchKey key;
		try {
			Thread.sleep(5000);
			while ((key = watchService.take()) != null) {
			    for (WatchEvent<?> event : key.pollEvents()) {
		    		String pluginID = getPluginID(key.watchable().toString());
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
			plugin.getLogger().error(e.getLocalizedMessage());
		}
	}

	private void checkOnCreate(String pluginID, String fileName) {
		long oldTime = System.currentTimeMillis();
		for(Locale locale : localeAPI.getLocalesList()) {
			if(fileName.contains(locale.toLanguageTag())) {
				if(fileName.contains("tmp")) {
					return;
				}
				if(updated.containsKey(pluginID) && updated.get(pluginID) + 5 > oldTime) return;
				if(fileName.contains("conf")) {
					plugin.getLogger().info("[FileWatcher] New locale file found -> " + fileName + "! Loading...");
					localeAPI.createPluginLocale(pluginID, ConfigTypes.HOCON, locale);
					plugin.getLogger().info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
				} else if(fileName.contains("yml")) {
					plugin.getLogger().info("[FileWatcher] New locale file found -> " + fileName + "! Loading...");
					localeAPI.createPluginLocale(pluginID, ConfigTypes.YAML, locale);
					plugin.getLogger().info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
				} else if(fileName.contains("json")) {
					plugin.getLogger().info("[FileWatcher] New locale file found -> " + fileName + "! Loading...");
					localeAPI.createPluginLocale(pluginID, ConfigTypes.JSON, locale);
					plugin.getLogger().info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
				} else if(fileName.contains("properties")) {
					plugin.getLogger().info("[FileWatcher] New locale file found -> " + fileName + "! Loading...");
					localeAPI.createPluginLocale(pluginID, ConfigTypes.PROPERTIES, locale);
					plugin.getLogger().info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
				}
				updated.put(pluginID + locale.toLanguageTag(), TimeUnit.MILLISECONDS.toSeconds(oldTime));
				break;
			}
		}
	}

	private void checkOnModify(String pluginID, String fileName) {
		long oldTime = System.currentTimeMillis();
		for(Locale locale : localeAPI.getLocalesList()) {
			if(updated.containsKey(pluginID + locale.toLanguageTag())) return;
			if(fileName.contains(locale.toLanguageTag()) && (fileName.contains("conf") || fileName.contains("yml") || fileName.equals("json") || fileName.contains("properties")) ) {
				if(updated.containsKey(fileName)) 
				plugin.getLogger().info("[FileWatcher] Locale file " + fileName + " has been changed! Reloading...");
				localeAPI.getOrDefaultLocale(pluginID, locale).reload();
				plugin.getLogger().info("[FileWatcher] Done. " + (System.currentTimeMillis() - oldTime) + "ms");
				updated.put(pluginID + locale.toLanguageTag(), TimeUnit.MILLISECONDS.toSeconds(oldTime));
				break;
			}
		}
	}

	private String getPluginID(String string) {
		String result = string.split("localeapi" + File.separator)[1];
		return result;
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
							plugin.getLogger().error(e.getLocalizedMessage());
						}
					}
				}
			}
		});
	}

}
