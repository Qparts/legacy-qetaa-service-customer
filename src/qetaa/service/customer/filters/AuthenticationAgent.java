package qetaa.service.customer.filters;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.annotation.Priority;
import javax.ejb.EJB;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import qetaa.service.customer.dao.DAO;
import qetaa.service.customer.helpers.AppConstants;
import qetaa.service.customer.model.security.AccessMap;
import qetaa.service.customer.model.security.AccessToken;
import qetaa.service.customer.model.security.WebApp;

@Secured
@Provider
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationAgent implements ContainerRequestFilter {
 
	@EJB
	private DAO dao;

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		// GET http autherization header from the request
		String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
		// check if http authorization header is present and formatted correctly
		if (authHeader == null || !authHeader.startsWith("Bearer") || authHeader.startsWith("Bearer null")) {
			requestContext.abortWith(Response.status(401).build());
		} else {
			String[] values = authHeader.split("&&");
			String tokenPart = values[0];
			String token = tokenPart.substring("Bearer".length()).trim();
			String username = values[1].trim();
			String appSecret = values[2].trim();
			String type = values[3].trim();// C customer, U = user
			try {
				// Validate the token
				// validateToken(token, username, appSecret);
				matchToken(token, username, appSecret, type, authHeader);
			} catch (NotAuthorizedException e) {
				requestContext.abortWith(Response.status(401).build());
			}
		}
	}

	public void matchToken(String token, String username, String appSecret, String type, String authHeader)
			throws NotAuthorizedException {
		AccessMap map = new AccessMap(username, appSecret, token, "");
		String link = "";
		if (type.equals("C")) {
			boolean matched = matchToken(map);
			if (!matched) {
				throw new NotAuthorizedException("Request authorization failed");// customer not allowed to access this
																					// resource
			}
		} else if (type.equals("U")) {
			link = AppConstants.USER_MATCH_TOKEN;
			Response r = this.postSecuredRequest(link, map, authHeader);
			if (r.getStatus() != 200) {
				throw new NotAuthorizedException("Request authorization failed");
			}
		}
		else {
			throw new NotAuthorizedException("Request authorization failed");
		}
		
		
	}

	public boolean matchToken(AccessMap usermap) {
		try {
			WebApp webApp = getWebAppFromSecret(usermap.getAppSecret());
			List<AccessToken> l = dao.getFourConditionsAndDateBefore(AccessToken.class, "customerId", "webApp.appCode",
					"status", "token", "expire", Long.parseLong(usermap.getUsername()), webApp.getAppCode(), 'A',
					usermap.getCode(), new Date());
			if (!l.isEmpty()) {
				return true;
			} else {
				return false;// forbidden response
			}
		} catch (Exception e) {
			return false;// unauthorized
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

	public <T> Response postSecuredRequest(String link, T t, String authHeader) {
		Builder b = ClientBuilder.newClient().target(link).request();
		b.header(HttpHeaders.AUTHORIZATION, authHeader);
		Response r = b.post(Entity.entity(t, "application/json"));// not secured
		return r;
	}
}
