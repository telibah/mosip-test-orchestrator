package io.mosip.actuator.healthcheck.util;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import org.json.simple.JSONObject;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import com.sun.mail.imap.protocol.FLAGS;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class VerifiyOtp {
	static Properties properties =null;
	
	static {
		try {
			properties = CreateDBUtil.readPropertiesFile("healthStatus.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static ReadContext sendOTP() throws IOException {
		JSONObject api_input = requestBuilder();
		Response api_response = (Response) given().contentType(ContentType.JSON).body(api_input).post(properties.getProperty("OTPurl"));
		ReadContext ctx = JsonPath.parse(api_response.getBody().asString());
		return ctx;
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject requestBuilder() throws IOException {
		JSONObject request_json = new JSONObject();
		request_json.put("userId", properties.getProperty("EMAIL_USER"));
		JSONObject api_input = new JSONObject();
		api_input.put("id", "mosip.pre-registration.login.sendotp");
		api_input.put("version", "1.0");
		api_input.put("requesttime", getCurrentDateAndTimeForAPI());
		api_input.put("request", request_json);
		return api_input;
	}

	@SuppressWarnings("serial")
	public static String checkForOTP() {
		String otp = "";
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		ArrayList<String> subjects = new ArrayList<String>() {
			{
				add("Otp message");
			}
		};
		String regex = "otp\\s([0-9]{6})";
		MailHelperResponse mailHelperResponse = readviaRegex(subjects, regex, properties.getProperty("EMAIL_USER"), 10);
		if (mailHelperResponse != null) {
			otp = mailHelperResponse.getRegexout();
		}
		return otp;
	}

	public static MailHelperResponse readviaRegex(ArrayList<String> subjects, String regex, String recipient,
			int maxMessageCount) {
		Message[] messages = null;
		Folder emailInbox;

		Properties sysProps = System.getProperties();
		sysProps.setProperty("mail.imap.ssl.enable", "true");
		sysProps.setProperty("mail.store.protocol", "imaps");
		try {
			Session session = Session.getInstance(sysProps, null);
			Store store = session.getStore();
			store.connect("outlook.office365.com", 993, properties.getProperty("EMAIL_USER"), properties.getProperty("EMAIL_PWD"));
			Thread.sleep(1000);
			emailInbox = store.getFolder("Inbox");
			emailInbox.open(Folder.READ_WRITE);

			messages = emailInbox.getMessages();
			for (int i = messages.length - 1; i > messages.length - maxMessageCount && i > 0; i--) {
				Message message = messages[i];
				try {
					if (message.getSubject() != null) {
						for (String sub : subjects) {
							if (message.getSubject().contains(sub)) {
								Address[] recipients = message.getRecipients(Message.RecipientType.TO);
								for (Address address : recipients) {
									if (recipient.equals(address.toString())) {
										String msg = getTextFromMessage(message);
										String regexout = regex(regex, msg);
										message.setFlag(FLAGS.Flag.SEEN, true);
										message.setFlag(FLAGS.Flag.DELETED, true);
										return new MailHelperResponse(msg, regexout);
									}
								}
							}
						}
					}
				} catch (MessagingException me) {
					// to do
				}
			}
			emailInbox.close(true);
			store.close();
		} catch (Exception mex) {
			mex.printStackTrace();
		}
		return null;
	}

	private static String getTextFromMessage(Message message) throws MessagingException, IOException {
		String result = "";
		if (message.isMimeType("text/plain")) {
			result = message.getContent().toString();
		} else if (message.isMimeType("multipart/*")) {
			MimeMultipart mimeMultipart = (MimeMultipart) message.getContent();
			result = getTextFromMimeMultipart(mimeMultipart);
		}
		return result;
	}

	private static String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
		String result = "";
		int count = mimeMultipart.getCount();
		for (int i = 0; i < count; i++) {
			BodyPart bodyPart = mimeMultipart.getBodyPart(i);
			if (bodyPart.isMimeType("text/plain")) {
				result = result + "\n" + bodyPart.getContent();
				break;
			} else if (bodyPart.getContent() instanceof MimeMultipart) {
				result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent());
			}
		}
		return result;
	}

	public static String regex(String exp, String str) {
		String parsed = new String();
		Pattern pattern = Pattern.compile(exp);
		Matcher m = pattern.matcher(str);
		while (m.find()) {
			if (m.group().length() > 0) {
				parsed = m.group(1);
			}
		}
		return parsed;
	}

	private static String getCurrentDateAndTimeForAPI() {
		return ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ISO_INSTANT);
	}
}
