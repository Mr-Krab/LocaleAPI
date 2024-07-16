package sawfowl.localeapi.api.event;

import java.util.Locale;

import org.spongepowered.api.event.Event;

import sawfowl.localeapi.api.PluginLocale;

public interface LocaleEvent extends Event {

	public interface Create extends LocaleEvent {

		public String configType();

	}

	public interface Reload extends LocaleEvent {}

	public String plugin();

	public Locale getLocale();

	public PluginLocale getLocaleConfig();

}
