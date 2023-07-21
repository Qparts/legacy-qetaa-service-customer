package qetaa.service.customer.model.security;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Table(name="cst_code_login")
@Entity
public class CodeLogin implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(name = "cst_code_login_id_seq_gen", sequenceName = "cst_code_login_id_seq", initialValue=1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_code_login_id_seq_gen")
	@Column(name="id")
	private long id;
	@Column(name="customer_id")
	private long customerId;
	@Column(name="cart_id")
	private long cartId;
	@Column(name="code")
	private String code;
	@Column(name="page")
	private String page;
	
	@Column(name="generated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date generated;
	@Column(name="expire")
	@Temporal(TemporalType.TIMESTAMP)
	private Date expire;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getCustomerId() {
		return customerId;
	}
	public void setCustomerId(long customerId) {
		this.customerId = customerId;
	}
	public long getCartId() {
		return cartId;
	}
	public void setCartId(long cartId) {
		this.cartId = cartId;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Date getGenerated() {
		return generated;
	}
	public void setGenerated(Date generated) {
		this.generated = generated;
	}
	public Date getExpire() {
		return expire;
	}
	public void setExpire(Date expire) {
		this.expire = expire;
	}
	public String getPage() {
		return page;
	}
	public void setPage(String page) {
		this.page = page;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (cartId ^ (cartId >>> 32));
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + (int) (customerId ^ (customerId >>> 32));
		result = prime * result + ((expire == null) ? 0 : expire.hashCode());
		result = prime * result + ((generated == null) ? 0 : generated.hashCode());
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((page == null) ? 0 : page.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CodeLogin other = (CodeLogin) obj;
		if (cartId != other.cartId)
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (customerId != other.customerId)
			return false;
		if (expire == null) {
			if (other.expire != null)
				return false;
		} else if (!expire.equals(other.expire))
			return false;
		if (generated == null) {
			if (other.generated != null)
				return false;
		} else if (!generated.equals(other.generated))
			return false;
		if (id != other.id)
			return false;
		if (page == null) {
			if (other.page != null)
				return false;
		} else if (!page.equals(other.page))
			return false;
		return true;
	}
	

}
