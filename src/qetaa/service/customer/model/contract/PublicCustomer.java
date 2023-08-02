package qetaa.service.customer.model.contract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import qetaa.service.customer.model.Customer;
import qetaa.service.customer.model.CustomerAddress;
import qetaa.service.customer.model.SocialMediaProfile;

public class PublicCustomer {
	
	private long id;
	private String email;
	private String firstName;
	private String lastName;
	private String mobile;
	private Integer countryId;
	private String defaultLang;
	private List<Map<String,Object>> socialMedia;
	private List<PublicAddress> addresses;
	private List<PublicVehicle> vehicles;
	
	public PublicCustomer(Customer customer, List<SocialMediaProfile> profiles, List<CustomerAddress> customerAddresses, List<PublicVehicle> vehicles) {
		this.id = customer.getId();
		this.email = customer.getEmail();
		this.firstName = customer.getFirstName();
		this.lastName = customer.getLastName();
		this.mobile = customer.getMobile();
		this.countryId = customer.getCountryId();
		this.defaultLang = customer.getDefaultLang();
		this.addresses = new ArrayList<>();
		for(CustomerAddress ca : customerAddresses) {
			PublicAddress address = new PublicAddress(ca);
			this.addresses.add(address);
		}
		
		this.vehicles = vehicles;
		
		socialMedia = new ArrayList<>();
		for(SocialMediaProfile smp : profiles) {
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("platform", smp.getPlatform());
			map.put("socialMediaId", smp.getSocialMediaId());
			this.socialMedia.add(map);
		}
	}
	
	

	
	public List<Map<String, Object>> getSocialMedia() {
		return socialMedia;
	}




	public void setSocialMedia(List<Map<String, Object>> socialMedia) {
		this.socialMedia = socialMedia;
	}




	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getMobile() {
		return mobile;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public Integer getCountryId() {
		return countryId;
	}
	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
	}
	public List<PublicAddress> getAddresses() {
		return addresses;
	}
	public void setAddresses(List<PublicAddress> addresses) {
		this.addresses = addresses;
	}

	public List<PublicVehicle> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<PublicVehicle> vehicles) {
		this.vehicles = vehicles;
	}




	public String getDefaultLang() {
		return defaultLang;
	}

	public void setDefaultLang(String defaultLang) {
		this.defaultLang = defaultLang;
	}
	
}
