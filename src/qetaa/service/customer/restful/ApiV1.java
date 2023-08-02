package qetaa.service.customer.restful;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import qetaa.service.customer.dao.DAO;
import qetaa.service.customer.filters.Secured;
import qetaa.service.customer.filters.ValidApp;
import qetaa.service.customer.helpers.AppConstants;
import qetaa.service.customer.helpers.Helper;
import qetaa.service.customer.model.Customer;
import qetaa.service.customer.model.CustomerAddress;
import qetaa.service.customer.model.CustomerEmailVerification;
import qetaa.service.customer.model.CustomerVehicle;
import qetaa.service.customer.model.SocialMediaProfile;
import qetaa.service.customer.model.contract.PublicAddress;
import qetaa.service.customer.model.contract.PublicCustomer;
import qetaa.service.customer.model.contract.PublicVehicle;
import qetaa.service.customer.model.security.AccessToken;
import qetaa.service.customer.model.security.WebApp;

@Path("/api/v1/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ApiV1 {

	@EJB
	private DAO dao;

	@EJB
	private AsyncService async;
	
	@Secured
	@PUT
	@Path("password")
	public Response updatePassword(Map<String,Object> map) {
		try {
			
			String oldPass = (String) map.get("oldPassword");
			String newPass = (String) map.get("newPassword");
			Long customerId = ((Number) map.get("customerId")).longValue();
			
			String jpql = "select b from Customer b where b.id = :value0 and b.password = :value1";
			Customer customer = dao.findJPQLParams(Customer.class, jpql, customerId, Helper.cypher(oldPass));
			if(customer == null) {
				Map<String,String> mapz = new HashMap<String,String>();
				mapz.put("result", "old password did not match");
				return Response.status(401).entity(mapz).build();
			}
			customer.setPassword(Helper.cypher(newPass));
			dao.update(customer);
			return Response.status(201).build();
		}catch(Exception ex) {
			return Response.status(500).build();
		}
	}
	
	@ValidApp
	@PUT
	@Path("change-email")
	public Response updateEmail(Map<String,String> map) {
		try {
			String code = map.get("code").trim();
			String email = map.get("email").trim();
			String jpql = "select b from CustomerEmailVerification b where b.code = :value0 and b.expire > :value1 and b.newEmail = :value2";
			List<CustomerEmailVerification> list = dao.getJPQLParams(CustomerEmailVerification.class, jpql, code, new Date(), email);
			if(list.isEmpty()) {
				List<CustomerEmailVerification> list2 = dao.getTwoConditions(CustomerEmailVerification.class, "newEmail", "code", email, code);
				if(!list2.isEmpty()) {
					CustomerEmailVerification ev = list2.get(list2.size()-1);
					if(ev.getExpire().before(new Date())){
						dao.delete(ev);
						return Response.status(410).entity("resource expired").build();
					}
				}
				return Response.status(404).entity("resource not found").build();
			}
			CustomerEmailVerification ev = list.get(0);
			Customer customer = dao.find(Customer.class, ev.getCustomerId());
			customer.setEmail(ev.getNewEmail());
			dao.update(customer);
			dao.delete(ev);
			return Response.status(201).build();
		}catch(Exception ex) {
			return Response.status(500).build();
		}
	}
	
	@Secured
	@POST
	@Path("change-email")
	public Response changeEmail(@HeaderParam("Authorization") String authHeader, Map<String,Object> map) {
		try {
			String email = (String) map.get("newEmail");
			Long customerId = ((Number) map.get("customerId")).longValue();
			
			if(!customerFound(customerId)) {
				return Response.status(404).build();
			}
			
			if(!validCustomerOperation(customerId, authHeader)) {
				return Response.status(401).build();
			}
			
			
			List<Customer> list = dao.getCondition(Customer.class, "email", email);
			if(!list.isEmpty()) {
				return Response.status(409).build();
			}
			String code = "";
			boolean available = false;
			do {
				code = Helper.getRandomSaltString(20);
				String jpql = "select b from CustomerEmailVerification b where b.code = :value0 and b.expire >= :value1";
				List<CustomerEmailVerification> l = dao.getJPQLParams(CustomerEmailVerification.class, jpql, code, new Date());
				if (l.isEmpty()) {
					available = true;
				}
			} while (!available);
			
			CustomerEmailVerification ev = new CustomerEmailVerification();
			ev.setCode(code);
			ev.setCode(code);
			ev.setCreated(new Date());
			ev.setCustomerId(customerId);
			ev.setExpire(Helper.addMinutes(ev.getCreated(), 60*24*3));
			ev.setNewEmail(email);
			dao.persist(ev);
			String body = Helper.prepareHtmlActivationEmail(AppConstants.getChangeEmailActivationLink(code, email));
			async.sendHtmlEmail(email, "Email Verification", body);
			return Response.status(201).build();
		}catch(Exception ex) {
			return Response.status(500).build();
		}
	}
	
	//reset sms
	@ValidApp
	@POST
	@Path("reset-sms/mobile")
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
				Map<String, Integer> map2 = new HashMap<String, Integer>();
				map2.put("code", code);
				return Response.status(200).entity(map2).build();
			}

		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("signup")
	public Response emailSignup(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String mobile = map.get("mobile");
			String email = map.get("email");
			String countryCode = map.get("countryCode");
			
			String fullMobile = Helper.getFullMobile(mobile, countryCode);
			List<Customer> l = dao.getCondition(Customer.class, "mobile", fullMobile);
			if(null != l && !l.isEmpty()) {
				return Response.status(409).entity("mobile").build();
			}
			
			List<Customer> l2 = dao.getCondition(Customer.class, "email", email);
			if(null != l2 && !l2.isEmpty()) {
				return Response.status(409).entity("email").build();
			}
			
			int code = Helper.getRandomInteger(1000, 9999);
			String mobileFull = Helper.getFullMobile(mobile, "966");
			String fullText = "رمز التحقق:" + code;
			if (countryCode.equals("966")) {
				this.async.sendSms(mobileFull, null, fullText, null, "registration");
			} else {
				async.sendEmail(email, "رمز التحقق للتسجيل في قطع.كوم", fullText);
			}
			Map<String, Integer> map2 = new HashMap<String, Integer>();
			map2.put("code", code);
			return Response.status(200).entity(map2).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}

	@ValidApp
	@POST
	@Path("login/email") // this is for public api
	public Response emailLogin(@HeaderParam("Authorization") String authHeader, Map<String, Object> map) {
		try {
			// already authenticated in facebook
			WebApp webApp = this.getWebAppFromAuthHeader(authHeader);
			String email = ((String) map.get("email")).trim().toLowerCase();
			String password = Helper.cypher((String) map.get("password"));
			String jpql = "select b from Customer b where (b.email = :value0 or b.mobile = :value1) and b.password = :value2";
			List<Customer> list = dao.getJPQLParams(Customer.class, jpql, email, Helper.getFullMobile(email, "966"),
					password);
			if (list.isEmpty()) {
				return Response.status(404).build();
			}
			Customer customer = list.get(0);// get customer
			Map<String, Object> resMap = getLoginObject(authHeader, customer, webApp);
			return Response.status(200).entity(resMap).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	@ValidApp
	@POST
	@Path("login/social-media")
	public Response socialMediaLogin(@HeaderParam("Authorization") String authHeader, Map<String, Object> map) {
		try {
			String platform = ((String) map.get("platform"));
			String socialMediaId = ((String) map.get("socialMediaId"));
			Customer customer = this.getCustomer(platform, socialMediaId);
			if(customer == null) {
				return Response.status(404).build();
			}
			Map<String, Object> resMap = getLoginObject(authHeader, customer, this.getWebAppFromAuthHeader(authHeader));
			return Response.status(200).entity(resMap).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	@Secured
	@POST
	@Path("social-media")
	public Response addSocialMedia(@HeaderParam("Authorization") String authHeader, Map<String, Object> map) {
		try {
			String platform = (String) map.get("platform");
			long customerId = ((Number) map.get("customerId")).longValue();
			String smid = (String) map.get("socialMediaId");
			if(!customerFound(customerId)) {
				return Response.status(404).build();
			}
			
			if(!validCustomerOperation(customerId, authHeader)) {
				return Response.status(401).build();
			}
						
			if(!checkSocialMediaIsAvailable(smid, platform)) {
				return Response.status(409).build();
			}
			
			Customer customer = dao.find(Customer.class, customerId);
			createSocialMediaLink(customer, smid, platform);
			return Response.status(201).build();
		}catch(Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}
	
	@Secured
	@PUT
	@Path("customer")
	public Response editCustomer(@HeaderParam("Authorization") String authHeader, Map<String,Object> map) {
		try {
			Long customerId = ((Number) map.get("customerId")).longValue();
			String firstName = (String) map.get("firstName");
			String lastName = (String) map.get("lastName");
			String defaultLang = (String) map.get("defaultLang");
			
			Customer c = dao.find(Customer.class, customerId);
			
			if(!customerFound(c.getId())) {
				return Response.status(404).build();
			}
			
			if(!validCustomerOperation(c.getId(), authHeader)) {
				return Response.status(401).build();
			}
			
			c.setFirstName(firstName);
			c.setLastName(lastName);
			c.setDefaultLang(defaultLang);
			
			dao.update(c);
			return Response.status(201).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	private Customer getCustomer(String platform, String socialMediaId) {
		SocialMediaProfile sm = dao.findTwoConditions(SocialMediaProfile.class, "platform", "socialMediaId", platform, socialMediaId);
		if(sm == null) {
			return null;
		}
		return dao.find(Customer.class, sm.getCustomerId());
	}
	
	
	@ValidApp
	@PUT
	@Path("reset-password")
	public Response resetPassword(@HeaderParam("Authorization") String authHeader, Map<String, String> map) {
		try {
			String mobile = map.get("mobile");
			String password = map.get("password");
			Customer customer = dao.findCondition(Customer.class, "mobile", Helper.getFullMobile(mobile.trim(), "966"));
			
			if(!customerFound(customer.getId())) {
				return Response.status(404).build();
			}
			
			String cypher = Helper.cypher(password);
			customer.setPassword(cypher);
			dao.update(customer);
			Map<String, Object> resMap = getLoginObject(authHeader, customer, getWebAppFromAuthHeader(authHeader));
			return Response.status(200).entity(resMap).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}
	
	private boolean customerFound(long customerId) {
		Customer c = dao.find(Customer.class, customerId);
		if(c == null) {
			return false;
		}
		return true;
	}
	
	private boolean validCustomerOperation(long customerId, String authHeader) {
		Customer check = getCustomerFromAuthHeader(authHeader);
		if(check.getId() != customerId) {
			return false;
		}
		return true;
	}
	
	@Secured
	@POST
	@Path("vehicle")
	public Response createVehicle(@HeaderParam("Authorization") String authHeader, PublicVehicle publicVehicle) {
		try {
			if(!customerFound(publicVehicle.getCustomerId())) {
				return Response.status(404).build();
			}
			if(!validCustomerOperation(publicVehicle.getCustomerId(), authHeader)) {
				return Response.status(401).build();
			}
			CustomerVehicle cv = new CustomerVehicle();
			cv.setCreated(new Date());
			cv.setStatus('A');
			cv.setCreatedBy(0);
			cv.setCustomerId(publicVehicle.getCustomerId());
			cv.setVehicleId(publicVehicle.getVehicleId());
			cv.setVin(publicVehicle.getVin());
			dao.persist(cv);
			publicVehicle.setId(cv.getId());
			publicVehicle.setVehicle(this.getVehicleFromId(authHeader, publicVehicle.getVehicleId()));
			return Response.status(200).entity(publicVehicle).build();
		}catch(Exception ex) {
			return Response.status(500).build();
		}
	}

	
	@Secured
	@POST
	@Path("address")
	public Response createAddress(@HeaderParam("Authorization") String authHeader, PublicAddress publicAddress) {
		try {
			if(!customerFound(publicAddress.getCustomerId())) {
				return Response.status(404).build();
			}
			
			if(!validCustomerOperation(publicAddress.getCustomerId(), authHeader)) {
				return Response.status(401).build();
			}
			
			CustomerAddress address= new CustomerAddress();
			address.setCityId(publicAddress.getCityId());
			address.setCreated(new Date());
			address.setCreatedBy(0);
			address.setCustomerId(publicAddress.getCustomerId());
			address.setLatitude(publicAddress.getLatitude());
			address.setLine1(publicAddress.getLine1());
			address.setLine2(publicAddress.getLine2());
			address.setLongitude(publicAddress.getLongitude());
			address.setStatus('A');
			address.setTitle(publicAddress.getTitle());
			address.setZipCode(publicAddress.getZipCode());
			dao.persist(address);
			publicAddress.setAddressId(address.getAddressId());
			return Response.status(200).entity(publicAddress).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	@ValidApp
	@POST
	@Path("register/social-media")
	public Response facebookRegister(@HeaderParam("Authorization") String authHeader, Map<String, Object> map) {
		try {
			String email = ((String) map.get("email")).trim().toLowerCase();
			String firstName = ((String) map.get("firstName")).trim();
			String lastName = ((String) map.get("lastName")).trim();
			Integer countryId = ((Number) map.get("countryId")).intValue();
			String mobile = ((String) map.get("mobile")).trim();
			String countryCode = ((String) map.get("countryCode"));
			String platform = ((String) map.get("platform"));
			String socialId = ((String) map.get("socialMediaId"));
			
			if(!checkSocialMediaIsAvailable(socialId, platform)) {
				return Response.status(409).build();
			}
			
			Customer c = new Customer();
			c.setCreated(new Date());
			c.setEmail(email);
			c.setFirstName(firstName);
			c.setLastName(lastName);
			c.setCountryId(countryId);
			c.setMobile(Helper.getFullMobile(mobile, countryCode));
			c.setStatus('V');
			c.setCreatedBy(0);
			dao.persist(c);
			
			createSocialMediaLink(c, socialId, platform);
			Map<String, Object> resMap = getLoginObject(authHeader, c, getWebAppFromAuthHeader(authHeader));
			return Response.status(200).entity(resMap).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(500).build();
		}
	}
	
	@ValidApp
	@POST
	@Path("register/email")
	public Response mobileRegister(@HeaderParam("Authorization") String authHeader, Map<String, Object> map) {
		try {
			String email = ((String) map.get("email")).trim().toLowerCase();
			String firstName = ((String) map.get("firstName")).trim();
			String lastName = ((String) map.get("lastName")).trim();
			String password = (String) map.get("password");
			Integer countryId = ((Number) map.get("countryId")).intValue();
			String mobile = ((String) map.get("mobile")).trim();
			String countryCode = ((String) map.get("countryCode"));
			
			Customer c = new Customer();
			c.setCreated(new Date());
			c.setEmail(email);
			c.setFirstName(firstName);
			c.setLastName(lastName);
			c.setPassword(Helper.cypher(password));
			c.setCountryId(countryId);
			c.setMobile(Helper.getFullMobile(mobile, countryCode));
			c.setStatus('V');
			c.setCreatedBy(0);
			dao.persist(c);
			Map<String, Object> resMap = getLoginObject(authHeader, c, getWebAppFromAuthHeader(authHeader));
			return Response.status(200).entity(resMap).build();
		} catch (Exception ex) {
			return Response.status(500).build();
		}
	}
	
	private void createSocialMediaLink(Customer c, String socialMediaId, String platform) throws Exception{
		SocialMediaProfile sm = new SocialMediaProfile();
		sm.setCustomerId(c.getId());
		sm.setPlatform(platform);
		sm.setSocialMediaId(socialMediaId);
		dao.persist(sm);
	}
	
	private List<SocialMediaProfile> getSocialMediaProfiles(long customerId){
		return dao.getCondition(SocialMediaProfile.class, "customerId", customerId);
	}
	
	private boolean checkSocialMediaIsAvailable(String socialMediaId, String platform) {
		SocialMediaProfile smCheck = dao.findTwoConditions(SocialMediaProfile.class, "platform", "socialMediaId", platform, socialMediaId);
		if(smCheck != null) {
			return false;
		}
		return true;
	}
	
	private Customer getCustomerFromAuthHeader(String authHeader) {
		try {
			String[] values = authHeader.split("&&");
			String username = values[1].trim();
			Customer c = dao.find(Customer.class, Long.parseLong(username));
			return c;
		} catch (Exception ex) {
			return null;
		}
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

	private Map<String, Object> getLoginObject(String authHeader, Customer customer, WebApp webApp) {
		AccessToken token = this.issueToken(customer, webApp, 60);// 60 minutes
		
		List<CustomerAddress> addresses = dao.getTwoConditions(CustomerAddress.class, "customerId", "status",
				customer.getId(), 'A');		
		List<SocialMediaProfile> smps = this.getSocialMediaProfiles(customer.getId());
		PublicCustomer pc = new PublicCustomer(customer, smps, addresses, getCustomerPublicVehicles(authHeader, customer.getId()));
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("token", token.getToken());
		map.put("tokenExpire", token.getExpire().getTime());
		map.put("customer", pc);
		return map;
	}
	
	private Map<String,Object> getVehicleFromId(String authHeader, int modelYearId){
		List<Integer> modelYearIds = new ArrayList<>();
		modelYearIds.add(modelYearId);
		Response r = this.postSecuredRequest(AppConstants.POST_GET_MODEL_YEARS_FROM_IDS, modelYearIds, authHeader);
		if(r.getStatus() == 200) {
			List<Map<String,Object>> list = r.readEntity(new GenericType<List<Map<String,Object>>>(){});
			return list.get(0);
		}
		return null;
	}
	
	private List<PublicVehicle> getCustomerPublicVehicles(String authHeader, long customerId){
		String jpql = "select b.vehicleId from CustomerVehicle b where b.customerId = :value0 and b.status = :value1"; 
		List<Integer> modelYearIds = dao.getJPQLParams(Integer.class, jpql, customerId, 'A');
		Response r = this.postSecuredRequest(AppConstants.POST_GET_MODEL_YEARS_FROM_IDS, modelYearIds, authHeader);
		List<PublicVehicle> pvs = new ArrayList<>();
		if(r.getStatus() == 200) {
			List<Map<String,Object>> list = r.readEntity(new GenericType<List<Map<String,Object>>>(){});
			List<CustomerVehicle> vehicles = dao.getTwoConditions(CustomerVehicle.class, "customerId", "status", customerId, 'A');
			for(CustomerVehicle cv : vehicles) {
				PublicVehicle pv = new PublicVehicle(cv);
				for(Map<String,Object> map : list) {
					if(((Integer)map.get("id")).intValue() == cv.getVehicleId()) {
						pv.setVehicle(map);
						break;
					}
				}
				pvs.add(pv);
			}
		}
		return pvs;
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

	private AccessToken issueToken(Customer customer, WebApp appCode, int expireMinutes) {
		deactivateOldTokens(customer);
		Date tokenTime = new Date();
		AccessToken accessToken = new AccessToken(customer.getId(), tokenTime);
		accessToken.setAppCode(appCode);
		accessToken.setExpire(Helper.addMinutes(tokenTime, expireMinutes));
		accessToken.setStatus('A');
		accessToken.setToken(Helper.getSecuredRandom());
		dao.persist(accessToken);
		return accessToken;
	}
	
	private void deactivateOldTokens(Customer customer) {
		List<AccessToken> tokens = dao.getTwoConditions(AccessToken.class, "customerId", "status", customer.getId(),
				'A');
		for (AccessToken t : tokens) {
			t.setStatus('K');// kill old token
			dao.update(t);
		}
	}

	public <T> Response postSecuredRequest(String link, T t, String authHeader) {
		Builder b = ClientBuilder.newClient().target(link).request();
		b.header(HttpHeaders.AUTHORIZATION, authHeader);
		Response r = b.post(Entity.entity(t, "application/json"));// not secured
		return r;
	}
	
}
