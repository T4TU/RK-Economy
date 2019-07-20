package me.t4tu.rkeconomy.banks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.t4tu.rkcore.inventories.InventoryGUI;
import me.t4tu.rkcore.inventories.InventoryGUIAction;
import me.t4tu.rkcore.inventories.InventoryGUIEventAction;
import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;

public class BankListener implements Listener {
	
	private Economy economy;
	private Map<Player, Integer> deposit;
	private Map<Player, Integer> withdraw;
	
	public BankListener(Economy economy) {
		this.economy = economy;
		deposit = new HashMap<Player, Integer>();
		withdraw = new HashMap<Player, Integer>();
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent e) {
		
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		
		if (deposit.containsKey(player)) {
			e.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Rahan tallettaminen peruttu!");
			deposit.remove(player);
		}
		if (withdraw.containsKey(player)) {
			e.setCancelled(true);
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Rahan nostaminen peruttu!");
			withdraw.remove(player);
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncPlayerChatEvent e) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		Player player = e.getPlayer();
		
		if (deposit.containsKey(player)) {
			e.setCancelled(true);
			int a = 0;
			try {
				a = Economy.moneyAsInt(Double.parseDouble(e.getMessage().replace(",", ".")));
				if (a > 0) {
					if (economy.takeCash(player, a)) {
						Economy.setMoney(player, Economy.getMoney(player) + a);
						player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
						player.sendMessage(tc2 + "Talletit pankkitilillesi " + tc1 + Economy.moneyAsString(a) + tc2 + "!");
						deposit.remove(player);
					}
					else {
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						deposit.remove(player);
					}
				}
				else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Virheellinen määrä, rahan tallettaminen peruttu!");
					deposit.remove(player);
				}
			}
			catch (Exception en) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Virheellinen määrä, rahan tallettaminen peruttu!");
				deposit.remove(player);
			}
		}
		else if (withdraw.containsKey(player)) {
			e.setCancelled(true);
			int a = 0;
			try {
				a = Economy.moneyAsInt(Double.parseDouble(e.getMessage().replace(",", ".")));
				if (a > 0) {
					if (Economy.getMoney(player) >= a) {
						int[] coins = Economy.moneyAsCoins(a);
						int goldAmount = coins[0];
						int silverAmount = coins[1];
						if (CoreUtils.hasEnoughRoom(player, economy.GOLD_COIN, goldAmount, economy.SILVER_COIN, silverAmount)) {
							Economy.setMoney(player, Economy.getMoney(player) - a);
							player.getInventory().addItem(economy.getGoldCoin(goldAmount));
							player.getInventory().addItem(economy.getSilverCoin(silverAmount));
							player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
							player.sendMessage(tc2 + "Nostit pankkitililtäsi " + tc1 + Economy.moneyAsString(a) + tc2 + "!");
							withdraw.remove(player);
						}
						else {
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksti tilaa!");
							withdraw.remove(player);
						}
					}
					else {
						player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
						player.sendMessage(tc3 + "Tililläsi ei ole tarpeeksi rahaa tämän rahamäärän nostamiseen!");
						withdraw.remove(player);
					}
				}
				else {
					player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
					player.sendMessage(tc3 + "Virheellinen määrä, rahan nostaminen peruttu!");
					withdraw.remove(player);
				}
			}
			catch (Exception en) {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Virheellinen määrä, nostaminen peruttu!");
				withdraw.remove(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerInteractEntity(PlayerInteractEntityEvent e) {
		if (e.getHand() == EquipmentSlot.HAND && CoreUtils.isNPCAndNamed(e.getRightClicked(), "§2Pankkivirkailija")) {
			openBankInventory(e.getPlayer());
		}
	}
	
	private void openBankInventory(Player player) {
		
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		
		InventoryGUI gui = new InventoryGUI(45, "Pankkitili: " + player.getName());
		
		new BukkitRunnable() {
			public void run() {
				
				gui.addItem(CoreUtils.getItem(Material.BOOK, "§6Pankkitili", Arrays.asList("", "§6Tilin haltija: §7" + player.getName(), "§6Saldo: §7" + 
						Economy.moneyAsString(Economy.getMoney(player))), 1), 13, null);
				
				gui.addItem(CoreUtils.getItem(Material.CHEST, "§6Nosta & talleta rahaa", 
						Arrays.asList("", "§7Klikkaa hiiren vasemmalla tallettaaksesi rahaa.", "§7Klikkaa hiiren oikealla nostaaksesi rahaa."), 1), 28, new InventoryGUIEventAction() {
							public void onClickAsync() { }
							public void onClick() { }
							public void onClickAsync(InventoryClickEvent e) { }
							public void onClick(InventoryClickEvent e) {
								int identifier = new Random().nextInt(10000);
								if (e.getClick() == ClickType.LEFT) {
									deposit.put(player, identifier);
									player.sendMessage("");
									player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", kuinka paljon haluat " + tc1 + "tallettaa" + tc2 + " rahaa:");
									gui.close(player);
									new BukkitRunnable() {
										public void run() {
											if (deposit.containsKey(player) && deposit.get(player) == identifier) {
												player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
												player.sendMessage(tc3 + "Rahan tallettaminen peruttu!");
												deposit.remove(player);
											}
										}
									}.runTaskLater(economy, 140);
								}
								else if (e.getClick() == ClickType.RIGHT) {
									withdraw.put(player, identifier);
									player.sendMessage("");
									player.sendMessage(tc2 + "Kirjoita " + tc1 + "chattiin" + tc2 + ", kuinka paljon haluat " + tc1 + "nostaa" + tc2 + " rahaa:");
									gui.close(player);
									new BukkitRunnable() {
										public void run() {
											if (withdraw.containsKey(player) && withdraw.get(player) == identifier) {
												player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
												player.sendMessage(tc3 + "Rahan nostaminen peruttu!");
												withdraw.remove(player);
											}
										}
									}.runTaskLater(economy, 140);
								}
							}
						});
				
				gui.addItem(CoreUtils.getItem(Material.GOLD_NUGGET, "§6Vaihda rahaa", Arrays.asList("", "§7Vaihda kultakolikoita hopeakolikoiksi", "§7tai toisinpäin klikkaamalla tästä."), 1), 30, 
						new InventoryGUIAction() {
					public void onClickAsync() { }
					public void onClick() {
						
						player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
						
						InventoryGUI gui = new InventoryGUI(45, "Vaihda rahaa");
						
						gui.addItem(CoreUtils.getItem(Material.ARROW, "§c« Palaa takaisin", null, 1), 0, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1, 1);
								openBankInventory(player);
							}
						});
						
						gui.addItem(CoreUtils.getItem(Material.NAME_TAG, "§61 kultakolikko§8 > §710 hopeakolikkoa", null, 1), 11, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								goldToSilver(player, 1, 10);
							}
						});
						
						gui.addItem(CoreUtils.getItem(Material.NAME_TAG, "§610 kultakolikkoa§8 > §7100 hopeakolikkoa", null, 1), 13, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								goldToSilver(player, 10, 100);
							}
						});
						
						gui.addItem(CoreUtils.getItem(Material.NAME_TAG, "§664 kultakolikkoa§8 > §7640 hopeakolikkoa", null, 1), 15, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								goldToSilver(player, 64, 640);
							}
						});
						
						gui.addItem(CoreUtils.getItem(Material.NAME_TAG, "§710 hopeakolikkoa§8 > §61 kultakolikko", null, 1), 29, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								silverToGold(player, 10, 1);
							}
						});
						
						gui.addItem(CoreUtils.getItem(Material.NAME_TAG, "§7100 hopeakolikkoa§8 > §610 kultakolikkoa", null, 1), 31, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								silverToGold(player, 100, 10);
							}
						});
						
						gui.addItem(CoreUtils.getItem(Material.NAME_TAG, "§7640 hopeakolikkoa§8 > §664 kultakolikkoa", null, 1), 33, new InventoryGUIAction() {
							public void onClickAsync() { }
							public void onClick() {
								silverToGold(player, 640, 64);
							}
						});
						
						gui.open(player);
					}
				});
				
				gui.addItem(CoreUtils.getItem(Material.PAPER, "§6Lunasta shekki", Arrays.asList("", "§7Lunasta kädessä pitelemäsi", "§7shekki klikkaamalla tästä."), 1), 32, new InventoryGUIAction() {
					public void onClickAsync() {
						ItemStack i = player.getInventory().getItemInMainHand();
						if (CoreUtils.isNotAir(i) && i.getType() == Material.PAPER) {
							if (CoreUtils.getDisplayName(i).equals("Shekki") && i.getItemMeta().hasLore()) {
								if (i.getAmount() == 1) {
									try {
										int a = Economy.moneyAsInt(Double.parseDouble(i.getItemMeta().getLore().get(0).split("§o")[1].split("кк")[0].replace(",", ".")));
										String receiver = i.getItemMeta().getLore().get(1).split("§o")[1];
										String s = i.getItemMeta().getLore().get(2).split("§o")[1];
										if (receiver.equals(player.getName())) {
											if (Economy.getMoney(s) >= a) {
												Economy.setMoney(s, Economy.getMoney(s) - a);
												if (Bukkit.getPlayer(s) != null) {
													Bukkit.getPlayer(s).playSound(Bukkit.getPlayer(s).getLocation(), Sound.ENTITY_VILLAGER_YES, 1, 1);
													Bukkit.getPlayer(s).sendMessage(tc2 + player.getName() + " lunasti juuri shekkisi, jonka arvo oli " + tc1 + Economy.moneyAsString(a) + tc2 + "!");
												}
												Economy.setMoney(player, Economy.getMoney(player) + a);
												player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
												player.sendMessage(tc2 + "Lunastit shekin, jonka arvo oli " + tc1 + Economy.moneyAsString(a) + tc2 + "! Summa lisättiin pankkitilillesi.");
												player.getInventory().setItemInMainHand(null);
												player.updateInventory();
											}
											else {
												player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
												player.sendMessage(tc3 + "Valitettavasti tällä shekillä ei ole katetta. Mikäli uskot, että sinua on huijattu, §nota yhteyttä henkilökuntaan" + tc3 + ".");
											}
										}
										else {
											player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
											player.sendMessage(tc3 + "Tätä shekkiä ei ole osoitettu sinulle!");
										}
										
									}
									catch (Exception e) {
										player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
										player.sendMessage(tc3 + "Tämä shekki on virheellinen!");
									}
								}
								else {
									player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
									player.sendMessage(tc3 + "Voit lunastaa ainoastaan yhden shekin kerrallaan!");
								}
							}
							else {
								player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
								player.sendMessage(tc3 + "Sinun täytyy pitää haluamaasi shekkiä kädessäsi voidaksesi lunastaa sen!");
							}
						}
						else {
							player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
							player.sendMessage(tc3 + "Sinun täytyy pitää haluamaasi shekkiä kädessäsi voidaksesi lunastaa sen!");
						}
					}
					public void onClick() { }
				});
				
				gui.addItem(CoreUtils.getItem(Material.WRITABLE_BOOK, "§6Lainat", Arrays.asList("", "§7Tulossa..."), 1), 34, null);
			}
		}.runTaskAsynchronously(economy);
		
		gui.open(player);
	}
	
	private void goldToSilver(Player player, int gold, int silver) {
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		if (economy.takeGoldCoins(player, gold)) {
			ItemStack coins = economy.getSilverCoin(silver);
			if (CoreUtils.hasEnoughRoom(player, coins, coins.getAmount())) {
				player.getInventory().addItem(coins);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				player.sendMessage(tc2 + "Vaihdettiin " + tc1 + gold + tc2 + " kultakolikkoa " + tc1 + silver + tc2 + " hopeakolikkoon!");
			}
			else {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksi tilaa!");
				player.getInventory().addItem(economy.getGoldCoin(gold));
			}
		}
		else {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Sinulla ei ole tarpeeksi kultakolikoita!");
		}
	}
	
	private void silverToGold(Player player, int silver, int gold) {
		String tc1 = CoreUtils.getHighlightColor();
		String tc2 = CoreUtils.getBaseColor();
		String tc3 = CoreUtils.getErrorBaseColor();
		if (economy.takeSilverCoins(player, silver)) {
			ItemStack coins = economy.getGoldCoin(gold);
			if (CoreUtils.hasEnoughRoom(player, coins, coins.getAmount())) {
				player.getInventory().addItem(coins);
				player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 2);
				player.sendMessage(tc2 + "Vaihdettiin " + tc1 + silver + tc2 + " hopeakolikkoa " + tc1 + gold + tc2 + " kultakolikkoon!");
			}
			else {
				player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
				player.sendMessage(tc3 + "Tavaraluettelossasi ei ole tarpeeksi tilaa!");
				player.getInventory().addItem(economy.getSilverCoin(silver));
			}
		}
		else {
			player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
			player.sendMessage(tc3 + "Sinulla ei ole tarpeeksi hopeakolikoita!");
		}
	}
}