package sawfowl.localeapi.event;

import org.spongepowered.api.event.Event;

import sawfowl.localeapi.api.LocaleService;

public interface LocaleServiseEvent {

	/**
	 * This event is called during the `ConstructPluginEvent` event. </br>
	 * Suitable for cases where LocaleAPI is mandatory.
	 */
	public interface Construct extends LocaleServiseEvent, Event{}

	/**
	 * This event is called during the `StartedEngineEvent<Server>` event. </br>
	 * Suitable for cases where LocaleAPI is optional.
	 */
	public interface Started extends LocaleServiseEvent, Event{}

	/**
	 *  Getting API for localizations.
	 * @return `LocaleService` interface
	 */
	public LocaleService getLocaleService();

}