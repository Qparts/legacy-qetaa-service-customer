package qetaa.service.customer.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="cst_loyalty_gift")
public class LoyaltyGift implements Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	@SequenceGenerator(name = "cst_loyalty_gift_id_seq_gen", sequenceName = "cst_loyalty_gift_id_seq", initialValue=1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cst_loyalty_gift_id_seq_gen")
	@Column(name = "id", updatable=false)
	private int id;
	@Column(name="gift_name")
	private String giftName;
	@Column(name="gift_name_ar")
	private String giftNameAr;
	@Column(name="points")
	private int points;
	@Column(name="status")
	private char status;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getGiftName() {
		return giftName;
	}
	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}
	public String getGiftNameAr() {
		return giftNameAr;
	}
	public void setGiftNameAr(String giftNameAr) {
		this.giftNameAr = giftNameAr;
	}
	public int getPoints() {
		return points;
	}
	public void setPoints(int points) {
		this.points = points;
	}
	public char getStatus() {
		return status;
	}
	public void setStatus(char status) {
		this.status = status;
	}
	
	
}
