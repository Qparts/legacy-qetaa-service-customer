package qetaa.service.customer.helpers;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class Helper {
	public static int getRandomInteger(int min, int max) {
		Random random = new Random();
		return random.nextInt(max - min + 1) + min;
	}
	
	public static int getPoints(double amount) {
		return (int) (amount / 20);
	}

	public static String getSecuredRandom() {
		SecureRandom random = new SecureRandom();
		return new BigInteger(130, random).toString(32);
	}
	
	public static String getFullMobile(String mobile, String countryCode){
		String mobileFull = mobile;
		mobileFull = mobileFull.replaceFirst("^0+(?!$)", "");
		mobileFull = countryCode + mobileFull;
		return mobileFull;
	}

	public static Date addMinutes(Date original, int minutes) {
		return new Date(original.getTime() + (1000L * 60 * minutes));
	}
	
	public String getDateFormat(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSX");
		return sdf.format(date);
	}
	
	public String getDateFormat(Date date, String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(date);
	}

	public static String cypher(String text) throws NoSuchAlgorithmException {
		String shaval = "";
		MessageDigest algorithm = MessageDigest.getInstance("SHA-256");

		byte[] defaultBytes = text.getBytes();

		algorithm.reset();
		algorithm.update(defaultBytes);
		byte messageDigest[] = algorithm.digest();
		StringBuilder hexString = new StringBuilder();

		for (int i = 0; i < messageDigest.length; i++) {
			String hex = Integer.toHexString(0xFF & messageDigest[i]);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		shaval = hexString.toString();

		return shaval;
	}
	
	public static String getSaltString() {
		 String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		 StringBuilder salt = new StringBuilder();
		 Random rnd = new Random();
		 while (salt.length() < 5) { // length of the random string.
	            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
	            salt.append(SALTCHARS.charAt(index));
		 }
		 String saltStr = salt.toString();
	     return saltStr;
	}

}