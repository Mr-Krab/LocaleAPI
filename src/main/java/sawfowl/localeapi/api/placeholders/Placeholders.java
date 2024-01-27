package sawfowl.localeapi.api.placeholders;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.ClassUtils;

import net.kyori.adventure.text.Component;

import sawfowl.localeapi.api.Text;
import sawfowl.localeapi.api.TextUtils;

public class Placeholders {

	private static final Map<Class<?>, Map<String, Placeholder<?>>> PLACEHOLDERS = new HashMap<>();

	public static <T> boolean register(Class<T> clazz, String id, Placeholder<T> placeholder) {
		if(PLACEHOLDERS.containsKey(clazz) && PLACEHOLDERS.get(clazz).containsKey(id)) return false;
		if(!PLACEHOLDERS.containsKey(clazz)) PLACEHOLDERS.put(clazz, new HashMap<>());
		PLACEHOLDERS.get(clazz).put(id, placeholder);
		return true;
	}

	public static <T> boolean register(Class<T> clazz, DefaultPlaceholderKeys key, Placeholder<T> placeholder) {
		return register(clazz, key.id(), placeholder);
	}

	@SuppressWarnings({ "unchecked" })
	public static <T> Text apply(Text text, T arg, Component def) {
		Class<?> clazz = arg.getClass();
		if(PLACEHOLDERS.containsKey(clazz)) PLACEHOLDERS.get(arg.getClass()).values().forEach(placeholder -> ((Placeholder<T>) placeholder).apply(text, arg, def));
		for(Class<?> clazz2 : clazz.getClasses()) {
			if(clazz != clazz2 && PLACEHOLDERS.containsKey(clazz2)) PLACEHOLDERS.get(clazz2).values().forEach(placeholder -> applyOther(text, cast(clazz2, arg), placeholder, def));
		}
		for(Class<?> clazz2 : ClassUtils.getAllInterfaces(clazz)) {
			if(clazz != clazz2 && PLACEHOLDERS.containsKey(clazz2)) PLACEHOLDERS.get(clazz2).values().forEach(placeholder -> applyOther(text, cast(clazz2, arg), placeholder, def));
		}
		return text;
	}

	public static Text apply(Text text, Component def, Object... args) {
		for(Object arg : args) apply(text, arg, def);
		return text;
	}

	public static Text apply(Component component, Component def, Object... args) {
		return apply(Text.of(component), def, args);
	}

	public static Text apply(String string, String def, Object... args) {
		return apply(Text.of(string), TextUtils.deserialize(def), args);
	}

	public static <T> Text apply(Component component, Component def, T arg) {
		return apply(Text.of(component), arg, def);
	}

	public static <T> Text apply(String string, String def, T arg) {
		return apply(Text.of(string), arg, TextUtils.deserialize(def));
	}

	private static <T> Text applyOther(Text text, T arg, Placeholder<? extends T> placeholder, Component def) {
		return cast(arg, placeholder).apply(text, arg, def);
	}

	@SuppressWarnings("unchecked")
	private static <T, C extends T> C cast(Class<C> clazz2, T arg) {
		return (C) arg;
	}

	@SuppressWarnings("unchecked")
	private static <T> Placeholder<T> cast(T arg, Placeholder<?> placeholder) {
		return (Placeholder<T>) placeholder;
	}

	public enum DefaultPlaceholderKeys {

		NAMEABLE {
			@Override
			public String textKey() {
				return "%name%";
			}
			@Override
			public String id() {
				return "Name";
			}
		},
		PLAYER_PING {
			@Override
			public String textKey() {
				return "%player-ping%";
			}
			@Override
			public String id() {
				return "PlayerPing";
			}
		},
		ENTITY_DISPLAY_NAME {
			@Override
			public String textKey() {
				return "%entity-display-name%";
			}
			@Override
			public String id() {
				return "EntityDisplayName";
			}
		},
		ENTITY_UUID {
			@Override
			public String textKey() {
				return "%entity-uuid%";
			}
			@Override
			public String id() {
				return "EntityUUID";
			}
		},
		WORLD {
			@Override
			public String textKey() {
				return "%world%";
			}
			@Override
			public String id() {
				return "World";
			}
		},
		LOCATION {
			@Override
			public String textKey() {
				return "%location%";
			}
			@Override
			public String id() {
				return "Location";
			}
		},
		POSITION {
			@Override
			public String textKey() {
				return "%position%";
			}
			@Override
			public String id() {
				return "Vector3d";
			}
		},
		BLOCK_POSITION {
			@Override
			public String textKey() {
				return "%block-position%";
			}
			@Override
			public String id() {
				return "Vector3i";
			}
		},
		PLAYER_PREFIX {
			@Override
			public String textKey() {
				return "%player-prefix%";
			}
			@Override
			public String id() {
				return "PlayerPrefix";
			}
		},
		PLAYER_SUFFIX {
			@Override
			public String textKey() {
				return "%player-suffix%";
			}
			@Override
			public String id() {
				return "PlayerSuffix";
			}
		},
		PLAYER_RANK {
			@Override
			public String textKey() {
				return "%player-rank%";
			}
			@Override
			public String id() {
				return "PlayerRank";
			}
		},
		ITEM {
			@Override
			public String textKey() {
				return "%item%";
			}
			@Override
			public String id() {
				return "Item";
			}
		},
		BLOCK {
			@Override
			public String textKey() {
				return "%block%";
			}
			@Override
			public String id() {
				return "Block";
			}
		};

		public abstract String textKey();
		public abstract String id();

	}

}
