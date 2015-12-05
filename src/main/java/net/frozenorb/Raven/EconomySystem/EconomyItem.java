package net.frozenorb.Raven.EconomySystem;

import org.bukkit.Material;

import net.frozenorb.Utilities.Serialization.ReflectionSerializer;
import net.frozenorb.Utilities.Serialization.SerializableClass;
import net.frozenorb.Utilities.Types.RedisOperation;

import java.util.UUID;

@SerializableClass
public class EconomyItem extends ReflectionSerializer {
	int id;
	double price;
	short durability;
	UUID seller;
	transient RedisOperation operation = RedisOperation.NONE;
	String oldValue;
	int amount;

	public EconomyItem(int id, double price, short data, UUID seller, int amount) {
		this(id, price, data, seller, amount, null);
	}

	public EconomyItem(int id, double price, short data, UUID seller, int amount, String oldValue) {
		this.id = id;
		this.price = price;
		this.durability = data;
		this.seller = seller;
		this.amount = amount;
		this.oldValue = oldValue;
	}

	public EconomyItem(EconomyItem item) {
		this(item.id, item.price, item.durability, item.seller, item.amount, item.oldValue);
		operation = item.getOperation();
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public void setSeller(UUID seller) {
		this.seller = seller;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public void setId(int id) {
		this.id = id;
	}

	public EconomyItem setOperation(RedisOperation operation) {
		this.operation = operation;
		return this;
	}

	public UUID getSeller() {
		return seller;
	}

	public double getPrice() {
		return price;
	}

	public int getId() {
		return id;
	}

	public short getDurability() {
		return durability;
	}

	public RedisOperation getOperation() {
		return operation;
	}

	public void cacheOldValue() {
		oldValue = toString();
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getOldValue() {
		if (oldValue == null) {
			return toString();
		}
		return oldValue;
	}

	@SuppressWarnings("deprecation")
	public String getKey() {
		return Material.getMaterial(id).toString() + ":" + durability;
	}

}
