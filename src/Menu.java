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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Menu extends JFrame {
	private boolean registered = false;
	public static String joueur = null;
	private boolean stayConnected;
	public Menu () {
		super("Menu");
		JPanel mainPanneau = new JPanel();
		mainPanneau.setLayout(null);
		mainPanneau.setBackground(new Color(170, 170, 170));

		Preferences prefs = Preferences.userRoot().node("Chess");
		
		///////////////////// TEXTE ERREUR MDP ET COMPTE //////////////////////////////////
		JLabel erreurMdp = new JLabel ("Mot de passe ou Identifiant inccorect");
		JLabel erreurCompte = new JLabel ("Ce compte n'existe pas");
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
			erreurMdp.setBounds(285,265,270,100);
			erreurMdp.setFont(new Font("Arial", Font.BOLD, 14));
			erreurMdp.setVisible(false);
			erreurMdp.setForeground(Color.RED);
			
			erreurCompte.setBounds(285,265,270,100);
			erreurCompte.setFont(new Font("Arial", Font.BOLD, 14));
			erreurCompte.setVisible(false);
			erreurCompte.setForeground(Color.RED);
			
			mainPanneau.add(erreurMdp);
			mainPanneau.add(erreurCompte);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		
		///////////////////// TEXTES INSCRIPTION //////////////////////////////////
		JLabel inscriptionReussi = new JLabel ("<html>Félicitations ! Votre inscription a été prise en compte."
				+ "<br>Vous pouvez maintenant vous connecter à CHESS.</html>");
		JLabel inscriptionEchoue = new JLabel ("<html>Oups... L'inscription a échoué."
				+ "<br>Il est possible que ce mail ou cet identifiant soit déjà utilisé."
				+ "<br>Le mot de passe doit contenir au moins 8 caractères, "
				+ "<br>une majuscule, une minuscule et un caractère spéciale</br></br></html>");
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
		    inscriptionReussi.setBounds(220,265,400,100);
		    inscriptionReussi.setFont(new Font("Arial", Font.BOLD, 14));
		    inscriptionReussi.setVisible(false);
		    inscriptionReussi.setForeground(Color.GREEN);
		    
		    inscriptionEchoue.setBounds(220,255,400,100);
		    inscriptionEchoue.setFont(new Font("Arial", Font.BOLD, 12));
		    inscriptionEchoue.setVisible(false);
		    inscriptionEchoue.setForeground(Color.RED);
			
			mainPanneau.add(inscriptionReussi);
			mainPanneau.add(inscriptionEchoue);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		
		///////////////////// BOX RESTER CONNECTE & MONTRER LE MOT DE PASSE //////////////////////////////////
		JCheckBox StayConnected = new JCheckBox ("Rester connecté(e)");
		StayConnected.setBackground(new Color(170, 170, 170));
		JCheckBox ShowPassword = new JCheckBox ("Afficher mot de passe");
		ShowPassword.setBackground(new Color(170, 170, 170));
		JCheckBox ShowPasswordBis = new JCheckBox ("Afficher mot de passe");
		ShowPasswordBis.setBackground(new Color(170, 170, 170));
		ShowPasswordBis.setVisible(false);
		JPasswordField zoneMdp = new JPasswordField();
		JPasswordField zoneMdpBis = new JPasswordField();
		zoneMdpBis.setVisible(false);
		try {
		InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
		Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		ge.registerFont(lobsterFont);
		
		StayConnected.setBounds(295,475,140,16);
		StayConnected.setFont(lobsterFont);
		
		StayConnected.addItemListener(e -> {
		    if (e.getStateChange() == ItemEvent.SELECTED) {
		        prefs.putBoolean("stayConnected", true); // sauvegarde
		        stayConnected = true;
		        System.out.println("Sauvegardé : stayConnected = " + true);		    	
		    } else {
		    	prefs.putBoolean("stayConnected", false); // sauvegarde
		        Main.stayConnected = false;
		        stayConnected = false;
		        System.out.println("Sauvegardé : stayConnected = " + false);
		    }
		});
		
		ShowPassword.setBounds(435,475,160,16);
		ShowPassword.setFont(lobsterFont);
		
		ShowPassword.addActionListener(e -> {
		    if (ShowPassword.isSelected()) {
		    	zoneMdp.setEchoChar((char) 0); // Affiche le texte
		    } else {
		    	zoneMdp.setEchoChar('●'); // Cache le texte
		    }
		});
		
		ShowPasswordBis.setBounds(295,550,160,16);
		ShowPasswordBis.setFont(lobsterFont);
		
		ShowPasswordBis.addActionListener(e -> {
		    if (ShowPasswordBis.isSelected()) {
		    	zoneMdpBis.setEchoChar((char) 0); // Affiche le texte
		    } else {
		    	zoneMdpBis.setEchoChar('●'); // Cache le texte
		    }
		});
		
		mainPanneau.add(ShowPasswordBis);
		mainPanneau.add(ShowPassword);
		mainPanneau.add(StayConnected);
		} catch (IOException | FontFormatException e) {
		e.printStackTrace();
		}

		///////////////////// BOUTON INSCRIPTION & CONNECTION //////////////////////////////////
		JLabel identifiant = new JLabel ("Identifiant/E-mail");
		JTextField zoneId = new JTextField();
		JLabel motDePasse = new JLabel ("Mot de passe");
		RoundedButton connection = new RoundedButton ("Connection",100);
		RoundedButton inscription = new RoundedButton ("Inscription",100);
		
		JLabel email = new JLabel ("E-Mail");
		JTextField zoneEmail = new JTextField();
		JLabel identifiantBis = new JLabel ("Identifiant");
		JTextField zoneIdBis = new JTextField();
		JLabel motDePasseBis = new JLabel ("Mot de passe");
		RoundedButton inscriptionBis = new RoundedButton ("Inscription",100);
		RoundedButton retour = new RoundedButton ("Retour",70);
		inscriptionBis.setVisible(false);
		email.setVisible(false);
		zoneEmail.setVisible(false);
		identifiantBis.setVisible(false);
		zoneIdBis.setVisible(false);
		motDePasseBis.setVisible(false);
		retour.setVisible(false);
		
		List<String> listEmail = new ArrayList<>();
		List<String> listId = new ArrayList<>();
		List<String> listMdp = new ArrayList<>();
		List<User> listUser = new ArrayList<>();
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(30f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
				///////////////////// BOUTON CONNECTION //////////////////////////////////
				identifiant.setBounds(295,300,250,100);
				identifiant.setFont(lobsterFont);
				
				zoneId.setBounds(295,370,250,30);
				zoneId.setFont(new Font("Arial", Font.PLAIN, 18));
				
				motDePasse.setBounds(295,370,250,100);
				motDePasse.setFont(lobsterFont);
				
				zoneMdp.setBounds(295,440,250,30);
				zoneMdp.setFont(new Font("Arial", Font.PLAIN, 18));
				
				connection.setBounds(295,500,250,100);
				connection.setFont(lobsterFont);
				
				connection.setBackground(new Color(50, 50, 50));
				connection.setForeground(Color.WHITE);
				connection.setFocusPainted(false);
				connection.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
				
				connection.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
				
				// Hover effect (effet survol)
				connection.addMouseListener(new java.awt.event.MouseAdapter() {
				    public void mouseEntered(java.awt.event.MouseEvent evt) {
				    	connection.setBackground(new Color(70, 70, 70));
				    }

				    public void mouseExited(java.awt.event.MouseEvent evt) {
				    	connection.setBackground(new Color(50, 50, 50));
				    }
				});
				
				connection.addActionListener(new ActionListener () {
					public void actionPerformed(ActionEvent e) {
						String Id = zoneId.getText();
						String Mdp = new String(zoneMdp.getPassword());
						prefs.put("Mdp", Mdp);  // Sauvegarde dans les préférences
						User user = getUserByLogin(Id,Mdp);
						if (user != null) {
							joueur = user.getId();
					        prefs.put("joueur", joueur);  // Sauvegarde dans les préférences
							String mail = new String(user.getEmail());
							if((Id.equals(user.getId()) || Id.equals(mail)) && Mdp.equals(user.getMdp())) {
								ShowPassword.setVisible(false);
								Window window = SwingUtilities.getWindowAncestor(mainPanneau);
				        	    if (window != null) {
				        	        window.dispose();
				        	    }
				        	    
				        	    if (stayConnected) {
					        	    Main.stayConnected = true;
				        	    }
								new Menu2();
							}  else {
								inscriptionReussi.setVisible(false);
								erreurMdp.setVisible(true);
								zoneMdp.setText("");
							}
						}  else {
							inscriptionReussi.setVisible(false);
							erreurMdp.setVisible(true);
							zoneMdp.setText("");
						}
					}
				});
				
				mainPanneau.add(identifiant);
				mainPanneau.add(zoneId);
				mainPanneau.add(motDePasse);
				mainPanneau.add(zoneMdp);
				mainPanneau.add(connection);
				///////////////////// BOUTON INSCRIPTION //////////////////////////////////
				email.setBounds(295,300,250,100);
				email.setFont(lobsterFont);
				
				zoneEmail.setBounds(295,370,250,30);
				zoneEmail.setFont(new Font("Arial", Font.PLAIN, 18));
				
				identifiantBis.setBounds(295,370,250,100);
				identifiantBis.setFont(lobsterFont);
				
				zoneIdBis.setBounds(295,440,250,30);
				zoneIdBis.setFont(new Font("Arial", Font.PLAIN, 18));
				
				motDePasseBis.setBounds(295,440,250,100);
				motDePasseBis.setFont(lobsterFont);
				
				zoneMdpBis.setBounds(295,510,250,30);
				zoneMdpBis.setFont(new Font("Arial", Font.PLAIN, 18));
				
				inscription.setBounds(295,610,250,100);
				
				inscription.setFont(lobsterFont);
				
				inscription.setBackground(new Color(50, 50, 50));
				inscription.setForeground(Color.WHITE);
				inscription.setFocusPainted(false);
				inscription.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
				
				inscription.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
				
				// Hover effect (effet survol)
				inscription.addMouseListener(new java.awt.event.MouseAdapter() {
				    public void mouseEntered(java.awt.event.MouseEvent evt) {
				        inscription.setBackground(new Color(70, 70, 70));
				    }

				    public void mouseExited(java.awt.event.MouseEvent evt) {
				        inscription.setBackground(new Color(50, 50, 50));
				    }
				});
				
				inscription.addActionListener(new ActionListener () {
					public void actionPerformed(ActionEvent e) {
						connection.setVisible(false);
						inscription.setVisible(false);
						identifiant.setVisible(false);
						zoneId.setVisible(false);
						motDePasse.setVisible(false);
						zoneMdp.setVisible(false);
						StayConnected.setVisible(false);
						inscriptionReussi.setVisible(false);
						inscriptionEchoue.setVisible(false);
						erreurMdp.setVisible(false);
						erreurCompte.setVisible(false);
						ShowPassword.setVisible(false);
						
						inscriptionBis.setVisible(true);
						email.setVisible(true);
						zoneEmail.setVisible(true);
						identifiantBis.setVisible(true);
						zoneIdBis.setVisible(true);
						motDePasseBis.setVisible(true);
						zoneMdpBis.setVisible(true);
						retour.setVisible(true);
						ShowPasswordBis.setVisible(true);
						
						zoneId.setText("");
						zoneMdp.setText("");
					}
				});
				
				email.setBounds(295,300,250,100);
				email.setFont(lobsterFont);
				
				zoneEmail.setBounds(295,370,250,30);
				zoneEmail.setFont(new Font("Arial", Font.PLAIN, 18));
				
				identifiantBis.setBounds(295,370,250,100);
				identifiantBis.setFont(lobsterFont);
				
				zoneIdBis.setBounds(295,440,250,30);
				zoneIdBis.setFont(new Font("Arial", Font.PLAIN, 18));
				
				motDePasseBis.setBounds(295,440,250,100);
				motDePasseBis.setFont(lobsterFont);
				
				zoneMdpBis.setBounds(295,510,250,30);
				zoneMdpBis.setFont(new Font("Arial", Font.PLAIN, 18));
				
				inscriptionBis.setBounds(295,610,250,100);
				
				inscriptionBis.setFont(lobsterFont);
				
				inscriptionBis.setBackground(new Color(50, 50, 50));
				inscriptionBis.setForeground(Color.WHITE);
				inscriptionBis.setFocusPainted(false);
				inscriptionBis.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
				
				inscriptionBis.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
				
				// Hover effect (effet survol)
				inscriptionBis.addMouseListener(new java.awt.event.MouseAdapter() {
				    public void mouseEntered(java.awt.event.MouseEvent evt) {
				        inscriptionBis.setBackground(new Color(70, 70, 70));
				    }

				    public void mouseExited(java.awt.event.MouseEvent evt) {
				        inscriptionBis.setBackground(new Color(50, 50, 50));
				    }
				});
				
				inscriptionBis.addActionListener(new ActionListener () {
					public void actionPerformed(ActionEvent e) {
						System.out.println(listUser);
						String mail = zoneEmail.getText();
						String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
						String Id = zoneIdBis.getText();
						String regex1 = "^(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z0-9]).{8,}$";
						String Mdp = new String(zoneMdpBis.getPassword());
						User user = null;
						if (!mail.isEmpty() && mail.matches(regex) 
							&& !Id.isEmpty()
							&& !Mdp.isEmpty()
							&& Mdp.matches(regex1)) {
							registered = true;
							listEmail.add(zoneEmail.getText());
							listId.add(zoneIdBis.getText());
							listMdp.add(Mdp);
							
							user = new User(mail,Id,Mdp);
							User.saveUser(user);
							System.out.println(listUser);
						} else {
							registered = false;
						}
						if (user == null) {
							user = getUserByLogin(Id,Mdp);
							if (!mail.isEmpty() && mail.matches(regex) 
								&& !Id.isEmpty()
								&& !Mdp.isEmpty()
								&& Mdp.matches(regex1)
								&& !User.saveUser(user)) {
								registered = true;
								listEmail.add(zoneEmail.getText());
								listId.add(zoneIdBis.getText());
								listMdp.add(Mdp);
								
								user = new User(mail,Id,Mdp);
								User.saveUser(user);
								System.out.println(listUser);
							} else {
								registered = false;
							}
						}
						
						if (registered) {
							inscriptionReussi.setVisible(true);
							connection.setVisible(true);
							inscription.setVisible(true);
							identifiant.setVisible(true);
							zoneId.setVisible(true);
							motDePasse.setVisible(true);
							zoneMdp.setVisible(true);
							StayConnected.setVisible(true);
							ShowPassword.setVisible(true);
							
							inscriptionEchoue.setVisible(false);
							inscriptionBis.setVisible(false);
							email.setVisible(false);
							zoneEmail.setVisible(false);
							identifiantBis.setVisible(false);
							zoneIdBis.setVisible(false);
							motDePasseBis.setVisible(false);
							zoneMdpBis.setVisible(false);
							ShowPasswordBis.setVisible(false);
							retour.setVisible(false);
							
							zoneEmail.setText("");
							zoneIdBis.setText("");
							zoneMdpBis.setText("");
						} else {
							inscriptionEchoue.setVisible(true);	
							inscriptionReussi.setVisible(false);
							
							zoneEmail.setText("");
							zoneIdBis.setText("");
							zoneMdpBis.setText("");
						}
					}
				});
				
				retour.setBounds(320,720,200,70);
				
				retour.setFont(lobsterFont);
				
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
						connection.setVisible(true);
						inscription.setVisible(true);
						identifiant.setVisible(true);
						zoneId.setVisible(true);
						motDePasse.setVisible(true);
						zoneMdp.setVisible(true);
						StayConnected.setVisible(true);
						ShowPassword.setVisible(true);
						
						inscriptionEchoue.setVisible(false);
						inscriptionBis.setVisible(false);
						email.setVisible(false);
						zoneEmail.setVisible(false);
						identifiantBis.setVisible(false);
						zoneIdBis.setVisible(false);
						motDePasseBis.setVisible(false);
						zoneMdpBis.setVisible(false);
						retour.setVisible(false);
						ShowPasswordBis.setVisible(false);
						
						zoneEmail.setText("");
						zoneIdBis.setText("");
						zoneMdpBis.setText("");
					}
				});
				
				mainPanneau.add(inscription);
				mainPanneau.add(inscriptionBis);
				mainPanneau.add(email);
				mainPanneau.add(zoneEmail);
				mainPanneau.add(identifiantBis);
				mainPanneau.add(zoneIdBis);
				mainPanneau.add(motDePasseBis);
				mainPanneau.add(zoneMdpBis);
				mainPanneau.add(retour);
			
			
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

}
