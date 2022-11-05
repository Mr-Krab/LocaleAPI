package sawfowl.localeapi.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextUtils {

	/**
	 * It is used {@link LegacyComponentSerializer#legacyAmpersand()}
	 */
	public static final String serializeLegacy(Component component) {
		return LegacyComponentSerializer.legacyAmpersand().serialize(component);
	}

	/**
	 * It is used {@link GsonComponentSerializer.gson()}
	 */
	public static final String serializeJson(Component component) {
		return GsonComponentSerializer.gson().serialize(component);
	}

	/**
	 * It is used {@link LegacyComponentSerializer#legacyAmpersand()}
	 */
	public static final Component deserializeLegacy(String string) {
		return LegacyComponentSerializer.legacyAmpersand().deserialize(string);
	}

	/**
	 * It is used {@link GsonComponentSerializer.gson()}
	 */
	public static final Component deserializeJson(String string) {
		return GsonComponentSerializer.gson().deserialize(string);
	}

	/**
	 * Removing all decorations from the text.
	 */
	public static final String clearDecorations(Component component) {
		return clearDecorations(serializeLegacy(component));
	}

	/**
	 * Removing all decorations from the text.
	 */
	public static final String clearDecorations(String string) {
		while(string.indexOf('&') != -1 && !string.endsWith("&") && isStyleChar(string.charAt(string.indexOf("&") + 1))) string = string.replaceAll("&" + string.charAt(string.indexOf("&") + 1), "");
		return string;
	}

	/**
	 * Adding the execution of arbitrary code when you click on text.<br>
	 * It is used {@link #createCallBack(Component, Consumer)}
	 */
	public static final Component createCallBack(Component component, Runnable runnable) {
		return createCallBack(component, cause -> {
			runnable.run();
		});
	}

	/**
	 * Adding the execution of arbitrary code when you click on text.<br>
	 * It is used {@link SpongeComponents#executeCallback(callback)}
	 */
	public static final Component createCallBack(Component component, final Consumer<CommandCause> callback) {
		return component.clickEvent(SpongeComponents.executeCallback(callback));
	}

	/**
	 * Creating a map for replacing values in text components.<br>
	 * In this map all values are converted to strings.<br>
	 * For correct replacement it is necessary in both lists the order of adding data must be the same. For example key 1 = value 1, key 4 = value 4.
	 * 
	 * @param keys - Keys contained in the text.
	 * @param values - Values that should be placed in the text instead of keys.
	 */
	public static Map<String, String> replaceMap(List<String> keys, List<Object> values) {
		Map<String, String> map = new HashMap<String, String>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.size() || i >= values.size()) break;
			map.put(key, values.get(i).toString());
			i++;
		}
		return map;
	}

	/**
	 * Creating a map for replacing values in text components.<br>
	 * In this map all values are converted to strings.<br>
	 * For correct replacement it is necessary in both lists the order of adding data must be the same. For example key 1 = value 1, key 4 = value 4.
	 * 
	 * @param keys - Keys contained in the text.
	 * @param values - Values that should be placed in the text instead of keys.
	 */
	public static Map<String, String> replaceMap(String[] keys, Object[] values) {
		Map<String, String> map = new HashMap<String, String>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.length || i >= values.length) break;
			map.put(key, values[i].toString());
			i++;
		}
		return map;
	}

	/**
	 * Creating a map for replacing values in text components.<br>
	 * For correct replacement it is necessary in both lists the order of adding data must be the same. For example key 1 = value 1, key 4 = value 4.
	 * 
	 * @param keys - Keys contained in the text.
	 * @param values - Values that should be placed in the text instead of keys.
	 */
	public static Map<String, Component> replaceMapComponents(List<String> keys, List<Component> values) {
		Map<String, Component> map = new HashMap<String, Component>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.size() || i >= values.size()) break;
			map.put(key, values.get(i));
			i++;
		}
		return map;
	}

	/**
	 * Creating a map for replacing values in text components.<br>
	 * For correct replacement it is necessary in both lists the order of adding data must be the same. For example key 1 = value 1, key 4 = value 4.
	 * 
	 * @param keys - Keys contained in the text.
	 * @param values - Values that should be placed in the text instead of keys.
	 */
	public static Map<String, Component> replaceMapComponents(String[] keys, Component[] values) {
		Map<String, Component> map = new HashMap<String, Component>();
		int i = 0;
		for(String key : keys) {
			if(i >= keys.length || i >= values.length) break;
			map.put(key, values[i]);
			i++;
		}
		return map;
	}

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	public static final Component replace(Component component, String[] keys, Object[] values) {
		return replace(component, replaceMap(keys, values));
	}

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	public static final Component replace(Component component, Map<String, String> map) {
		for(Entry<String, String> entry : map.entrySet()) {
			component = component.replaceText(TextReplacementConfig.builder().match(entry.getKey()).replacement(Component.text(entry.getValue())).build());
		}
		return component;
	}

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	public static final Component replaceToComponents(Component component, String[] keys, Component[] values) {
		return replaceToComponents(component, replaceMapComponents(keys, values));
	}

	/**
	 * This method works similarly to {@link String#replace(String, String)}
	 */
	public static final Component replaceToComponents(Component component, Map<String, Component> map) {
		for(Entry<String, Component> entry : map.entrySet()) {
			component = component.replaceText(TextReplacementConfig.builder().match(entry.getKey()).replacement(entry.getValue()).build());
		}
		return component;
	}

	private static boolean isStyleChar(char ch) {
		return "0123456789abcdefklmnor".indexOf(ch) != -1;
	}

}
