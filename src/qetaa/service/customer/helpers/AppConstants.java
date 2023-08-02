package qetaa.service.customer.helpers;

public final class AppConstants {
	private final static String USER_SERVICE = "http://localhost:8080/service-qetaa-user/rest/";
	private final static String QUOTATION_SERVICE = "http://localhost:8080/service-qetaa-quotation/rest/";
	private final static String WEBSITE_BASE_URL = "http://qtest.fareed9.com/";
	private final static String PUBLIC_VEHICLE_SERVICE = "http://localhost:8080/service-qetaa-vehicle/rest/api/v1/";
	public final static String USER_MATCH_TOKEN = USER_SERVICE + "match-token";
	
	private static final String SMS_PROVIDER_HOST="https://www.lanasms.net/api/sendsms.php?";
	private static final String SMS_MAX_PROVIDER_HOST="https://www.jawalbsms.ws/api.php/sendsms?";
	private static final String SMS_PROVIDER_USERNAME="qit3.com";
	private static final String SMS_MAX_PROVIDER_USERNAME="qetaa";
	private static final String SMS_PROVIDER_PASSWORD="123123s";
	private static final String SMS_MAX_PROVIDER_PASSWORD="qetaa13AyMg";
	private static final String SMS_PROVIDER_SENDER="qetaa.com";
	private static final String SMS_MAX_PROVIDER_SENDER="qetaa.com";
	
	public final static String PAYFORT_ACCESS_CODE = "";
	public final static String PAYFORT_MERCHANT_ID = "";
	public final static String PAYFORT_CURRENCY = "SAR";
	
	public final static String POST_GET_MODEL_YEARS_FROM_IDS = PUBLIC_VEHICLE_SERVICE + "model-year-from-ids";
		
	public final static String getChangeEmailActivationLink(String code, String email) {
		return WEBSITE_BASE_URL + "change-email?code=" + code + "&email=" + email;
	}
	
	public static String getPurchaseId(long cartId){
		return "PO-" + cartId;
	}
	
	public static String getCustomerIdFromCartId(Long cartId) {
		return QUOTATION_SERVICE + "customer-id/cart/" + cartId;
	}
	
	public final static String getSMSRegister(String mobile,int text){
		return SMS_PROVIDER_HOST
				+ "username="+SMS_PROVIDER_USERNAME
				+ "&password="+SMS_PROVIDER_PASSWORD
				+ "&message="+text
				+ "&numbers="+mobile
				+ "&sender="+SMS_PROVIDER_SENDER
				+ "&return=json";
	}
	
	public final static String getSMSLink(String mobile,String text){
		return SMS_PROVIDER_HOST
				+ "username="+SMS_PROVIDER_USERNAME
				+ "&password="+SMS_PROVIDER_PASSWORD
				+ "&message="+text
				+ "&numbers="+mobile
				+ "&sender="+SMS_PROVIDER_SENDER
				+ "&unicode=E"
				+ "&return=json";
	}
	
	public final static String getSMSMaxLink(String mobile,String text){
		return SMS_MAX_PROVIDER_HOST
				+ "user="+SMS_MAX_PROVIDER_USERNAME
				+ "&pass="+SMS_MAX_PROVIDER_PASSWORD
				+ "&to="+mobile
				+ "&message="+text
				+ "&sender="+SMS_MAX_PROVIDER_SENDER;
	}
	
	public void replaceCharacter() {
		
	}
	
}
