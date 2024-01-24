package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.BasicConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.plugin.PluginContainer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.key.Key;

import sawfowl.localeapi.api.serializetools.SerializeOptions;

/**
 * The class is intended for working with item data when it is necessary to access it before registering item data in the registry.
 */
@ConfigSerializable
public class SerializedItemStackJsonNbt {

	SerializedItemStackJsonNbt(){}

		public SerializedItemStackJsonNbt(ItemStack itemStack) {
			serialize(itemStack);
		}

		public SerializedItemStackJsonNbt(String type, int quantity, JsonObject nbt) {
			itemType = type;
			itemQuantity = quantity;
			this.nbt = nbt;
		}

		@Setting("ItemType")
		private String itemType;
		@Setting("Quantity")
		private Integer itemQuantity;
		@Setting("NBT")
		private JsonObject nbt;
		private ItemStack itemStack;
		private TagUtil.Json tagUtil;

		/**
		 * Getting {@link ItemStack}
		 */
		public ItemStack getItemStack() {
			if(itemStack != null) return itemStack.copy();
			if(getItemType().isPresent()) {
				itemStack = ItemStack.of(getItemType().get());
				itemStack.setQuantity(itemQuantity);
				if(nbt != null) {
					try {
						itemStack = ItemStack.builder().fromContainer(itemStack.toContainer().set(DataQuery.of("UnsafeData"), DataFormats.JSON.get().read(nbt.toString()))).build();
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

		public String getItemTypeAsString() {
			return itemType;
		}

		/**
		 * The method returns a copy of the item's NBT tag collection in Json format.
		 */
		public JsonObject getNBT() {
			return nbt == null ? null : nbt.deepCopy();
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
		public TagUtil.Json getOrCreateTag() {
			return tagUtil == null ? tagUtil = new NbtEdit() : tagUtil;
		}

		public SerializedItemStackPlainNBT toSerializedItemStackPlainNBT() {
			return new SerializedItemStackPlainNBT(itemType, 0, nbt.toString());
		}

		@Override
		public String toString() {
			return  "ItemType: " + itemType +
					", Quantity: " + itemQuantity + 
					", Nbt: " + nbt.toString();
		}

		private void serialize(ItemStack itemStack) {
			itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
			itemQuantity = itemStack.quantity();
			if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
				try {
					nbt = JsonParser.parseString(DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get())).getAsJsonObject();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.itemStack = itemStack;
		}

		private class NbtEdit implements TagUtil.Json {

			public void putNbtPluginObject(PluginContainer container, String key, JsonElement object) {
				if(nbt == null) nbt = new JsonObject();
				putChildMaps(nbt, "PluginTags", getPluginId(container)).add(key, object);
				itemStack = null;
			}

			public <T extends CompoundTag> void putCompoundTag(PluginContainer container, String key, T tag) {
				if(nbt == null) nbt = new JsonObject();
				JsonObject json = tag.toJsonObject();
				putChildMaps(nbt, "PluginTags", getPluginId(container)).addProperty(key, json == null ? createStringFromCustomTag(tag) : json.toString());
				itemStack = null;
				json = null;
			}

			public boolean containsTag(PluginContainer container, String key) {
				return nbt != null && containsTag(nbt, 0, "PluginTags", getPluginId(container), key);
			}

			public void removeTag(PluginContainer container, String key) {
				JsonObject object = getDeepChildObject(nbt, "PluginTags", getPluginId(container));
				if(object.has(key)) object.remove(key);
				clear(object, "PluginTags", getPluginId(container));
			}

			public JsonElement getJsonObject(PluginContainer container, String key) {
				return containsTag(container, key) ? getDeepChildObject(nbt, "PluginTags", getPluginId(container)).get(key) : null;
			}

			public <T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key) {
				return containsTag(container, key) ? createTagFromString(getDeepChildObject(nbt, "PluginTags", getPluginId(container), key).getAsString(), clazz) : Optional.empty();
			}

			private boolean containsTag(JsonObject root, int currentElement, String... keys) {
				return root != null && root.has(keys[currentElement]) && (currentElement == keys.length || (root.get(keys[currentElement]).isJsonObject() && containsTag(root.get(keys[currentElement]).getAsJsonObject(), currentElement++, keys)));
			}

			private JsonObject putChildMaps(JsonObject root, String... keys) {
				for(String key : keys) {
					if(!root.has(key)) root.add(key, new JsonObject());
					return putChildMaps(root.get(key).getAsJsonObject(), keys.length == 1 ? new String[] {} : Arrays.copyOfRange(keys, 1, keys.length));
				}
				return root;
			}

			private JsonObject getDeepChildObject(JsonObject root, String... keys) {
				for(String key : keys) {
					return root.has(key) ? getDeepChildObject(root.get(key).getAsJsonObject(), keys.length == 1 ? new String[] {} : Arrays.copyOfRange(keys, 1, keys.length)) : root;
				}
				return root;
			}

			private void clear(JsonObject root, String... keys) {
				if(keys.length == 0) return;
				String last = keys[keys.length - 1];
				keys = keys.length < 2 ? new String[] {} : Arrays.copyOfRange(keys, 0, keys.length);
				JsonObject object = getDeepChildObject(root, keys);
				if(object.has(last) && object.get(last).isJsonObject() && object.get(last).getAsJsonObject().asMap().isEmpty()) {
					object.remove(last);
					clear(root, keys);
					last = null;
				} else keys = null;
			}

			private <T extends CompoundTag> String createStringFromCustomTag(T tag) {
				JsonObject json = tag.toJsonObject();
				if(json == null) {
					StringWriter sink = new StringWriter();
					GsonConfigurationLoader loader = createWriter(sink);
					ConfigurationNode node = loader.createNode();
					try {
						node.set(tag.getClass(), tag);
						loader.save(node);
					} catch (ConfigurateException e) {
						e.printStackTrace();
					}
					node = null;
					loader = null;
					return sink.toString();
				}
				return json.toString();
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

			private String getPluginId(PluginContainer container) {
				return container.metadata().id();
			}

			private ConfigurationNode serializeNodeFromString(String string) {
				try {
					return createLoader(new StringReader(string)).load();
				} catch (ConfigurateException e) {
					e.printStackTrace();
				}
				return BasicConfigurationNode.root();
			}

		}

}
