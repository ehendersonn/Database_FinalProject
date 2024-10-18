/*
 * Example code to connect to a SQLite database and print its tables.
 * Requires a driver file (sqlite-jdbc-x.xx.xx.xx)
 * Connection string specifies the location of the SQL db file.
 * 
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class MainClass {

	public static void main(String[] args) {
		String dbURL = "jdbc:sqlite:../univ.db";

		System.out.println("Trying to connect...");		
		try (Connection conn = DriverManager.getConnection(dbURL);
			 Statement stmt = conn.createStatement();
			)
		{
			System.out.println("Executing query...");		
			ResultSet rset = 
				stmt.executeQuery("Select name from sqlite_schema where type = 'table'");
			while (rset.next()) {
				System.out.println("table: " + rset.getString("name"));
			}
		} catch (SQLException e) {
			System.err.println(e);
		}
		System.out.println("Done!");		

	}

}
