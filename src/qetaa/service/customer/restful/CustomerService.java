package qetaa.service.customer.restful;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import qetaa.service.customer.dao.DAO;
import qetaa.service.customer.filters.Secured;
import qetaa.service.customer.filters.SecuredUser;
import qetaa.service.customer.filters.ValidApp;
import qetaa.service.customer.helpers.Helper;
import qetaa.service.customer.model.ActiveSessions;
import qetaa.service.customer.model.Customer;
import qetaa.service.customer.model.CustomerAddress;
import qetaa.service.customer.model.CustomerVisitIndex;
import qetaa.service.customer.model.FacebookProfile;
import qetaa.service.customer.model.HitActivity;
import qetaa.service.customer.model.HitActivityGroup;
import qetaa.service.customer.model.HitCounter;
import qetaa.service.customer.model.LoyaltyGift;
import qetaa.service.customer.model.LoyaltyPoints;
import qetaa.service.customer.model.LoyaltyPointsCreation;
import qetaa.service.customer.model.VisitIndex;
import qetaa.service.customer.model.security.AccessMap;
import qetaa.service.customer.model.security.AccessToken;
import qetaa.service.customer.model.security.CodeLogin;
import qetaa.service.customer.model.security.CustomerAccess;
import qetaa.service.customer.model.security.EmailAccess;
import qetaa.service.customer.model.security.FacebookAccess;
import qetaa.service.customer.model.security.WebApp;

@Path("/")
public class CustomerService {
	@EJB
	private DAO dao;

	@EJB
	private AsyncService async;

	@Secured
	private void test() {

	}

	private void addLoyaltyPoints(Customer customer) {
		List<LoyaltyPoints> lps = dao.getCondition(LoyaltyPoints.class, "customer", customer);
		customer.setLoyaltyPoints(lps);
	}

