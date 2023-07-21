package qetaa.service.customer.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;



@Entity
@Table(name="cst_customer_visit_index")
@IdClass(CustomerVisitIndex.CustomerVisitIndexPK.class)
public class CustomerVisitIndex {
	
	@Id
	@JoinColumn(name="customer_id", referencedColumnName="id")
	@ManyToOne
	private Customer customer;
	
	@Id
	@JoinColumn(name="visit_index", referencedColumnName="id")
	@ManyToOne
	private VisitIndex visitIndex;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((customer == null) ? 0 : customer.hashCode());
		result = prime * result + ((visitIndex == null) ? 0 : visitIndex.hashCode());
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
		CustomerVisitIndex other = (CustomerVisitIndex) obj;
		if (customer == null) {
			if (other.customer != null)
				return false;
		} else if (!customer.equals(other.customer))
			return false;
		if (visitIndex == null) {
			if (other.visitIndex != null)
				return false;
		} else if (!visitIndex.equals(other.visitIndex))
			return false;
		return true;
	}







	public Customer getCustomer() {
		return customer;
	}







	public void setCustomer(Customer customer) {
		this.customer = customer;
	}







	public VisitIndex getVisitIndex() {
		return visitIndex;
	}







	public void setVisitIndex(VisitIndex visitIndex) {
		this.visitIndex = visitIndex;
	}







	public static class CustomerVisitIndexPK implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		protected long customer;
		protected long visitIndex;
		
		public CustomerVisitIndexPK() {}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (customer ^ (customer >>> 32));
			result = prime * result + (int) (visitIndex ^ (visitIndex >>> 32));
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
			CustomerVisitIndexPK other = (CustomerVisitIndexPK) obj;
			if (customer != other.customer)
				return false;
			if (visitIndex != other.visitIndex)
				return false;
			return true;
		}
		
		

		
		
		
	}

}
