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
	private PluginContainer pluginContainer;
	public LocaleService getLocaleService() {
		return localeService;
	}

	@Inject
	public Main(PluginContainer pluginContainer, @ConfigDir(sharedRoot = false) Path configDirectory) {
		this.pluginContainer = pluginContainer;
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
			localeService.createPluginLocale(pluginContainer, ConfigTypes.HOCON, Locales.DEFAULT);
			//		                          ^^ pluginContainer or "pluginid".
			localeService.createPluginLocale(pluginContainer, ConfigTypes.HOCON, Locales.DEFAULT);
			getLocaleUtil(Locales.DEFAULT).checkString("Your string for localization.", "Optional comment", "Path");
		}
		// Get message from locale. You can get and use the player's localization 'player.locale();'.
		// The boolean parameter defines what type of string serializer will be used.
		logger.info(getLocaleUtil(Locales.DEFAULT).getComponent("Path"));
	}

	public PluginLocale getPluginLocale(Locale locale) {
		return localeService.getOrDefaultLocale(pluginContainer, locale);
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
	implementation 'com.github.SawFowl:LocaleAPI:3.0.0'
}
