package suggestionSystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class RatingAdjuster {
	private static Map<String, Float> avgtime_map = new HashMap<String, Float>();
	private static Map<String, Float> rushedtime_map = new HashMap<String, Float>();
	public static SQL_Handler sql_h = new SQL_Handler();
	private static float weight_rushed = 0.61f;
	private static float weight_average = 0.39f;
	
	public static void AddGameHoursByUID(int uid) {
		String query = "select table1.title as title, t3.\"Average Play Time\", t3.\"Rushed Play Time\" "
				+ "from usergamedata as table1, usergamedata as table2, gamedataset as t3 "
				+ "where table1.uid == " + uid + " and table2.uid != table1.uid and table1.title == table2.title and t3.Title == table1.title group by table1.title;";
		
		try {
			ResultSet rs = sql_h.retrieveData(query);
			while(rs.next()) {
				String title = rs.getString("title");
				float avgtime = Float.parseFloat(rs.getString("Average Play Time"));
				float rushedtime = Float.parseFloat(rs.getString("Rushed Play Time"));
				avgtime_map.put(title, avgtime);
				rushedtime_map.put(title, rushedtime);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static float getRating(String gameTitle, float timePlayed) {
		float avgtime = getAvgTime(gameTitle);
		float rushedtime = getRushedTime(gameTitle);
		float rating = timePlayed / (rushedtime * weight_rushed + avgtime * weight_average) ; 

		return (float) (rating > 2.5 ? 2.5 : rating);
	}
	
	public static float getAvgTime(String gameTitle) {
		return avgtime_map.containsKey(gameTitle) ? avgtime_map.get(gameTitle) : 0;
	}
	
	public static float getRushedTime(String gameTitle) {
		return rushedtime_map.containsKey(gameTitle) ? rushedtime_map.get(gameTitle) : 0;
	}
}
