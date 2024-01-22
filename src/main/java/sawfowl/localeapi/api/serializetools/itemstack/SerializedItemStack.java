package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
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
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.key.Key;

import sawfowl.localeapi.api.serializetools.SerializeOptions;

@ConfigSerializable
@Deprecated
/**
 * Use {@link SerializedItemStackPlainNBT}
 */
public class SerializedItemStack {

	SerializedItemStack(){}

	/**
	 * Use {@link SerializedItemStackPlainNBT}
	 */
	public SerializedItemStack(ItemStack itemStack) {
		serialize(itemStack);
	}


	/**
	 * Use {@link SerializedItemStackPlainNBT}
	 */
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

	public String getItemTypeAsString() {
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
		if(getItemType().isPresent()) {
			itemStack = ItemStack.of(getItemType().get());
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
		} else itemStack = ItemStack.empty();
		return itemStack.copy();
	}

	/**
	 * Getting {@link ItemType}
	 */
	public Optional<ItemType> getItemType() {
		return Sponge.game().registry(RegistryTypes.ITEM_TYPE).findValue(ResourceKey.resolve(itemType));
	}

	/**
	 * The resulting value can be used to display the item in chat.
	 */
	public Key getItemKey() {
		return getItemType().isPresent() ? Key.key(itemType) : Key.key("air");
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

	public SerializedItemStackJsonNbt toSerializedItemStackJsonNbt() {
		return new SerializedItemStackJsonNbt(itemType, 0, nbt == null ? null : JsonParser.parseString(nbt).getAsJsonObject());
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
				nbt = DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.itemStack = itemStack;
	}

	private String createStringFromCustomTag(CompoundTag tag) {
		StringWriter sink = new StringWriter();
		GsonConfigurationLoader loader = createWriter(sink);
		ConfigurationNode node = loader.createNode();
		try {
			if(tag.toJsonObject() == null) {
				node.set(CompoundTag.class, tag);
				if(!node.node("__class__").virtual()) node.removeChild("__class__");
			} else node.set(JsonObject.class, tag.toJsonObject());
			loader.save(node);
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		node = null;
		loader = null;
		return sink.toString();
	}

	private <T extends CompoundTag> Optional<T> createTagFromString(String string, Class<T> clazz) {
		try {
			return tagFromNode(serializeNodeFromString(string), clazz);
		} catch (ConfigurateException e) {
			e.printStackTrace();
			return Optional.empty();
		}
	}

	private <T extends CompoundTag> Optional<T> tagFromNode(ConfigurationNode node, Class<T> clazz) throws SerializationException {
		return node.virtual() || node.empty() ? Optional.empty() : Optional.ofNullable(node.get(clazz));
	}

	private GsonConfigurationLoader createWriter(StringWriter sink) {
		return GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(1)).sink(() -> new BufferedWriter(sink)).build();
	}

	private GsonConfigurationLoader createLoader(StringReader source) {
		return GsonConfigurationLoader.builder().defaultOptions(SerializeOptions.selectOptions(1)).source(() -> new BufferedReader(source)).build();
	}

	private ConfigurationNode serializeNodeFromString(String string) {
		try {
			return createLoader(new StringReader(string)).load();
		} catch (ConfigurateException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getPluginId(PluginContainer container) {
		return container.metadata().id();
	}

	class VanillaNBT implements CompoundNBT {

		@Override
		public void remove(PluginContainer container, String key) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!containsTag(container, key)) return;
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).remove(key);
			if(nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).isEmpty()) nmsStack.getOrCreateTag().getCompound("PluginTags").remove(getPluginId(container));
			if(nmsStack.getOrCreateTag().getCompound("PluginTags").isEmpty()) nmsStack.getOrCreateTag().remove("PluginTags");
			if(nmsStack.getOrCreateTag().isEmpty()) {
				itemStack = ItemStack.of(getItemType().get());
				itemStack.setQuantity(itemQuantity);
			} else {
				serialize((ItemStack) ((Object) nmsStack));
			}
		}

