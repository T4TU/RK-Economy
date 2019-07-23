package me.t4tu.rkeconomy;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

import me.t4tu.rkcore.utils.CoreUtils;

public class EconomyListener implements Listener {
	
	private Economy economy;
	
	public EconomyListener(Economy economy) {
		this.economy = economy;
	}
	
	@EventHandler
	public void onItemCraft(CraftItemEvent e) {
		boolean sendMessage = false;
		for (ItemStack item : e.getInventory().getMatrix()) {
			if (CoreUtils.isNotAir(item) && (item.isSimilar(economy.GOLD_COIN) || item.isSimilar(economy.SILVER_COIN))) {
				e.setCancelled(true);
				sendMessage = true;
			}
		}
		if (sendMessage) {
			e.getWhoClicked().sendMessage(CoreUtils.getErrorBaseColor() + "Et voi käyttää kolikoita työstämiseen!");
		}
	}
}