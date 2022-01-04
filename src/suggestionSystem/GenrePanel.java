package suggestionSystem;

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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import suggestionSystem.SQL_Handler;

public class GenrePanel extends JPanel{
	private JComboBox<String> genre_cb;
	private JComboBox<String> cameraPerspective_cb;
	private JComboBox<String> passageoftime_cb;
	private SQL_Handler sql;
	private SQL_Handler sql2;
	private JButton selectButton;
	private JButton suggestButton;
	private JList<String> gamesList;
	private JList<String> suggestionsList;
	
	ResultSet rs;
	ResultSet source;
	private ArrayList<String> gamesStringList;
	private ArrayList<String> suggestionStringList;
	private static Map<String, Float> similarity_map = new HashMap<String, Float>();
	
	public GenrePanel() {
		super();
		sql = new SQL_Handler();
		sql2 = new SQL_Handler();
		gamesStringList = new ArrayList<String>();
		suggestionStringList = new ArrayList<String>();
		
		JLabel lblNewLabel = new JLabel("Games that similiar to your choice");
		lblNewLabel.setBounds(500, 50, 306, 14);
		this.add(lblNewLabel);
		
		
		String[] genreChoices = { "Puzzle", "Race", "Simulation", "Shooter", "Role-playing", "Action-RPG" , "Survival", "Action" , "Action-Adventure" , "Adventure", "Strategy" , "Platformer" };
	    genre_cb = new JComboBox<String>(genreChoices);
	    genre_cb.setBounds(250, 52, 101, 23);
	    this.add(genre_cb);
	    genre_cb.setVisible(true);
	    
	    String[] cameraPerspectiveChoices = { "Top Down","Third Person","Free Camera","Isometric","First Person","Side Scroller","Follow Camera","Shoulder" };
	    cameraPerspective_cb = new JComboBox<String>(cameraPerspectiveChoices);
	    cameraPerspective_cb.setBounds(40, 52, 101, 23);
	    this.add(cameraPerspective_cb);
	    cameraPerspective_cb.setVisible(true);
	    
	    String[] passageOfTimeChoices = { "Real Time","Turn Based" };
	    passageoftime_cb = new JComboBox<String>(passageOfTimeChoices);
	    passageoftime_cb.setBounds(145, 52, 101, 23);
	    this.add(passageoftime_cb);
	    passageoftime_cb.setVisible(true);
	    
	    suggestionsList = new JList<String>();
	    suggestionsList.setLayoutOrientation(JList.VERTICAL);
	    suggestionsList.setBounds(500, 80, 200, 200);
	    suggestionsList.setVisibleRowCount(10);
	    suggestionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    this.add(suggestionsList);
	    suggestionsList.setVisible(true);
	    
	    gamesList = new JList<String>();
	    gamesList.setLayoutOrientation(JList.VERTICAL);
	    gamesList.setBounds(70, 80, 200, 200);
	    gamesList.setVisibleRowCount(10);
	    gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    this.add(gamesList);
	    gamesList.setVisible(true);
	    
	    
		selectButton = new JButton("Filter Games");
		selectButton.setBounds(360, 52, 120, 23);
		this.add(selectButton);
		
		selectButton.addActionListener(new ActionListener() {
			@Override
			
			public void actionPerformed(ActionEvent e) {
				String tmp;
				try {
					gamesStringList.clear();
					String selectedGenre = genre_cb.getSelectedItem().toString();
					String selectedCameraPerspective = cameraPerspective_cb.getSelectedItem().toString();
					
					rs = sql.retrieveData("SELECT Title FROM gamedataset WHERE Genre = '" + selectedGenre + "' AND `Camera Perspective` = '" + selectedCameraPerspective + "' ;" );
					while (rs.next()) {
						 tmp = rs.getString("Title");
			             gamesStringList.add(tmp);
			         }
					String[] array = new String[gamesStringList.size()];
					for(int i = 0; i < gamesStringList.size(); i++) array[i] = gamesStringList.get(i);
					
					gamesList.setListData(array);
					
				} catch (SQLException e21) {
					// TODO Auto-generated catch block
					e21.printStackTrace();
				}
			}
		});
		
		
		suggestButton = new JButton("Suggest Games!");
		suggestButton.setBounds(300,150, 150, 23);
		this.add(suggestButton);
		
		suggestButton.addActionListener(new ActionListener() {
			@Override
			
			public void actionPerformed(ActionEvent e) {
				String title;
				String genre = null;
				String camera = null;
				String time = null;
				float averagePT = 0;
				
				String selectedTitle = gamesList.getSelectedValue();
				ResultSet rsLocal = null;
				try {
					rsLocal = sql2.retrieveData("SELECT * FROM gamedataset Where Title = \"" + selectedTitle + "\"");
					title = rsLocal.getString("Title");
					genre = rsLocal.getString("Genre");
					camera = rsLocal.getString("Camera Perspective");
					time = rsLocal.getString("Passage of Time");
					averagePT = Float.parseFloat(rsLocal.getString("Average Play Time"));
					rsLocal.close();
					} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
				try {
					ResultSet table;
					float topSim = 0;
					float tmpSim;
					
					
					
					table = sql.retrieveData("SELECT * FROM gamedataset");
					
					while (table.next()) {
	
						
						float similarity = 0;
						float timeSimilarity;
						float destAvg;
						
						
						if(camera.equals(table.getString("Camera Perspective"))) {
							similarity += 2;
						}
						
						if(genre.equals(table.getString("Genre"))) {
							similarity += 10;
						}
						
						if(time.equals(table.getString("Passage of Time"))) {
							similarity += 1;
						}
						
						destAvg = Float.parseFloat(table.getString("Average Play Time"));
						
						timeSimilarity = 2 * ( 1 / Math.abs(averagePT - destAvg) );
						similarity += timeSimilarity;
						//System.out.println(rs.getString("Title") + Float.toString(tmpSim));
						similarity_map.put(table.getString("Title"), similarity);							
					}
					
				} catch (SQLException e21) {
					// TODO Auto-generated catch block
					e21.printStackTrace();
				}
				
				ArrayList<Map.Entry<String, Float>> sorted_list = new ArrayList<>(similarity_map.entrySet());
				sorted_list.sort(Map.Entry.comparingByValue());
				
				int list_size = sorted_list.size();
				String[] list_arr = new String[list_size];
				for(int i = list_size - 2; i > list_size - 6; i--) {
					list_arr[list_size - 2 - i] = sorted_list.get(i).getKey();
				}				
				suggestionsList.setListData(list_arr);
				
			}
		});
	}
	
