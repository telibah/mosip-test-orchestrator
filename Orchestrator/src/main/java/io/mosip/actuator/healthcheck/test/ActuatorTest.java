package io.mosip.actuator.healthcheck.test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.internal.BaseTestMethod;
import org.testng.internal.TestResult;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.mosip.actuator.healthcheck.util.HealthCheckTest;

public class ActuatorTest {
	protected static String testCaseName = "";
	@BeforeMethod(alwaysRun=true)
	public void getTestCaseName(Method method, Object[] testdata, ITestContext ctx) {
		System.out.println(method);
		testCaseName =  (String) testdata[0];
	}
	@DataProvider(name="serviceMap")
	public Object[][] getStatus() throws JsonParseException, JsonMappingException, IOException, SQLException {
		
		Map<String,String> serviceMap= new HashMap<String, String>();
		Map<String,Map<String,String>> moduleMap= new HashMap<String,Map<String, String>>();
		serviceMap=HealthCheckTest.healthCheckStatus();
		List<String> listOfModules=new ArrayList<String>();
		listOfModules.add("kernel");
		listOfModules.add("pre-registration");
		listOfModules.add("registration-processor");
		listOfModules.add("authentication");
		listOfModules.add("admin");
		listOfModules.add("id-repository");
		for(String module:listOfModules) {
			Map<String,String> tempMap= new HashMap<String, String>();
			for(Map.Entry<String, String> entry:serviceMap.entrySet()) {
				if(entry.getKey().contains(module)) {
					
					tempMap.put(entry.getKey(), entry.getValue());
					moduleMap.put(module, tempMap);
				}
			}
			
		}
		Object[][] array = new Object[moduleMap.size()][2];
		int count = 0;
		for(Entry<String, Map<String, String>> entry : moduleMap.entrySet()){
		    array[count][0] = entry.getKey();
		    array[count][1] = entry.getValue();
		    count++;
		}
		return array;
	}
	
	@Test(dataProvider="serviceMap",groups="actuatorTest")
	public void actuatorHealthTest(Object module,Object status) {
		boolean assertstatus=true; 
		Map<String,String> statusMap=(Map<String, String>) status;
		for(Map.Entry<String, String> entry:statusMap.entrySet()) {
			if(entry.getValue().equals("Down"))
			{
				assertstatus=false;
				break;
			}
		}
		Assert.assertTrue(assertstatus);
	}
	
	@AfterMethod(alwaysRun = true)
	public void setResultTestName(ITestResult result) {

		Field method;
		try {
			method = TestResult.class.getDeclaredField("m_method");
			method.setAccessible(true);
			method.set(result, result.getMethod().clone());
			BaseTestMethod baseTestMethod = (BaseTestMethod) result.getMethod();
			Field f = baseTestMethod.getClass().getSuperclass().getDeclaredField("m_methodName");
			f.setAccessible(true);
			f.set(baseTestMethod, ActuatorTest.testCaseName);
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			e.printStackTrace();
		}
		
	}
}
