package sawfowl.localeapi.event;

import java.util.Locale;

import org.spongepowered.api.event.Event;

import sawfowl.localeapi.api.ConfigTypes;

public interface LocaleEvent extends Event {

	public interface Create extends LocaleEvent {

		public ConfigTypes configType();

	}

	public interface Reload extends LocaleEvent {}

	public String plugin();

	public Locale getLocale();

}
