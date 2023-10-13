package sawfowl.localeapi.api;

import java.util.function.Consumer;

import org.spongepowered.api.adventure.SpongeComponents;
import org.spongepowered.api.command.CommandCause;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class TextUtils {


	/**
	 * Adding the execution of arbitrary code when you click on text.<br>
	 * It is used {@link #createCallBack(Component, Consumer)}
	 */
	public Component createCallBack(Component component, Runnable runnable) {
		return createCallBack(component, cause -> {
			runnable.run();
		});
	}

	/**
	 * Adding the execution of arbitrary code when you click on text.<br>
	 * It is used {@link SpongeComponents#executeCallback(callback)}
	 */
	public Component createCallBack(Component component, Consumer<CommandCause> callback) {
		return component.clickEvent(SpongeComponents.executeCallback(callback));
	}

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
	 * Removing all decorations from the text.
	 */
	public static final Component removeDecorations(Component component) {
		return removeDecorations(serializeLegacy(component));
	}

	/**
	 * Removing all decorations from the text.
	 */
	public static final Component removeDecorations(String string) {
		return deserializeLegacy(clearDecorations(string));
	}

	/**
	 * String to {@link Component} conversion.
	 */
	public static final  Component deserialize(String string) {
		if(isLegacyDecor(string)) {
			return deserializeLegacy(string);
		} else try {
			return deserializeJson(string);
		} catch (Exception e) {
			return deserializeLegacy(string);
		}
	}

	/**
	 * Checking the string for decorations.
	 */
	public static boolean isLegacyDecor(String string) {
		return string.indexOf('&') != -1 /*&& !string.endsWith("&")*/ && isStyleChar(string.charAt(string.indexOf("&") + 1));
	}

	private static boolean isStyleChar(char ch) {
		return "0123456789abcdefklmnor".indexOf(ch) != -1;
	}

}
