package sawfowl.localeapi.api;

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
    };

	ConfigTypes(String string) {}

}
