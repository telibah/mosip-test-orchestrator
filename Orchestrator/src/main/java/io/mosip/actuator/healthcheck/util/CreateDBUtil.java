package io.mosip.actuator.healthcheck.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.mosip.orchestrator.testrunner.MosipTestRunner;

public class CreateDBUtil {
	private static final String INSERT_QUERY = "insert into healthStatusRig(TaskId,serviceName,endPoint,status) values(?,?,?,?)";
	static Properties properties =null;
	
	
		static {
		try {
			properties = CreateDBUtil.readPropertiesFile("healthStatus.properties");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	public static int insertDB(String serviceName, String endPoint, String status)
			throws ClassNotFoundException, SQLException {
		int count = 0;
		PreparedStatement preParedStatement = null;
		Class.forName(properties.getProperty("Driver"));
		Connection conn = DriverManager.getConnection(properties.getProperty("JDBC_URL"));
		if (conn != null) {
			preParedStatement = conn.prepareStatement(INSERT_QUERY);
			preParedStatement.setString(1, getMaxTaskId());
			preParedStatement.setString(2, serviceName);
			preParedStatement.setString(3, endPoint);
			preParedStatement.setString(4, status);
			count = preParedStatement.executeUpdate();
			System.out.println("No. of Record inserted :" + count);
		}
		if (conn != null)
			conn.close();
		if (preParedStatement != null)
			preParedStatement.close();
		return count;
	}

	private static String getMaxTaskId() throws ClassNotFoundException, SQLException {
		String taskId = "101";
		Class.forName(properties.getProperty("Driver"));
		Connection conn = DriverManager.getConnection(properties.getProperty("JDBC_URL"));
		ResultSet resultSet = conn.createStatement().executeQuery("select * from healthStatusRig");
		while (resultSet.next()) {
			System.out.println("");
			taskId = resultSet.getString("TASKID");
			if (!taskId.isEmpty()) {
				int id = Integer.valueOf(taskId);
				taskId = id + 1 + "";
			}
		}
		if (conn != null)
			conn.close();
		return taskId;

	}
	
	
	public static void insertBasedOnStatus(String status) throws SQLException {
		int count = 0;
		PreparedStatement preParedStatement = null;
		try {
			Class.forName(properties.getProperty("Driver"));
			Connection conn = DriverManager.getConnection(properties.getProperty("JDBC_URL"));
			if (conn != null) {
				
				  preParedStatement = conn.
				  prepareStatement("insert into healthStatus(moudleName,status) values(?,?)");
				  preParedStatement.setString(1, "HealthCheck-Module");
				  preParedStatement.setString(2, status); 
				  count = preParedStatement.executeUpdate();
				  System.out.println("No. of Record inserted :" + count);
				 
			}
			if (conn != null)
				conn.close();
			if (preParedStatement != null)
				preParedStatement.close();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
	
	
	public static void createDB(String tableName,String createQuery) {
		try {
			Class.forName(properties.getProperty("Driver"));
			Connection conn = DriverManager.getConnection(properties.getProperty("JDBC_URL"));
			if (conn != null) {
				System.out.println("Connected to DB testRig");
				DatabaseMetaData dbm = conn.getMetaData();
				ResultSet rs = dbm.getTables(null, "APP", tableName, null);
				if (rs.next()) {
					conn.createStatement().execute("drop table "+tableName);
					conn.createStatement().execute(
							createQuery);
				}else {
					conn.createStatement().execute(
							createQuery);
				}
			} else {
				System.out.println("Problem while connecting to DB !!!");
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

	}
	
	public static void insertBasedOnCertificateStatus(String expiryDate ,String status) throws SQLException {
		int count = 0;
		PreparedStatement preParedStatement = null;
		try {
			Class.forName(properties.getProperty("Driver"));
			Connection conn = DriverManager.getConnection(properties.getProperty("JDBC_URL"));
			if (conn != null) {
				
				  preParedStatement = conn.
				  prepareStatement("insert into Certificate(taskId,expiryDate,isExpiredStatus) values(?,?,?)");
				  preParedStatement.setString(1, "101");
				  preParedStatement.setString(2, expiryDate); 
				  preParedStatement.setString(3, status); 
				  count = preParedStatement.executeUpdate();
				  System.out.println("No. of Record inserted :" + count);
				 
			}
			if (conn != null)
				conn.close();
			if (preParedStatement != null)
				preParedStatement.close();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void insertBasedOnOTPStatus(String OTP) throws SQLException {
		int count = 0;
		PreparedStatement preParedStatement = null;
		try {
			Class.forName(properties.getProperty("Driver"));
			Connection conn = DriverManager.getConnection(properties.getProperty("JDBC_URL"));
			if (conn != null) {
				
				  preParedStatement = conn.
				  prepareStatement("insert into OTPfunctionality(taskId,OTP) values(?,?)");
				  preParedStatement.setString(1, "101");
				  preParedStatement.setString(2, OTP); 
				  count = preParedStatement.executeUpdate();
				  System.out.println("No. of Record inserted :" + count);
				 
			}
			if (conn != null)
				conn.close();
			if (preParedStatement != null)
				preParedStatement.close();
		} catch (ClassNotFoundException e) {
			
			e.printStackTrace();
		}
	}
	
	
	

	@SuppressWarnings("unchecked")
	public static String parseYML() throws FileNotFoundException, JsonProcessingException {
		InputStream inputStream = null;
		CreateDBUtil util= new CreateDBUtil();
		inputStream = new FileInputStream(MosipTestRunner.getGlobalResourcePath()+"/services.yaml");
		Yaml yaml = new Yaml();
		Map<String, Object> obj = (Map<String, Object>) yaml.load(inputStream);
		Object object = obj.get("spec");
		Map<String, Object> obj1 = (Map<String, Object>) object;

		ObjectMapper jsonWriter = new ObjectMapper();
		String jsonValue = jsonWriter.writeValueAsString(obj1.get("rules"));
		String substr = jsonValue.substring(19);
		return substr;
	}

	private  File getFileFromResources(String fileName) {
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(fileName);
		if (resource == null) {
			throw new IllegalArgumentException("file is not found!");
		} else {
			return new File(resource.getFile());
		}

	}
	
	public static Properties readPropertiesFile(String fileName) throws IOException {
		
	      FileInputStream fis = null;
	      Properties prop = null;
	      try {
	         fis = new FileInputStream(MosipTestRunner.getGlobalResourcePath()+"/"+fileName);
	         prop = new Properties();
	         prop.load(fis);
	      } catch(FileNotFoundException fnfe) {
	         fnfe.printStackTrace();
	      } catch(IOException ioe) {
	         ioe.printStackTrace();
	      } finally {
	         fis.close();
	      }
	      return prop;
	   }


}
