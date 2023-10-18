package sawfowl.localeapi.apiclasses.serializers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang3.math.NumberUtils;

import org.spongepowered.api.ResourceKey;
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
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import net.kyori.adventure.text.Component;
import sawfowl.localeapi.api.TextUtils;
import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStack;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {

	@Override
	public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
		if(!node.node("NBT").virtual() && !node.node("NBT").isMap()) return node.get(SerializedItemStack.class).getItemStack();
		if(!node.node("UnsafeData").virtual() && !node.node("UnsafeData").empty() && node.node("UnsafeData").isMap()) return SerializeOptions.SERIALIZER_COLLECTION_VARIANT_3.get(ItemStack.class).deserialize(type, node);
		return node.node("NBT").virtual() || node.node("NBT").childrenMap().isEmpty() ? ItemStack.of(ItemTypes.registry().findValue(ResourceKey.resolve(node.node("ItemType").getString("minecraft:air"))).orElse(ItemTypes.AIR.get()), node.node("Quantity").getInt(1)) : setNbt(type, node.node("NBT"), ItemStack.of(ItemTypes.registry().findValue(ResourceKey.resolve(node.node("ItemType").getString())).orElse(ItemTypes.AIR.get()), node.node("Quantity").getInt()));
	}

	@Override
	public void serialize(Type type, @org.checkerframework.checker.nullness.qual.Nullable ItemStack itemStack, ConfigurationNode node) throws SerializationException {
		node.node("ItemType").set(RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString());
		node.node("Quantity").set(itemStack.quantity());
		if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) try {
			StringReader source = new StringReader(DataFormats.HOCON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get()));
			HoconConfigurationLoader loader = createLoader(source);
			ConfigurationNode tempNode = loader.load();
			ConfigurationNode tempNode2 = BasicConfigurationNode.root(n -> n.options().shouldCopyDefaults(true).serializers(SerializeOptions.SERIALIZER_COLLECTION_VARIANT_2));
			if(!tempNode.childrenMap().isEmpty()) {
				for(Entry<Object, ? extends ConfigurationNode> entry : tempNode.childrenMap().entrySet()) {
					if(entry.getValue().isList()) {
						tempNode2.node(entry.getKey()).set(entry.getValue());
					} else if(entry.getValue().raw().toString().contains("{") &&  entry.getValue().raw().toString().contains("}")) {
						tempNode2.node(entry.getKey()).from(serializeChildFromString(entry.getValue().raw().toString()));
					} else tempNode2.node(entry.getKey()).set(entry.getValue().raw());
				}
				tempNode.from(tempNode2);
				tempNode2 = null;
			}
			node.node("NBT").from(tempNode);
			source = null;
			loader = null;
			tempNode = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HoconConfigurationLoader createWriter(StringWriter sink) {
		return HoconConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(2)).sink(() -> new BufferedWriter(sink)).build();
	}

	private HoconConfigurationLoader createLoader(StringReader source) {
		return HoconConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(2)).source(() -> new BufferedReader(source)).build();
	}

	private ItemStack setNbt(Type type, ConfigurationNode node, ItemStack itemStack) {
		try {
			return ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.HOCON.get().read(serializedNbtToString(type, node, new StringWriter())))).build();
		} catch (InvalidDataException | IOException e) {
			e.printStackTrace();
		}
		return itemStack;
	}

	private String serializedNbtToString(Type type, ConfigurationNode node, StringWriter sink) throws ConfigurateException {
		HoconConfigurationLoader loader = createWriter(sink);
		ConfigurationNode tempNode = loader.createNode();
		tempNode.from(node);
		for(Entry<Object, ? extends ConfigurationNode> entry : tempNode.childrenMap().entrySet()) {
			if(!entry.getValue().childrenMap().isEmpty()) {
				Set<ConfigurationNode> textNodes = findTextNodes(entry.getValue());
				if(!textNodes.isEmpty()) {
					for(ConfigurationNode textNode : textNodes) {
						if(NumberUtils.isCreatable(textNode.parent().key().toString()) && textNode.parent().parent() != null) {
							textNode.parent().parent().setList(String.class, textNode.parent().parent().getList(Component.class).stream().map(TextUtils::serializeJson).toList());
						} else {
							Component component = textNode.parent().get(Component.class);
							String text = TextUtils.serializeJson(component);
							textNode.parent().set(text);
						}
					}
				} else tempNode.node(entry.getKey()).set(nodeToString(entry.getValue(), new StringWriter()));
			} else tempNode.node(entry.getKey()).set(entry.getValue().raw());
		}
		loader.save(tempNode);
		loader = null;
		tempNode = null;
		return sink.toString();
	}

	private Set<ConfigurationNode> findTextNodes(ConfigurationNode node) {
		Set<ConfigurationNode> nodes = new HashSet<ConfigurationNode>();
		if(node.isMap()) {
			for(ConfigurationNode child : node.childrenMap().values()) {
				if(!child.key().toString().equals("text")) {
					nodes.addAll(findTextNodes(child));
				} else if(child.parent().parent() != null && child.parent().parent().key() != null && !nodes.stream().filter(n -> n.parent() != null && n.parent().key() != null && n.parent().key().toString().equals(child.parent().parent().key().toString())).findFirst().isPresent()) nodes.add(child);
			}
		}
		if(node.isList()) {
			for(ConfigurationNode child : node.childrenList()) {
				if(!child.key().toString().equals("text")) {
					nodes.addAll(findTextNodes(child));
				} else if(child.parent().parent() != null && child.parent().parent().key() != null && !nodes.stream().filter(n -> n.parent() != null && n.parent().key() != null && n.parent().key().toString().equals(child.parent().parent().key().toString())).findFirst().isPresent()) nodes.add(child);
			}
		}
		return nodes;
	}

	private ConfigurationNode serializeChildFromString(String string) throws ConfigurateException {
		return createLoader(new StringReader(string)).load();
	}

	private String nodeToString(ConfigurationNode node, StringWriter writer) throws ConfigurateException {
		createWriter(writer).save(node);
		return writer.toString();
	}

}
