package me.t4tu.rkeconomy.shops;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import me.t4tu.rkeconomy.Economy;
import net.md_5.bungee.api.ChatColor;

public class ShopManager {
	
	private ArrayList<Shop> shops = new ArrayList<Shop>();
	private HashMap<String, ItemStack> buyers = new HashMap<String, ItemStack>();
	private double price = 1;
	private Economy economy;
	
	public ShopManager(Economy economy) {
		this.economy = economy;
	}
	
	private FileConfiguration getConfig() {
		return economy.getConfig();
	}
	
	private void saveConfig() {
		economy.saveConfig();
	}
	
	public double getPrice() {
		return price;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public ArrayList<Shop> getShops() {
		return shops;
	}
	
	public HashMap<String, ItemStack> getBuyers() {
		return buyers;
	}
	
	public Shop getShopById(int id) {
		for (Shop shop : shops) {
			if (shop.getId() == id) {
				return shop;
			}
		}
		return null;
	}
	
	public Shop getShopByName(String name) {
		for (Shop shop : shops) {
			if (shop.getName().equals(name)) {
				return shop;
			}
		}
		return null;
	}
	
	public Shop getShopByEditInventory(Inventory inventory) {
		for (Shop shop : shops) {
			if (shop.getInventory().equals(inventory)) {
				return shop;
			}
		}
		return null;
	}
	
	public Shop getShopByShopInventory(Inventory inventory) {
		for (Shop shop : shops) {
			if (shop.getShopInventory().equals(inventory)) {
				return shop;
			}
		}
		return null;
	}
	
	public Shop newShop(int id, String name, int rows, String trigger) {
		Shop shop = new Shop(id, name, rows * 9, ChatColor.translateAlternateColorCodes('&', trigger), economy);
		shops.add(shop);
		getConfig().set("shops." + id + ".name", name);
		getConfig().set("shops." + id + ".rows", rows);
		getConfig().set("shops." + id + ".trigger", trigger);
		getConfig().set("shops." + id + ".items", shop.getInventory().getContents());
		saveConfig();
		return shop;
	}
	
	public void removeShop(Shop shop) {
		shops.remove(shop);
		getConfig().set("shops." + shop.getId(), null);
		saveConfig();
	}
	
	public void loadShopsFromConfig() {
		shops.clear();
		if (getConfig().getConfigurationSection("shops") != null) {
			for (String s : getConfig().getConfigurationSection("shops").getKeys(false)) {
				try {
					int id = Integer.parseInt(s);
					int rows = getConfig().getInt("shops." + s + ".rows");
					String name = getConfig().getString("shops." + s + ".name");
					String trigger = ChatColor.translateAlternateColorCodes('&', getConfig().getString("shops." + s + ".trigger"));
					ItemStack[] items = new ItemStack[rows * 9];
					for (int x = 0; x < rows * 9; x++) {
						items[x] = (ItemStack) getConfig().getList("shops." + s + ".items").get(x);
					}
					shops.add(new Shop(id, name, rows * 9, trigger, items, economy));
				}
				catch (Exception e) {
					Bukkit.getConsoleSender().sendMessage("Virhe ladattaessa NPC-kauppaa ID:llä '" + s + "'");
				}
			}
		}
	}
}