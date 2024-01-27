package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.BasicConfigurationNode;
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
public class SerializedItemStackJsonNbt implements CompoundTag {

	SerializedItemStackJsonNbt(){}

		public SerializedItemStackJsonNbt(ItemStack itemStack) {
			serialize(itemStack);
		}

		public SerializedItemStackJsonNbt(BlockState block) {
			if(block.type().item().isPresent()) {
				serialize(ItemStack.of(block.type().item().get(), 1));
				if(block.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
					try {
						JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write((DataView) block.toContainer().get(DataQuery.of("UnsafeData")).get()));
						nbt = json.isJsonObject() ? json.getAsJsonObject() : null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} serialize(ItemStack.of(ItemTypes.AIR.get(), 1));
		}

		public SerializedItemStackJsonNbt(BlockSnapshot block) {
			if(block.state().type().item().isPresent()) {
				serialize(ItemStack.of(block.state().type().item().get(), 1));
				if(block.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
					try {
						JsonElement json = JsonParser.parseString(DataFormats.JSON.get().write((DataView) block.toContainer().get(DataQuery.of("UnsafeData")).get()));
						nbt = json.isJsonObject() ? json.getAsJsonObject() : null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} serialize(ItemStack.of(ItemTypes.AIR.get(), 1));
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
		public JsonObject toJsonObject() {
			JsonObject object = new JsonObject();
			object.addProperty("ItemType", itemType);
			object.addProperty("Quantity", itemQuantity);
			if(nbt != null) object.add("NBT", nbt);
			return object;
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

			public void putJsonElement(PluginContainer container, String key, JsonElement object) {
				if(object == null) return;
				if(nbt == null) nbt = new JsonObject();
				putChildMaps(nbt, "PluginTags", getPluginId(container)).add(key, object);
				itemStack = null;
			}

			public <T extends CompoundTag> void putCompoundTag(PluginContainer container, String key, T tag) {
				if(tag == null) return;
				if(nbt == null) nbt = new JsonObject();
				JsonObject json = tag.toJsonObject();
				if(json == null) json = convertTagToJson(tag);
				if(json == null) return;
				putJsonElement(container, key, json);
				json = null;
			}

			public boolean containsTag(PluginContainer container, String key) {
				return nbt != null && containsTag(nbt, "PluginTags", getPluginId(container), key);
			}

			public void removeTag(PluginContainer container, String key) {
				JsonObject object = getDeepChildObject(nbt, "PluginTags", getPluginId(container));
				if(object.has(key)) object.remove(key);
				clear(object, "PluginTags", getPluginId(container));
			}

			public Optional<JsonElement> getJsonObject(PluginContainer container, String key) {
				return containsTag(container, key) ? Optional.ofNullable(getDeepChildObject(nbt, "PluginTags", getPluginId(container)).get(key)) : Optional.empty();
			}

			@Override
			public ConfigurationNode getAsConfigurationNode(PluginContainer container) {
				if(containsTag(nbt, "PluginTags", getPluginId(container))) try {
					return GsonConfigurationLoader.builder().defaultOptions(options -> options.serializers(SerializeOptions.selectSerializersCollection(2))).build().createNode().set(JsonElement.class, getDeepChildObject(nbt, "PluginTags", getPluginId(container)).deepCopy());
				} catch (SerializationException e) {
					e.printStackTrace();
				}
				return BasicConfigurationNode.root(o -> o.options().serializers(SerializeOptions.selectSerializersCollection(2)));
			}

			public <T extends CompoundTag> Optional<T> getCompoundTag(Class<T> clazz, PluginContainer container, String key) {
				return containsTag(container, key) ? Optional.ofNullable(convertJsonToTag(clazz, getDeepChildObject(nbt, "PluginTags", getPluginId(container), key))) : Optional.empty();
			}

			private boolean containsTag(JsonObject root, String... keys) {
				if(root == null) return false;
				for(String key : keys) {
					return root.has(key) && (keys.length == 1 || (root.get(key).isJsonObject() && containsTag(root.get(key).getAsJsonObject(), Arrays.copyOfRange(keys, 1, keys.length))));
				}
				return true;
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

			private <T extends CompoundTag> JsonObject convertTagToJson(T tag) {
				try {
					return BasicConfigurationNode.root(o -> o.options().serializers(SerializeOptions.SERIALIZER_COLLECTION_VARIANT_1)).set(tag.getClass(), tag).get(JsonObject.class);
				} catch (SerializationException | RuntimeException e) {
					e.printStackTrace();
				}
				return null;
			}

			private <T extends CompoundTag> T convertJsonToTag(Class<T> clazz, JsonObject jsonObject) {
				try {
					return BasicConfigurationNode.root(o -> o.options().serializers(SerializeOptions.SERIALIZER_COLLECTION_VARIANT_1)).set(JsonObject.class, jsonObject).get(clazz);
				} catch (SerializationException | RuntimeException e) {
					e.printStackTrace();
				}
				return null;
			}

			private String getPluginId(PluginContainer container) {
				return container.metadata().id();
			}

			@Override
			public int size(PluginContainer container) {
				return nbt == null || !nbt.has("PluginTags") || !nbt.getAsJsonObject("PluginTags").has(getPluginId(container)) ? 0 : nbt.getAsJsonObject("PluginTags").getAsJsonObject(getPluginId(container)).size();
			}

		}

}
