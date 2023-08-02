package qetaa.service.customer.model.contract;

import java.util.Map;

import qetaa.service.customer.model.CustomerVehicle;

public class PublicVehicle {
	private long id;
	private Integer vehicleId;
	private long customerId;
	private String vin;
	private Map<String,Object> vehicle;
	
	public PublicVehicle() {
		
	}
	
	public PublicVehicle(CustomerVehicle cv) {
		this.id = cv.getId();
		this.vehicleId = cv.getVehicleId();
		this.customerId = cv.getCustomerId();
		this.vin = cv.getVin();
	}
	
	public Map<String, Object> getVehicle() {
		return vehicle;
	}

	public void setVehicle(Map<String, Object> vehicle) {
		this.vehicle = vehicle;
	}

	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Integer getVehicleId() {
		return vehicleId;
	}
	public void setVehicleId(Integer vehicleId) {
		this.vehicleId = vehicleId;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public String getVin() {
		return vin;
	}
	public void setVin(String vin) {
		this.vin = vin;
	}
	
	
}
