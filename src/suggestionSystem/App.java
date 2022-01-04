package suggestionSystem;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.sql.ResultSet;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class App {
	
	private JFrame frame;
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
	
	}
	
	private void initialize() {
		frame = new JFrame();
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
		
		JPanel genrePanel = new GenrePanel();
		tabbedPane.addTab("Genre", null, genrePanel, "get list of games from selected genre");
		genrePanel.setLayout(null);
		
		JPanel similarityPanel = new SimilarityPanel();
		tabbedPane.addTab("Similiarity", null, similarityPanel, "get list of games similiar to your taste ");
		similarityPanel.setLayout(null);
	}

	
	
	
	
}


