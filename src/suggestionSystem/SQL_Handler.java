package suggestionSystem;


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.sqlite.JDBC;

public class SQL_Handler {
	public ResultSet table;
	private Connection connect(String path) {
        // SQLite connection string
        Connection conn = null;
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
	
	public void getTable(String path){
        String query = "SELECT * FROM gamedataset;";
        
        try (Connection conn = this.connect(path);
        	Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(query)){
            table = rs;
            // loop through the result set
            while (rs.next()) {
                System.out.println(
                                   rs.getString("Title") + "\t"
                                   );
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
	
	public ResultSet retrieveData(String query) throws SQLException{
	      //Registering the Driver
	      //DriverManager.registerDriver(new com.mysql.jdbc.Driver());
	      //Getting the connection
	      String mysqlUrl = "jdbc:sqlite:" + "data.sqlite";
	      Connection con = DriverManager.getConnection(mysqlUrl);
	      //System.out.println("Connection established......");
	      //Creating a Statement object
	      Statement stmt = con.createStatement();
	      //Retrieving the data
	      ResultSet rs = stmt.executeQuery(query);
	      return rs;
	   }
	
	
}
