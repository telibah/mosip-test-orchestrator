package io.mosip.actuator.healthcheck.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used to check the DB tables , it act as client
 * @@changed the corresponding SELECT statement to fetch the record from DB.
 *
 */
public class QueryDBClient {
	public static final	String  SELECT1="select * from healthStatusRig";
	public static final	String  SELECT2="select * from healthStatus";
	public static final	String JDBC_URL="jdbc:derby:testRig";
	
	public static Map<String, String> queryDB() throws SQLException{
		Connection connection=DriverManager.getConnection(JDBC_URL);
		Statement statement=connection.createStatement();
		ResultSet resultSet=statement.executeQuery(SELECT1);
		ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
		int columCount=resultSetMetaData.getColumnCount();
		Map<String,String> statusMap=new HashMap<String,String>();
		for (int x = 1; x <= columCount; x++) {
			System.out.format("%10s", resultSetMetaData.getColumnName(x)+"|| ");
			
		}
		System.out.println();
		System.out.print("-------------------------------------------------------");
		while(resultSet.next()) {
			System.out.println("");
			for (int x = 1; x <= columCount; x++) {
				System.out.format("%10s", resultSet.getString(x)+" | ");
				statusMap.put(resultSet.getString(2), resultSet.getString(4));
			}
			
		}
		if(statement!=null)statement.close();
		if(connection!=null)connection.close();
		return statusMap;
	}
}
