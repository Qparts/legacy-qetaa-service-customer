package qetaa.service.customer.model;

import java.util.List;

public class LoyaltyGiftCategory {

	private List<LoyaltyGift> orangeLevel;
	private List<LoyaltyGift> blueLevel;
	private List<LoyaltyGift> greenLevel;
	
	public List<LoyaltyGift> getOrangeLevel() {
		return orangeLevel;
	}
	public void setOrangeLevel(List<LoyaltyGift> orangeLevel) {
		this.orangeLevel = orangeLevel;
	}
	public List<LoyaltyGift> getBlueLevel() {
		return blueLevel;
	}
	public void setBlueLevel(List<LoyaltyGift> blueLevel) {
		this.blueLevel = blueLevel;
	}
	public List<LoyaltyGift> getGreenLevel() {
		return greenLevel;
	}
	public void setGreenLevel(List<LoyaltyGift> greenLevel) {
		this.greenLevel = greenLevel;
	}
	
	
}
