package sawfowl.localeapi.apiclasses.serializers.itemstack;

import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import sawfowl.localeapi.api.serializetools.SerializeOptions;
import sawfowl.localeapi.api.serializetools.itemstack.SerializedItemStackPlainNBT;

public class PlainItemStackSerializer implements TypeSerializer<ItemStack> {

	@Override
	public ItemStack deserialize(Type type, ConfigurationNode node) throws SerializationException {
		if(!node.node("NBT").virtual() && node.node("NBT").isMap()) return SerializeOptions.SERIALIZER_COLLECTION_VARIANT_2.get(ItemStack.class).deserialize(type, node);
		if(!node.node("UnsafeData").virtual() && !node.node("UnsafeData").empty() && node.node("UnsafeData").isMap()) return SerializeOptions.SERIALIZER_COLLECTION_VARIANT_3.get(ItemStack.class).deserialize(type, node);
		return node.get(SerializedItemStackPlainNBT.class).getItemStack();
	}

	@Override
	public void serialize(Type type, @Nullable ItemStack item, ConfigurationNode node) throws SerializationException {
		node.set(SerializedItemStackPlainNBT.class, new SerializedItemStackPlainNBT(item));
	}

}
