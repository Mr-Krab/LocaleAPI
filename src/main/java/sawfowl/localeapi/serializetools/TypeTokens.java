package sawfowl.localeapi.serializetools;

import java.util.List;
import java.util.Map;

import io.leangen.geantyref.TypeToken;

public class TypeTokens {

	public static final TypeToken<SerializedItemStack> SERIALIZED_STACK_TOKEN = new TypeToken<SerializedItemStack>(){};

	public static final TypeToken<List<SerializedItemStack>> LIST_SERIALIZED_STACKS_TOKEN = new TypeToken<List<SerializedItemStack>>(){};

	public static final TypeToken<Map<String, SerializedItemStack>> MAP_SERIALIZED_STACKS_TOKEN = new TypeToken<Map<String, SerializedItemStack>>(){};

	public static final TypeToken<List<String>> LIST_STRINGS_TOKEN = new TypeToken<List<String>>(){};

	public static final TypeToken<Boolean> BOOLEAN_TOKEN = new TypeToken<Boolean>(){};

	public static final TypeToken<String> STRING_TOKEN = new TypeToken<String>(){};

	public static final TypeToken<Integer> INTEGER_TOKEN = new TypeToken<Integer>(){};

	public static final TypeToken<Double> DOUBLE_TOKEN = new TypeToken<Double>(){};

}
