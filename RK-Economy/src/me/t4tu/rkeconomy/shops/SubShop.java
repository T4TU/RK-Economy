package me.t4tu.rkeconomy.shops;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.meowj.langutils.lang.LanguageHelper;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;

public class SubShop extends Shop {
	
	private int parentId;
	
	public SubShop(int id, String name, int size, int parentId, Economy economy) {
		super(id, name, size, null, economy);
		this.parentId = parentId;
	}

	public SubShop(int id, String name, int size, int parentId, ItemStack[] items, Economy economy) {
		super(id, name, size, null, items, economy);
		this.parentId = parentId;
	}
	
	@Override
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
			if (!CoreUtils.isNotAir(shopInventory.getItem(0))) {
				shopInventory.setItem(0, CoreUtils.getItem(Material.ARROW, "§c« Palaa takaisin", null, 1));
			}
		}
	}
	
	public int getParentId() {
		return parentId;
	}
}