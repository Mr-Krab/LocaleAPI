package sawfowl.localeapi.api.serializetools.itemstack;

import java.util.List;
import java.util.Map;

import io.leangen.geantyref.TypeToken;

import net.kyori.adventure.text.Component;

public class TypeTokens {

	public static final TypeToken<SerializedItemStackPlainNBT> SERIALIZED_STACK_TOKEN = new TypeToken<SerializedItemStackPlainNBT>(){};

	public static final TypeToken<List<SerializedItemStackPlainNBT>> LIST_SERIALIZED_STACKS_TOKEN = new TypeToken<List<SerializedItemStackPlainNBT>>(){};

	public static final TypeToken<Map<String, SerializedItemStackPlainNBT>> MAP_SERIALIZED_STACKS_TOKEN = new TypeToken<Map<String, SerializedItemStackPlainNBT>>(){};

	public static final TypeToken<List<String>> LIST_STRINGS_TOKEN = new TypeToken<List<String>>(){};

	public static final TypeToken<List<Component>> LIST_COMPONENTS_TOKEN = new TypeToken<List<Component>>(){};

	public static final TypeToken<Boolean> BOOLEAN_TOKEN = new TypeToken<Boolean>(){};

	public static final TypeToken<String> STRING_TOKEN = new TypeToken<String>(){};

	public static final TypeToken<Integer> INTEGER_TOKEN = new TypeToken<Integer>(){};

	public static final TypeToken<Double> DOUBLE_TOKEN = new TypeToken<Double>(){};

	public static <T> TypeToken<T> createToken(Class<T> clazz) {
		return TypeToken.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T> TypeToken<T> createToken(T object) {
		return createToken((Class<T>) object.getClass());
	}

}
