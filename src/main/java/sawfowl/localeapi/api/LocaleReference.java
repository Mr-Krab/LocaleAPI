package sawfowl.localeapi.api;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import net.kyori.adventure.text.Component;

@ConfigSerializable
public interface LocaleReference {

	default Text text(Component component) {
		return Text.of(component);
	}

	default Text text(String string) {
		return Text.of(string);
	}

	default Component replace(Component component, String[] keys, Component... values) {
		return text(component).replace(keys, values).get();
	}

	default Component replace(Component component, String[] keys, String... values) {
		return text(component).replace(keys, values).get();
	}

	default Component replace(Component component, String[] keys, Object... values) {
		return text(component).replace(keys, values).get();
	}

	default String replaceToPlain(Component component, String[] keys, Component... values) {
		return text(component).replace(keys, values).toPlain();
	}

	default String replaceToPlain(Component component, String[] keys, String... values) {
		return text(component).replace(keys, values).toPlain();
	}

	default String replaceToPlain(Component component, String[] keys, Object... values) {
		return text(component).replace(keys, values).toPlain();
	}

}
