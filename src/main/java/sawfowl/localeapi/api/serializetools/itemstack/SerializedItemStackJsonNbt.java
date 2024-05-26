package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import net.kyori.adventure.key.Key;
import sawfowl.localeapi.api.ClassUtils;
import sawfowl.localeapi.api.serializetools.SerializeOptions;

/**
 * The class is intended for working with item data when it is necessary to access it before registering item data in the registry.
 */
@ConfigSerializable
public class SerializedItemStackJsonNbt implements PluginComponent {

	SerializedItemStackJsonNbt(){}

		public SerializedItemStackJsonNbt(ItemStack itemStack) {
			serialize(itemStack);
		}

		public SerializedItemStackJsonNbt(BlockState block) {
			if(block.type().item().isPresent()) {
				serialize(ItemStack.of(block.type().item().get(), 1));
				if(block.toContainer().get(DataQuery.of(ComponentUtil.COMPONENTS)).isPresent()) {
					try {
						JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write((DataView) block.toContainer().get(DataQuery.of(ComponentUtil.COMPONENTS)).get()));
						components = json.isJsonObject() ? json.getAsJsonObject() : null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} serialize(ItemStack.of(ItemTypes.AIR.get(), 1));
		}

		public SerializedItemStackJsonNbt(BlockSnapshot block) {
			if(block.state().type().item().isPresent()) {
				serialize(ItemStack.of(block.state().type().item().get(), 1));
				if(block.toContainer().get(DataQuery.of(ComponentUtil.COMPONENTS)).isPresent()) {
					try {
						JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write((DataView) block.toContainer().get(DataQuery.of(ComponentUtil.COMPONENTS)).get()));
						components = json.isJsonObject() ? json.getAsJsonObject() : null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} serialize(ItemStack.of(ItemTypes.AIR.get(), 1));
		}

		public SerializedItemStackJsonNbt(String type, int quantity, JsonObject nbt) {
			itemType = type;
			itemQuantity = quantity;
			this.components = nbt;
		}

		@Setting("ItemType")
		private String itemType;
		@Setting("Quantity")
		private Integer itemQuantity;
		@Setting("ComponentsMap")
		private JsonObject components;
		@Setting("NBT")
		private JsonObject nbt;
		private transient ItemStack itemStack;
		private transient ComponentUtil tagUtil;
		private transient DataContainer itemContainer = null;

		/**
		 * Getting {@link ItemStack}
		 */
		public ItemStack getItemStack() {
			if(itemStack != null) return itemStack.copy();
			if(getItemType().isPresent()) {
				itemStack = ItemStack.of(getItemType().get());
				itemStack.setQuantity(itemQuantity);
				if(itemContainer == null) itemContainer = itemStack.toContainer();
				if(nbt != null) {
					try {
						itemStack = ItemStack.builder()
								.fromContainer(
								itemStack.toContainer()
								.set(DataQuery.of("UnsafeData"), DataFormats.JSON.get().read(nbt.toString()))
								).build();
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

		public String getItemTypeAsString() {
			return itemType;
		}

		/**
		 * The method returns a copy of the item's NBT tag collection in Json format.
		 */
		public JsonElement getComponents() {
			return components == null ? null : components.deepCopy();
		}

		public Integer getQuantity() {
			return itemQuantity;
		}

		/**
		 * The resulting value can be used to display the item in chat.
		 */
		public Key getItemKey() {
			return getItemType().isPresent() ? Key.key(itemType) : Key.key("air");
		}

		/**
		 * Changing {@link ItemStack} volume.
		 */
		public void setQuantity(int quantity) {
			itemQuantity = quantity;
		}

		/**
		 * Gaining access to the NBT tags of an item.
		 */
		public ComponentUtil getOrCreateComponent() {
			return tagUtil == null ? tagUtil = new EditNBT() : tagUtil;
		}

		public SerializedItemStackPlainNBT toSerializedItemStackPlainNBT() {
			return new SerializedItemStackPlainNBT(itemType, 0, components.toString());
		}

		@Override
		public JsonObject toJsonObject() {
			JsonObject object = new JsonObject();
			object.addProperty("ItemType", itemType);
			object.addProperty("Quantity", itemQuantity);
			if(components != null) object.add("ComponentsMap", components);
			return object;
		}

		@Override
		public String toString() {
			return  "ItemType: " + itemType +
					", Quantity: " + itemQuantity + 
					", ComponentsMap: " + components.toString();
		}

		private void serialize(ItemStack itemStack) {
			itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
			itemQuantity = itemStack.quantity();
			if(itemStack.toContainer().get(DataQuery.of(ComponentUtil.COMPONENTS)).isPresent()) {
				try {
					components = JsonParser.parseString(DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of(ComponentUtil.COMPONENTS)).get())).getAsJsonObject();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.itemStack = itemStack;
		}

		private class EditNBT implements ComponentUtil {

		EditNBT() {
			checkContainer();
		}

		private void updateNbt() {
			itemStack = null;
			try {
				components = JsonParser.parseString(DataFormats.JSON.get().write(itemContainer)).getAsJsonObject();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public <T> ComponentUtil putObject(PluginContainer container, String key, T object) {
			if(!ClassUtils.isPrimitiveOrBasicDataClass(object)) {
				try {
					throw new RuntimeException("This method accepts only primitives, or Java base data classes: '" + ClassUtils.getValuesToString() + "'.");
				} catch (Exception e) {
				}
			} else {
				checkContainer();
				itemContainer.set(createPath(container, key), object);
				updateNbt();
			}
			return this;
		}

		@Override
		public <T> ComponentUtil putObjects(PluginContainer container, String key, List<T> objects) {
			if(objects.isEmpty()) return this;
			checkContainer();
			objects = objects.stream().filter(object -> ClassUtils.isPrimitiveOrBasicDataClass(object)).toList();
			if(objects.isEmpty()) return this;
			itemContainer.set(createPath(container, key), objects);
			updateNbt();
			return this;
		}

		@Override
		public <K, V> ComponentUtil putObjects(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> objects) {
			if(objects.isEmpty()) return this;
			checkContainer();
			itemContainer.set(createPath(container, key), objects);
			updateNbt();
			return this;
		}

		@Override
		public <T extends PluginComponent> ComponentUtil putPluginComponent(PluginContainer container, String key, T object) {
			JsonObject json = null;
			if(object.toJsonObject() != null) {
				json = object.toJsonObject();
			} else try {
				json = BasicConfigurationNode.root(SerializeOptions.OPTIONS_VARIANT_2).set(object.getClass(), object).get(JsonObject.class);
			} catch (SerializationException e) {
				e.printStackTrace();
			}
			if(json == null) return this;
			checkContainer();
			putJsonObject(json, COMPONENTS, CUSTOM_DATA, PLUGINCOMPONENTS, getPluginId(container), key);
			itemStack = null;
			getItemStack();
			return this;
		}

		@Override
		public ComponentUtil removeComponent(PluginContainer container, String key) {
			checkContainer();
			if(itemContainer.contains(createPath(container, key))) itemContainer.remove(DataQuery.of(PLUGINCOMPONENTS, getPluginId(container), key));
			updateNbt();
			return this;
		}

		@Override
		public boolean containsComponent(PluginContainer container, String key) {
			return getItemStack().toContainer().contains(createPath(container, key));
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T> T getObject(PluginContainer container, String key, T def) {
			if(containsComponent(container, key)) {
				Object object = getItemStack().toContainer().get(createPath(container, key)).get();
				if(!object.getClass().isAssignableFrom(def.getClass())) {
					try {
						return (T) BasicConfigurationNode.root(SerializeOptions.OPTIONS_VARIANT_2).set(object).get(def.getClass());
					} catch (SerializationException e) {
					}
				} else return (T) object;
			}
			return def;
		}

		@Override
		public <T> List<T> getObjectsList(Class<T> clazz, PluginContainer container, String key, List<T> def) {
			if(containsComponent(container, key)) {
				Object object = getItemStack().toContainer().get(createPath(container, key)).get();
				if(object instanceof Collection) {
					Collection<?> objects = ((Collection<?>) object);
					if(objects.isEmpty()) return def;
					List<T> result = new ArrayList<T>();
					objects.forEach(o -> convert(o, clazz).ifPresent(result::add));
					object = null;
					objects = null;
					if(!result.isEmpty()) return result;
				} 
			}
			return def;
		}

		@SuppressWarnings("unchecked")
		@Override
		public <K, V> Map<K, V> getObjectsMap(Class<K> mapKey, Class<V> mapValue, PluginContainer container, String key, Map<K, V> def) {
			if(containsComponent(container, key)) {
				Object object = getItemStack().toContainer().get(createPath(container, key)).get();
				if(object instanceof DataView dataView) {
					@SuppressWarnings("rawtypes")
					Map objects = new HashMap<>();
					for(DataQuery childKey : dataView.keys(false)) {
						Optional<Object> o = dataView.get(childKey);
						if(o.isPresent() && childKey.iterator().hasNext() && ClassUtils.isPrimitiveOrBasicDataClass(o.get())) objects.put(childKey.iterator().next(), o.get());
						o = null;
					}
					Map<K, V> result = new HashMap<K, V>();
					objects.forEach((k, v) -> convert(k, mapKey).ifPresent(rk -> convert(v, mapValue).ifPresent(rv -> result.put(rk, rv))));
					object = null;
					objects = null;
					if(!result.isEmpty()) return result;
				}
			}
			return def;
		}

		@Override
		public <T extends PluginComponent> Optional<T> getPluginComponent(Class<T> clazz, PluginContainer container, String key) {
			try {
				return Optional.ofNullable(BasicConfigurationNode.root(SerializeOptions.OPTIONS_VARIANT_2).set(components).node(CUSTOM_DATA, PLUGINCOMPONENTS, getPluginId(container), key).get(clazz));
			} catch (SerializationException | JsonSyntaxException e) {
				return Optional.empty();
			}
		}

		@Override
		public Set<String> getAllKeys(PluginContainer container) {
			return getItemStack().toContainer().get(createPath(COMPONENTS, CUSTOM_DATA, PLUGINCOMPONENTS, getPluginId(container))).map(data -> ((DataView) data).keys(false).stream().map(q -> q.asString(';')).collect(Collectors.toSet())).orElse(new HashSet<String>());
		}

		@Override
		public int size(PluginContainer container) {
			return getItemStack().toContainer().get(createPath(COMPONENTS, CUSTOM_DATA, PLUGINCOMPONENTS, getPluginId(container))).map(data -> ((DataView) data).keys(false).size()).orElse(0);
		}

		private String getPluginId(PluginContainer container) {
			return container.metadata().id();
		}

		private DataQuery createPath(PluginContainer plugin, String key) {
			return DataQuery.of(COMPONENTS, CUSTOM_DATA, PLUGINCOMPONENTS, getPluginId(plugin), key);
		}

		private DataQuery createPath(String... path) {
			return DataQuery.of(path);
		}

		private void checkContainer() {
			if(itemContainer == null) itemContainer = getItemStack().toContainer();
		}

		@SuppressWarnings("unchecked")
		private <O, T> Optional<T> convert(O original, Class<T> clazz) {
			try {
				return Optional.ofNullable((T) original);
			} catch (Exception e) {
				try {
					if(clazz.isAssignableFrom(Number.class) && original.getClass().isAssignableFrom(CharSequence.class) && NumberUtils.isParsable(original.toString())) {
						return Optional.ofNullable(BasicConfigurationNode.root(SerializeOptions.OPTIONS_VARIANT_2).set(NumberUtils.createNumber(original.toString())).get(clazz));
					} else return Optional.ofNullable(BasicConfigurationNode.root(SerializeOptions.OPTIONS_VARIANT_2).set(original).get(clazz));
				} catch (SerializationException e1) {
					return Optional.empty();
				}
			}
		}

		private void putJsonObject(JsonObject object, String... keys) {
			object.asMap().forEach((k,v) -> putJson(v, ArrayUtils.add(keys, k)));
		}

		private void putJson(JsonElement element, String... keys) {
			if(element.isJsonArray()) {
				putJsonArray(element.getAsJsonArray(), 0, keys);
			} else if(element.isJsonObject()) {
				putJsonObject(element.getAsJsonObject(), keys);
			} else if(element.isJsonPrimitive()) putPrimitive(element.getAsJsonPrimitive(), keys);
		}

		private void putJsonArray(JsonArray array, int arrayNumber, String... keys) {
			List<Object> objects = new ArrayList<>();
			array.asList().forEach(element -> {
				if(element.isJsonArray()) {
					putJsonArray(element.getAsJsonArray(), arrayNumber + 1, ArrayUtils.add(keys, "" + arrayNumber));
				} else if(element.isJsonObject()) {
					element.getAsJsonObject().asMap().forEach((k, v) -> {
						putJsonObject(v.getAsJsonObject(), ArrayUtils.add(keys, k));
					});
				} else if(element.isJsonPrimitive()) {
					if(element.getAsJsonPrimitive().isNumber()) {
						objects.add(element.getAsJsonPrimitive().getAsNumber());
					} else if(element.getAsJsonPrimitive().isBoolean()) {
						objects.add(element.getAsJsonPrimitive().getAsBoolean());
					} else if(element.getAsJsonPrimitive().isString()) {
						objects.add(element.getAsJsonPrimitive().getAsString());
					}
				}
			});
		}

		private void putPrimitive(JsonPrimitive primitive, String... keys) {
			if(primitive.isNumber()) {
				itemContainer.set(createPath(keys), primitive.getAsNumber());
			} else if(primitive.isBoolean()) {
				itemContainer.set(createPath(keys), primitive.getAsBoolean());
			} else if(primitive.isString()) {
				itemContainer.set(createPath(keys), primitive.getAsString());
			}
		}

	}

}
