package suggestionSystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GameData {
	/**
	 * Created seperately for each user.
	 */
	private int userID;
	private Map<String, Float> timePlayedMap;
	private Map<String, Float> rating_map;
	
	public GameData(int userID) {
		this.userID = userID;
		this.timePlayedMap = new HashMap<String, Float>();
		this.rating_map = new HashMap<String, Float>();
	}
	
	public void addGameData(String gameTitle, float hours) {
		this.timePlayedMap.put(gameTitle, hours);
	}
	
	public float getHoursPlayed(String gameTitle) {
		if(this.timePlayedMap.containsKey(gameTitle)) {
			return this.timePlayedMap.get(gameTitle);
		}
		return 0; 
	}

	public void setCenteredRatings(SQL_Handler sql_h) {
		// query database to get this data 
		// recommendation algorithm playtime / ((rushed + avg.)/2) and shouldn't be bigger than 2.5
		// TODO: Center those by differing average rating
		
		try {
			ResultSet rs = sql_h.retrieveData("select t2.title as Title, playtime as \"User Playtime\", \"Average Play Time\", \"Rushed Play Time\" "
					+ "from gamedataset as t1, usergamedata as t2 where t2.uid = " + this.userID + " and t1.Title == t2.title;");
			
			while(rs.next()) {
				String title = rs.getString("Title");
				float playtime = rs.getFloat("User Playtime");
				float avgtime = Float.parseFloat(rs.getString("Average Play Time"));
				float rushedtime = Float.parseFloat(rs.getString("Rushed Play Time"));
				float rating = playtime / ((rushedtime + avgtime)/2); 
				rating = (float) (rating > 2.5 ? 2.5 : rating);
				rating_map.put(title, rating);
				System.out.println("Title: " + title + " -- " + rating);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Game data of UID ");
		sb.append(this.userID).append(": \n");
		for(Map.Entry<String, Float> entry : this.timePlayedMap.entrySet()) {
			sb.append("Title: ").append(entry.getKey()).append(" -- ").append(entry.getValue()).append('\n');
		}
		return sb.toString();
	}
}
