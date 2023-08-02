package qetaa.service.customer.restful;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Properties;

import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import qetaa.service.customer.dao.DAO;
import qetaa.service.customer.helpers.AppConstants;
import qetaa.service.customer.model.SmsSent;

@Stateless
public class AsyncService {

	private static String EMAIL_FROM = "no-reply@qetaa.com";
	private static String PASSWORD = "qetaa3!Cs@";
	private static String SMTP = "smtp.zoho.com";

	@EJB
	private DAO dao;

	@Asynchronous
	public void sendHtmlEmail(String email, String subject, String body) {
		Properties properties = System.getProperties();
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(EMAIL_FROM, PASSWORD);
			}
		});
		properties.setProperty("mail.smtp.host", SMTP);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.starttls.enable", "true");
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(EMAIL_FROM));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject(subject);
			message.setContent(body, "text/html; charset=utf-8");
			Transport.send(message);
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}
	
	@Asynchronous
	public void sendEmail(String email, String subject, String text) {
		Properties properties = System.getProperties();
		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(EMAIL_FROM, PASSWORD);
			}
		});
		properties.setProperty("mail.smtp.host", SMTP);
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.starttls.enable", "true");
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(EMAIL_FROM));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(email));
			message.setSubject(subject);
			message.setText(text);
			Transport.send(message);
		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}

	public void sendSms(String mobileFull, Long customerId, String text, String cartId, String purpose) {
		try {
			String textEncoded = URLEncoder.encode(text, "utf-8");
			String url = AppConstants.getSMSMaxLink(mobileFull, textEncoded);
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			SmsSent smsSent = new SmsSent();
			smsSent.setMobile(mobileFull);
			smsSent.setCartId(Long.parseLong(cartId));
			smsSent.setPurpose(purpose);
			smsSent.setText(text);
			smsSent.setCreated(new Date());
			smsSent.setCustomerId(customerId);
			if (responseCode == 200) {
				// check inputLine status, if 100 .. status = 'S', if other status = 'D'
				smsSent.setStatus('S');// success
			} else {
				smsSent.setStatus('F');// success
			}
			dao.persist(smsSent);

		} catch (Exception ex) {

		}
	}

	public Response getSecuredRequest(String link, String authHeader) {
		Builder b = ClientBuilder.newClient().target(link).request();
		b.header(HttpHeaders.AUTHORIZATION, authHeader);
		Response r = b.get();
		return r;
	}

	public <T> Response postSecuredRequest(String link, T t, String authHeader) {
		Builder b = ClientBuilder.newClient().target(link).request();
		b.header(HttpHeaders.AUTHORIZATION, authHeader);
		Response r = b.post(Entity.entity(t, "application/json"));// not secured
		return r;
	}

}
