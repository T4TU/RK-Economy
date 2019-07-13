package me.t4tu.rkeconomy.shops;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import com.meowj.langutils.lang.LanguageHelper;

import me.t4tu.rkeconomy.Economy;

public class Shop {
	
	protected int id;
	protected int size;
	protected String name;
	protected String trigger;
	protected Inventory inventory;
	protected Inventory shopInventory;
	protected Economy economy;
	
	public Shop(int id, String name, int size, String trigger, Economy economy) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.trigger = trigger;
		this.economy = economy;
		inventory = Bukkit.createInventory(null, size, name);
		shopInventory = Bukkit.createInventory(null, size, name);
		sync();
	}
	
	public Shop(int id, String name, int size, String trigger, ItemStack[] items, Economy economy) {
		this.id = id;
		this.name = name;
		this.size = size;
		this.trigger = trigger;
		this.economy = economy;
		inventory = Bukkit.createInventory(null, size, name);
		inventory.setContents(items);
		shopInventory = Bukkit.createInventory(null, size, name);
		sync();
	}
	
	public void sync() {
		if (inventory.getContents() != null) {
			shopInventory.clear();
			int x = 0;
			for (ItemStack stack : inventory.getContents()) {
				if (stack != null && stack.getType() != null && stack.getType() != Material.AIR) {
					ItemStack shopStack = stack.clone();
					ItemMeta shopMeta = shopStack.getItemMeta();
					if (shopMeta.hasDisplayName() && shopMeta.getDisplayName().startsWith("§9Kategoria: ")) {
						shopMeta.setLore(null);
					}
					else {
						shopMeta.setDisplayName("§a" + LanguageHelper.getItemDisplayName(shopStack, "fi_FI") + "§6 " + economy.applyMultiplier(getPrice(x)) + "£");
					}
					shopStack.setItemMeta(shopMeta);
					shopInventory.setItem(x, shopStack);
				}
				x++;
			}
		}
	}
	
	public void open(Player player) {
		player.openInventory(shopInventory);
	}
	
	public void close(Player player) {
		new BukkitRunnable() {
			public void run() {
				player.closeInventory();
			}
		}.runTaskLater(economy, 1);
	}
	
	public void edit(Player editor) {
		editor.openInventory(inventory);
	}
	
	public void save() {
		economy.getConfig().set("shops." + id + ".items", inventory.getContents());
		economy.saveConfig();
		sync();
	}
	
	public double getPrice(int slot) {
		if (economy.getConfig().get("shops." + id + ".prices." + slot) != null) {
			return economy.getConfig().getDouble("shops." + id + ".prices." + slot);
		}
		else {
			return 1.0;
		}
	}
	
	public int getId() {
		return id;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTrigger() {
		return trigger;
	}
	
	public void setTrigger(String trigger) {
		this.trigger = trigger;
	}
	
	public Inventory getInventory() {
		return inventory;
	}
	
	public Inventory getShopInventory() {
		return shopInventory;
	}
}