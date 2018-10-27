package me.t4tu.rkeconomy.markets;

public enum MarketStallType {
	
	TORIKOJU("Torikoju"), LIIKEKIINTEISTÖ("Liikekiinteistö");
	
	private String friendlyName;
	
	private MarketStallType(String friendlyName) {
		this.friendlyName = friendlyName;
	}
	
	public String getFriendlyName() {
		return friendlyName;
	}
}