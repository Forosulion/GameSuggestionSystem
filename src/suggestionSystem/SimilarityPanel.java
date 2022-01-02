package suggestionSystem;

import javax.swing.JPanel;
import javax.swing.JTextField;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

public class SimilarityPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private SQL_Handler sql_handler;
	private ArrayList<String> usernames;
	private ArrayList<Integer> uids;
	
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
			
			//corr_gd_map.put(user_id, user_gd);
			GameData curr_gd = null;
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
			
			for(GameData gd : corr_gd_map.values()) {
				System.out.println(gd.toString());
			}
			
			// GameData gd = new GameData(user_id);
			// gd.setCenteredRatings(sql_handler);
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
