package sawfowl.localeapi.apiclasses.serializers.itemstack;

import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStack;

public class PlainItemStackSerializer implements TypeSerializer<ItemStack> {

	@Override
	public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
		if(!node.node("ComponentsMap").virtual() && node.node("ComponentsMap").isMap()) return SerializeOptions.SERIALIZER_COLLECTION_VARIANT_2.get(ItemStack.class).deserialize(type, node);
		if((!node.node("UnsafeData").virtual() && !node.node("UnsafeData").empty() && node.node("UnsafeData").isMap()) || (!node.node("components").virtual() && !node.node("components").empty() && node.node("components").isMap())) return SerializeOptions.SERIALIZER_COLLECTION_VARIANT_3.get(ItemStack.class).deserialize(type, node);
		return node.get(SerializedItemStack.class).getItemStack();
	}

	@Override
	public void serialize(Type type, @Nullable ItemStack item, ConfigurationNode node) throws SerializationException {
		SerializedItemStack stack = new SerializedItemStack(item);
		node.node("ItemType").set(stack.getItemTypeAsString());
		node.node("Quantity").set(stack.getQuantity());
		node.node("Components").set(stack.getComponentsAsString());
		stack = null;
	}

}
