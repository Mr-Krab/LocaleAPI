package sawfowl.localeapi.serializetools;

import java.io.IOException;

import org.spongepowered.api.ResourceKey;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataQuery;
import org.spongepowered.api.data.persistence.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.registry.RegistryTypes;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Setting;

@ConfigSerializable
public class SerializedItemStack {

	SerializedItemStack(){}

	public SerializedItemStack(ItemStack itemStack) {
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
		if(itemStack == null || !isPresent()) {
			itemStack = ItemStack.empty();
		}
		if(isPresent()) {
			itemStack = ItemStack.of(getDeserializedType());
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

	public ItemType getDeserializedType() {
		return RegistryTypes.ITEM_TYPE.find().get().value(ResourceKey.resolve(itemType));
	}

	public void setQuantity(int quantity) {
		itemQuantity = quantity;
	}

	public boolean isPresent() {
		return RegistryTypes.ITEM_TYPE.find().get().findValue(ResourceKey.resolve(itemType)).isPresent();
		//return RegistryTypes.ITEM_TYPE.find().of(itemType).isPresent();
	}

	@Override
	public String toString() {
		return  "ItemType: " + itemType +
				//", ItemSubType: " + itemSubType +
				", Quantity: " + itemQuantity + 
				", Nbt: " + getNBT();
	}

}