	public void calculateClosest() {
		String selectedgame = gamesList.getSelectedValue();
		ResultSet rs;
		ResultSet topSimRow;
		float topSim = 0;
		float tmpSim;
		
		
		try {
			rs = sql.retrieveData("SELECT * FROM gamedataset ;" );
			
			while (rs.next()) {
				System.out.println(rs.getString("Title") );
				tmpSim = calculateSimilarity(source, rs);
				//System.out.println(rs.getString("Title") + Float.toString(tmpSim));
				if (topSim < tmpSim) {
					 topSim = tmpSim;
				 }
				
	         }
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public float calculateSimilarity(ResultSet source, ResultSet dest) throws SQLException {
		float similarity = 0;
		float timeSimilarity;
		float sourceAvg;
		float destAvg;
		
		
		if(source.getString("`Camera Perspective`").equals(dest.getString("`Camera Perspective`"))) {
					similarity += 1;
				}
		
		if(source.getString("Genre").equals(dest.getString("Genre"))) {
			similarity += 5;
		}
		
		
		if(source.getString("`Passage of Time`").equals(dest.getString("`Passage of Time"))) {
			similarity += 1;
		}
		
		sourceAvg = Float.parseFloat(source.getString("`Average Play Time`"));
		destAvg = Float.parseFloat(dest.getString("`Average Play Time`"));
		
		timeSimilarity = 5 * ( 1 / Math.abs(sourceAvg - destAvg) );
		similarity += timeSimilarity;
		
		
		return similarity;
	}
	
}

