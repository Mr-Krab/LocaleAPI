# LocaleAPI
API for working with plugin localizations on SpongeAPI7.
Test plugin -> https://github.com/SawFowl/LocaleTestPlugin/tree/API7

```JAVA
@Plugin(id = "pluginid",
	name = "PluginName",
	version = "version",
	authors = "authors",
	dependencies = {
		@Dependency(id = "localeapi", optional = false)
	})
public class PluginName {
	// Get API
	private static LocaleAPI localeAPI;
	public LocaleAPI getLocaleAPI() {
		return localeAPI;
	}

	@Listener
	public void onPostInitialization(GamePostInitializationEvent event) {
		localeAPI = LocaleAPIMain.getInstance().getAPI();
		localeAPI.saveLocales(this);
		//		       ^^ Main class or pluginid
		// Send message
		CommandSpec command = CommandSpec.builder()
    	        .executor((src, args) -> {
    	        	src.sendMessage(getOrDefaultLocale(src.getLocale()).getString("message-key"));
    	            return CommandResult.success();
    	        })
    	        .build();
		Sponge.getCommandManager().register(this, command, "command");
	}

	// Get locale. Similarly for Sponge configurations(HoconLocaleUtil,  JsonLocaleUtil,  YamlLocaleUtil). These methods are optional. Used to simplify access to the localization API.
	public Map<Locale, LocaleUtil> getLocales() {
		return localeAPI.getLocalesMap("pluginid");
		//				   ^^ Main class or pluginid
	}
	public LocaleUtil getLocale(Locale locale) {
		return getLocales().get(locale);
	}
	public LocaleUtil getDefaultLocale() {
		return getLocales().get(localeAPI.getDefaultLocale());
	}
	public LocaleUtil getOrDefaultLocale(Locale locale) {
		return getLocales().getOrDefault(locale, getDefaultLocale());
	}
}
```
##### Gradle
```gradle
repositories {
    ...
    maven { 
        name = "jitpack"
        url = 'https://jitpack.io' 
    }
}
dependencies {
    ...
    implementation 'com.github.Mr-Krab:LocaleAPI:1.0'
}

```

