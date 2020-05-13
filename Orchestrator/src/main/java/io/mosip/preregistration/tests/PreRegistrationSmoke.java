package io.mosip.preregistration.tests;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import io.mosip.orchestrator.dao.DbUtil;
import io.mosip.preregistration.util.PreRegistartionUtil;
import io.mosip.preregistration.util.TestRigException;
import io.restassured.response.Response;

public class PreRegistrationSmoke {
	String preID ="";
	JSONObject adultRequest=null;
	Response createReponse=null;
	Response documentUploadResponse=null;
	Response fetchCenterResponse=null;
	Response bookingAppointmentResponse=null;
	PreRegistartionUtil preRegistrationUtil=new PreRegistartionUtil();
	String token="";
	String moduleName="preReg";
	Map<String,Map<String,String>> mapOfStatus=new LinkedHashMap<String,Map<String,String>>();
	Map<String,String> mapOfAssert=new LinkedHashMap<String,String>();
	@Test(priority=1,groups="preReg")
	public void generateOtp() {
		try {
		token=preRegistrationUtil.getToken();
		Assert.assertTrue(token.equals("")==false);
		mapOfAssert.put("generateOtp", "PASS");
		} catch(Exception e) {
			mapOfAssert.put("generateOtp", "Fail");
			Assert.fail();
		}
	}
	@Test(priority=2,groups="preReg")
	public void createApplication() {
		try {
		adultRequest=preRegistrationUtil.getRequest("adultRequest");
		createReponse = preRegistrationUtil.createApplication(adultRequest, token);
		preID = createReponse.jsonPath().get("response.preRegistrationId").toString();
		Assert.assertTrue(preID.equals("")==false);
		mapOfAssert.put("createApplication", "PASS");
		}catch (Exception e) {
			mapOfAssert.put("createApplication", "FAIL");
			Assert.fail();
		}
	}
	
	@Test(priority=3,groups="preReg")
	public void documentUpload() {
		try {
		List<String> documents=new ArrayList<>();
		documents.add("Rental contract");
		documents.add("CNIE card");
		for(String document:documents)
		{
			JSONObject documentRequest = preRegistrationUtil.getRequest("documents/"+document);
			documentUploadResponse=preRegistrationUtil.documentUpload(createReponse, documentRequest, token,document);
		}
		String assertResponse=documentUploadResponse.jsonPath().get("response").toString();
		Assert.assertTrue(assertResponse.equals("null")==false);
		mapOfAssert.put("documentUpload", "PASS");
	}catch (Exception e) {
		mapOfAssert.put("documentUpload", "FAIL");
		Assert.fail();
	}
	}		
	
	@Test(priority=4,groups="preReg")
	public void fetchCenter() {
		try {
		 fetchCenterResponse = preRegistrationUtil.FetchCentre(token);
		 List<String> appointmentDetails=preRegistrationUtil.getAppointmentDetails(fetchCenterResponse);
		 Assert.assertTrue(appointmentDetails.size()!=0);
		 mapOfAssert.put("fetchCenter", "PASS");
		}catch (Exception e) {
			mapOfAssert.put("fetchCenter", "FAIL");
			Assert.fail();
		}
	}
	
	@Test(priority=5,groups="preReg")
	public void bookAppointment() {
		try {
		bookingAppointmentResponse=preRegistrationUtil.BookAppointment(fetchCenterResponse, preID, token);
		String bookingResponse=bookingAppointmentResponse.jsonPath().get("response.bookingMessage").toString();
		Assert.assertTrue(bookingResponse.equals("Appointment booked successfully"));
		mapOfAssert.put("bookAppointment", "PASS");
		}catch (Exception e) {
			mapOfAssert.put("bookAppointment", "FAIL");
			Assert.fail();
		}
	}
	
	@AfterClass
	public void insertIntoDb() throws TestRigException, SQLException {
		mapOfStatus.put(moduleName, mapOfAssert);
		DbUtil dbUtil=new DbUtil();
		dbUtil.insertIntoDb(mapOfStatus, moduleName);
	}
}
