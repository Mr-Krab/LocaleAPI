package sawfowl.localeapi.serializetools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;

import net.kyori.adventure.key.Key;

import sawfowl.localeapi.api.SerializeOptions;

@ConfigSerializable
public class SerializedItemStack {

	SerializedItemStack(){}

	public SerializedItemStack(ItemStack itemStack) {
		serialize(itemStack);
	}

	public SerializedItemStack(String type, int quantity, String nbt) {
		itemType = type;
		itemQuantity = quantity;
		this.nbt = nbt;
	}

	@Setting("ItemType")
	private String itemType;
	//@Setting("ItemSubType")
	//private Integer itemSubType;
	@Setting("Quantity")
	private Integer itemQuantity;
	@Setting("NBT")
	private String nbt;
	private ItemStack itemStack;
	private CompoundNBT compoundNBT;

	public String getType() {
		return itemType;
	}

	/*public Integer getSubType() {
		return itemSubType;
	}*/

	/**
	 * Getting {@link ItemStack} volume.
	 */
	public Integer getQuantity() {
		return itemQuantity;
	}

	/**
	 * Get all tags as a string.
	 */
	public String getNBT() {
		return nbt != null ? nbt : "";
	}

	/**
	 * Getting {@link ItemStack}
	 */
	public ItemStack getItemStack() {
		if(itemStack != null) return itemStack.copy();
		if(getOptItemType().isPresent()) {
			itemStack = ItemStack.of(getOptItemType().get());
			itemStack.setQuantity(itemQuantity);
			//net.minecraft.world.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
			//nmsStack.setDamageValue(itemSubType);
			//itemStack = ItemStackUtil.fromNative(nmsStack);
			if(nbt != null) {
				try {
					itemStack = ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.HOCON.get().read(nbt))).build();
				} catch (InvalidDataException | IOException e) {
					e.printStackTrace();
				}
			}
		} else itemStack = ItemStack.empty();
		return itemStack.copy();
	}

	/**
	 * Getting {@link ItemType}
	 */
	public Optional<ItemType> getOptItemType() {
		return Sponge.game().registry(RegistryTypes.ITEM_TYPE).findValue(ResourceKey.resolve(itemType));
	}

	/**
	 * The resulting value can be used to display the item in chat.
	 */
	public Key getItemKey() {
		return getOptItemType().isPresent() ? Key.key(itemType) : Key.key("air");
	}

	/**
	 * Getting a tag collection is similar to what's in Minecraft code.
	 */
	public CompoundNBT getOrCreateTag() {
		return compoundNBT != null ? compoundNBT : (compoundNBT = /*isForgeItem() ? new ForgeNBT() :*/ new VanillaNBT());
	}

	/**
	 * Changing {@link ItemStack} volume.
	 */
	public void setQuantity(int quantity) {
		itemQuantity = quantity;
	}
