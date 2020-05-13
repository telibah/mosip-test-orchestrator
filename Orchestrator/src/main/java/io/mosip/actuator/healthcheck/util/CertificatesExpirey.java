package io.mosip.actuator.healthcheck.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Properties;

public class CertificatesExpirey {

	public static boolean checkCertificatesExpirey() throws ParseException, IOException {
		Properties properties = CreateDBUtil.readPropertiesFile("healthStatus.properties");
		boolean status = false;
		try (Connection con = DriverManager.getConnection(properties.getProperty("kernelUrl"), properties.getProperty("kernel_user"), properties.getProperty("kernel_password"));
				Statement st = con.createStatement();
				ResultSet rs = st.executeQuery(properties.getProperty("selectQuery"))) {
			if (rs.next()) {
				String dbDate = rs.getString(1);
				status = formatAndCompareDate(dbDate, status);
				CreateDBUtil.createDB(properties.getProperty("createCertificateTable"),
						properties.getProperty("createQueryForCertificate"));
				CreateDBUtil.insertBasedOnCertificateStatus(dbDate, String.valueOf(status));
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}

		return status;
	}
	
	private static boolean formatAndCompareDate(String dbDate, boolean status) throws ParseException {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime now = LocalDateTime.now();
		String crrent_date = dtf.format(now);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date db_date = sdf.parse(dbDate);
		Date curr_date = sdf.parse(crrent_date);
		if (db_date.compareTo(curr_date) > 0) {
			status = false;
			System.out.println("Next Certificate Expiry Date : " + dbDate);
		} else if (db_date.compareTo(curr_date) < 0) {
			status = true;
			System.out.println("Certificate has been Expired : " + dbDate);
		} else if (db_date.compareTo(curr_date) == 0) {
			status = false;
			System.out.println("Certificate will Expiry Today : " + dbDate);
		}
		return status;
	}
	
}
