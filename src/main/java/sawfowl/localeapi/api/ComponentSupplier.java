package sawfowl.localeapi.api;

import java.util.Locale;

import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface ComponentSupplier {

	Component get(Locale locale);

}
