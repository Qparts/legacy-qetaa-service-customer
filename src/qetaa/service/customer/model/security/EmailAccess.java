package qetaa.service.customer.model.security;

import java.io.Serializable;

public class EmailAccess implements Serializable{
	private static final long serialVersionUID = 1L;
	private String appSecret;//from which app was logged in
	private String email;//contact email
	private String firstName;//first name from facebook
	private String lastName;//last name from facebook
	private String mobile;//mobile number (only for registration)
	private String password;//password
	private Integer countryId;
	private String countryCode;
	private int createdBy;
	
	
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public Integer getCountryId() {
		return countryId;
	}
	public void setCountryId(Integer countryId) {
		this.countryId = countryId;
	}
	public int getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(int createdBy) {
		this.createdBy = createdBy;
	}
	public String getAppSecret() {
		return appSecret;
	}
	public String getEmail() {
		return email;
	}
	public String getFirstName() {
		return firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public String getMobile() {
		return mobile;
	}
	public String getPassword() {
		return password;
	}
	public void setAppSecret(String appSecret) {
		this.appSecret = appSecret;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public void setMobile(String mobile) {
		this.mobile = mobile;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
}
