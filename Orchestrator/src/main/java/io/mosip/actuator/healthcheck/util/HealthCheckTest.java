package io.mosip.actuator.healthcheck.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
//import org.codehaus.jackson.JsonParseException;
//import org.codehaus.jackson.map.JsonMappingException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

/**
 * This class is the entry point for HealthCheck
 *
 */
@SuppressWarnings("unused")
public class HealthCheckTest {
	static boolean healthStatus = false;
	static Properties properties = null;
	QueryDBClient dbClient=new QueryDBClient();
	
/*	public static void main(String[] args) {
		try {
			healthCheckStatus();
		} catch (JsonParseException | JsonMappingException | SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	
	public static Map<String,String> healthCheckStatus() throws JsonParseException, JsonMappingException, IOException, SQLException {
		Map<String,String> serviceMap=new HashMap<>();
		String pathValue = "";
		String backendValue = "";
		System.out.println("1.------Services Health check --------------");
		properties = CreateDBUtil.readPropertiesFile("healthStatus.properties");
		CreateDBUtil.createDB(properties.getProperty("createHealhtStatusTable"),
				properties.getProperty("CREATE_TABLE_HEALTH_STATUS_RIG"));

		String substr = CreateDBUtil.parseYML();
		String[] stringArray = substr.split(",");
		
		
		String servicePortValue = "";
		for (int i = 0; i < stringArray.length - 3; i++) {
			String path = stringArray[i];
			pathValue = path.substring(9, path.length() - 1);

			String backend = stringArray[++i];
			backendValue = backend.substring(26, backend.length() - 1);

			String servicePort = stringArray[++i];
			servicePortValue = servicePort.substring(14, servicePort.length() - 2);

			String url = properties.getProperty("hostName") + pathValue + properties.getProperty("actuator");
			System.out.println(url);
			Response api_response = null;
			if (url.contains("print-stage/actuator/health"))
				continue;
			api_response = (Response) given().contentType(ContentType.JSON).get(url);
			ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
			String status = null;
			try {
				status = ctx.read("$['status']");
			} catch (Exception exception) {
			}

			if (status != null && !status.isEmpty() && status.equalsIgnoreCase("UP")) {
				try {
					CreateDBUtil.insertDB(backendValue, url, status);
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}

			} else {
				try {
					CreateDBUtil.insertDB(backendValue, url, "Down");
					healthStatus = true;
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
			}
		}

		checkStatusUP_OR_Down();

		System.out.println("2.---------Certificate Check----------");
		try {
			CertificatesExpirey.checkCertificatesExpirey();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		System.out.println("3.---------OTP Check----------");
		OTPFunctionality.checkOTPFunctionality();
		
		serviceMap=QueryDBClient.queryDB();
		return serviceMap;
	}


	private static void checkStatusUP_OR_Down() throws SQLException {
		if (healthStatus) {
			CreateDBUtil.createDB(properties.getProperty("healthStatusTable"),
					properties.getProperty("createQueryForHealthStatus"));
			CreateDBUtil.insertBasedOnStatus("Down");
		} else {
			CreateDBUtil.createDB(properties.getProperty("healthStatusTable"),
					properties.getProperty("createQueryForHealthStatus"));
			CreateDBUtil.insertBasedOnStatus("UP");
		}
	}

}
