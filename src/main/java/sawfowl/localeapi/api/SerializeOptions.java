package sawfowl.localeapi.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.annotation.Nullable;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.NodeResolver;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;

/**
 * These options disable serialization of objects not marked by the <b>@Setting</b> annotation.
 */
public class SerializeOptions {

	/**
	 * Creating a `.yml` config with serializers applied and standard options preserved.
	 */
	public static YamlConfigurationLoader.Builder createYamlConfigurationLoader() {
		return YamlConfigurationLoader.builder().defaultOptions(options -> options.shouldCopyDefaults(true).serializers(CONFIGURATIO_NOPTIONS.serializers()));
	}

	public static final TypeSerializer<ItemStack> ITEMSTACK_SERIALIZER = new TypeSerializer<ItemStack>() {

		@Override
		public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
			if(node.node("NBT").virtual() || node.node("NBT").empty()) return ItemStack.of(ItemTypes.registry().findValue(ResourceKey.resolve(node.node("ItemType").getString())).orElse(ItemTypes.AIR.get()), node.node("Quantity").getInt());
			ItemStack itemStack = ItemStack.of(ItemTypes.registry().findValue(ResourceKey.resolve(node.node("ItemType").getString())).orElse(ItemTypes.AIR.get()), node.node("Quantity").getInt());
			try {
				StringWriter sink = new StringWriter();
				HoconConfigurationLoader loader = createLoader(sink);
				loader.save(node.node("NBT"));
				itemStack = ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.HOCON.get().read(sink.toString()))).build();
				sink = null;
				loader = null;
			} catch (InvalidDataException | IOException e) {
				e.printStackTrace();
			}
			return itemStack;
		}

		private HoconConfigurationLoader createLoader(StringWriter sink) {
			return HoconConfigurationLoader.builder().defaultOptions(CONFIGURATIO_NOPTIONS).sink(() -> new BufferedWriter(sink)).build();
		}

		@Override
		public void serialize(Type type, @org.checkerframework.checker.nullness.qual.Nullable ItemStack itemStack, ConfigurationNode node) throws SerializationException {
			if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
				HoconConfigurationLoader loader = HoconConfigurationLoader.builder().defaultOptions(CONFIGURATIO_NOPTIONS).source(() -> new BufferedReader(new StringReader(DataFormats.HOCON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get())))).build();
				try {
					ConfigurationNode tempNode = loader.load();
					ConfigurationNode tempNode2 = BasicConfigurationNode.root();
					if(!tempNode.childrenMap().isEmpty()) {
						for(Entry<Object, ? extends ConfigurationNode> entry : tempNode.childrenMap().entrySet()) {
							if(entry.getValue().getString().contains(" {") && entry.getValue().getString().contains("    ") && entry.getValue().getString().endsWith("}\n")) {
								tempNode2.node(entry.getKey()).from(serializeChildFromString(entry.getValue().getString()));
							}
						}
						tempNode.from(tempNode2);
					}
					node.node("NBT").from(tempNode);
					loader = null;
					tempNode2 = null;
					tempNode = null;
				} catch (ConfigurateException e) {
					e.printStackTrace();
				}
			}
			node.node("ItemType").set(RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString());
			node.node("Quantity").set(itemStack.quantity());
		}

		private ConfigurationNode serializeChildFromString(String string) {
			try {
				return HoconConfigurationLoader.builder().defaultOptions(CONFIGURATIO_NOPTIONS).source(() -> new BufferedReader(new StringReader(string))).build().load();
			} catch (ConfigurateException e) {
				e.printStackTrace();
			}
			return null;
		}

	};

	public static final TypeSerializer<DataContainer> DATA_CONTAINER_SERIALIZER = new TypeSerializer<DataContainer>() {

		@Override
		public DataContainer deserialize(Type type, ConfigurationNode node) throws SerializationException {
			DataContainer container = DataContainer.createNew();
			for (ConfigurationNode query : node.childrenMap().values()) {
				Map<List<String>, Object> values = findValue(query, new ArrayList<>());
				for (Map.Entry<List<String>, Object> entry : values.entrySet()) {
					DataQuery valueQuery = DataQuery.of(entry.getKey());
					container = container.set(valueQuery, entry.getValue());
				}
			}
			return container;
		}

		@Override
		public void serialize(Type type, @Nullable DataContainer obj, ConfigurationNode node) throws SerializationException {
			if (obj == null) {
				node.set(null);
				return;
			}
			for (DataQuery key : obj.keys(true)) {
				Optional<Object> opValue = obj.get(key);
				if (!opValue.isPresent()) {
					System.err.println("Skipping '" + key + "'. Could not read value");
					continue;
				}
				Object[] nodes = key.parts().stream().map(s -> (Object) s).toArray();
				Object value = opValue.get();

				node.node(nodes).node("value").set(value);
				node.node(nodes).node("type").set(value.getClass().getTypeName());
			}
		}

		private Map<List<String>, Object> findValue(ConfigurationNode node, List<String> path) {
			List<String> newPath = new ArrayList<>(path);
			Map<List<String>, Object> newMap = new HashMap<>();
			newPath.add(node.key().toString());
			if (node.node("type").isNull()) {
				for (ConfigurationNode child : node.childrenList()) {
					Map<List<String>, Object> returnedMap = findValue(child, newPath);
					newMap.putAll(returnedMap);
				}
				return newMap;
			}
			String type = node.node("type").getString();
			Class<?> clazz;
			try {
				clazz = Class.forName(type);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			try {
				Object value = node.node("value").get(clazz);
				newMap.put(newPath, value);
				return newMap;
			} catch (SerializationException e) {
				throw new RuntimeException(e);
			}
		}
	};

	public static final ObjectMapper.Factory FACTORY = ObjectMapper.factoryBuilder().addNodeResolver(NodeResolver.onlyWithSetting()).build();
	public static final TypeSerializerCollection TYPE_SERIALIZER_COLLECTION = TypeSerializerCollection.defaults().childBuilder().registerAnnotatedObjects(FACTORY).register(DataContainer.class, DATA_CONTAINER_SERIALIZER).register(ItemStack.class, ITEMSTACK_SERIALIZER).registerAll(TypeSerializerCollection.defaults()).registerAll(ConfigurateComponentSerializer.configurate().serializers()).build();
	public static final ConfigurationOptions CONFIGURATIO_NOPTIONS = ConfigurationOptions.defaults().serializers(TYPE_SERIALIZER_COLLECTION);

}
