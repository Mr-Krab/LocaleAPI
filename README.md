# LocaleAPI
API for working with plugin localizations on SpongeAPI8. \
Test plugin -> https://github.com/SawFowl/LocaleTestPlugin/tree/API8 \
javadoc -> https://sawfowl.github.io/LocaleAPI/

```JAVA
@Plugin("pluginid")
public class Main {
	private Main instance;
	private Logger logger;
	private static LocaleService api;
	public LocaleService getLocaleAPI() {
		return api;
	}

	@Listener
	public void onEnable(StartedEngineEvent<Server> event) {
		instance = this;
		// Apache logger
		logger = LogManager.getLogger("PluginName");
		// Get API
		if(Sponge.pluginManager().plugin("localeapi").isPresent() && Sponge.pluginManager().isLoaded("localeapi")) {
			localeAPI = ((LocaleAPIMain) Sponge.pluginManager().plugin("localeapi").get().instance()).getAPI();
		}
		api.checkLocalesExist(instance, ConfigTypes.HOCON);
		api.createPluginLocale(instance, ConfigTypes.HOCON, Locales.DEFAULT);
		//		       ^^ Main class or "pluginid".
		api.createPluginLocale(instance, ConfigTypes.HOCON, Locales.DEFAULT);
		getLocaleUtil(Locales.DEFAULT).checkString("Your string for localization.", "Optional comment", "Path");
		
		// Get message from locale. You can get and use the player's localization 'player.locale();'.
		logger.info(getLocaleUtil(Locales.DEFAULT).getComponent(false, "Path"));
	}

	public LocaleUtil getLocaleUtil(Locale locale) {
		return api.getOrDefaultLocale(instance, locale);
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
    implementation 'com.github.SawFowl:LocaleAPI:API8-SNAPSHOT'
}

```

