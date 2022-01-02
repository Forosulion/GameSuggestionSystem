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
	private GameData personalData;
	private float avg_rating = -1;
	public float similarity = -1;
	
	public GameData(int userID) {
		this.userID = userID;
		this.timePlayedMap = new HashMap<String, Float>();
		this.rating_map = new HashMap<String, Float>();
		this.personalData = null;
	}
	
	public void addGameData(String gameTitle, float hours) {
		this.timePlayedMap.put(gameTitle, hours);
		this.rating_map.put(gameTitle, RatingAdjuster.getRating(gameTitle, hours));
	}
	
	public float getHoursPlayed(String gameTitle) {
		if(this.timePlayedMap.containsKey(gameTitle)) {
			return this.timePlayedMap.get(gameTitle);
		}
		return 0; 
	}
	
	public float getRating(String gameTitle) {
		return this.rating_map.containsKey(gameTitle) ? this.rating_map.get(gameTitle) : 0f;
	}
	
	public int getUserID() {
		return this.userID;
	}
	
	// This method should be called after the timePlayedMap field is filled to get true results.
	// Basically, we convert playedtime data to centered (by subtracting average rating) ratings data.
	public float getAverageRating() {
		float avg = 0;
		for(float rating : this.rating_map.values()) {
			avg += rating;
		}
		
		return avg / this.rating_map.size();
	}
	
	public float getSimilarityToUser(GameData user_gd) {
		if(this.similarity != -1) {
			return this.similarity;
		}
		float user_avg = user_gd.getAverageRating();
		float this_avg = this.getAvgFromAllData();
		
		float dividend = 0;
		float divisor_user = 0;
		float divisor_this = 0;
		
		for(Map.Entry<String, Float> entry : this.rating_map.entrySet()) {
			float user_r = user_gd.getRating(entry.getKey());
			float this_r = entry.getValue();
			float diff_user = user_r - user_avg + 0.001f;
			float diff_this = this_r - this_avg + 0.001f;
			dividend += diff_user * diff_this;
			divisor_user += diff_user * diff_user;
			divisor_this += diff_this * diff_this;
		}
		this.similarity = (float) (dividend / Math.sqrt(divisor_user * divisor_this)) + 1f;
		this.personalData.similarity = this.similarity;
		return this.similarity;
	}
	
	public float getAvgFromAllData() {
		if(this.avg_rating != -1) {
			return this.avg_rating;
		}
		GameData personal_gd = new GameData(this.userID);
		this.personalData = personal_gd;
		RatingAdjuster.AddGameHoursByUID(this.userID);
		String query = "select title, playtime from usergamedata where uid == " + this.userID;
		ResultSet rs;
		try {
			rs = RatingAdjuster.sql_h.retrieveData(query);
			while(rs.next()) {
				String game_title = rs.getString("title");
				float user_playtime = rs.getFloat("playtime");
				personal_gd.addGameData(game_title, user_playtime);
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.avg_rating = personal_gd.getAverageRating();
		personal_gd.avg_rating = this.avg_rating;
		return this.avg_rating;
	}

	public GameData getPersonalData() {
		return this.personalData;
	}
	
	/*
	public void setAllRatings(SQL_Handler sql_h) {
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
	}*/
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Game data of UID ");
		sb.append(this.userID).append(": \n");
		for(Map.Entry<String, Float> entry : this.timePlayedMap.entrySet()) {
			sb.append("Title: ").append(entry.getKey()).append(" -- Hours: ").append(entry.getValue()).append(" -- Rating: ").append(this.rating_map.get(entry.getKey())).append('\n');
		}
		return sb.toString();
	}
}
