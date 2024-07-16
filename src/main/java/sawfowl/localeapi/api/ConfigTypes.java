package sawfowl.localeapi.api;

import java.util.stream.Stream;

public enum ConfigTypes {

	HOCON(".conf") {
		@Override
		public String toString() {
			return ".conf";
		}
	},
	JSON(".json") {
		@Override
		public String toString() {
			return ".json";
		}
	},
	YAML(".yml") {
		@Override
		public String toString() {
			return ".yml";
		}
	},
	PROPERTIES(".properties") {
		@Override
		public String toString() {
			return ".properties";
		}
	},
	UNKNOWN(""){};

	ConfigTypes(String string) {}

	public static ConfigTypes find(String type) {
		return Stream.of(ConfigTypes.values()).filter(value -> value.toString().equals(type)).findFirst().orElse(UNKNOWN);
	}

}