	@ValidApp
	@GET
	@Path("new-visit-index")
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateNewVisitIndex() {
		try {
			VisitIndex vi = new VisitIndex();
			vi.setCreated(new Date());
			dao.persist(vi);
			return Response.status(200).entity(vi.getId()).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("customer/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomer(@PathParam(value = "param") long customerId) {
		try {
			Customer customer = dao.find(Customer.class, customerId);
			if (customer == null) {
				return Response.status(404).build();
			}

			addLoyaltyPoints(customer);
			return Response.status(200).entity(customer).build();

		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@POST
	@Path("customers-from-ids")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCusotmersFromIds(long[] cids) {
		try {
			String sql = "select * from cst_customer where id in (0";
			for (int i = 0; i < cids.length; i++) {
				sql = sql + "," + cids[i];
			}
			sql = sql + ")";
			List<Customer> customers = dao.getNative(Customer.class, sql);
			for (Customer c : customers) {
				addLoyaltyPoints(c);
			}
			return Response.status(200).entity(customers).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("customers")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCustomers() {
		try {
			List<Customer> customers = dao.get(Customer.class);
			for (Customer c : customers) {
				addLoyaltyPoints(c);
			}
			return Response.status(200).entity(customers).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("reset-sms")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response resetSMS(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String mobile = map.get("mobile");
			String mobileFull = Helper.getFullMobile(mobile, "966");
			List<Customer> list = dao.getCondition(Customer.class, "mobile", mobileFull);
			if (list.isEmpty() || list.size() > 1) {
				return Response.status(404).build();
			} else {
				int code = Helper.getRandomInteger(1000, 9999);
				String fullText = "رمز التحقق:" + code;
				this.async.sendSms(mobileFull, null, fullText, null, "registration");
				return Response.status(200).entity(code).build();
			}

		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("register-email")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response requestEmail(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String mobile = map.get("mobile");
			String email = map.get("email");
			String countryCode = map.get("countryCode");
			List<Customer> l = dao.getTwoOrConditions(Customer.class, "mobile", "email",
					Helper.getFullMobile(mobile, countryCode), email);
			if (null != l && !l.isEmpty()) {
				return Response.status(409).build();
			}
			int code = Helper.getRandomInteger(1000, 9999);
			String fullText = "رمز التحقق:" + code;
			async.sendEmail(email, "رمز التحقق للتسجيل في قطع.كوم", fullText);
			return Response.status(200).entity(code).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("register-sms")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response requestSMS(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String mobile = map.get("mobile");
			String email = map.get("email");
			List<Customer> l = dao.getTwoOrConditions(Customer.class, "mobile", "email",
					Helper.getFullMobile(mobile, "966"), email);
			if (null != l && !l.isEmpty()) {
				return Response.status(409).build();
			}
			int code = Helper.getRandomInteger(1000, 9999);
			String mobileFull = Helper.getFullMobile(mobile, "966");
			String fullText = "رمز التحقق:" + code;
			this.async.sendSms(mobileFull, null, fullText, null, "registration");
			return Response.status(200).entity(code).build();

		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@PUT
	@Path("edit-customer")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response editCustomer(Customer customer) {
		try {
			dao.update(customer);
			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("search-customers/{any}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchCustomersAny(@PathParam(value = "any") String any) {
		try {
			List<Customer> customers = new ArrayList<>();
			String jpql = "select b from Customer b where b.mobile = :value0 or b.firstName like lower(:value1)"
					+ " or b.lastName like lower(:value1) or b.email like lower(:value1) order by b.id";
			customers = dao.getJPQLParams(Customer.class, jpql, Helper.getFullMobile(any, "966"),
					"%" + any.toLowerCase() + "%");
			for (Customer c : customers) {
				addLoyaltyPoints(c);
			}
			return Response.status(200).entity(customers).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("search-customers/mobile/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response searchCustomers(@PathParam(value = "param") String mobile) {
		try {
			List<Customer> customers = new ArrayList<>();
			if (mobile == null || mobile.trim().length() == 0) {
				customers = dao.getOrderBy(Customer.class, "id");
			} else {
				String jpql = "select b from Customer b where b.mobile = :value0 order by b.id";
				customers = dao.getJPQLParams(Customer.class, jpql, Helper.getFullMobile(mobile, "966"));
			}
			for (Customer c : customers) {
				addLoyaltyPoints(c);
			}
			return Response.status(200).entity(customers).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("all-customers-number")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllCustomersNumber() {
		try {
			return Response.status(200).entity(totalNumberCustomers()).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}

	}

	@SecuredUser
	@GET
	@Path("login-to-customer-ratio")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoginsToCustomerRatio() {
		double c = totalLogins() / totalNumberCustomers();
		return Response.status(200).entity(c).build();
	}

	@SecuredUser
	@GET
	@Path("logins-count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTotalLogins() {
		long c = totalLogins();
		return Response.status(200).entity(c).build();
	}

	@SecuredUser
	@GET
	@Path("login-count/date/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoginsCount(@PathParam(value = "param") long dateLong) {
		try {
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());

			String jpql = "select count(b) from AccessToken b where b.id > :value0 and b.created between :value1 and :value2";
			Long count = dao.findJPQLParams(Long.class, jpql, 0L, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("hit-count")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAllHitCount() {
		try {
			String jpql = "select sum(b.counter) from HitCounter b";
			Long count = dao.findJPQLParams(Long.class, jpql);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("hit-count/active")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveCounts() {
		try {
			// last hour
			Date from = Helper.addMinutes(new Date(), -60);
			Date to = new Date();
			String windows = "select count(b) from HitCounter b where b.created between :value0 and :value1 and b.device = :value2";
			Long wcount = dao.findJPQLParams(Long.class, windows, from, to, "Windows");
			String unix = "select count(b) from HitCounter b where b.created between :value0 and :value1 and b.device = :value2";
			Long unixCount = dao.findJPQLParams(Long.class, unix, from, to, "Unix");
			String mac = "select count(b) from HitCounter b where b.created between :value0 and :value1 and b.device = :value2";
			Long macCount = dao.findJPQLParams(Long.class, mac, from, to, "Mac");
			String unknown = "select count(b) from HitCounter b where b.created between :value0 and :value1 and b.device = :value2";
			Long unknownCount = dao.findJPQLParams(Long.class, unknown, from, to, "UnKnown");
			String android = "select count(b) from HitCounter b where b.created between :value0 and :value1 and b.device = :value2";
			Long androidCount = dao.findJPQLParams(Long.class, android, from, to, "Android");
			ActiveSessions as = new ActiveSessions();
			as.setWindows(wcount.intValue());
			as.setAndroid(androidCount.intValue());
			as.setMac(macCount.intValue());
			as.setUnix(unixCount.intValue());
			as.setUnknown(unknownCount.intValue());
			as.setIos(0);
			return Response.status(200).entity(as).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("hit-count/date/{param}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getHitCounts(@PathParam(value = "param") long dateLong) {
		try {
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());

			String jpql = "select sum(b.counter) from HitCounter b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			if (count == null)
				count = 0L;
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("hit-activities/customer/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCustomerHitActivities(@PathParam(value = "param") long customerId) {
		try {
			String sql = "select * from cst_hit_activity_group where customer = " + customerId + " or visit_index in ("
					+ "  select visit_index from cst_customer_visit_index where customer_id = " + customerId
					+ ") order by created";
			List<HitActivity> uniqueHits = dao.getNative(HitActivity.class, sql);
			List<HitActivityGroup> groups = new ArrayList<>();
			for (HitActivity hit : uniqueHits) {
				HitActivityGroup group = new HitActivityGroup();
				group.setFirstActivity(hit);
				List<HitActivity> tailing = dao.getTwoConditionsOrdered(HitActivity.class, "ip", "sessionId",
						hit.getIp(), hit.getSessionId(), "created", "asc");
				group.setTailingActicities(tailing);
				groups.add(group);
			}
			return Response.status(200).entity(groups).build();

		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("hit-activities/date/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getHitActivities(@PathParam(value = "param") long dateLong) {
		try {

			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());

			Helper h = new Helper();
			String sql = "select * from cst_hit_activity_group where created between '" + h.getDateFormat(from)
					+ "' and '" + h.getDateFormat(to) + "' order by created";
			List<HitActivity> uniqueHits = dao.getNative(HitActivity.class, sql);
			List<HitActivityGroup> groups = new ArrayList<>();
			for (HitActivity hit : uniqueHits) {
				HitActivityGroup group = new HitActivityGroup();
				group.setFirstActivity(hit);
				List<HitActivity> tailing = dao.getTwoConditionsOrdered(HitActivity.class, "ip", "sessionId",
						hit.getIp(), hit.getSessionId(), "created", "asc");
				group.setTailingActicities(tailing);
				groups.add(group);
			}
			return Response.status(200).entity(groups).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("hit-activities")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createActivityHits(List<HitActivity> activityHits) {
		try {
			Customer c = activityHits.get(0).getCustomer();
			Long visitIndex = activityHits.get(0).getVisitIndex();
			Customer foundCustomer = null;
			if (null == c) {
				List<CustomerVisitIndex> cvi = dao.getCondition(CustomerVisitIndex.class, "visitIndex.id", visitIndex);
				if (!cvi.isEmpty()) {
					foundCustomer = cvi.get(0).getCustomer();
				}
			}
			for (HitActivity hi : activityHits) {
				List<HitActivity> checks = dao.getTwoConditions(HitActivity.class, "sessionId", "created",
						hi.getSessionId(), hi.getCreated());
				if (checks.isEmpty()) {
					if (hi.getCustomer() == null) {
						hi.setCustomer(foundCustomer);
					}
					dao.persist(hi);
				}
			}
			if (null != c && visitIndex != null) {
				CustomerVisitIndex cvi = dao.findTwoConditions(CustomerVisitIndex.class, "customer", "visitIndex.id", c,
						visitIndex);
				if (null == cvi) {
					String sql = "insert into cst_customer_visit_index (customer_id, visit_index) values (" + c.getId()
							+ "," + visitIndex + ")";
					dao.insertNative(sql);
				}
			}

			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("hit")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createNewHit(Map<String, String> map) {
		try {
			String ip = map.get("ip");
			String device = map.get("device");

			Helper h = new Helper();
			String date = h.getDateFormat(new Date(), "yyyy-MM-dd");
			String sql = "select * from cst_hit_counter where ip = '" + ip
					+ "' and created \\:\\:timestamp\\:\\:date = '" + date + "'";
			List<HitCounter> hitCounters = dao.getNative(HitCounter.class, sql);
			if (!hitCounters.isEmpty()) {
				hitCounters.get(0).setCounter(hitCounters.get(0).getCounter() + 1);
				dao.update(hitCounters.get(0));
			} else {
				HitCounter newHit = new HitCounter();
				newHit.setCounter(1);
				newHit.setCreated(new Date());
				newHit.setIp(ip);
				newHit.setDevice(device);
				dao.persist(newHit);
			}
			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	private Long totalNumberCustomers() {
		String jpql = "select count(b) from Customer b where b.id > :value0";
		return dao.findJPQLParams(Long.class, jpql, 0L);
	}

	public Long totalLogins() {
		String jpql = "select count(b) from AccessToken b where b.id > :value0";
		return dao.findJPQLParams(Long.class, jpql, 0L);
	}
	
	
	@SecuredUser
	@GET
	@Path("new-visit-count/date/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getNewVisitors(@PathParam(value="param") long dateLong) {
		try {
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());

			String jpql = "select count(b) from VisitIndex b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	
	@SecuredUser
	@GET
	@Path("returning-visit-count/date/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getReturningVisitors(@PathParam(value="param") long dateLong) {
		try {
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());
			String jpql = "select count(b) from VisitIndex b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	
	@SecuredUser
	@GET
	@Path("visit-count/date/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVisitors(@PathParam(value="param") long dateLong) {
		try {
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());

			String jpql = "select count(b) from HitCounter b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("registered-count/date/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRegisteredCount(@PathParam(value = "param") long dateLong) {
		try {
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());

			String jpql = "select count(b) from Customer b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("make-hits/date/{param}/make/{param2}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMakeHits(@PathParam(value = "param") long dateLong, @PathParam(value = "param2") String make) {
		try {
			
			Date from = new Date(dateLong);
			Calendar cFrom = Calendar.getInstance();
			cFrom.setTime(from);
			cFrom.set(Calendar.HOUR_OF_DAY, 0);
			cFrom.set(Calendar.MINUTE, 0);
			cFrom.set(Calendar.SECOND, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date(dateLong);
			Calendar cTo = Calendar.getInstance();
			cTo.setTime(to);
			cTo.set(Calendar.HOUR_OF_DAY, 23);
			cTo.set(Calendar.MINUTE, 59);
			cTo.set(Calendar.SECOND, 59);
			cTo.set(Calendar.MILLISECOND, 999);
			to.setTime(cTo.getTimeInMillis());
			
			String jpql = "select count(distinct visitIndex) From HitActivity where activity like :value0 and created between :value1 and :value2";
			Long count = dao.findJPQLParams(Long.class, jpql, ("%" + make + "%"), from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}


	@SecuredUser
	@GET
	@Path("current-month-customers-number")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCurrentMonthCustomersNumber() {
		try {
			Date from = new Date();
			Calendar cFrom = Calendar.getInstance();
			cFrom.set(cFrom.get(Calendar.YEAR), cFrom.get(Calendar.MONTH), 1, 0, 0, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date();
			Calendar cTo = Calendar.getInstance();
			cTo.set(cTo.get(Calendar.YEAR), cTo.get(Calendar.MONTH), cTo.getActualMaximum(Calendar.DAY_OF_MONTH),
					cTo.getActualMaximum(Calendar.HOUR_OF_DAY), 59, 59);
			to.setTime(cTo.getTimeInMillis());

			String jpql = "select count(b) from Customer b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}

	}

	@SecuredUser
	@GET
	@Path("last-month-customers-number")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLastMonthCustomersNumber() {
		try {
			Date from = new Date();
			Calendar cFrom = Calendar.getInstance();
			cFrom.set(cFrom.get(Calendar.YEAR), cFrom.get(Calendar.MONTH), 1, 0, 0, 0);
			cFrom.set(Calendar.MILLISECOND, 0);
			cFrom.add(Calendar.MONTH, -1);
			from.setTime(cFrom.getTimeInMillis());

			Date to = new Date();
			Calendar cTo = Calendar.getInstance();
			cTo.set(cTo.get(Calendar.YEAR), cTo.get(Calendar.MONTH), cTo.getActualMaximum(Calendar.DAY_OF_MONTH),
					cTo.getActualMaximum(Calendar.HOUR_OF_DAY), 59, 59);
			cTo.add(Calendar.MONTH, -1);
			to.setTime(cTo.getTimeInMillis());
			String jpql = "select count(b) from Customer b where b.created between :value0 and :value1";
			Long count = dao.findJPQLParams(Long.class, jpql, from, to);
			return Response.status(200).entity(count).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}

	}

	@SecuredUser
	@GET
	@Path("customer-registration/year/{param}/month/{param2}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPaymentReport(@PathParam(value = "param") int year, @PathParam(value = "param2") int month) {
		try {
			Date from = new Date();
			Date to = new Date();
			if (month == 12) {
				Calendar cFrom = new GregorianCalendar();
				cFrom.set(year, 0, 1, 0, 0, 0);
				cFrom.set(Calendar.MILLISECOND, 0);
				from.setTime(cFrom.getTimeInMillis());

				Calendar cTo = new GregorianCalendar();
				cTo.set(year, 11, 31, 23, 59, 59);
				cTo.set(Calendar.MILLISECOND, 0);
				to.setTime(cTo.getTimeInMillis());
			} else {
				Calendar cFrom = new GregorianCalendar();
				cFrom.set(year, month, 1, 0, 0, 0);
				cFrom.set(Calendar.MILLISECOND, 0);
				from.setTime(cFrom.getTimeInMillis());

				Calendar cTo = new GregorianCalendar();
				cTo.set(year, month, 1, 23, 59, 59);
				cTo.set(Calendar.MILLISECOND, 0);
				cTo.set(Calendar.DAY_OF_MONTH, cTo.getActualMaximum(Calendar.DAY_OF_MONTH));
				to.setTime(cTo.getTimeInMillis());
			}

			String jpql = "select b from Customer b where b.created between :value0 and :value1 order by b.id";
			List<Customer> customers = dao.getJPQLParams(Customer.class, jpql, from, to);
			for (Customer customer : customers) {
				addLoyaltyPoints(customer);
			}
			return Response.status(200).entity(customers).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@POST
	@Path("/send-sms-to-customer/append-generated-code")
	public Response userSendSmsAndAppendCode(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String cId = map.get("customerId");
			String text = map.get("text");
			String cartId = map.get("cartId");
			String purpose = map.get("purpose");
			Customer c = dao.find(Customer.class, Long.parseLong(cId));
			text = text + generateCodeLogin(Long.valueOf(cId), Long.valueOf(cartId));
			if (c.getCountryId() == 1) {
				this.async.sendSms(c.getMobile(), c.getId(), text, cartId, purpose);
			} else {
				this.async.sendEmail(c.getEmail(), "الطلب رقم " + cartId, text);
			}
			return Response.status(200).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@Secured
	@POST
	@Path("/send-sms-to-customer")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response userSendSms(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String cId = map.get("customerId");
			String text = map.get("text");
			String cartId = map.get("cartId");
			String purpose = map.get("purpose");
			Customer c = dao.find(Customer.class, Long.parseLong(cId));
			if (c.getCountryId() == 1) {
				this.async.sendSms(c.getMobile(), c.getId(), text, cartId, purpose);
			} else {	
				this.async.sendEmail(c.getEmail(), "الطلب رقم " + cartId, text);
			}
			return Response.status(200).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@Secured
	@POST
	@Path("/match-token")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response matchToken(AccessMap usermap) {
		try {
			WebApp webApp = getWebAppFromSecret(usermap.getAppSecret());
			System.out.println("Matching token in customer service: ");
			System.out.println("Secret " +usermap.getAppSecret());
			System.out.println("Username " +usermap.getUsername());
			System.out.println("code " +usermap.getCode());
			System.out.println("Language " +usermap.getLanguage());
			List<AccessToken> l = dao.getFourConditionsAndDateBefore(AccessToken.class, "customerId", "webApp.appCode",
					"status", "token", "expire", Long.parseLong(usermap.getUsername()), webApp.getAppCode(), 'A',
					usermap.getCode(), new Date());
			if (!l.isEmpty()) {
				System.out.println("Found Token");
				return Response.status(200).build();
			} else {
				System.out.println("No token matched");
				return Response.status(403).build();// forbidden response
			}
		} catch (Exception e) {
			System.out.println("An Error occured in matching oken");
			return Response.status(403).build();// unauthorized
		}
	}

	@ValidApp
	@PUT
	@Path("reset-password")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response resetPassword(Map<String, String> map) {
		try {
			String mobile = map.get("mobile");
			String password = map.get("password");
			String appSecret = map.get("appSecret");
			Customer customer = dao.findCondition(Customer.class, "mobile", Helper.getFullMobile(mobile.trim(), "966"));
			String cypher = Helper.cypher(password);
			customer.setPassword(cypher);
			dao.update(customer);
			String token = issueToken(customer, getWebAppFromSecret(appSecret), 60);
			CustomerAccess access = new CustomerAccess();
			access.setToken(token);
			addLoyaltyPoints(customer);
			access.setCustomer(customer);
			return Response.status(200).entity(access).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("mobile-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response mobileRegister(EmailAccess eaccess) {
		try {
			CustomerAccess access = new CustomerAccess();
			Customer c = new Customer();
			c.setCreated(new Date());
			c.setEmail(eaccess.getEmail().trim().toLowerCase());
			c.setFirstName(eaccess.getFirstName());
			c.setLastName(eaccess.getLastName());
			c.setPassword(Helper.cypher(eaccess.getPassword()));
			c.setCountryId(eaccess.getCountryId());
			c.setMobile(Helper.getFullMobile(eaccess.getMobile().trim(), "966"));
			c.setStatus('V');

			c.setCreatedBy(eaccess.getCreatedBy());
			c = dao.persistAndReturn(c);
			access.setCustomer(c);
			String token = issueToken(c, getWebAppFromSecret(eaccess.getAppSecret()), 60);
			access.setToken(token);
			return Response.status(200).entity(access).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("facebook-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response facebookRegister(FacebookAccess map) {
		try {
			CustomerAccess access = new CustomerAccess();
			Customer c = new Customer();
			c.setCreated(new Date());
			c.setEmail(map.getContactEmail().trim().toLowerCase());
			c.setFirstName(map.getFirstName());
			c.setLastName(map.getLastName());
			c.setMobile(Helper.getFullMobile(map.getMobile().trim(), map.getCountryCode()));
			c.setStatus('V');// verified
			c.setCountryId(map.getCountryId());
			c = dao.persistAndReturn(c);
			// sendVerificationEmail(c);
			createFacebookProfile(map, c);
			access.setCustomer(c);
			String token = issueToken(c, getWebAppFromSecret(map.getAppSecret()), 60);
			access.setToken(token);
			return Response.status(200).entity(access).build();

		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("email-register")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response emailRegister(EmailAccess map) {
		try {
			CustomerAccess access = new CustomerAccess();
			Customer c = new Customer();
			c.setCreated(new Date());
			c.setEmail(map.getEmail().trim().toLowerCase());
			c.setFirstName(map.getFirstName());
			c.setLastName(map.getLastName());
			c.setPassword(Helper.cypher(map.getPassword()));
			c.setCountryId(map.getCountryId());
			c.setMobile(Helper.getFullMobile(map.getMobile().trim(), map.getCountryCode()));
			c.setStatus('V');
			c.setCreatedBy(map.getCreatedBy());
			c = dao.persistAndReturn(c);
			access.setCustomer(c);
			String token = issueToken(c, getWebAppFromSecret(map.getAppSecret()), 60);
			access.setToken(token);
			return Response.status(200).entity(access).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@GET
	@Path("code-login/code/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response requestCodeLogin(@HeaderParam("Authorization") String authHeader,
			@PathParam(value = "param") String code) {
		try {
			WebApp webApp = getWebAppFromAuthHeader(authHeader);
			String jpql = "select b from CodeLogin b where b.code = :value0" + " and expire >= :value1";
			List<CodeLogin> list = dao.getJPQLParams(CodeLogin.class, jpql, code, new Date());
			if (list.size() == 1) {
				CodeLogin cl = list.get(0);
				Customer c = dao.find(Customer.class, cl.getCustomerId());
				String token = this.issueToken(c, webApp, 60);// 60 minutes
				CustomerAccess access = new CustomerAccess();
				access.setToken(token);
				addLoyaltyPoints(c);
				access.setCustomer(c);
				access.setCartId(cl.getCartId());
				return Response.status(200).entity(access).build();
			}
			return Response.status(404).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}

	}

	// password is not encrypted
	@ValidApp
	@POST
	@Path("email-login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response emailLogin(EmailAccess map) {
		try {
			// already authenticated in facebook
			WebApp webApp = getWebAppFromSecret(map.getAppSecret());
			if (map.getEmail() == null) {
				map.setEmail("");
			}

			String jpql = "select b from Customer b where (email = :value0 or mobile = :value1) and password = :value2";
			List<Customer> list = dao.getJPQLParams(Customer.class, jpql, map.getEmail().trim().toLowerCase(),
					Helper.getFullMobile(map.getEmail().trim(), "966"), map.getPassword());
			if (list.isEmpty()) {
				return Response.status(404).build();
			}
			Customer c = list.get(0);// get customer
			if (c.getStatus() == 'V') {// only if email is verified
				String token = this.issueToken(c, webApp, 60);// 60 minutes
				CustomerAccess access = new CustomerAccess();
				access.setToken(token);
				addLoyaltyPoints(c);
				access.setCustomer(c);
				return Response.status(200).entity(access).build();
			} else {// email not verified
				return Response.status(201).build();
			}
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("facebook-login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response facebookLogin(FacebookAccess map) {
		try {
			// already authenticated in facebook
			WebApp webApp = getWebAppFromSecret(map.getAppSecret());
			// get customer from facebook
			FacebookProfile profile = getFacebookProfile(map.getFacebookId());
			if (profile != null) {
				String token = this.issueToken(profile.getCustomer(), webApp, 60);
				CustomerAccess access = new CustomerAccess();
				access.setToken(token);
				addLoyaltyPoints(profile.getCustomer());
				access.setCustomer(profile.getCustomer());
				if (profile.getCustomer().getStatus() == 'V') {
					// email is verified
					return Response.status(200).entity(access).build();
				} else {
					// email is not verified
					return Response.status(201).entity(access).build();
				}
			} else {// no profile found
				return Response.status(404).build();
			}
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	private FacebookProfile getFacebookProfile(long facebookId) {
		try {
			FacebookProfile profile = dao.find(FacebookProfile.class, facebookId);
			if (null != profile) {
				return profile;
			}
			return null;
		} catch (Exception ex) {
			return null;
		}
	}

	@ValidApp
	@POST
	@Path("validate-app")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateApp(String secret) {
		try {
			this.getWebAppFromSecret(secret);
			return Response.status(200).build();
		} catch (Exception e) {
			return Response.status(404).build();
		}
	}

	@Secured
	@GET
	@Path("address/{param}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAddress(@PathParam(value = "param") long addressId) {
		try {
			CustomerAddress address = dao.find(CustomerAddress.class, addressId);
			return Response.status(200).entity(address).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@DELETE
	@Path("loyalty-points/cart/{cartId}/customer/{customerId}")
	public Response deleteLoyaltyPoints(@PathParam(value = "cartId") long cartId,
			@PathParam(value = "customerId") long customerId) {
		try {
			LoyaltyPoints lp = dao.findThreeConditions(LoyaltyPoints.class, "cartId", "customer.id", "source", cartId,
					customerId, "Purchase");
			dao.delete(lp);
			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@Secured
	@POST
	@Path("loyalty-points/cart")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createLoyaltyPointsFromPurchase(LoyaltyPointsCreation map) {
		try {
			map.setAmount(map.getAmount() / 100);
			LoyaltyPoints lp = new LoyaltyPoints();
			lp.setCartId(map.getCartId());
			lp.setCreated(new Date());
			Customer customer = dao.find(Customer.class, map.getCustomerId());
			lp.setCustomer(customer);
			lp.setGift(null);
			lp.setPoints(Helper.getPoints(map.getAmount()));
			lp.setPurchaseAmount(map.getAmount());
			lp.setSource("Purchase");
			List<LoyaltyPoints> lps = dao.getTwoConditions(LoyaltyPoints.class, "cartId", "customer.id",
					map.getCartId(), map.getCustomerId());
			if (!lps.isEmpty()) {
				return Response.status(409).build();
			}
			dao.persist(lp);
			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@Secured
	@GET
	@Path("loyalty-points/customer/{cid}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoyaltyPoints(@PathParam(value = "cid") long customerId) {
		try {
			List<LoyaltyPoints> lpoints = dao.getCondition(LoyaltyPoints.class, "customer.id", customerId);
			return Response.status(200).entity(lpoints).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@POST
	@Path("loyalty-gift")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createLoyaltyGift(LoyaltyGift gift) {
		try {
			List<LoyaltyGift> check = dao.getTwoConditions(LoyaltyGift.class, "giftNameAr", "points",
					gift.getGiftNameAr(), gift.getPoints());
			if (!check.isEmpty()) {
				return Response.status(409).build();
			}
			dao.persist(gift);
			return Response.status(200).entity(gift.getId()).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@SecuredUser
	@GET
	@Path("loyalty-gifts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLoyaltyGifts() {
		try {
			List<LoyaltyGift> gifts = dao.get(LoyaltyGift.class);
			return Response.status(200).entity(gifts).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@Secured
	@GET
	@Path("active-loyalty-gifts")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getActiveLoyaltyGifts() {
		try {
			String jpql = "select b from LoyaltyGift b where b.status = :value0";
			List<LoyaltyGift> gifts = dao.getJPQLParams(LoyaltyGift.class, jpql, 'A');
			return Response.status(200).entity(gifts).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}

	@Secured
	@PUT
	@Path("address")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateAddress(CustomerAddress address) {
		try {
			dao.update(address);
			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@Secured
	@POST
	@Path("create-address")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createAddress(CustomerAddress address) {
		try {
			address.setCreated(new Date());
			CustomerAddress address2 = dao.persistAndReturn(address);
			return Response.status(200).entity(address2.getAddressId()).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	private void createFacebookProfile(FacebookAccess map, Customer c) {
		FacebookProfile fb = new FacebookProfile();
		fb.setCustomer(c);
		fb.setFacebookId(map.getFacebookId());
		fb.setFirstName(map.getFirstName());
		fb.setLastName(map.getLastName());
		fb.setEmail(map.getEmail());
		dao.persist(fb);
	}

	private String issueToken(Customer customer, WebApp appCode, int expireMinutes) {
		deactivateOldTokens(customer);
		Date tokenTime = new Date();
		AccessToken accessToken = new AccessToken(customer.getId(), tokenTime);
		accessToken.setAppCode(appCode);
		accessToken.setExpire(Helper.addMinutes(tokenTime, expireMinutes));
		accessToken.setStatus('A');
		accessToken.setToken(Helper.getSecuredRandom());
		dao.persist(accessToken);
		return accessToken.getToken();
	}

	private void deactivateOldTokens(Customer customer) {
		List<AccessToken> tokens = dao.getTwoConditions(AccessToken.class, "customerId", "status", customer.getId(),
				'A');
		for (AccessToken t : tokens) {
			t.setStatus('K');// kill old token
			dao.update(t);
		}
	}

	private String generateCodeLogin(long customerId, long cartId) {
		CodeLogin cl = new CodeLogin();
		boolean available = false;
		String code = "";
		do {
			code = Helper.getSaltString();
			String jpql = "select b from CodeLogin b where b.code = :value0 and b.expire >= :value1";
			List<CodeLogin> l = dao.getJPQLParams(CodeLogin.class, jpql, code, new Date());
			if (l.isEmpty()) {
				available = true;
			}
		} while (!available);
		cl.setCode(code);
		cl.setCustomerId(customerId);
		cl.setCartId(cartId);
		cl.setGenerated(new Date());
		cl.setExpire(Helper.addMinutes(cl.getGenerated(), 60 * 24 * 3));
		dao.persist(cl);
		return code;
	}

	private WebApp getWebAppFromAuthHeader(String authHeader) {
		try {
			String[] values = authHeader.split("&&");
			String appSecret = values[2].trim();
			// Validate app secret
			return getWebAppFromSecret(appSecret);
		} catch (Exception ex) {
			return null;
		}
	}

	// retrieves app object from app secret
	private WebApp getWebAppFromSecret(String secret) throws Exception {
		// verify web app secret
		WebApp webApp = dao.findTwoConditions(WebApp.class, "appSecret", "active", secret, true);
		if (webApp == null) {
			throw new Exception();
		}
		return webApp;
	}
}
