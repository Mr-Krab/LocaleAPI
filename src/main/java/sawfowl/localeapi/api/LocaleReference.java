package sawfowl.localeapi.api;


import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

@ConfigSerializable
public interface LocaleReference {

	default Component deserialize(String string) {
		return TextUtils.deserialize(string);
	}

	default Text text(Component component) {
		return Text.of(component);
	}

	default Text text(String string) {
		return Text.of(string);
	}

	default Component replace(Component component, String key, Component value) {
		return component.replaceText(TextReplacementConfig.builder().match(key).replacement(value).build());
	}

	default Component replace(Component component, String key, String value) {
		return replace(component, key, deserialize(value));
	}

	default Component replace(Component component, String key, Object value) {
		return component.replaceText(TextReplacementConfig.builder().match(key).replacement(value.toString()).build());
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

	default String[] array(String... strings) {
		return strings;
	}

	default Component[] array(Component... components) {
		return components;
	}

	default Object[] array(Object... objects) {
		return objects;
	}

}