/*
	public boolean isForgeItem() {
		try {
			Class.forName("net.minecraft.item.ItemStack");
			return true;
		} catch (Exception e) {
			return false;
		}
	}
*/
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

	private void serialize(ItemStack itemStack) {
		itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
		itemQuantity = itemStack.quantity();
		//net.minecraft.world.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
		//itemSubType = nmsStack.getDamageValue();
		if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
			try {
				nbt = DataFormats.HOCON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.itemStack = itemStack;
	}

	private String createStringFromCustomTag(String key, CompoundTag tag, StringWriter sink) {
		HoconConfigurationLoader loader = createWriter(sink);
		ConfigurationNode node = loader.createNode();
		try {
			node.node(key).set(CompoundTag.class, tag);
			if(!node.node(key).node("__class__").virtual()) node.node(key).removeChild("__class__");
			loader.save(node);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		node = null;
		loader = null;
		return sink.toString();
	}

	private Optional<CompoundTag> createTagFromString(String string, Class<CompoundTag> clazz) {
		try {
			return tagFromNode(serializeNodeFromString(string), clazz);
		} catch (ConfigurateException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private Optional<CompoundTag> tagFromNode(ConfigurationNode node, Class<CompoundTag> clazz) throws SerializationException {
		return node.virtual() || node.empty() ? Optional.empty() : Optional.ofNullable(node.get(clazz));
	}

	private HoconConfigurationLoader createWriter(StringWriter sink) {
		return HoconConfigurationLoader.builder().defaultOptions(SerializeOptions.CONFIGURATIO_NOPTIONS).sink(() -> new BufferedWriter(sink)).build();
	}

	private HoconConfigurationLoader createLoader(StringReader source) {
		return HoconConfigurationLoader.builder().defaultOptions(SerializeOptions.CONFIGURATIO_NOPTIONS).source(() -> new BufferedReader(source)).build();
	}

	private ConfigurationNode serializeNodeFromString(String string) {
		try {
			return createLoader(new StringReader(string)).load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return null;
	}

	class VanillaNBT implements CompoundNBT {

		@Override
		public void remove(String key) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.hasTag() || !nmsStack.getOrCreateTag().contains(key)) return;
			nmsStack.getOrCreateTag().remove(key);
			if(nmsStack.getOrCreateTag().isEmpty()) {
				itemStack = ItemStack.of(getOptItemType().get());
				itemStack.setQuantity(itemQuantity);
			} else {
				serialize((ItemStack) ((Object) nmsStack));
			}
		}

		@Override
		public boolean containsTag(String key) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			return nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key);
		}

		@Override
		public void putString(String key, String value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putString(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putUUID(String key, UUID value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putUUID(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putShort(String key, short value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putShort(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putInteger(String key, int value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putInt(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLong(String key, long value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putLong(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putFloat(String key, float value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putFloat(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putDouble(String key, double value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putDouble(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putByte(String key, byte value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putByte(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putBoolean(String key, boolean value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putBoolean(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putIntArray(String key, int[] value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putIntList(String key, List<Integer> value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLongArray(String key, long[] value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLongList(String key, List<Long> value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putByteArray(String key, byte[] value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			nmsStack.getOrCreateTag().putByteArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putTag(String key, CompoundTag tag) {
			putString("CustomTags", createStringFromCustomTag(key, tag, new StringWriter()));
		}

		@Override
		public Set<String> getAllKeys() {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			return nmsStack.getOrCreateTag().getAllKeys();
		}

		@Override
		public Optional<String> getString(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getString(key)) : Optional.empty();
		}

		@Override
		public Optional<UUID> getUUID(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getUUID(key)) : Optional.empty();
		}

		@Override
		public Optional<Short> getShort(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getShort(key)) : Optional.empty();
		}

		@Override
		public Optional<Integer> getInteger(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getInt(key)) : Optional.empty();
		}

		@Override
		public Optional<Long> getLong(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getLong(key)) : Optional.empty();
		}

		@Override
		public Optional<Float> getFloat(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getFloat(key)) : Optional.empty();
		}

		@Override
		public Optional<Double> getDouble(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getDouble(key)) : Optional.empty();
		}

		@Override
		public Optional<Byte> getByte(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getByte(key)) : Optional.empty();
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getBoolean(key)) : Optional.empty();
		}

		@Override
		public Optional<int[]> getIntArray(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getIntArray(key)) : Optional.empty();
		}

		@Override
		public Optional<List<Integer>> getIntList(String key) {
			return getIntArray(key).isPresent() ? Optional.ofNullable(Arrays.stream(getIntArray(key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
		}

		@Override
		public Optional<long[]> getLongArray(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getLongArray(key)) : Optional.empty();
		}

		@Override
		public Optional<List<Long>> getLongList(String key) {
			return getLongArray(key).isPresent() ? Optional.ofNullable(Arrays.stream(getLongArray(key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
		}

		@Override
		public Optional<byte[]> getByteArray(String key) {
			return containsTag(key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getByteArray(key)) : Optional.empty();
		}

		@Override
		public Optional<CompoundTag> getTag(String key, Class<CompoundTag> clazz) {
			return !getString("CustomTags").isPresent() || clazz == null ? Optional.empty() : createTagFromString(getString("CustomTags").get(), clazz);
		}

		@Override
		public int size() {
			return getVanillaStack().getOrCreateTag().size();
		}

		private net.minecraft.world.item.ItemStack getVanillaStack() {
			return (net.minecraft.world.item.ItemStack) ((Object) getItemStack());
		}
		
	}
/*
	class ForgeNBT implements CompoundNBT {

		@Override
		public void remove(String key) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			if(!nmsStack.hasTag() || !nmsStack.getOrCreateTag().contains(key)) return;
			nmsStack.getOrCreateTag().remove(key);
			if(nmsStack.getOrCreateTag().isEmpty()) {
				itemStack = ItemStack.of(getOptItemType().get());
				itemStack.setQuantity(itemQuantity);
			} else {
				serialize((ItemStack) ((Object) nmsStack));
			}
		}

		@Override
		public boolean containsTag(String key) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			return nmsStack.hasTag() && nmsStack.getOrCreateTag().contains(key);
		}

		@Override
		public void putString(String key, String value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putString(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putUUID(String key, UUID value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putUUID(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putShort(String key, short value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putShort(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putInteger(String key, int value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putInt(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLong(String key, long value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putLong(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putFloat(String key, float value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putFloat(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putDouble(String key, double value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putDouble(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putByte(String key, byte value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putByte(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putBoolean(String key, boolean value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putBoolean(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putIntArray(String key, int[] value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putIntList(String key, List<Integer> value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLongArray(String key, long[] value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLongList(String key, List<Long> value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putByteArray(String key, byte[] value) {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			nmsStack.getOrCreateTag().putByteArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putTag(String key, CompoundTag tag) {
			putString(key, createStringFromCustomTag(key, tag));
		}

		@Override
		public Set<String> getAllKeys() {
			net.minecraft.item.ItemStack nmsStack = getForgeStack();
			return nmsStack.getOrCreateTag().getAllKeys();
		}

		@Override
		public Optional<String> getString(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getString(key)) : Optional.empty();
		}

		@Override
		public Optional<UUID> getUUID(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getUUID(key)) : Optional.empty();
		}

		@Override
		public Optional<Short> getShort(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getShort(key)) : Optional.empty();
		}

		@Override
		public Optional<Integer> getInteger(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getInt(key)) : Optional.empty();
		}

		@Override
		public Optional<Long> getLong(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getLong(key)) : Optional.empty();
		}

		@Override
		public Optional<Float> getFloat(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getFloat(key)) : Optional.empty();
		}

		@Override
		public Optional<Double> getDouble(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getDouble(key)) : Optional.empty();
		}

		@Override
		public Optional<Byte> getByte(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getByte(key)) : Optional.empty();
		}

		@Override
		public Optional<Boolean> getBoolean(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getBoolean(key)) : Optional.empty();
		}

		@Override
		public Optional<int[]> getIntArray(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getIntArray(key)) : Optional.empty();
		}

		@Override
		public Optional<List<Integer>> getIntList(String key) {
			return getIntArray(key).isPresent() ? Optional.ofNullable(Arrays.stream(getIntArray(key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
		}

		@Override
		public Optional<long[]> getLongArray(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getLongArray(key)) : Optional.empty();
		}

		@Override
		public Optional<List<Long>> getLongList(String key) {
			return getLongArray(key).isPresent() ? Optional.ofNullable(Arrays.stream(getLongArray(key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
		}

		@Override
		public Optional<byte[]> getByteArray(String key) {
			return containsTag(key) ? Optional.ofNullable(getForgeStack().getOrCreateTag().getByteArray(key)) : Optional.empty();
		}

		@Override
		public Optional<CompoundTag> getTag(String key, Class<CompoundTag> clazz) {
			return !getString(key).isPresent() || clazz == null ? Optional.empty() : createTagFromString(getString(key).get(), clazz);
		}

		@Override
		public int size() {
			return getForgeStack().getOrCreateTag().size();
		}

		private net.minecraft.item.ItemStack getForgeStack() {
			return (net.minecraft.item.ItemStack) ((Object) getItemStack());
		}

	}
*/
}
