package sawfowl.localeapi.api.serializetools;

import java.nio.file.Path;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.minecraft.world.level.block.state.BlockState;
import sawfowl.localeapi.api.ConfigTypes;
import sawfowl.localeapi.apiclasses.serializers.JsonCollectionSerializers;
import sawfowl.localeapi.apiclasses.serializers.itemstack.ItemStackSerializer;
import sawfowl.localeapi.apiclasses.serializers.itemstack.PlainItemStackSerializer;

/**
 * These options disable serialization of objects not marked by the <b>@Setting</b> annotation.
 */
public class SerializeOptions {

	/**
	 * Creating a YAML config with serializers applied and standard options preserved.
	 */
	public static YamlConfigurationLoader.Builder createYamlConfigurationLoader(int itemStackSerializerVariant) {
		return YamlConfigurationLoader.builder().defaultOptions(options -> options.shouldCopyDefaults(true).serializers(selectSerializersCollection(itemStackSerializerVariant))).nodeStyle(NodeStyle.BLOCK);
	}

	/**
	 * Creating a HOCON config with serializers applied and standard options preserved.
	 */
	public static HoconConfigurationLoader.Builder createHoconConfigurationLoader(int itemStackSerializerVariant) {
		return HoconConfigurationLoader.builder().defaultOptions(options -> options.shouldCopyDefaults(true).serializers(selectSerializersCollection(itemStackSerializerVariant)));
	}

	/**
	 * Creating a JSON config with serializers applied and standard options preserved.
	 */
	public static GsonConfigurationLoader.Builder createJsonConfigurationLoader(int itemStackSerializerVariant) {
		return GsonConfigurationLoader.builder().defaultOptions(options -> options.shouldCopyDefaults(true).serializers(selectSerializersCollection(itemStackSerializerVariant)));
	}

	/**
	 * Creating a configuration loader with a type.
	 * 
	 * @param <T> loaderClass - Configuration Loader Class.
	 * @param <C> nodeClass - Configuration node processing class. Note that `{@link CommentedConfigurationNode}` is not suitable for configurations in Json format.
	 * @param path - Path to the configuration file.
	 * @param configType - Configuration Type. To avoid errors, it must point to the same class loader as the `Class<T> loaderClass` parameter.
	 * @param itemStackSerializerVariant - The type of item serialization used. See {@linkplain #selectSerializersCollection(int)}
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T, C extends ConfigurationNode> ConfigurationLoader<C> createConfigLoader(Class<T> loaderClass, Class<C> nodeClass, Path path, ConfigTypes configType, int itemStackSerializerVariant) {
		createHoconConfigurationLoader(itemStackSerializerVariant).path(path).build();
		switch (configType) {
		case HOCON: return (ConfigurationLoader<C>) createHoconConfigurationLoader(itemStackSerializerVariant).path(path).build();
		case YAML: return (ConfigurationLoader<C>) createYamlConfigurationLoader(itemStackSerializerVariant).path(path).build();
		case JSON: return (ConfigurationLoader<C>) createJsonConfigurationLoader(itemStackSerializerVariant).path(path).build();
		default: throw new IllegalArgumentException("Inappropriate value: " + configType);
		}
	}

	private static final TypeSerializer<ItemStack> ITEMSTACK_SERIALIZER_1 = new PlainItemStackSerializer();
	private static final TypeSerializer<ItemStack> ITEMSTACK_SERIALIZER_2 = new ItemStackSerializer();
	public static final ObjectMapper.Factory FACTORY = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build();
	public static final TypeSerializerCollection SERIALIZER_COLLECTION_VARIANT_1 = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStack.class, ITEMSTACK_SERIALIZER_1).register(BlockState.class, Sponge.game().configManager().serializers().get(BlockState.class)).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JsonCollectionSerializers.SERIALIZERS).build();
	public static final TypeSerializerCollection SERIALIZER_COLLECTION_VARIANT_2 = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(ItemStack.class, ITEMSTACK_SERIALIZER_2).register(BlockState.class, Sponge.game().configManager().serializers().get(BlockState.class)).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JsonCollectionSerializers.SERIALIZERS).build();
	public static final TypeSerializerCollection SERIALIZER_COLLECTION_VARIANT_3 = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).registerAll(Sponge.game().configManager().serializers()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).registerAll(JsonCollectionSerializers.SERIALIZERS).build();
	public static final ConfigurationOptions OPTIONS_VARIANT_1 = ConfigurationOptions.defaults().serializers(SERIALIZER_COLLECTION_VARIANT_1);
	public static final ConfigurationOptions OPTIONS_VARIANT_2 = ConfigurationOptions.defaults().serializers(SERIALIZER_COLLECTION_VARIANT_2);
	public static final ConfigurationOptions OPTIONS_VARIANT_3 = ConfigurationOptions.defaults().serializers(SERIALIZER_COLLECTION_VARIANT_3);

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>1</b> - All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.<br>
	 * <b>2</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>3</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public static ConfigurationOptions selectOptions(int itemStackSerializerVariant) {
		switch(itemStackSerializerVariant) {
			case 2: return OPTIONS_VARIANT_1;
			case 3: return OPTIONS_VARIANT_2;
			default: return OPTIONS_VARIANT_3;
		}
	}

	/**
	 * Selecting serialization variant for items.<br>
	 * <b>1</b> - All NBT tags will be written in 1 line. This option is the most reliable, but significantly complicates manual editing of NBT tags in config.<br>
	 * <b>2</b> - Advanced recording. Easier to make manual changes to the config. If you have problems with this type of serialization, you should report errors to the LocaleAPI plugin developer.<br>
	 * <b>3</b> - Using Sponge serializer. Some data will be written in 1 line. If you encounter problems with this type of serialization, you should report bugs to the Sponge developers.<br>
	 */
	public static TypeSerializerCollection selectSerializersCollection(int itemStackSerializerVariant) {
		switch(itemStackSerializerVariant) {
			case 2: return SERIALIZER_COLLECTION_VARIANT_2;
			case 3: return SERIALIZER_COLLECTION_VARIANT_3;
			default: return SERIALIZER_COLLECTION_VARIANT_1;
		}
	}

	

}