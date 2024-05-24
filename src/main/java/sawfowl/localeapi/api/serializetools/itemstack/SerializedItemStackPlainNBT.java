package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.key.Key;

import sawfowl.localeapi.api.ClassUtils;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

/**
 * The class is intended for working with item data when it is necessary to access it before registering item data in the registry.
 */
@ConfigSerializable
public class SerializedItemStackPlainNBT implements CompoundTag {

	SerializedItemStackPlainNBT(){}

	public SerializedItemStackPlainNBT(ItemStack itemStack) {
		serialize(itemStack);
	}

	public SerializedItemStackPlainNBT(BlockState block) {
		if(block.type().item().isPresent()) {
			serialize(ItemStack.of(block.type().item().get(), 1));
			if(block.toContainer().get(DataQuery.of("components")).isPresent()) {
				try {
					components = DataFormats.JSON.get().write((DataView) block.toContainer().get(DataQuery.of("components")).get());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} serialize(ItemStack.of(ItemTypes.AIR.get(), 1));
	}

	public SerializedItemStackPlainNBT(BlockSnapshot block) {
		if(block.state().type().item().isPresent()) {
			serialize(ItemStack.of(block.state().type().item().get(), 1));
			if(block.toContainer().get(DataQuery.of("components")).isPresent()) {
				try {
					components = DataFormats.JSON.get().write((DataView) block.toContainer().get(DataQuery.of("components")).get());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} serialize(ItemStack.of(ItemTypes.AIR.get(), 1));
	}

	public SerializedItemStackPlainNBT(String type, int quantity, String nbt) {
		itemType = type;
		itemQuantity = quantity;
		this.components = nbt;
	}

	@Setting("ItemType")
	private String itemType;
	@Setting("Quantity")
	private Integer itemQuantity;
	@Setting("ComponentsMap")
	private String components;
	@Setting("NBT")
	private String nbt;
	private transient ItemStack itemStack;
	private transient TagUtil.Advanced tagUtil;
	private transient DataContainer itemContainer = DataContainer.createNew();

	public String getItemTypeAsString() {
		return itemType;
	}

	/**
	 * Getting {@link ItemStack} volume.
	 */
	public Integer getQuantity() {
		return itemQuantity;
	}

	/**
	 * Get all tags as a string.
	 */
	public String getComponents() {
		return components != null ? components : "";
	}

	/**
	 * Getting {@link ItemStack}
	 */
	public ItemStack getItemStack() {
		if(tagUtil != null) {
			tagUtil = null;
		}
		if(itemStack != null) return itemStack.copy();
		if(getItemType().isPresent()) {
			itemStack = ItemStack.of(getItemType().get());
			itemStack.setQuantity(itemQuantity);
			if(itemContainer == null) itemContainer = itemStack.toContainer();
			if(nbt != null && !nbt.equals("")) {
				try {
					itemContainer.set(DataQuery.of("UnsafeData"), DataFormats.JSON.get().read(nbt));
				} catch (InvalidDataException | IOException e) {
					e.printStackTrace();
				}
			}
			itemStack = ItemStack.builder().fromContainer(itemContainer).build();
		} else itemStack = ItemStack.empty();
		itemContainer = null;
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
	 * Gaining access to the NBT tags of an item.
	 */
	public TagUtil.Advanced getOrCreateTag() {
		return tagUtil == null ? tagUtil = new EditNBT() : tagUtil;
	}

	/**
	 * Changing {@link ItemStack} volume.
	 */
	public void setQuantity(int quantity) {
		itemQuantity = quantity;
	}

	public SerializedItemStackJsonNbt toSerializedItemStackJsonNbt() {
		return new SerializedItemStackJsonNbt(itemType, 0, components == null ? null : JsonParser.parseString(components).getAsJsonObject());
	}

	@Override
	public int hashCode() {
		return Objects.hash(itemQuantity, itemStack, itemType, components);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof SerializedItemStackPlainNBT)) return false;
		SerializedItemStackPlainNBT other = (SerializedItemStackPlainNBT) obj;
		return Objects.equals(itemQuantity, other.itemQuantity) && Objects.equals(itemType, other.itemType) && Objects.equals(components, other.components);
	}

	public boolean equalsWhithoutQuantity(SerializedItemStackPlainNBT itemStack) {
		return this == itemStack || (Objects.equals(itemType, itemStack.itemType) && Objects.equals(components, itemStack.components));
	}

	public boolean equalsWhithoutNBT(SerializedItemStackPlainNBT itemStack) {
		return this == itemStack || (Objects.equals(itemType, itemStack.itemType) && Objects.equals(itemQuantity, itemStack.itemQuantity));
	}

	public boolean equalsToItemStack(ItemStack itemStack) {
		return equals(new SerializedItemStackPlainNBT(itemStack));
	}

	@Override
	public String toString() {
		return  "ItemType: " + itemType +
				", Quantity: " + itemQuantity + 
				", ComponentsMap: " + components;
	}

	private void serialize(ItemStack itemStack) {
		itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
		itemQuantity = itemStack.quantity();
		if(itemStack.toContainer().get(DataQuery.of("components")).isPresent()) {
			try {
				components = DataFormats.JSON.get().write((DataView) (itemContainer = itemStack.toContainer()).get(DataQuery.of("components")).get());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.itemStack = itemStack;
	}

	@Override
	public JsonObject toJsonObject() {
		JsonObject object = new JsonObject();
		object.addProperty("ItemType", itemType);
		object.addProperty("Quantity", itemQuantity);
		if(components != null) object.addProperty("ComponentsMap", components);
		return object;
	}

	class EditNBT implements TagUtil.Advanced {

		EditNBT() {
			checkContainer();
		}

		private void updateNbt() {
			itemStack = null;
			try {
				components = DataFormats.JSON.get().write(itemContainer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public <T> void putObject(PluginContainer container, String key, T object) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(object)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (Exception e) {
				}
				return;
			} else {
				checkContainer();
				itemContainer.set(createPath(getPluginId(container), key.toLowerCase()), object);
				updateNbt();
			}
		}

		@Override
		public <T> void putObjects(PluginContainer container, String key, List<T> objects) {
			if(objects.isEmpty()) return;
			checkContainer();
			objects = objects.stream().filter(object -> ClassUtils.isPrimitiveOrBasicDataClass(object)).toList();
			if(objects.isEmpty()) return;
			itemContainer.set(createPath(getPluginId(container), key.toLowerCase()), objects);
			updateNbt();
		}

		@Override
		public <K, V> void putObjects(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects) {
			if(objects.isEmpty()) return;
			checkContainer();
			objects.forEach((k,v) -> {
				if(ClassUtils.isPrimitiveOrBasicDataClass(k) && ClassUtils.isPrimitiveOrBasicDataClass(v)) {
					itemContainer.set(createPath(getPluginId(container), key.toLowerCase(), k.toString().toLowerCase()), v);
				}
			});
			updateNbt();
		}

		@Override
		public <T extends CompoundTag> void putCompoundTag(PluginContainer container, String key, T object) {
			if(object.toJsonObject() != null) {
				checkContainer();
				itemContainer.set(createPath(getPluginId(container), key.toLowerCase()), object.toJsonObject());
			} else try {
				checkContainer();
				itemContainer.set(createPath(getPluginId(container), key.toLowerCase()), BasicConfigurationNode.root(SerializeOptions.OPTIONS_VARIANT_2).set(object.getClass(), object).get(JsonElement.class));
			} catch (SerializationException e) {
				e.printStackTrace();
			}
			updateNbt();
		}

		@Override
		public void removeTag(PluginContainer container, String key) {
			checkContainer();
			if(itemContainer.contains(DataQuery.of("plugintags", getPluginId(container), "tag:" + key.toLowerCase()))) itemContainer.remove(DataQuery.of("plugintags", getPluginId(container), key));
			updateNbt();
		}

		@Override
		public boolean containsTag(PluginContainer container, String key) {
			return getItemStack().toContainer().contains(DataQuery.of("plugintags", getPluginId(container), "tag:" + key.toLowerCase()));
		}

		@Override
		public <T> T getObject(Class<T> clazz, PluginContainer container, String key, T def) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(clazz)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (Exception e) {
				}
				return def;
			} /*else if(components != null && node != null && !node.node("plugintags", getPluginId(container), "tag:" + key.toLowerCase()).virtual()) {
				try {
					return node.node("plugintags", getPluginId(container), "tag:" + key.toLowerCase()).get(clazz, def);
				} catch (SerializationException e) {
					e.printStackTrace();
				}
			}*/
			return def;
		}

		@Override
		public <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(clazz)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (RuntimeException e) {
				}
				return def;
			}/* else if(components != null && node != null && !node.node("plugintags", getPluginId(container), "tag:" + key.toLowerCase()).virtual()) {
				try {
					return node.node("plugintags", getPluginId(container), "tag:" + key.toLowerCase()).getList(clazz, def);
				} catch (SerializationException e) {
					e.printStackTrace();
				}
			}*/
			return def;
		}

		@Override
		public <K, V> Map<K, V> getObjectsMap(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects) {
			/*if(containsTag(container, "tag:" + key.toLowerCase())) {
				return node.node("plugintags", getPluginId(container), "tag:" + key.toLowerCase()).childrenMap().entrySet().stream().collect(Collectors.toMap(entry -> (K) entry.getKey(), entry -> {
					try {
						return entry.getValue().get(mapValue);
					} catch (SerializationException e) {
						e.printStackTrace();
					}
					return null;
				}));
			}*/
			return objects;
		}

		@Override
		public <T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key) {
			/*try {
				return containsTag(container, "tag:" + key.toLowerCase()) ? Optional.ofNullable(node.get(clazz)) : Optional.empty();
			} catch (SerializationException e) {
				e.printStackTrace();
			}*/
			return Optional.empty();
		}

		@Override
		public Set<String> getAllKeys(PluginContainer container) {
			return getItemStack().toContainer().get(createPath("components", "minecraft:custom_data", "plugintags", getPluginId(container))).map(data -> ((DataView) data).keys(false).stream().map(q -> q.asString('.')).collect(Collectors.toSet())).orElse(new HashSet<String>());
		}

		@Override
		public int size(PluginContainer container) {
			return getItemStack().toContainer().get(createPath("components", "minecraft:custom_data", "plugintags", getPluginId(container))).map(data -> ((DataView) data).keys(false).size()).orElse(0);
		}

		private String getPluginId(PluginContainer container) {
			return container.metadata().id();
		}

		private DataQuery createPath(String plugin, String key) {
			return DataQuery.of("components", "minecraft:custom_data", "plugintags", plugin, key);
		}

		private DataQuery createPath(String... path) {
			return DataQuery.of(path);
		}

		private void checkContainer() {
			if(itemContainer == null) itemContainer = getItemStack().toContainer();
		}

	}

}