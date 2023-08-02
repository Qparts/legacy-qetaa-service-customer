package qetaa.service.customer.model.contract;

import qetaa.service.customer.model.CustomerAddress;

public class PublicAddress {
	private long addressId;
	private long customerId;
	private String line1;
	private String line2;
	private Integer cityId;
	private String zipCode;
	private String title;
	private Double latitude;
	private Double longitude;
	
	public PublicAddress() {
		
	}
	
	public PublicAddress(CustomerAddress customerAddress) {
		this.addressId = customerAddress.getAddressId();
		this.customerId = customerAddress.getCustomerId();
		this.line1 = customerAddress.getLine1();
		this.line2 = customerAddress.getLine2();
		this.cityId = customerAddress.getCityId();
		this.zipCode = customerAddress.getZipCode();
		this.title = customerAddress.getTitle();
		this.latitude = customerAddress.getLatitude();
		this.longitude = customerAddress.getLongitude();
	}
	
	
	
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public long getAddressId() {
		return addressId;
	}
	public void setAddressId(long addressId) {
		this.addressId = addressId;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public String getLine1() {
		return line1;
	}
	public void setLine1(String line1) {
		this.line1 = line1;
	}
	public String getLine2() {
		return line2;
	}
	public void setLine2(String line2) {
		this.line2 = line2;
	}
	public Integer getCityId() {
		return cityId;
	}
	public void setCityId(Integer cityId) {
		this.cityId = cityId;
	}
	public String getZipCode() {
		return zipCode;
	}
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	
	
}
