package me.t4tu.rkeconomy.markets;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.inventory.ItemStack;

import me.t4tu.rkcore.utils.CoreUtils;
import me.t4tu.rkeconomy.Economy;
import net.md_5.bungee.api.ChatColor;

public class MarketStall {
	
	private int id;
	private MarketStallType type;
	private String name;
	private String ownerUuid;
	private String ownerName;
	private boolean rented;
	private long expires;
	private int profit;
	private Location signLocation;
	private List<SellingPoint> sellingPoints;
	private List<String> history;
	private List<ItemStack> lostAndFound;
	private String previousOwner;
	private Economy economy;
	
	public MarketStall(int id, MarketStallType type, String name, String ownerUuid, String ownerName, boolean rented, long expires, int profit, Location signLocation, 
			List<SellingPoint> sellingPoints, List<String> history, List<ItemStack> lostAndFound, String previousOwner, Economy economy) {
		this.id = id;
		this.type = type;
		this.name = name;
		this.ownerUuid = ownerUuid;
		this.ownerName = ownerName;
		this.rented = rented;
		this.expires = expires;
		this.profit = profit;
		this.signLocation = signLocation;
		this.sellingPoints = sellingPoints;
		this.history = history;
		this.lostAndFound = lostAndFound;
		this.previousOwner = previousOwner;
		this.economy = economy;
	}
	
	public void save() {
		economy.getConfig().set("stalls." + id + ".type", type.toString());
		economy.getConfig().set("stalls." + id + ".name", name);
		economy.getConfig().set("stalls." + id + ".owner-uuid", ownerUuid);
		economy.getConfig().set("stalls." + id + ".owner-name", ownerName);
		economy.getConfig().set("stalls." + id + ".rented", rented);
		economy.getConfig().set("stalls." + id + ".expires", expires);
		economy.getConfig().set("stalls." + id + ".profit", profit);
		if (signLocation != null) {
			CoreUtils.setLocation(economy, "stalls." + id + ".sign-location", signLocation);
		}
		else {
			economy.getConfig().set("stalls." + id + ".sign-location", null);
		}
		economy.getConfig().set("stalls." + id + ".selling-points", null);
		for (SellingPoint sellingPoint : sellingPoints) {
			sellingPoint.save();
		}
		economy.getConfig().set("stalls." + id + ".history", history);
		economy.getConfig().set("stalls." + id + ".lost-and-found", lostAndFound.toArray());
		economy.getConfig().set("stalls." + id + ".previous-owner", previousOwner);
		economy.saveConfig();
	}
	
	public void saveLostAndFound() {
		economy.getConfig().set("stalls." + id + ".lost-and-found", lostAndFound.toArray());
		economy.saveConfig();
	}
	
	public void remove() {
		economy.getConfig().set("stalls." + id, null);
		economy.saveConfig();
		economy.getMarketManager().getMarketStalls().remove(this);
	}
	
	public void updateSign() {
		if (signLocation != null) {
			Block block = signLocation.getBlock();
			if (block != null && block.getState() instanceof Sign) {
				Sign sign = (Sign) block.getState();
				sign.setLine(0, "§8[" + type.getFriendlyName() + "]");
				sign.setLine(1, "");
				if (rented) {
					if (name != null && type == MarketStallType.LIIKEKIINTEISTÖ) {
						sign.setLine(2, ChatColor.translateAlternateColorCodes('&', name));
						sign.setLine(3, "");
					}
					else {
						sign.setLine(2, ownerName);
						sign.setLine(3, "");
					}
				}
				else {
					sign.setLine(2, "§4Vuokraa");
					sign.setLine(3, "§4klikkaamalla!");
				}
				sign.update();
			}
		}
	}
	
	public SellingPoint getSellingPoint(int number) {
		for (SellingPoint sellingPoint : sellingPoints) {
			if (sellingPoint.getId() == number) {
				return sellingPoint;
			}
		}
		return null;
	}
	
	public int getId() {
		return id;
	}
	
	public MarketStallType getType() {
		return type;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getOwnerUuid() {
		return ownerUuid;
	}
	
	public void setOwnerUuid(String ownerUuid) {
		this.ownerUuid = ownerUuid;
	}
	
	public String getOwnerName() {
		return ownerName;
	}
	
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	
	public boolean isRented() {
		return rented;
	}
	
	public void setRented(boolean rented) {
		this.rented = rented;
	}
	
	public long getExpires() {
		return expires;
	}
	
	public void setExpires(long expires) {
		this.expires = expires;
	}
	
	public int getProfit() {
		return profit;
	}
	
	public void setProfit(int profit) {
		this.profit = profit;
	}
	
	public Location getSignLocation() {
		return signLocation;
	}
	
	public Sign getSign() {
		Block block = signLocation.getWorld().getBlockAt(signLocation);
		if (block != null && block.getState() instanceof Sign) {
			Sign sign = (Sign) block.getState();
			return sign;
		}
		return null;
	}
	
	public List<SellingPoint> getSellingPoints() {
		return sellingPoints;
	}
	
	public void setSellingPoints(List<SellingPoint> sellingPoints) {
		this.sellingPoints = sellingPoints;
	}
	
	public List<String> getHistory() {
		return history;
	}
	
	public void setHistory(List<String> history) {
		this.history = history;
	}
	
	public List<ItemStack> getLostAndFound() {
		return lostAndFound;
	}
	
	public void setLostAndFound(List<ItemStack> lostAndFound) {
		this.lostAndFound = lostAndFound;
	}
	
	public String getPreviousOwner() {
		return previousOwner;
	}
	
	public void setPreviousOwner(String previousOwner) {
		this.previousOwner = previousOwner;
	}
}