		@Override
		public boolean containsTag(PluginContainer container, String key) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			return nmsStack.hasTag() && nmsStack.getOrCreateTag().contains("PluginTags") && nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container)) && nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).contains(key);
		}

		@Override
		public void putString(PluginContainer container, String key, String value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putString(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putUUID(PluginContainer container, String key, UUID value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putUUID(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putShort(PluginContainer container, String key, short value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putShort(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putInteger(PluginContainer container, String key, int value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putInt(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLong(PluginContainer container, String key, long value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putLong(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putFloat(PluginContainer container, String key, float value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putFloat(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putDouble(PluginContainer container, String key, double value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putDouble(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putByte(PluginContainer container, String key, byte value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putByte(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putBoolean(PluginContainer container, String key, boolean value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putBoolean(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putIntArray(PluginContainer container, String key, int[] value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putIntList(PluginContainer container, String key, List<Integer> value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putIntArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLongArray(PluginContainer container, String key, long[] value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putLongList(PluginContainer container, String key, List<Long> value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putLongArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putByteArray(PluginContainer container, String key, byte[] value) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			if(!nmsStack.getOrCreateTag().contains("PluginTags")) nmsStack.getOrCreateTag().put("PluginTags", new net.minecraft.nbt.CompoundTag());
			if(!nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container))) nmsStack.getOrCreateTag().getCompound("PluginTags").put(getPluginId(container), new net.minecraft.nbt.CompoundTag());
			nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).putByteArray(key, value);
			serialize((ItemStack) ((Object) nmsStack));
		}

		@Override
		public void putTag(PluginContainer container, String key, CompoundTag tag) {
			putString(container, key, createStringFromCustomTag(tag));
		}

		@Override
		public Set<String> getAllKeys(PluginContainer container) {
			net.minecraft.world.item.ItemStack nmsStack = getVanillaStack();
			return nmsStack.getOrCreateTag().contains("PluginTags") && nmsStack.getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container)) ? nmsStack.getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getAllKeys() : new HashSet<String>();
		}

		@Override
		public Optional<String> getString(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getString(key)) : Optional.empty();
		}

		@Override
		public Optional<UUID> getUUID(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getUUID(key)) : Optional.empty();
		}

		@Override
		public Optional<Short> getShort(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getShort(key)) : Optional.empty();
		}

		@Override
		public Optional<Integer> getInteger(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getInt(key)) : Optional.empty();
		}

		@Override
		public Optional<Long> getLong(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getLong(key)) : Optional.empty();
		}

		@Override
		public Optional<Float> getFloat(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getFloat(key)) : Optional.empty();
		}

		@Override
		public Optional<Double> getDouble(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getDouble(key)) : Optional.empty();
		}

		@Override
		public Optional<Byte> getByte(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getByte(key)) : Optional.empty();
		}

		@Override
		public Optional<Boolean> getBoolean(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getBoolean(key)) : Optional.empty();
		}

		@Override
		public Optional<int[]> getIntArray(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getIntArray(key)) : Optional.empty();
		}

		@Override
		public Optional<List<Integer>> getIntList(PluginContainer container, String key) {
			return getIntArray(container, key).isPresent() ? Optional.ofNullable(Arrays.stream(getIntArray(container, key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
		}

		@Override
		public Optional<long[]> getLongArray(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getLongArray(key)) : Optional.empty();
		}

		@Override
		public Optional<List<Long>> getLongList(PluginContainer container, String key) {
			return getLongArray(container, key).isPresent() ? Optional.ofNullable(Arrays.stream(getLongArray(container, key).get()).boxed().collect(Collectors.toList())) : Optional.empty();
		}

		@Override
		public Optional<byte[]> getByteArray(PluginContainer container, String key) {
			return containsTag(container, key) ? Optional.ofNullable(getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).getByteArray(key)) : Optional.empty();
		}

		@Override
		public <T extends CompoundTag> Optional<T> getTag(PluginContainer container, String key, Class<T> clazz) {
			return !getString(container, key).isPresent() || clazz == null ? Optional.empty() : createTagFromString(getString(container, key).get(), clazz);
		}

		@Override
		public int size(PluginContainer container) {
			return getVanillaStack().getOrCreateTag().contains("PluginTags") && getVanillaStack().getOrCreateTag().getCompound("PluginTags").contains(getPluginId(container)) ? getVanillaStack().getOrCreateTag().getCompound("PluginTags").getCompound(getPluginId(container)).size() : 0;
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