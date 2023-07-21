package qetaa.service.customer.model.security;

import qetaa.service.customer.model.Customer;

public class CustomerAccess {

	private Customer customer; 
	private String token;
	private long cartId;
	public Customer getCustomer() {
		return customer;
	}
	public String getToken() {
		return token;
	}
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public long getCartId() {
		return cartId;
	}
	public void setCartId(long cartId) {
		this.cartId = cartId;
	}
	
	
	
}
