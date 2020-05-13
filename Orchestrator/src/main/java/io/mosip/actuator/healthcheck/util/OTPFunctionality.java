package io.mosip.actuator.healthcheck.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;

import com.jayway.jsonpath.ReadContext;

public class OTPFunctionality {
	public static Boolean checkOTPFunctionality() throws SQLException, IOException {
		Properties properties = CreateDBUtil.readPropertiesFile("healthStatus.properties");
		boolean isOtpSend = true;
		ReadContext ctx = VerifiyOtp.sendOTP();
		if (ctx.read("$['response']") == null) {
			System.out.println("Failed to send otp ");
			isOtpSend = false;
		} else {
			System.out.println("OTP send Successfully");
			int counter = 0;
			int repeats = 10;
			String otp = "";
			while (counter < repeats) {
				System.out.println("Checking the User email (" + properties.getProperty("EMAIL_USER") + ") for OTP");
				otp = VerifiyOtp.checkForOTP();
				CreateDBUtil.createDB(properties.getProperty("createOTPTable"),
						properties.getProperty("createQueryForOTPCheck"));
				CreateDBUtil.insertBasedOnOTPStatus(otp);
				if (otp != null && !otp.isEmpty()) {
					System.out.println("OTP retrieved: " + otp);
					break;
				} else {
					System.out.println("OTP not found");
				}
				counter++;
			}
			if (!otp.isEmpty()) {
				System.out.println("OTP functionality is working fine..Go to next Stage");
			} else {
				System.out.println("failed to read OTP even after " + repeats + " retries");
			}
		}
		return isOtpSend;
	}
}
