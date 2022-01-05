package suggestionSystem;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;

public class SimilarityPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private SQL_Handler sql_handler;
	private ArrayList<String> usernames;
	private ArrayList<Integer> uids;
	
	private JList<String> gamesList;
	
	public SimilarityPanel() {
		super();
		this.setLayout(null);
		this.sql_handler = new SQL_Handler();
		this.setUsernamesFromDatabase();
			
		JLabel lblNewLabel = new JLabel("Select a user and click to the recommend button.");
		lblNewLabel.setBounds(10, 11, 306, 14);
		this.add(lblNewLabel);
		
		String[] username_arr = new String[this.usernames.size()];
		JComboBox<String> username_cb = new JComboBox<String>(this.usernames.toArray(username_arr));
		username_cb.setBounds(10, 32, 101, 23);
		this.add(username_cb);
		username_cb.setVisible(true);
		
		JButton recommendButton = new JButton("Recommend");
		recommendButton.setBounds(120, 32, 131, 23);
		this.add(recommendButton);
		recommendButton.setVisible(true);
		
		gamesList = new JList<String>();
	    gamesList.setLayoutOrientation(JList.VERTICAL);
	    gamesList.setBounds(10, 70, 301, 200);
	    gamesList.setVisibleRowCount(10);
	    gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    this.add(gamesList);
	    gamesList.setVisible(true);
		
		recommendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int selectedUserIndex = username_cb.getSelectedIndex();
				recommendGame(selectedUserIndex);
				System.out.println("Started the recommendation."); // TODO: Create recommendation system.
			}
		});
			
		System.out.println("Similarity panel has been created!");
	}
	
	private void recommendGame(int selectedUserIndex) {
		// user map and inside user map game data map
		try {
			int user_id = this.uids.get(selectedUserIndex);
			GameData user_gd = new GameData(user_id);
			//user_gd.setCenteredRatings(this.sql_handler); // this doesn't work
			Map<Integer, GameData> corr_gd_map = new HashMap<Integer, GameData>(); // correlated game data map keys: uid, values: gd
			ResultSet correlatedGamesRs = this.sql_handler.retrieveData("select table1.uid as uid, table2.uid as t_uid, table1.title as title, table1.playtime as playtime, table2.playtime as t_playtime "
					+ "from usergamedata as table1, usergamedata as table2 "
					+ "where table1.uid == " + user_id + " and table2.uid != table1.uid and table1.title == table2.title;");
			
			// Getting the neccessarry data to calculate ratings.
			RatingAdjuster.AddGameHoursByUID(user_id); 
			//corr_gd_map.put(user_id, user_gd);
			GameData curr_gd = null;
			// Reading users data on correlated games.
			while(correlatedGamesRs.next()) {
				int target_uid = correlatedGamesRs.getInt("t_uid");
				String game_title = correlatedGamesRs.getString("title");
				float user_playtime = correlatedGamesRs.getFloat("playtime");
				float target_playtime = correlatedGamesRs.getFloat("t_playtime");
				
				// Adding other users' data.
				if(corr_gd_map.containsKey(target_uid)) {
					curr_gd = corr_gd_map.get(target_uid);
					curr_gd.addGameData(game_title, target_playtime);
				} else {
					curr_gd = new GameData(target_uid);
					corr_gd_map.put(target_uid, curr_gd);
					curr_gd.addGameData(game_title, target_playtime);
				}
				
				// Adding main user's data
				user_gd.addGameData(game_title, user_playtime);
			}
			
			System.out.println(user_gd.toString());
			System.out.println("Similarity of UID " + user_gd.getUserID() + ": " + user_gd.getSimilarityToUser(user_gd) + '\n');
			GameData max_similar_data = curr_gd;
			for(GameData gd : corr_gd_map.values()) {
				System.out.println(gd.toString());
				float similarity = gd.getSimilarityToUser(user_gd);
				if(max_similar_data != null && similarity > max_similar_data.getSimilarityToUser(user_gd)) {
					max_similar_data = gd;
				}
				System.out.println("Similarity of UID " + gd.getUserID() + ": " + similarity + '\n');
			}
			
			System.out.println("Max similar user is UID " + max_similar_data.getUserID() + ": " + max_similar_data.getSimilarityToUser(user_gd));
			
			// Now to predict the ratings for the selected user we need to get the union of all the games played by the users.
			ResultSet allPlayedGamesRs = this.sql_handler.retrieveData("select distinct title from usergamedata where title not in (select t2.title from usergamedata as t2 where t2.uid == " + user_id + ")");
			// Calculate predictions
			Map<String, Float> predictedRatings = new HashMap<String, Float>();
			while(allPlayedGamesRs.next()) {
				String gameTitle = allPlayedGamesRs.getString("title");
				float dividend = 0;
				float divisor = 0;
				int sim_counter = 0;
				for(GameData gd : corr_gd_map.values()) {
					float rating = gd.getPersonalData().getRating(gameTitle);
					if(rating > 0) {
						//System.out.println("User similarity: " + gd.getSimilarityToUser(user_gd) + " -- Rating: " + rating);
						dividend += gd.getSimilarityToUser(user_gd) * rating;
						divisor += gd.getSimilarityToUser(user_gd);
						sim_counter++;
					}
				}
				if(sim_counter > 1) {
					float predictedRating = (divisor == 0 ? 0 : (dividend / divisor));
					predictedRatings.put(gameTitle, predictedRating);
				}
			}
			// Show predictions
			ArrayList<Map.Entry<String, Float>> sorted_list = new ArrayList<>(predictedRatings.entrySet());
			sorted_list.sort(Map.Entry.comparingByValue());
			
			for(Map.Entry<String, Float> entry : sorted_list) {
				System.out.println("Rating to " + entry.getKey() + ":  " + entry.getValue());
			}
			
			int list_size = sorted_list.size();
			String[] list_arr = new String[list_size];
			for(int i = list_size - 1; i > list_size - 6; i--) {
				list_arr[list_size - 1 - i] = sorted_list.get(i).getKey();
			}
			this.gamesList.setListData(list_arr);
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setUsernamesFromDatabase() {
		this.usernames = new ArrayList<String>();
		this.uids = new ArrayList<Integer>();
		try {
			ResultSet rs = this.sql_handler.retrieveData("SELECT username, uid FROM users");
			
			while(rs.next()) {
				String username = rs.getString("Username");
				int uid = rs.getInt("uid");
				this.usernames.add(username);
				this.uids.add(uid);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println("Something went wrong while reading users from the database file.");
			e.printStackTrace();
		}
		
		if(this.usernames.isEmpty()) {
			this.usernames.add("No user");
			this.uids.add(0);
		}
		
	}
}
