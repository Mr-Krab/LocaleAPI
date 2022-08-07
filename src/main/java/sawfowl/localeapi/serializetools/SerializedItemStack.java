package sawfowl.localeapi.serializetools;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import net.kyori.adventure.key.Key;

@ConfigSerializable
public class SerializedItemStack {

	SerializedItemStack(){}

	public SerializedItemStack(ItemStack itemStack) {
		serialize(itemStack);
	}

	@Setting("ItemType")
	private String itemType;
	//@Setting("ItemSubType")
	//private Integer itemSubType;
	@Setting("ItemQuantity")
	private Integer itemQuantity;
	@Setting("Nbt")
	private String nbt;
	private ItemStack itemStack;

	public String getType() {
		return itemType;
	}

	/*public Integer getSubType() {
		return itemSubType;
	}*/

	public Integer getQuantity() {
		return itemQuantity;
	}

	public String getNBT() {
		return nbt != null ? nbt : "";
	}

	public ItemStack getItemStack() {
		if(itemStack == null || !getOptItemType().isPresent()) {
			itemStack = ItemStack.empty();
		}
		if(getOptItemType().isPresent()) {
			itemStack = ItemStack.of(getOptItemType().get());
			itemStack.setQuantity(itemQuantity);
			//net.minecraft.world.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
			//nmsStack.setDamageValue(itemSubType);
			//itemStack = ItemStackUtil.fromNative(nmsStack);
			if(nbt != null) {
				try {
					itemStack = ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.JSON.get().read(nbt))).build();
				} catch (InvalidDataException | IOException e) {
					e.printStackTrace();
				}
			}
		}
		return itemStack.copy();
	}

	public Optional<ItemType> getOptItemType() {
		return Sponge.game().registry(RegistryTypes.ITEM_TYPE).findValue(ResourceKey.resolve(itemType));
	}

	public void setQuantity(int quantity) {
		itemQuantity = quantity;
	}

	public Key getItemKey() {
		return getOptItemType().isPresent() ? Key.key(itemType) : Key.key("air");
	}

	public void removeFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(!nmsStack.hasTag() || !nmsStack.getOrCreateTag().contains(key)) return;
			if(nmsStack.getOrCreateTag().isEmpty()) {
				itemStack = ItemStack.of(getOptItemType().get());
				itemStack.setQuantity(itemQuantity);
			} else {
				serialize((ItemStack) ((Object) nmsStack));
			}
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(!nmsStack.hasTag() || !nmsStack.getOrCreateTag().contains(key)) return;
			if(nmsStack.getOrCreateTag().isEmpty()) {
				itemStack = ItemStack.of(getOptItemType().get());
				itemStack.setQuantity(itemQuantity);
			} else {
				serialize((ItemStack) ((Object) nmsStack));
			}
		}
	}

	public void addStringToNBT(String key, String value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putString(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putString(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addUUIDToNBT(String key, UUID value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putUUID(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putUUID(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addShortToNBT(String key, short value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putShort(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putShort(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addIntegerToNBT(String key, int value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putInt(key, value);
			serialize((ItemStack) ((Object) nmsStack));
			System.out.println("NBT ТЕГ -> " + nbt);
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putInt(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addLongToNBT(String key, long value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putLong(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putLong(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addFloatToNBT(String key, float value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putFloat(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putFloat(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addDoubleToNBT(String key, double value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putDouble(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putDouble(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addByteToNBT(String key, byte value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putByte(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putByte(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addBooleanToNBT(String key, boolean value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putBoolean(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putBoolean(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addIntegerArrayToNBT(String key, int[] value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addIntegerListToNBT(String key, List<Integer> value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addLongArrayToNBT(String key, long[] value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addLongListToNBT(String key, List<Long> value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public void addByteArrayToNBT(String key, byte[] value) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putByteArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			nmsStack.getOrCreateTag().putByteArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}
	}

	public Optional<String> getStringFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getString(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getString(key));
		}
		return Optional.empty();
	}

	public Optional<UUID> getUUIDFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getUUID(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getUUID(key));
		}
		return Optional.empty();
	}

	public Optional<Short> getShortFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getShort(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getShort(key));
		}
		return Optional.empty();
	}

	public Optional<Integer> getIntegerFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getInt(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getInt(key));
		}
		return Optional.empty();
	}

	public Optional<Long> getLongFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getLong(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getLong(key));
		}
		return Optional.empty();
	}

	public Optional<Float> getFloatFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getFloat(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getFloat(key));
		}
		return Optional.empty();
	}

	public Optional<Double> getDoubleFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getDouble(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getDouble(key));
		}
		return Optional.empty();
	}

	public Optional<Byte> getByteFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getByte(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getByte(key));
		}
		return Optional.empty();
	}

	public Optional<Boolean> getBooleanFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getBoolean(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getBoolean(key));
		}
		return Optional.empty();
	}

	public Optional<int[]> getIntegerArayFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getIntArray(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getIntArray(key));
		}
		return Optional.empty();
	}

	public Optional<List<Integer>> getIntegerListFromNBT(String key) {
		return getIntegerArayFromNBT(key).isPresent() ? Optional.ofNullable(Arrays.stream(getIntegerArayFromNBT(key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
	}

	public Optional<long[]> getLongArayFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getLongArray(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getLongArray(key));
		}
		return Optional.empty();
	}

	public Optional<List<Long>> getLongListFromNBT(String key) {
		return getLongListFromNBT(key).isPresent() ? Optional.ofNullable(Arrays.stream(getLongArayFromNBT(key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
	}

	public Optional<byte[]> getByteArayFromNBT(String key) {
		if(isForgeItem()) {
			net.minecraft.item.ItemStack nmsStack = (net.minecraft.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key)) return Optional.ofNullable(nmsStack.getOrCreateTag().getByteArray(key));
		} else {
			net.minecraft.world.item.ItemStack nmsStack = (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
			if(nmsStack.hasTag() && nmsStack.getTag().contains(key)) return Optional.ofNullable(nmsStack.getTag().getByteArray(key));
		}
		return Optional.empty();
	}

	private boolean isForgeItem() {
		try {
			Class.forName("net.minecraft.item.ItemStack");
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	private void serialize(ItemStack itemStack) {
		itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
		itemQuantity = itemStack.quantity();
		//net.minecraft.world.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
		//itemSubType = nmsStack.getDamageValue();
		if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
			try {
				nbt = DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.itemStack = itemStack;
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemQuantity, itemStack, itemType, nbt);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof SerializedItemStack)) return false;
		SerializedItemStack other = (SerializedItemStack) obj;
		return Objects.equals(itemQuantity, other.itemQuantity) && Objects.equals(itemType, other.itemType) && Objects.equals(nbt, other.nbt);
	}

	public boolean equalsWhithoutQuantity(SerializedItemStack itemStack) {
		return this == itemStack || (Objects.equals(itemType, itemStack.itemType) && Objects.equals(nbt, itemStack.nbt));
	}

	public boolean equalsWhithoutNBT(SerializedItemStack itemStack) {
		return this == itemStack || (Objects.equals(itemType, itemStack.itemType) && Objects.equals(itemQuantity, itemStack.itemQuantity));
	}

	public boolean equalsToItemStack(ItemStack itemStack) {
		return equals(new SerializedItemStack(itemStack));
	}

	@Override
	public String toString() {
		return  "ItemType: " + itemType +
				//", ItemSubType: " + itemSubType +
				", Quantity: " + itemQuantity + 
				", Nbt: " + getNBT();
	}

}
