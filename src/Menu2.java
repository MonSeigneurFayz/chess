import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class Menu2 extends JFrame {
	public static String color;
	public static boolean trainingMode = false;
	public static String opponent = null;
	public static String opponentColor;
	public Menu2 () {
		super("Menu");
		JPanel mainPanneau = new JPanel();
		mainPanneau.setLayout(null);
		mainPanneau.setBackground(new Color(170, 170, 170));
		
		Preferences prefs = Preferences.userRoot().node("Chess");
		
		String joueur = Menu.joueur;
		
		// Si Menu.joueur n'est pas défini (null ou vide), on essaie de récupérer celui sauvegardé précédemment :
		if (joueur == null || joueur.isEmpty()) {
		    joueur = prefs.get("joueur", "Invité");
		}
		
		setUserConnectionStatus(joueur, true);
		///////////////////// TEXTE CHOIX COULEUR //////////////////////////////////
		JLabel choseColor = new JLabel ("Veuillez choisir une couleur!");
		JLabel chargement = new JLabel("En attente d'un adversaire");
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(18f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(lobsterFont);
			

		    chargement.setBounds(320,330,200,20);
		    chargement.setFont(lobsterFont);
		    chargement.setVisible(false);
		    
			choseColor.setBounds(320,330,200,20);
		    choseColor.setFont(lobsterFont);
		    choseColor.setForeground(Color.RED);
		    choseColor.setVisible(false);
		    
		    mainPanneau.add(choseColor);
		    mainPanneau.add(chargement);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		///////////////////// CHECKBOX COULEURS //////////////////////////////////
		JCheckBox white = new JCheckBox();
		JLabel labelwhite = new JLabel("Blanc");
		JCheckBox black = new JCheckBox();
		JLabel labelblack = new JLabel("Noir");
		JCheckBox random = new JCheckBox();
		JLabel labelrandom = new JLabel("Aléatoire");
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(lobsterFont);
			
			white.setBounds(300,470,20,16);
			white.setBackground(new Color(170, 170, 170));
			labelwhite.setBounds(290,490,100,16);
			labelwhite.setFont(lobsterFont);
			
			white.addItemListener(e -> {
			    if (e.getStateChange() == ItemEvent.SELECTED) {
			    	black.setSelected(false);
			        random.setSelected(false);
			        color = "white";
			    } else {
			    	
			    }
			});
			
			black.setBounds(510,470,20,16);
			black.setBackground(new Color(170, 170, 170));
			labelblack.setBounds(505,490,100,16);
			labelblack.setFont(lobsterFont);
			
			black.addItemListener(e -> {
			    if (e.getStateChange() == ItemEvent.SELECTED) {
			    	white.setSelected(false);
			        random.setSelected(false);
			        color = "black";
			    } else {
			    	
			    }
			});
			
			random.setBounds(405,470,20,16);
			random.setBackground(new Color(170, 170, 170));
			labelrandom.setBounds(390,490,100,16);
			labelrandom.setFont(lobsterFont);
			
			random.addItemListener(e -> {
			    if (e.getStateChange() == ItemEvent.SELECTED) {
			    	white.setSelected(false);
			        black.setSelected(false);
			        Random rand = new Random();
			        color = rand.nextBoolean() ? "white" : "black";
			        System.out.println("Couleur choisie aléatoirement : " + color);
			    } else {
			    	
			    }
			});
						
			mainPanneau.add(white);
			mainPanneau.add(labelwhite);
			mainPanneau.add(black);
			mainPanneau.add(labelblack);
			mainPanneau.add(random);
			mainPanneau.add(labelrandom);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}

		///////////////////// BOUTON PLAY //////////////////////////////////
		RoundedButton play = new RoundedButton ("PLAY",100);
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(50f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
			play.setBounds(265,350,300,100);			
			play.setFont(lobsterFont);
			
			play.setBackground(new Color(50, 50, 50));
			play.setForeground(Color.WHITE);
			play.setFocusPainted(false);
			play.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
			
			play.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
			
			// Hover effect (effet survol)
			play.addMouseListener(new java.awt.event.MouseAdapter() {
			    public void mouseEntered(java.awt.event.MouseEvent evt) {
			        play.setBackground(new Color(70, 70, 70));
			    }
			    
			    public void mouseExited(java.awt.event.MouseEvent evt) {
			        play.setBackground(new Color(50, 50, 50));
			    }
			});
			
			final String joueurBis = joueur;
			play.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					trainingMode = false;
			        joinQueue(joueurBis);
	        		chargement.setVisible(false);
			        
			        for (int i=0; i<10; i++) {
			        	opponent = findOpponent (joueurBis);
						chargement.setVisible(true);
			        	if (opponent != null) {
			        		prefs.put("opponent", opponent);
			        		Random rand = new Random();
					        color = rand.nextBoolean() ? "white" : "black";
					        System.out.println("Couleur choisie aléatoirement : " + color);
					        opponentColor = color.equals("white")? "black" : "white";
					        System.out.println("Couleur adversaire : " + opponentColor);
					        
			        		matchPlayers(joueurBis, opponent);
			                JOptionPane.showMessageDialog(null, "Match trouvé avec " + opponent);
							Window window = SwingUtilities.getWindowAncestor(mainPanneau);
			        	    if (window != null) {
			        	        window.dispose();
			        	    }
							Board board = new Board();
							new winBoard(board);		
							break;
			        	}
			        	
			        	try {
			                Thread.sleep(1000); // attendre 1 seconde

							chargement.setVisible(false);
			            } catch (InterruptedException e1) {
			                e1.printStackTrace();
			            }
			        }
			        if (opponent == null) {
			        	JOptionPane.showMessageDialog(null, "Aucun adversaire trouvé pour le moment.");			        	
			        }			        					
				}
			});
			
			mainPanneau.add(play);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		
		///////////////////// BOUTON ENTRAINEMENT //////////////////////////////////
		RoundedButton entrainement = new RoundedButton ("ENTRAINEMENT",70);
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(25f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
			entrainement.setBounds(295,520,250,60);			
			entrainement.setFont(lobsterFont);
			
			entrainement.setBackground(new Color(50, 50, 50));
			entrainement.setForeground(Color.WHITE);
			entrainement.setFocusPainted(false);
			entrainement.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
			
			entrainement.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
			
			// Hover effect (effet survol)
			entrainement.addMouseListener(new java.awt.event.MouseAdapter() {
			    public void mouseEntered(java.awt.event.MouseEvent evt) {
			        entrainement.setBackground(new Color(70, 70, 70));
			    }
			    
			    public void mouseExited(java.awt.event.MouseEvent evt) {
			        entrainement.setBackground(new Color(50, 50, 50));
			    }
			});
			
			entrainement.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					trainingMode = true;
					if(color != null) {
						Window window = SwingUtilities.getWindowAncestor(mainPanneau);
		        	    if (window != null) {
		        	        window.dispose();
		        	    }
						Board board = new Board();
						new winBoard(board);							
					} else {
						choseColor.setVisible(true);
					}
				}
			});
			
			mainPanneau.add(entrainement);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		
		///////////////////// BOUTON CLASSEMENT //////////////////////////////////
		RoundedButton classement = new RoundedButton ("CLASSEMENT",70);
		RoundedButton retour = new RoundedButton ("RETOUR",70);
		List<User> users = getAllUsers();
		
		String[] colonnes = {"Rang", "Nom", "Elo", "Victoire", "Nul", "Défaite", "Connexion"};
		
		List<Object[]> tempDonnees = new ArrayList<>();
		
		int rang = 1;
		for (User user : users) {
			String connexion;
			if (user.isConnected()) {
	        	connexion = "En ligne";
	        } else {
	        	connexion = "Hors ligne";
	        }
			Object[] ligne = {
			        rang++,                    // Rang
			        user.getId(),              // Nom
			        user.getElo(),             // Elo
			        user.getVictoire(),        // Victoire
			        user.getNul(),             // Nul
			        user.getDefaite(),         // Défaite
			        connexion                  // connexion
			    };
			    tempDonnees.add(ligne);
		}
		
		Object[][] donnees = tempDonnees.toArray(new Object[0][]);
		DefaultTableModel model = new DefaultTableModel(donnees, colonnes);
		
		JTable rankTable = new JTable(model);
		
		JLabel rank = new JLabel("Classement");
		try {
		InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
		Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(25f);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(lobsterFont);
		
		rank.setBounds(360,320,120,30);
		rank.setFont(lobsterFont);
		rank.setVisible(false);
		
		JScrollPane scrollPane = new JScrollPane(rankTable);
		scrollPane.setBounds(70,350,700,200);
		scrollPane.setVisible(false);
		
		
		classement.setBounds(295,600,250,60);			
		classement.setFont(lobsterFont);
		
		classement.setBackground(new Color(50, 50, 50));
		classement.setForeground(Color.WHITE);
		classement.setFocusPainted(false);
		classement.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
		
		classement.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
		
		// Hover effect (effet survol)
		classement.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        classement.setBackground(new Color(70, 70, 70));
		    }
		    
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        classement.setBackground(new Color(50, 50, 50));
		    }
		});
		
		classement.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				retour.setVisible(true);
				scrollPane.setVisible(true);
				rank.setVisible(true);
				
				classement.setVisible(false);
				play.setVisible(false);
				entrainement.setVisible(false);
				white.setVisible(false);
				labelwhite.setVisible(false);
				black.setVisible(false);
				labelblack.setVisible(false);
				random.setVisible(false);
				labelrandom.setVisible(false);
				choseColor.setVisible(false);
			}
		});
		
		retour.setBounds(295,600,250,60);			
		retour.setFont(lobsterFont);
		retour.setVisible(false);
		
		retour.setBackground(new Color(50, 50, 50));
		retour.setForeground(Color.WHITE);
		retour.setFocusPainted(false);
		retour.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
		
		retour.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
		
		// Hover effect (effet survol)
		retour.addMouseListener(new java.awt.event.MouseAdapter() {
		    public void mouseEntered(java.awt.event.MouseEvent evt) {
		        retour.setBackground(new Color(70, 70, 70));
		    }
		    
		    public void mouseExited(java.awt.event.MouseEvent evt) {
		        retour.setBackground(new Color(50, 50, 50));
		    }
		});
		
		retour.addActionListener(new ActionListener () {
			public void actionPerformed(ActionEvent e) {
				retour.setVisible(false);
				scrollPane.setVisible(false);
				rank.setVisible(false);
				
				classement.setVisible(true);
				play.setVisible(true);
				entrainement.setVisible(true);
				white.setVisible(true);
				labelwhite.setVisible(true);
				black.setVisible(true);
				labelblack.setVisible(true);
				random.setVisible(true);
				labelrandom.setVisible(true);
			}
		});
		
		mainPanneau.add(classement);
		mainPanneau.add(retour);
		mainPanneau.add(scrollPane);
		mainPanneau.add(rank);
		} catch (IOException | FontFormatException e) {
		e.printStackTrace();
		}

		///////////////////// BOUTON DECONNEXION & JOUEUR //////////////////////////////////
		RoundedButton deconnexion = new RoundedButton ("Déconnexion",10);
		
		
		JLabel compte = new JLabel(joueur);
		System.out.println(joueur);
		compte = new JLabel(joueur);
		try {
		InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
		Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(lobsterFont);
		
		
		compte.setFont(lobsterFont);
		compte.setForeground(new Color(30, 30, 30));
		compte.setBounds(320, 10, 200, 20);
		compte.setHorizontalAlignment(SwingConstants.CENTER);
		
		deconnexion.setBounds(650,10,150,20);
		
		deconnexion.setFont(lobsterFont);
		
		deconnexion.setBackground(new Color(50, 50, 50));
		deconnexion.setForeground(Color.WHITE);
		deconnexion.setFocusPainted(false);
		deconnexion.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
		
		deconnexion.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
		
		// Hover effect (effet survol)
		deconnexion.addMouseListener(new java.awt.event.MouseAdapter() {
		public void mouseEntered(java.awt.event.MouseEvent evt) {
		    deconnexion.setBackground(new Color(70, 70, 70));
		}
		
		public void mouseExited(java.awt.event.MouseEvent evt) {
		    deconnexion.setBackground(new Color(50, 50, 50));
		}
		});
		
		final String joueurBis = joueur;
		deconnexion.addActionListener(new ActionListener () {
		public void actionPerformed(ActionEvent e) {
			setUserConnectionStatus(joueurBis, false);
			prefs.putBoolean("stayConnected", false); // sauvegarde
		    Main.stayConnected = false;
		    System.out.println("Sauvegardé : stayConnected = " + false);
		    Window window = SwingUtilities.getWindowAncestor(mainPanneau);
		    if (window != null) {
		        window.dispose();
		    }
		    new Menu();
		}
		});
		mainPanneau.add(compte);
		mainPanneau.add(deconnexion);
		
		} catch (IOException | FontFormatException e) {
		e.printStackTrace();
		}
		
		///////////////////// TITRE //////////////////////////////////
		try {
		InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
		Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(150f);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(lobsterFont);
		
		JLabel titre = new JLabel("CHESS");
		titre.setFont(lobsterFont);
		titre.setForeground(new Color(30, 30, 30)); // Gris foncé
		titre.setBounds(110, 100, 600, 200); 
		titre.setHorizontalAlignment(SwingConstants.CENTER);
		
		JLabel ligne = new JLabel("__________");
		ligne.setFont(new Font("Segoe UI", Font.BOLD, 80));
		ligne.setForeground(new Color(30, 30, 30)); // Gris foncé
		ligne.setBounds(110, 130, 600, 200); 
		ligne.setHorizontalAlignment(SwingConstants.CENTER);
		
		mainPanneau.add(titre);
		mainPanneau.add(ligne);
		} catch (IOException | FontFormatException e) {
		e.printStackTrace();
		}
		
		setContentPane(mainPanneau);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(815,1000);
		setMinimumSize(new Dimension(820, 1000));
		setLocationRelativeTo(null);
		setVisible(true);
	}
	
	public static List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                    rs.getString("email"),
                    rs.getString("identifiant"),
                    rs.getString("mot_de_passe")
                );
                user.setConnected(rs.getBoolean("connected"));
                users.add(user);
            }

        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des utilisateurs : " + e.getMessage());
        }

        return users;
    }
	
	public static User getUserByLogin(String identifiantOuEmail, String motDePasse) {
	    String sql = "SELECT * FROM user WHERE (identifiant = ? OR email = ?) AND mot_de_passe = ?";

	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, identifiantOuEmail);
	        stmt.setString(2, identifiantOuEmail);
	        stmt.setString(3, motDePasse);

	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            return new User(
	                rs.getString("email"),
	                rs.getString("identifiant"),
	                rs.getString("mot_de_passe")
	            );
	        }

	    } catch (SQLException e) {
	        System.out.println("Erreur lors de la tentative de connexion : " + e.getMessage());
	    }

	    return null;
	}
	
	public static void setUserConnectionStatus(String identifiantOuEmail, boolean isConnected) {
	    String sql = "UPDATE user SET connected = ? WHERE identifiant = ? OR email = ?";

	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setBoolean(1, isConnected);
	        stmt.setString(2, identifiantOuEmail);
	        stmt.setString(3, identifiantOuEmail);
	        stmt.executeUpdate();

	    } catch (SQLException e) {
	        System.out.println("Erreur lors de la mise à jour de l'état de connexion : " + e.getMessage());
	    }
	}
	
	public void joinQueue(String username) {
	    String sql = "UPDATE user SET in_queue = TRUE WHERE identifiant = ?";
	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, username);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public String findOpponent(String username) {
	    String sql = "SELECT identifiant FROM user WHERE connected = TRUE AND in_queue = TRUE AND identifiant != ? LIMIT 1";
	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setString(1, username);
	        ResultSet rs = stmt.executeQuery();

	        if (rs.next()) {
	            return rs.getString("identifiant");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
	public void matchPlayers(String player1, String player2) {
	    String sql = "UPDATE user SET in_queue = FALSE, opponent = ? WHERE identifiant = ?";
	    try (Connection conn = Database.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        // Update player1
	        stmt.setString(1, player2);
	        stmt.setString(2, player1);
	        stmt.executeUpdate();

	        // Update player2
	        stmt.setString(1, player1);
	        stmt.setString(2, player2);
	        stmt.executeUpdate();

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
}

