# LocaleAPI
API for working with plugin localizations on SpongeAPI8. \
Test plugin -> https://github.com/SawFowl/LocaleTestPlugin/tree/API8 \
javadoc -> https://sawfowl.github.io/LocaleAPI/

```JAVA
@Plugin("pluginid")
public class Main {
	private Main instance;
	private Logger logger;
	private static LocaleService localeService;
	public LocaleService getLocaleService() {
		return localeService;
	}

	// Get API. Variant 1. This happens in event `ConstructPluginEvent`.  It's recommended if LocaleAPI is mandatory.
	@Listener
	public void onLocaleServisePostEvent(LocaleServiseEvent.Construct event) {
		instance = this;
		logger = LogManager.getLogger("PluginName");
		localeService = event.getLocaleService();
		testLocales();
	}

	// Get API. Variant 2. This happens in event `StartedEngineEvent<Server>`. It's recommended if LocaleAPI is optional.
	// In this case, you can specify another class as the event listener.
	@Listener
	public void onLocaleServisePostEvent(LocaleServiseEvent.Started event) {
		localeService = event.getLocaleService();
		testLocales();
	}

	public void testLocales() {
		if(!localeService.localesExist(instance)) {
			localeService.createPluginLocale(instance, ConfigTypes.HOCON, Locales.DEFAULT);
			//		                          ^^ Main class or "pluginid".
			localeService.createPluginLocale(instance, ConfigTypes.HOCON, Locales.DEFAULT);
			getLocaleUtil(Locales.DEFAULT).checkString("Your string for localization.", "Optional comment", "Path");
		}
		// Get message from locale. You can get and use the player's localization 'player.locale();'.
		// The boolean parameter defines what type of string serializer will be used.
		logger.info(getLocaleUtil(Locales.DEFAULT).getComponent(false, "Path"));
	}

	public AbstractLocaleUtil getLocaleUtil(Locale locale) {
		return localeService.getOrDefaultLocale(instance, locale);
	}
}
```
##### Gradle
```gradle
repositories {
	...
	maven { 
		name = "JitPack"
		url 'https://jitpack.io' 
	}
}
dependencies {
	...
	implementation 'com.github.SawFowl:LocaleAPI:2.2.0'
}
