package me.t4tu.rkeconomy.shops;

import java.util.Random;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;

public class ShopListener implements Listener {
	
	private Economy economy;
	
	public ShopListener(Economy economy) {
		this.economy = economy;
	}
	
	@EventHandler
	public void onShopClick(InventoryClickEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Inventory inventory = e.getInventory();
		if (economy.getShopManager().getShopByShopInventory(inventory) != null && e.getWhoClicked() instanceof Player) {
			Shop shop = economy.getShopManager().getShopByShopInventory(inventory);
			Player player = (Player) e.getWhoClicked();
			int slot = e.getSlot();
			e.setCancelled(true);
			if (e.getClickedInventory() != null && e.getClickedInventory().equals(shop.getShopInventory())) {
				for (String key : economy.getShopManager().getBuyers().keySet()) {
					if (key.split(":")[0].equals(player.getName())) {
						return;
					}
				}
				try {
					if (e.getCurrentItem().getItemMeta().getDisplayName().contains("§6 ")) {
						ItemStack original = shop.getInventory().getItem(slot);
						String name = e.getCurrentItem().getItemMeta().getDisplayName().split("§6 ")[0];
						double price = shop.getPrice(slot);
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_TRADE, 1, 1);
						player.sendMessage("");
						player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", kuinka monta kappaletta haluat\nostaa tuotetta " + name + tc2 + ":");
						shop.close(player);
						int random = new Random().nextInt(10000);
						economy.getShopManager().getBuyers().put(player.getName() + ":" + name.replace(":", "=^=") + ":" + price + ":" + random, original);
						new BukkitRunnable() {
							public void run() {
								if (economy.getShopManager().getBuyers().containsKey(player.getName() + ":" + name.replace(":", "=^=") + ":" + price + ":" + random)) {
									economy.getShopManager().getBuyers().remove(player.getName() + ":" + name.replace(":", "=^=") + ":" + price + ":" + random);
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Ostaminen peruttu!");
								}
							}
						}.runTaskLater(economy, 140);
					}
				}
				catch (Exception ex) {
				}
			}
		}
	}
	
	@EventHandler
	public void onShopOpen(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof Player) {
			for (Shop shop : economy.getShopManager().getShops()) {
				if (CoreUtils.isNPCAndNamed(e.getRightClicked(), shop.getTrigger())) {
					shop.open(e.getPlayer());
				}
			}
		}
	}
	
	@EventHandler
	public void onShopClose(InventoryCloseEvent e) {
		
		String tc2 = CoreUtils.getBaseColor();
		
		Inventory inventory = e.getInventory();
		if (economy.getShopManager().getShopByEditInventory(inventory) != null && e.getPlayer() instanceof Player) {
			Player player = (Player) e.getPlayer();
			player.sendMessage(tc2 + "Muokkasit kauppaa! Muista tallentaa muutokset komennolla /shop save!");
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPaymentCancel(PlayerCommandPreprocessEvent e) {
		
		String tc3 = CoreUtils.getErrorBaseColor();
		
		for (String key : economy.getShopManager().getBuyers().keySet()) {
			if (key.split(":")[0].equals(e.getPlayer().getName())) {
				e.setCancelled(true);
				economy.getShopManager().getBuyers().remove(key);
				e.getPlayer().playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				e.getPlayer().sendMessage(tc3 + "Ostaminen peruttu!");
				return;
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onShopBuy(AsyncPlayerChatEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		
		for (String key : economy.getShopManager().getBuyers().keySet()) {
			if (key.split(":")[0].equals(player.getName())) {
				
				e.setCancelled(true);
				ItemStack item = economy.getShopManager().getBuyers().get(key).clone();
				String name = key.split(":")[1].replace("=^=", ":");
				double price = Double.parseDouble(key.split(":")[2]);
				economy.getShopManager().getBuyers().remove(key);
				
				int amount = 0;
				try {
					amount = Integer.parseInt(e.getMessage());
					if (amount <= 0) {
						player.sendMessage(tc3 + "Virheellinen määrä, ostaminen peruttu!");
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						return;
					}
				}
				catch (Exception en) {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Virheellinen määrä, ostaminen peruttu!");
					return;
				}
				
				item.setAmount(amount);
				
				if (CoreUtils.hasEnoughRoom(player, item, amount, economy.SILVER_COIN, 9)) {
					if (economy.takeCash(player, price * amount)) {
						player.getInventory().addItem(item);
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
						player.sendMessage(tc2 + "Ostit " + tc1 + amount + tc2 + " kappaletta tuotetta " + tc1 + name + tc2 + " hintaan " + tc1 + price + "£" + tc2 + "!");
					}
					else {
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					}
				}
				else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksi tilaa!");
				}
				return;
			}
		}
	}
}