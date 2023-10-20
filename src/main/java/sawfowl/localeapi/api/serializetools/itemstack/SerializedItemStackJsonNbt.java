package sawfowl.localeapi.api.serializetools.itemstack;

import java.io.IOException;
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
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.kyori.adventure.key.Key;

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
		//@Setting("ItemSubType")
		//private Integer itemSubType;
		@Setting("Quantity")
		private Integer itemQuantity;
		@Setting("NBT")
		private JsonObject nbt;
		private ItemStack itemStack;

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

		public JsonObject getNBT() {
			return nbt;
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

		public SerializedItemStack toSerializedItemStack() {
			return new SerializedItemStack(itemType, 0, nbt.toString());
		}

		private void serialize(ItemStack itemStack) {
			itemType = RegistryTypes.ITEM_TYPE.get().valueKey(itemStack.type()).asString();
			itemQuantity = itemStack.quantity();
			//net.minecraft.world.item.ItemStack nmsStack = ItemStackUtil.toNative(itemStack);
			//itemSubType = nmsStack.getDamageValue();
			if(itemStack.toContainer().get(DataQuery.of("UnsafeData")).isPresent()) {
				try {
					nbt = JsonParser.parseString(DataFormats.JSON.get().write((DataView) itemStack.toContainer().get(DataQuery.of("UnsafeData")).get())).getAsJsonObject();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			this.itemStack = itemStack;
		}

}
