package sawfowl.localeapi.api;

import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

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

	WatchLocales getWatchLocales() {
		return watchLocales;
	}

	void stopWatch() {
		watchLocales.stopWatch();
		work = false;
		watchLocales = null;
		this.interrupt();
	}

}
