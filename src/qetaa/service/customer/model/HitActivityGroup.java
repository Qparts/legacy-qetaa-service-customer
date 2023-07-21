package qetaa.service.customer.model;

import java.io.Serializable;
import java.util.List;

public class HitActivityGroup implements Serializable{

	private static final long serialVersionUID = 1L;

	private HitActivity firstActivity;
	private List<HitActivity> tailingActicities;
	
	
	public HitActivity getFirstActivity() {
		return firstActivity;
	}
	public void setFirstActivity(HitActivity firstActivity) {
		this.firstActivity = firstActivity;
	}
	public List<HitActivity> getTailingActicities() {
		return tailingActicities;
	}
	public void setTailingActicities(List<HitActivity> tailingActicities) {
		this.tailingActicities = tailingActicities;
	}
	
	
}
