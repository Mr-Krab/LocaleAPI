package sawfowl.localeapi.apiclasses.serializers.itemstack;

import java.io.IOException;
import java.lang.reflect.Type;
import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStackPlainNBT;

public class ItemStackSerializer implements TypeSerializer<ItemStack> {

	@Override
	public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
		if(!node.node("NBT").virtual() && !node.node("NBT").isMap()) return node.get(SerializedItemStackPlainNBT.class).getItemStack();
		if(!node.node("UnsafeData").virtual() && !node.node("UnsafeData").empty() && node.node("UnsafeData").isMap()) return SerializeOptions.SERIALIZER_COLLECTION_VARIANT_3.get(ItemStack.class).deserialize(type, node);
		return node.node("NBT").virtual() || node.node("NBT").childrenMap().isEmpty() ? createStack(node) : setNbt(node.node("NBT"), createStack(node));
	}

	@Override
	public void serialize(Type type, @org.checkerframework.checker.nullness.qual.Nullable ItemStack itemStack, ConfigurationNode node) throws SerializationException {
		node.node("ItemType").set(RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString());
		node.node("Quantity").set(itemStack.quantity());
		if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) try {
			node.node("NBT").set(JsonObject.class, JsonParser.parseString(DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get())).getAsJsonObject());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ItemStack createStack(ConfigurationNode node) {
		return ItemStack.of(ItemTypes.registry().findValue(ResourceKey.resolve(node.node("ItemType").getString("minecraft:air"))).orElse(ItemTypes.AIR.get()), node.node("Quantity").getInt(1));
	}

	private ItemStack setNbt(ConfigurationNode node, ItemStack itemStack) {
		try {
			return ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.JSON.get().read(node.get(JsonObject.class).toString()))).build();
		} catch (InvalidDataException | IOException e) {
			e.printStackTrace();
		}
		return itemStack;
	}

}
