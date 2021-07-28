package sawfowl.localeapi.api;

import sawfowl.localeapi.LocaleAPIMain;

public class WatchThread extends Thread {

	LocaleAPIMain plugin;
	LocaleAPI localeAPI;
	private WatchLocales watchLocales;
	public WatchThread(LocaleAPIMain instance, LocaleAPI localeAPI) {
		plugin = instance;
		this.localeAPI = localeAPI;
		watchLocales = new WatchLocales(plugin, localeAPI);
	}

	public void run() {
		for(;;) {
			watchLocales.startWatch();
		}
	}

	WatchLocales getWatchLocales() {
		return watchLocales;
	}

}
