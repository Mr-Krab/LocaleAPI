package sawfowl.localeapi.utils;

import java.nio.file.Path;

import sawfowl.localeapi.api.LocaleService;
import sawfowl.localeapi.api.Logger;

public class WatchThread extends Thread {

	private WatchLocales watchLocales;
	private boolean work = true;
	private final Logger logger;
	public WatchThread(LocaleService localeService, Logger logger, Path path) {
		watchLocales = new WatchLocales(localeService, logger, path);
		this.logger = logger;
	}

	public void run() {
		logger.info("[FileWatcher] File tracking has been launched.");
		while(work) {
			watchLocales.startWatch();
		}
	}

	public WatchLocales getWatchLocales() {
		return watchLocales;
	}

	public void stopWatch() {
		watchLocales.stopWatch();
		work = false;
		watchLocales = null;
		this.interrupt();
	}

}
