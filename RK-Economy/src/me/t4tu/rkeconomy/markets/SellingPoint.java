package me.t4tu.rkeconomy.markets;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;

public class SellingPoint {
	
	private int id;
	private String world;
	private String facing;
	private int x, y, z;
	private int amount;
	private int price;
	private Inventory inventory;
	private MarketStall owner;
	private Economy economy;
	
	public SellingPoint(int id, String world, String facing, int x, int y, int z, int amount, int price, Inventory inventory, MarketStall owner, Economy economy) {
		this.id = id;
		this.world = world;
		this.facing = facing;
		this.x = x;
		this.y = y;
		this.z = z;
		this.amount = amount;
		this.price = price;
		this.inventory = inventory;
		this.owner = owner;
		this.economy = economy;
	}
	
	public void save() {
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".world", world);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".facing", facing);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".x", x);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".y", y);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".z", z);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".amount", amount);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".price", price);
		economy.getConfig().set("stalls." + owner.getId() + ".selling-points." + id + ".inventory", inventory.getContents().clone());
		economy.saveConfig();
	}
	
	public void reset() {
		amount = 1;
		price = 10;
		inventory = Bukkit.createInventory(null, 27, "Muokkaa myyntipistettä");
		inventory.setItem(25, CoreUtils.getItem(Material.OAK_SIGN, "§a§lHinnan tyyppi", Arrays.asList("§a> кк/1kpl <", "§7  кк/10kpl", "§7  кк/64kpl"), 1));
		inventory.setItem(26, CoreUtils.getItem(Material.NAME_TAG, "§a§lAseta hinta", Arrays.asList("§7Aseta tämän myyntipisteen", "§7hinta klikkaamalla tästä!"), 1));
	}
	
	public int getId() {
		return id;
	}
	
	public String getWorld() {
		return world;
	}
	
	public String getFacing() {
		return facing;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
	}
	
	public int getPrice() {
		return price;
	}
	
	public void setPrice(int price) {
		this.price = price;
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public MarketStall getOwner() {
		return owner;
	}
	
	public int getAmountInStorage() {
		int amount = 0;
		for (int i = 0; i < inventory.getContents().length - 2; i++) {
			ItemStack item = inventory.getContents()[i];
			if (CoreUtils.isNotAir(item)) {
				amount += item.getAmount();
			}
		}
		return amount;
	}
	
	public Location getLocation() {
		return new Location(Bukkit.getWorld(world), x, y, z);
	}
	
	public Location getLocationCenter() {
		return new Location(Bukkit.getWorld(world), x + 0.5, y + 0.5, z + 0.5);
	}
	
	public ItemFrame getFrame() {
		for (Entity entity : Bukkit.getWorld(world).getEntities()) {
			if (entity instanceof ItemFrame) {
				ItemFrame frame = (ItemFrame) entity;
				Location location = frame.getLocation();
				if (world.equals(location.getWorld().getName()) && x == location.getBlockX() && y == location.getBlockY() && z == location.getBlockZ() && facing.equals(frame.getFacing().toString())) {
					return frame;
				}
			}
		}
		return null;
	}
	
	public ItemStack getPreviewItemStack() {
		ItemStack preview = null;
		for (int i = 0; i < inventory.getContents().length - 2; i++) {
			ItemStack item = inventory.getContents()[i];
			if (CoreUtils.isNotAir(item)) {
				preview = item.clone();
				preview.setAmount(amount);
				break;
			}
		}
		return preview;
	}
}