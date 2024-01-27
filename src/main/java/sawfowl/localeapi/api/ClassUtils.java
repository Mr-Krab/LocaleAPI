package sawfowl.localeapi.api;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.gson.JsonObject;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;

public class ClassUtils {

	private static final List<String> CLASSES = getNames();
	private static final String VALUES = String.join(", ", CLASSES.stream().map(clazz -> clazz).toList());

	public static boolean isPrimitiveOrBasicDataClass(Class<?> clazz) {
		return clazz != null && (clazz.isPrimitive() || CLASSES.contains(clazz.getName()));
	}

	public static <T> boolean isPrimitiveOrBasicDataClass(T object) {
		return isPrimitiveOrBasicDataClass(object.getClass());
	}

	public static String getValuesToString() {
		return VALUES;
	}

	private static List<Class<?>> knownClasses() {
		return Arrays.asList(String.class, Character.class, UUID.class, Short.class, Integer.class, Long.class, Float.class, Double.class, Number.class, Byte.class, Boolean.class, JsonObject.class, Component.class, TextComponent.class);
	}

	private static List<String> getNames() {
		return Stream.concat(knownClasses().stream().map(clazz -> clazz.getName()), Stream.of("net.kyori.adventure.text.TextComponentImpl")).toList();
	}

}
