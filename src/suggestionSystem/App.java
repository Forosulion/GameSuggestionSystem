package suggestionSystem;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.text.PlainDocument;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import javax.swing.JButton;
import javax.swing.JComboBox;

import suggestionSystem.SQL_Handler;

public class App {
	
	private JFrame frame;
	private SQL_Handler sql;
	private JButton selectButton;
	private JComboBox<String> genre_cb;
	private JComboBox<String> releaseDate_cb;
	private JComboBox<String> platform_cb;
	private JComboBox<String> cameraPerspective_cb;
	private JComboBox<String> passageOfTime_cb;
	private JList<String> gamesList;
	private ArrayList<String> gamesStringList;
	ResultSet rs;

	public static void main(String[] args) {
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					App window = new App();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public App() {
		initialize();
		
		sql = new SQL_Handler();
		
		
		
			
	}
	private void initialize() {
		frame = new JFrame();
		sql = new SQL_Handler();
		gamesStringList = new ArrayList<String>();
		int windowWidth = 800;
		int windowHeight = 415;
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension screenSize = kit.getScreenSize();
		int screenHeight = screenSize.height / 2;
		int screenWidth = screenSize.width / 2;
		
		
		
		
		 
		 
		frame.setBounds(screenWidth - windowWidth / 2,  screenHeight - windowHeight / 2, windowWidth, windowHeight);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.setBounds(0, 0, 794, 350);
		frame.getContentPane().add(tabbedPane);
		
		JPanel genrePanel = new JPanel();
		tabbedPane.addTab("Genre", null, genrePanel, "get list of games from selected genre");
		genrePanel.setLayout(null);
		
		JPanel similarityPanel = new JPanel();
		tabbedPane.addTab("Similiarity", null, similarityPanel, "get list of games similiar to your taste ");
		similarityPanel.setLayout(null);
		
		String[] genreChoices = { "Puzzle", "Race", "Simulation", "Shooter", "Role-playing", "Action-RPG" , "Survival", "Action" , "Action-Adventure" , "Adventure", "Strategy" , "Platformer" };
	    genre_cb = new JComboBox<String>(genreChoices);
	    genre_cb.setBounds(250, 52, 101, 23);
	    genrePanel.add(genre_cb);
	    genre_cb.setVisible(true);
	    
	    String[] platformChoices = { "Switch","PC","PS Vita","VR","PS","PS3","PS4","PS5","3DS","Xbox","X360" };
	    platform_cb = new JComboBox<String>(platformChoices);
	    platform_cb.setBounds(145, 52, 101, 23);
	    genrePanel.add(platform_cb);
	    platform_cb.setVisible(true);
	    
	    String[] cameraPerspectiveChoices = { "Top Down","Third Person","Free Camera","Isometric","First Person","Side Scroller","Follow Camera","Shoulder" };
	    cameraPerspective_cb = new JComboBox<String>(cameraPerspectiveChoices);
	    cameraPerspective_cb.setBounds(40, 52, 101, 23);
	    genrePanel.add(cameraPerspective_cb);
	    cameraPerspective_cb.setVisible(true);
		
	    gamesList = new JList<String>();
	    gamesList.setLayoutOrientation(JList.VERTICAL);
	    gamesList.setBounds(145, 80, 101, 200);
	    gamesList.setVisibleRowCount(10);
	    gamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    genrePanel.add(gamesList);
	    gamesList.setVisible(true);
	    
	    
		selectButton = new JButton("EEEEEEE");
		selectButton.setBounds(500, 52, 101, 23);
		genrePanel.add(selectButton);
		selectButton.addActionListener(new ActionListener() {
			@Override
			
			public void actionPerformed(ActionEvent e) {
				String tmp;
				try {
					String selectedGenre = genre_cb.getSelectedItem().toString();
					String selectedPlatform = platform_cb.getSelectedItem().toString();
					String selectedCameraPerspective = cameraPerspective_cb.getSelectedItem().toString();
					System.out.println("SELECT Title FROM gamedataset "
											+"Where Genre = '" + selectedGenre + "' AND Platform = '" + selectedPlatform + "' AND 'Camera Perspective' = '" + selectedCameraPerspective + "'");
					
					rs = sql.retrieveData("SELECT Title FROM gamedataset "
											+"WHERE Genre = '" + selectedGenre + "' AND Platform = '" + selectedPlatform + "' AND `Camera Perspective` = '" + selectedCameraPerspective + "' ;" );
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
		
		
	}

	
	
	
	
}


