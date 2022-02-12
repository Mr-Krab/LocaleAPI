package sawfowl.localeapi.api;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;


public class WatchThread extends Thread {

	private WatchLocales watchLocales;
	public WatchThread(LocaleService localeService, Logger logger, Path path) {
		watchLocales = new WatchLocales(localeService, logger, path);
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
