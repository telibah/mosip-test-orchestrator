package io.mosip.orchestrator.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import io.mosip.preregistration.util.TestRigException;





public class DbUtil {
	private static final String CREATE_TABLE_HEALTH_STATUS_RIG = "create table happyFlowRig(ServiceId varchar(3),ModuleName varchar(100),ProcessName varchar(200), status varchar(20))";
	private static final String DROP_QUERY="Drop Table happyFlowRig";
	private static final String INSERT_QUERY = "insert into happyFlowRig(ServiceId,ModuleName,ProcessName,status) values(?,?,?,?)";
	private static final String Driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private static final String DB_URL = "jdbc:derby:testRig;create=true";
	Connection connection;
	public void connectDatabase() throws TestRigException {
		try {
			connection = DriverManager.getConnection(DB_URL);
			DatabaseMetaData databaseMetaData = connection.getMetaData();
			 ResultSet rs = databaseMetaData.getTables(null, "APP", "HAPPYFLOWRIG", null);
			 Statement statement=connection.createStatement();
			 if(rs.next()) {
				 statement.executeUpdate(DROP_QUERY);
			 }
			statement.executeUpdate(CREATE_TABLE_HEALTH_STATUS_RIG);
			statement.close();
			
		} catch (SQLException e) {
			throw new TestRigException("Could Not Load The Derby DB"+e.getCause());
		}
	}
	
	public void insertIntoDatabase(int id,String moduleName,String ProcessName,String status) throws TestRigException, SQLException {
		PreparedStatement preParedStatement = null;
		try {
			
			Class.forName(Driver);
			int count = 0;
			if (connection != null) {
				preParedStatement = connection.prepareStatement(INSERT_QUERY);
				preParedStatement.setLong(1, id);
				preParedStatement.setString(2, moduleName);
				preParedStatement.setString(3, ProcessName);
				preParedStatement.setString(4, status);
				count = preParedStatement.executeUpdate();
			}
			System.out.println();
			
		} catch (SQLException | ClassNotFoundException e) {
			throw new TestRigException("Could Not Load The Derby Statement -->" + e.getCause());
		} finally {
			preParedStatement.close();
		
		}
	}
	
	public void viewRecords() throws SQLException {
		 Statement stmt = connection.createStatement();
		 ResultSet rs=stmt.executeQuery("Select * from happyFlowRig");
		 ResultSetMetaData rsmd = rs.getMetaData();
		 int columnsNumber = rsmd.getColumnCount();  
		 for (int x = 1; x <= columnsNumber; x++) {
				System.out.format("%10s", rsmd.getColumnName(x)+"|| ");
				
			}
			System.out.println();
			System.out.print("-------------------------------------------------------");
			while(rs.next()) {
				System.out.println("");
				for (int x = 1; x <= columnsNumber; x++) {
					System.out.format("%10s", rs.getString(x)+" | ");
					
				}
				
			}
			if(stmt!=null)stmt.close();
			if(connection!=null)connection.close();
		}
	
/*	public static void main(String[] args) throws TestRigException, SQLException {
		DbUtil dbUtil=new DbUtil();
		dbUtil.connectDatabase();
		dbUtil.insertIntoDatabase(1, "prereg", "create pre-ID", "PASS");
		dbUtil.viewRecords();
	
	}*/
	public void insertIntoDb(Map<String,Map<String,String>> mapOfStatus,String moduleName) throws TestRigException, SQLException {
		DbUtil dbUtil=new DbUtil();
		dbUtil.connectDatabase();
		int ServiceId=1;
		Map<String,String> statusOfService=mapOfStatus.get(moduleName);
		for(Map.Entry<String,String> entry:statusOfService.entrySet()) {
			dbUtil.insertIntoDatabase(ServiceId, moduleName, entry.getKey(), entry.getValue());
			ServiceId++;
		}
		dbUtil.viewRecords();
	}
}
