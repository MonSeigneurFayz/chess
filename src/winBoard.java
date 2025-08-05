import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SpringLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class winBoard extends JFrame {
	private JButton selectedPiece = null;
	private JButton selectedPieceChoice = null;
	private JButton selectedPieceChoice2 = null;
	private GameState gameState = new GameState();
	private JButton captureChoice = null;
	private JLabel joueur1Label;  // Joueur blanc
	private JLabel joueur2Label;  // Joueur noir
	private int joueur1Score = 0;
	private int joueur2Score = 0;
	private List<int[]> legalMoves = new ArrayList<>();
	private boolean checkMate = false;
	
	Preferences prefs = Preferences.userRoot().node("Chess");
	String joueur = Menu.joueur;
	String opponent = Menu2.opponent;
	int gameId = createGame(joueur, opponent);
	
	public winBoard (Board board) {
		super("Chess");
		JPanel mainPanneau = new JPanel();
		mainPanneau.setLayout(null);
		mainPanneau.setBackground(new Color(150, 150, 150));
		
		JLayeredPane layeredPanneau = new JLayeredPane();
		layeredPanneau.setBounds(0, 80, 800, 800);
        layeredPanneau.setPreferredSize(new Dimension(800, 800));
		
		JPanel panneau = new JPanel(new GridLayout(board.getCol(), board.getRow()));
		panneau.setOpaque(true);
		panneau.setPreferredSize(new Dimension(800, 800));
		panneau.setMaximumSize(new Dimension(800, 800));
		panneau.setMinimumSize(new Dimension(800, 800));
		panneau.setBounds(5, 0, 800, 800);
		
		
		JPanel panneauCoordonnees = new JPanel(null); 
        panneauCoordonnees.setOpaque(false);
        panneauCoordonnees.setBounds(5, 0, 800, 800);
        
        JPanel panneauArmée = new JPanel(null); 
        panneauArmée.setOpaque(false);
        panneauArmée.setBounds(5, 0, 800, 800);
        
        String[] lettres = {"A", "B", "C", "D", "E", "F", "G", "H"};
        
		for (int i=board.getCol() ; i>=1;i--) {
			for (int j=1 ; j<=board.getRow();j++) {
				JButton tile = new JButton("Bouton" + (i) + (j));
				tile.setText("");
				tile.setOpaque(true);
				tile.setBorderPainted(false);
				tile.setEnabled(true); // Permet le clic sur une case vide

				// Couleur du damier
				if ((i + j) % 2 == 0) {
				    tile.setBackground(Color.LIGHT_GRAY);
				} else {
				    tile.setBackground(Color.WHITE);
				}
				
				// Action : désélectionner si clic sur une case vide
				tile.addActionListener(new ActionListener() {
				    public void actionPerformed(ActionEvent e) {
				        if (selectedPiece != null) {
				            // Enlever l'effet visuel de sélection
				            selectedPiece.setBorderPainted(false);
				            selectedPiece.setContentAreaFilled(false);
				            selectedPiece.setBackground(null);
				            selectedPiece = null;
				        }

				        // Supprimer les cases de déplacement et capture
				        Component[] components = panneauArmée.getComponents();
				        for (int i = components.length - 1; i >= 0; i--) {
				            if (components[i] instanceof Choice) {
				                panneauArmée.remove(components[i]);
				            }
				        }

				        // Réinitialiser les références
				        selectedPieceChoice = null;
				        selectedPieceChoice2 = null;
				        captureChoice = null;

				        panneauArmée.repaint();
				    }
				});


				initialize (i,j,panneauArmée);
                                
				panneau.add(tile);
			}
		}
		
		for (int i=0 ; i<lettres.length ; i++) {
			JLabel labelLettre = new JLabel (lettres[i]);
			labelLettre.setForeground(Color.BLACK);
            labelLettre.setBounds(100 * i + 85, 780, 20, 20); // bas
            panneauCoordonnees.add(labelLettre);
		}
		
		if (Menu2.color == "white") {
			for (int i=1; i<=8 ; i++) {
				JLabel labelChiffre = new JLabel ("" + i);
				labelChiffre.setForeground(Color.BLACK);
				labelChiffre.setBounds(10, 800 - 100 * i, 20,20);
				panneauCoordonnees.add(labelChiffre);
			}
		}
		
		if (Menu2.color == "black") {
			for (int i=0; i<=8 ; i++) {
				JLabel labelChiffre = new JLabel ("" + i);
				labelChiffre.setForeground(Color.BLACK);
				labelChiffre.setBounds(10, 100 * (i-1), 20,20);
				panneauCoordonnees.add(labelChiffre);
			}
		}
		
		layeredPanneau.add(panneau, JLayeredPane.DEFAULT_LAYER);
		layeredPanneau.add(panneauCoordonnees, JLayeredPane.PALETTE_LAYER);
		layeredPanneau.add(panneauArmée, JLayeredPane.DRAG_LAYER);
		
		
		
		// Si Menu.joueur n'est pas défini (null ou vide), on essaie de récupérer celui sauvegardé précédemment :
		if (joueur == null || joueur.isEmpty()) {
		    joueur = prefs.get("joueur", "Invité");
		}
		
		// Si Menu.opponent n'est pas défini (null ou vide), on essaie de récupérer celui sauvegardé précédemment :
		if (opponent == null || opponent.isEmpty()) {
			opponent = prefs.get("opponent", "Invité");
		}
		
		if (opponent != null && !Menu2.trainingMode) {
			if (Menu2.color == "white") {
				joueur1Label = new JLabel (joueur + " - Score: " , SwingConstants.CENTER);
				joueur2Label = new JLabel (opponent + " - Score: " , SwingConstants.CENTER);
			} else {
				joueur1Label = new JLabel (opponent + " - Score: " , SwingConstants.CENTER);
				joueur2Label = new JLabel (joueur + " - Score: " , SwingConstants.CENTER);
			}
		} else {
			if (Menu2.color == "white") {
				joueur1Label = new JLabel (joueur + " - Score: " , SwingConstants.CENTER);
				joueur2Label = new JLabel ("Joueur 2 (Noir) - Score: " , SwingConstants.CENTER);
			} else {
				joueur1Label = new JLabel ("Joueur 1 (Blanc) - Score: " , SwingConstants.CENTER);
				joueur2Label = new JLabel (joueur + " - Score: " , SwingConstants.CENTER);
			}
		}
		
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(20f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
		    if (Menu2.color == "white") {
		    	joueur2Label.setBounds(295, 20, 250, 30);
		    } else {
		    	joueur2Label.setBounds(295, 900, 250, 30);
		    }
		    
		    joueur2Label.setFont(lobsterFont);
		    //joueur2Label.setForeground(Color.GREEN);
			
			mainPanneau.add(joueur2Label);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		
		mainPanneau.add(layeredPanneau);
		
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(20f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
		    if (Menu2.color == "white") {
		    	joueur1Label.setBounds(295, 900, 250, 30);
		    } else {
		    	joueur1Label.setBounds(295, 20, 250, 30);
		    }
		    
		    joueur1Label.setFont(lobsterFont);
		    //joueur1Label.setForeground(Color.GREEN);
			
			mainPanneau.add(joueur1Label);
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		
		
		RoundedButton quitter = new RoundedButton ("Quitter",20);
		RoundedButton recommencer = new RoundedButton ("Recommencer",20);
		
		try {
			InputStream is = getClass().getResourceAsStream("/Lobster-Regular.ttf");
			Font lobsterFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(20f);
		    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		    ge.registerFont(lobsterFont);
		    
		    if (!Menu2.trainingMode) {
		    	RoundedButton abandon = new RoundedButton ("Abandonner",20);
			    
			    abandon.setBounds(10,900,100,25);
			    abandon.setFont(lobsterFont);
			    
			    mainPanneau.add(abandon);
		    }
		    
		    quitter.setBounds(10,905,100,25);
		    quitter.setFont(lobsterFont);
		    
		    recommencer.setBounds(115,905,150,25);
		    recommencer.setFont(lobsterFont);
		    
		    quitter.setBackground(new Color(50, 50, 50));
			quitter.setForeground(Color.WHITE);
			quitter.setFocusPainted(false);
			quitter.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
			
			quitter.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
			
			// Hover effect (effet survol)
			quitter.addMouseListener(new java.awt.event.MouseAdapter() {
			    public void mouseEntered(java.awt.event.MouseEvent evt) {
			        quitter.setBackground(new Color(70, 70, 70));
			    }
			    
			    public void mouseExited(java.awt.event.MouseEvent evt) {
			        quitter.setBackground(new Color(50, 50, 50));
			    }
			});
			
			quitter.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					Window window = SwingUtilities.getWindowAncestor(panneau);
	            	Point location = window.getLocation();
	                if (window != null) {
	                    window.dispose();
	                }
	                new Menu2();
				}
			});
			
			recommencer.setBackground(new Color(50, 50, 50));
			recommencer.setForeground(Color.WHITE);
			recommencer.setFocusPainted(false);
			recommencer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Padding interne
			
			recommencer.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Curseur main
			
			// Hover effect (effet survol)
			recommencer.addMouseListener(new java.awt.event.MouseAdapter() {
			    public void mouseEntered(java.awt.event.MouseEvent evt) {
			        recommencer.setBackground(new Color(70, 70, 70));
			    }
			    
			    public void mouseExited(java.awt.event.MouseEvent evt) {
			        recommencer.setBackground(new Color(50, 50, 50));
			    }
			});
			
			recommencer.addActionListener(new ActionListener () {
				public void actionPerformed(ActionEvent e) {
					panneauArmée.removeAll();
	            	joueur1Score = 0;
	            	joueur2Score = 0;
	            	updateScoreLabels();
	            	Board board = new Board();
	            	gameState = new GameState();
	            	panneauArmée.repaint();
	            	for (int i=board.getCol() ; i>=1;i--) {
	        			for (int j=1 ; j<=board.getRow();j++) {
	        				initialize(i,j,panneauArmée);
	        			}
	            	}
				}
			});
			
		    mainPanneau.add(quitter);
		    mainPanneau.add(recommencer);
		    
		} catch (IOException | FontFormatException e) {
		    e.printStackTrace();
		}
		setContentPane(mainPanneau);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(815,1000);
		setMinimumSize(new Dimension(820, 1000));
		setLocationRelativeTo(null);
		setVisible(true);
		
		if (!Menu2.trainingMode) {
			new Thread(() -> {
			    String lastSeenMove = null;
			    while (true) {
			        String opponentMove;
					try {
						opponentMove = getLastOpponentMove(String.valueOf(gameId), opponent);
						if (opponentMove != null && !opponentMove.equals(lastSeenMove)) {
				            lastSeenMove = opponentMove;
		
				            // Appliquer le coup sur le plateau
				            SwingUtilities.invokeLater(() -> {
				                applyMoveToBoard(opponentMove, panneauArmée);
				                gameState.switchTurn(); 
				                selectedActivation(panneauArmée); 
				                addLegalMoves(panneauArmée); 
				            });
				        } 
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        
			        
			        try {
			            Thread.sleep(1000); // 1 sec de pause
			        } catch (InterruptedException e) {
			            e.printStackTrace();
			        }
			    }
			}).start();
		}
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void addPawn(int ligne, int colonne , String color, JPanel panneau) {
	    Pawn pawn = new Pawn(color,ligne,colonne);
	    pawn.setBorderPainted(false);
	    pawn.setContentAreaFilled(false);
	    pawn.setFocusPainted(false);
	    pawn.setEnabled(true);
	    final int[] pawnRow = {ligne};
	    final int[] pawnCol = {colonne - 1};
	    int row = pawnRow[0];
	    int col = pawnCol[0];
	    
	    

	    pawn.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	            // Désélectionner l'ancien pion
	            if (selectedPiece != null && selectedPiece != pawn) {
	                selectedPiece.setBorderPainted(false);
	                selectedPiece.setContentAreaFilled(false);
	                selectedPiece.setBackground(null);
	                selectedPiece = null;                
	                clearSelections(panneau);
	            }

	            // Désélectionner si on reclique dessus
	            if (selectedPiece == pawn) {
	                selectedPiece.setBorderPainted(false);
	                selectedPiece.setContentAreaFilled(false);
	                selectedPiece.setBackground(null);
	                selectedPiece = null;

	                if (selectedPieceChoice != null) panneau.remove(selectedPieceChoice);
	                if (selectedPieceChoice2 != null) panneau.remove(selectedPieceChoice2);
	                if (captureChoice != null) panneau.remove(captureChoice);
	                panneau.repaint();
	                selectedPieceChoice = null;
	                selectedPieceChoice2 = null;
	                captureChoice = null;
	                
	                
	            } else {
	                selectedPiece = pawn;
	                pawn.setBorderPainted(true);
	                pawn.setContentAreaFilled(true);
	                pawn.setBackground(Color.CYAN);

	                // Nettoyer anciens Choice
	                if (selectedPieceChoice != null) panneau.remove(selectedPieceChoice);
	                if (selectedPieceChoice2 != null) panneau.remove(selectedPieceChoice2);
	                if (captureChoice != null) panneau.remove(captureChoice);
	                selectedPieceChoice = null;
	                selectedPieceChoice2 = null;
	                captureChoice = null;

	                if (!pawn.getColor().equals(gameState.getCurrentPlayerColor())) {
	                    // Si ce n'est pas au tour de cette couleur, on ne montre pas les déplacements.
	                	selectedPieceChoice = null;
                        selectedPieceChoice2 = null;
                        captureChoice = null;
                        panneau.repaint();
	                    return;
	                }
	                
	                for (Component c : panneau.getComponents()) {
	                	if (c instanceof ColoredPiece) {
	                		ColoredPiece piece = (ColoredPiece) c;
	                		if (!Menu2.trainingMode && !piece.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	                    // Pas le bon tour, on ignore
	    	                    return;
	    	                }
	    	                
	    	                showPawnMoves(pawn, pawnRow, pawnCol, panneau, color, pawn.hasMoved());
	                	}
	                }
	                

	                panneau.repaint();
	            }

	        }
	    });

	    panneau.add(pawn);
	}
	
	private void showPawnMoves(Pawn pawn, int[] pawnRow, int[] pawnCol, JPanel panneau, String color, boolean hasMoved) {
		int currentRow = pawnRow[0];
		int currentCol = pawnCol[0];
		
		int nextRow1 = 0;
		int nextRow2 = 0;
		
		if (Menu2.color == "white") {
			nextRow1 = color.equals("white") ? currentRow - 1 : currentRow + 1;
	        nextRow2 = color.equals("white") ? currentRow - 2 : currentRow + 2;
		}
		
		else if (Menu2.color == "black") {
			nextRow1 = color.equals("white") ? currentRow + 1 : currentRow - 1;
	        nextRow2 = color.equals("white") ? currentRow + 2 : currentRow - 2;
		}
        

        boolean isNextRow1Occupied = false;
        boolean isNextRow2Occupied = false;
        
        for (Component c : panneau.getComponents()) {
            if (c instanceof JButton && c != pawn) {
                Rectangle r = c.getBounds();
                int cRow = r.y / 100;
                int cCol = r.x / 100;

                if (cRow == nextRow1 && cCol == currentCol) {
                	isNextRow1Occupied = true;
                }
                if (cRow == nextRow2 && cCol == currentCol) {
                	isNextRow2Occupied = true;
                }
                if (cRow == nextRow1 && (cCol == currentCol + 1 || cCol == currentCol - 1)) {
                	int targetCol = cCol;
                	if (willKingBeInCheck(pawn, nextRow1, targetCol, panneau)) {
                		continue;
                	}
                	final int targetRow1 = nextRow1;
                	final int targetRow2 = nextRow2;
                	//Prise d'un pion par un pion
                	if (c instanceof Pawn) {
                		Pawn otherPawn = (Pawn) c;
                		if (!otherPawn.getColor().equals(pawn.getColor())) {
                			captureChoice = new Choice();
                			captureChoice.setBounds(targetCol * 100, nextRow1 * 100, 100, 100);
                			captureChoice.setBorderPainted(false);
                			captureChoice.setContentAreaFilled(false);
                			captureChoice.setFocusPainted(false);
                			captureChoice.setEnabled(true);
                			captureChoice.setVisible(true);
                			panneau.add(captureChoice, 0);
                			
                			captureChoice.addActionListener(new ActionListener() {
                				public void actionPerformed(ActionEvent e) {
                					panneau.remove(otherPawn);
                					pawn.setBounds(targetCol * 100, targetRow1 * 100, 100, 100);
                					
                					String from = coordToNotation(pawnRow[0], pawnCol[0]);
        	                		String to = coordToNotation(targetRow1, targetCol);        		
        	                		String move = "P:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
                					pawnRow[0] = targetRow1;
                					pawnCol[0] = targetCol;
                					pawn.sethasMoved(true);
                					
                					if (otherPawn.getColor().equals("white")) {
                						joueur2Score += otherPawn.getValue();
                						joueur1Score -= otherPawn.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherPawn.getValue();
                						joueur2Score -= otherPawn.getValue();
                						updateScoreLabels();
                					}
                					
                					clearSelections(panneau);
                                    
                                    gameState.switchTurn();
                                    gameState.resetLastDoubleStepPawn();
                                    selectedActivation(panneau);
                                    
                                    addLegalMoves(panneau);
                				}
                			});
                		}
                	}
                	
                	// Prise d'un fou par un pion
                	else if (c instanceof Bishop) {
                		Bishop otherBishop = (Bishop) c;
                		if (!otherBishop.getColor().equals(pawn.getColor())) {
                			captureChoice = new Choice();
                			captureChoice.setBounds(targetCol * 100, nextRow1 * 100, 100, 100);
                			captureChoice.setBorderPainted(false);
                			captureChoice.setContentAreaFilled(false);
                			captureChoice.setFocusPainted(false);
                			captureChoice.setEnabled(true);
                			captureChoice.setVisible(true);
                			panneau.add(captureChoice, 0);
                			
                			captureChoice.addActionListener(new ActionListener() {
                				public void actionPerformed(ActionEvent e) {
                					panneau.remove(otherBishop);
                					pawn.setBounds(targetCol * 100, targetRow1 * 100, 100, 100);
                					
                					String from = coordToNotation(pawnRow[0], pawnCol[0]);
        	                		String to = coordToNotation(targetRow1, targetCol);        		
        	                		String move = "P:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
                					pawnRow[0] = targetRow1;
                					pawnCol[0] = targetCol;
                					pawn.sethasMoved(true);
                					
                					if (otherBishop.getColor().equals("white")) {
                						joueur2Score += otherBishop.getValue();
                						joueur1Score -= otherBishop.getValue();
                						updateScoreLabels();
                					}else {
                						joueur1Score += otherBishop.getValue();
                						joueur2Score -= otherBishop.getValue();
                						updateScoreLabels();
                					}
                					
                					//Promotion
                			        promotion(pawn, color, pawnRow[0], pawnCol[0], panneau);
                			        
                					clearSelections(panneau);
                                    
                                    gameState.switchTurn();
                                    gameState.resetLastDoubleStepPawn();
                                    selectedActivation(panneau);
                                    
                                    addLegalMoves(panneau);
                				}
                			});
                		}
                	}
                	
                	// Prise d'un cavalier par un pion
                	else if (c instanceof Knight) {
                		Knight otherKnight = (Knight) c;
                		if (!otherKnight.getColor().equals(pawn.getColor())) {
                			captureChoice = new Choice();
                			captureChoice.setBounds(targetCol * 100, nextRow1 * 100, 100, 100);
                			captureChoice.setBorderPainted(false);
                			captureChoice.setContentAreaFilled(false);
                			captureChoice.setFocusPainted(false);
                			captureChoice.setEnabled(true);
                			captureChoice.setVisible(true);
                			panneau.add(captureChoice, 0);
                			
                			captureChoice.addActionListener(new ActionListener() {
                				public void actionPerformed(ActionEvent e) {
                					panneau.remove(otherKnight);
                					pawn.setBounds(targetCol * 100, targetRow1 * 100, 100, 100);
                					
                					String from = coordToNotation(pawnRow[0], pawnCol[0]);
        	                		String to = coordToNotation(targetRow1, targetCol);        		
        	                		String move = "P:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
                					pawnRow[0] = targetRow1;
                					pawnCol[0] = targetCol;
                					pawn.sethasMoved(true);
                					
                					if (otherKnight.getColor().equals("white")) {
                						joueur2Score += otherKnight.getValue();
                						joueur1Score -= otherKnight.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherKnight.getValue();
                						joueur2Score -= otherKnight.getValue();
                						updateScoreLabels();
                					}
                					
                					//Promotion
                			        promotion(pawn, color, pawnRow[0], pawnCol[0], panneau);
                			        
                					clearSelections(panneau);
                                    
                                    gameState.switchTurn();
                                    gameState.resetLastDoubleStepPawn();
                                    selectedActivation(panneau);
                                    
                                    addLegalMoves(panneau);
                				}
                			});
                		}
                	}
                	
                	// Prise d'une tour par un pion
                	else if (c instanceof Rook) {
                		Rook otherRook = (Rook) c;
                		if (!otherRook.getColor().equals(pawn.getColor())) {
                			captureChoice = new Choice();
                			captureChoice.setBounds(targetCol * 100, nextRow1 * 100, 100, 100);
                			captureChoice.setBorderPainted(false);
                			captureChoice.setContentAreaFilled(false);
                			captureChoice.setFocusPainted(false);
                			captureChoice.setEnabled(true);
                			captureChoice.setVisible(true);
                			panneau.add(captureChoice, 0);
                			
                			captureChoice.addActionListener(new ActionListener() {
                				public void actionPerformed(ActionEvent e) {
                					panneau.remove(otherRook);
                					pawn.setBounds(targetCol * 100, targetRow1 * 100, 100, 100);
                					
                					String from = coordToNotation(pawnRow[0], pawnCol[0]);
        	                		String to = coordToNotation(targetRow1, targetCol);        		
        	                		String move = "P:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
                					pawnRow[0] = targetRow1;
                					pawnCol[0] = targetCol;
                					pawn.sethasMoved(true);
                					
                					if (otherRook.getColor().equals("white")) {
                						joueur2Score += otherRook.getValue();
                						joueur1Score -= otherRook.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherRook.getValue();
                						joueur2Score -= otherRook.getValue();
                						updateScoreLabels();
                					}
                					
                					//Promotion
                			        promotion(pawn, color, pawnRow[0], pawnCol[0], panneau);
                			        
                					clearSelections(panneau);
                                    
                                    gameState.switchTurn();
                                    gameState.resetLastDoubleStepPawn();
                                    selectedActivation(panneau);
                                    
                                    addLegalMoves(panneau);
                				}
                			});
                		}
                	}
                	
                	// Prise d'un reine par un pion
                	else if (c instanceof Queen) {
                		Queen otherQueen = (Queen) c;
                		if (!otherQueen.getColor().equals(pawn.getColor())) {
                			captureChoice = new Choice();
                			captureChoice.setBounds(targetCol * 100, nextRow1 * 100, 100, 100);
                			captureChoice.setBorderPainted(false);
                			captureChoice.setContentAreaFilled(false);
                			captureChoice.setFocusPainted(false);
                			captureChoice.setEnabled(true);
                			captureChoice.setVisible(true);
                			panneau.add(captureChoice, 0);
                			
                			captureChoice.addActionListener(new ActionListener() {
                				public void actionPerformed(ActionEvent e) {
                					panneau.remove(otherQueen);
                					pawn.setBounds(targetCol * 100, targetRow1 * 100, 100, 100);
                					
                					String from = coordToNotation(pawnRow[0], pawnCol[0]);
        	                		String to = coordToNotation(targetRow1, targetCol);        		
        	                		String move = "P:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
                					pawnRow[0] = targetRow1;
                					pawnCol[0] = targetCol;
                					pawn.sethasMoved(true);
                					
                					if (otherQueen.getColor().equals("white")) {
                						joueur2Score += otherQueen.getValue();
                						joueur1Score -= otherQueen.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherQueen.getValue();
                						joueur2Score -= otherQueen.getValue();
                						updateScoreLabels();
                					}
                					
                					//Promotion
                			        promotion(pawn, color, pawnRow[0], pawnCol[0], panneau);
                			        
                					clearSelections(panneau);
                                    
                                    gameState.switchTurn();
                                    gameState.resetLastDoubleStepPawn();
                                    selectedActivation(panneau);
                                    
                                    addLegalMoves(panneau);
                				}
                			});
                		}
                	}
                }
                
                // En Passant
                if (cRow == currentRow && (cCol == currentCol + 1 || cCol == currentCol - 1)) {
                	int targetCol = cCol;
                	if (willKingBeInCheck(pawn, nextRow1, targetCol, panneau)) {
                		continue;
                	}
                	
                	final int targetRow1 = nextRow1;
                	final int targetRow2 = nextRow2;
                	if (c instanceof Pawn) {
                		Pawn otherPawn = (Pawn) c;
                		if (!otherPawn.getColor().equals(pawn.getColor()) && gameState.isLastDoubleStepPawn(cRow, cCol)) {
                			captureChoice = new Choice();
                			captureChoice.setBounds(targetCol * 100, nextRow1 * 100, 100, 100);
                			captureChoice.setBorderPainted(false);
                			captureChoice.setContentAreaFilled(false);
                			captureChoice.setFocusPainted(false);
                			captureChoice.setEnabled(true);
                			captureChoice.setVisible(true);
                			panneau.add(captureChoice, 0);
                			
                			captureChoice.addActionListener(new ActionListener() {
                				public void actionPerformed(ActionEvent e) {
                					panneau.remove(otherPawn);
                					pawn.setBounds(targetCol * 100, targetRow1 * 100, 100, 100);
                					
                					String from = coordToNotation(pawnRow[0], pawnCol[0]);
        	                		String to = coordToNotation(targetRow1, targetCol);        		
        	                		String move = "P:" + from + "x" + to + " e.p.";
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
                					pawnRow[0] = targetRow1;
                					pawnCol[0] = targetCol;
                					pawn.sethasMoved(true);
                					
                					if (otherPawn.getColor().equals("white")) {
                						joueur2Score += otherPawn.getValue();
                						joueur1Score -= otherPawn.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherPawn.getValue();
                						joueur2Score -= otherPawn.getValue();
                						updateScoreLabels();
                					}
                					
                					clearSelections(panneau);
                                    
                                    gameState.switchTurn();
                                    gameState.resetLastDoubleStepPawn();
                                    selectedActivation(panneau);
                                    
                                    addLegalMoves(panneau);
                				}
                			});
                		}
                	}
                }
            }
        }
        
     // Créer Choice 1 seulement si la case devant est libre
        if (!isNextRow1Occupied) {
        	final int targetRow1 = nextRow1;
        	final int targetRow2 = nextRow2;
        	if (isKingInCheck(pawn.getColor(), panneau)) {
        		if (!willKingBeInCheck(pawn, nextRow1, currentCol, panneau)) {
    	            selectedPieceChoice = new Choice();
    	            selectedPieceChoice.setBounds(currentCol * 100, nextRow1 * 100, 100, 100);
    	            selectedPieceChoice.setBorderPainted(false);
    	            selectedPieceChoice.setContentAreaFilled(false);
    	            selectedPieceChoice.setFocusPainted(false);
    	            selectedPieceChoice.setEnabled(true);
    	            selectedPieceChoice.setVisible(true);
    	            panneau.add(selectedPieceChoice);
    	
    	            // Action sur le Choice 1
    	            selectedPieceChoice.addActionListener(new ActionListener() {
    	                public void actionPerformed(ActionEvent e) {
    	                    pawn.setBounds(currentCol * 100, targetRow1 * 100, 100, 100);
    	                    
    	                    String from = coordToNotation(pawnRow[0], pawnCol[0]);
	                		String to = coordToNotation(targetRow1, currentCol);        		
	                		String move = "P:" + from + "->" + to;
	                		
	                		
	                		System.out.println(move);
	                		saveMove(gameId, joueur, move);
	                		
	                		try {
								sendMove( String.valueOf(gameId), move, joueur);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
	                		
    	                    pawnRow[0] = targetRow1;
    	                    pawnCol[0] = currentCol;
        					pawn.sethasMoved(true);
    	
    				        //Promotion
    				        promotion(pawn, color, pawnRow[0], pawnCol[0], panneau);
    	
    	                    clearSelections(panneau);
    	                    
    	                    gameState.switchTurn();
    	                    gameState.resetLastDoubleStepPawn();
    	                    selectedActivation(panneau);
    	                    
    	                    addLegalMoves(panneau);
    	                }
    	            });
        		}
	            // Créer Choice 2 si le pion na pas encore bougé et la case 2 en avant est libre
        		else if (!willKingBeInCheck(pawn, nextRow2, currentCol, panneau)) {	
        			if (!pawn.hasMoved() && !isNextRow2Occupied) {
            	
	            		selectedPieceChoice2 = new Choice();
		                selectedPieceChoice2.setBounds(currentCol * 100, nextRow2 * 100, 100, 100);
		                selectedPieceChoice2.setBorderPainted(false);
		                selectedPieceChoice2.setContentAreaFilled(false);
		                selectedPieceChoice2.setFocusPainted(false);
		                selectedPieceChoice2.setEnabled(true);
		                selectedPieceChoice2.setVisible(true);
		                panneau.add(selectedPieceChoice2);
		
		                // Action sur le Choice 2
		                selectedPieceChoice2.addActionListener(new ActionListener() {
		                    public void actionPerformed(ActionEvent e) {
		                        pawn.setBounds(currentCol * 100, targetRow2 * 100, 100, 100);
		                        
		                        String from = coordToNotation(pawnRow[0], pawnCol[0]);
		                		String to = coordToNotation(targetRow2, currentCol);        		
		                		String move = "P:" + from + "->" + to;
		                		
		                		
		                		System.out.println(move);
		                		saveMove(gameId, joueur, move);
		                		
		                		try {
									sendMove( String.valueOf(gameId), move, joueur);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
		                		
		                        gameState.setLastDoubleStepPawn(targetRow2, currentCol);
		                        pawnRow[0] = targetRow2;
		                        pawnCol[0] = currentCol;
            					pawn.sethasMoved(true);
		
		                        clearSelections(panneau);
		                        
		                        gameState.switchTurn();
		                        selectedActivation(panneau);
		                        
		                        addLegalMoves(panneau);
		                    }
		                });
	            	}
	            }
        	} else {
	            selectedPieceChoice = new Choice();
	            selectedPieceChoice.setBounds(currentCol * 100, nextRow1 * 100, 100, 100);
	            selectedPieceChoice.setBorderPainted(false);
	            selectedPieceChoice.setContentAreaFilled(false);
	            selectedPieceChoice.setFocusPainted(false);
	            selectedPieceChoice.setEnabled(true);
	            selectedPieceChoice.setVisible(true);
	            panneau.add(selectedPieceChoice);
	
	            // Action sur le Choice 1
	            selectedPieceChoice.addActionListener(new ActionListener() {
	                public void actionPerformed(ActionEvent e) {
	                    pawn.setBounds(currentCol * 100, targetRow1 * 100, 100, 100);
	                    
	                    String from = coordToNotation(pawnRow[0], pawnCol[0]);
                		String to = coordToNotation(targetRow1, currentCol);        		
                		String move = "P:" + from + "->" + to;
                		
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
                		
                		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                		
	                    pawnRow[0] = targetRow1;
	                    pawnCol[0] = currentCol;
    					pawn.sethasMoved(true);
    					
				        //Promotion
				        promotion(pawn, color, pawnRow[0], pawnCol[0], panneau);
	
	                    clearSelections(panneau);
	                    
	                    gameState.switchTurn();
	                    gameState.resetLastDoubleStepPawn();
	                    selectedActivation(panneau);
	                    
	                    addLegalMoves(panneau);
	                }
	            });
	
	            // Créer Choice 2 si le pion na pas encore bougé et la case 2 en avant est libre
	            if (!pawn.hasMoved() && !isNextRow2Occupied) {
            		selectedPieceChoice2 = new Choice();
	                selectedPieceChoice2.setBounds(currentCol * 100, nextRow2 * 100, 100, 100);
	                selectedPieceChoice2.setBorderPainted(false);
	                selectedPieceChoice2.setContentAreaFilled(false);
	                selectedPieceChoice2.setFocusPainted(false);
	                selectedPieceChoice2.setEnabled(true);
	                selectedPieceChoice2.setVisible(true);
	                panneau.add(selectedPieceChoice2);
	
	                // Action sur le Choice 2
	                selectedPieceChoice2.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent e) {
	                        pawn.setBounds(currentCol * 100, targetRow2 * 100, 100, 100);
	                        
	                        String from = coordToNotation(pawnRow[0], pawnCol[0]);
	                		String to = coordToNotation(targetRow2, currentCol);        		
	                		String move = "P:" + from + "->" + to;
	                		
	                		
	                		System.out.println(move);
	                		saveMove(gameId, joueur, move);
	                		
	                		try {
								sendMove( String.valueOf(gameId), move, joueur);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
	                		
	                        gameState.setLastDoubleStepPawn(targetRow2, currentCol);
	                        pawnRow[0] = targetRow2;
	                        pawnCol[0] = currentCol;
        					pawn.sethasMoved(true);
	
	                        clearSelections(panneau);
	                        
	                        gameState.switchTurn();
	                        selectedActivation(panneau);
	                        
	                        addLegalMoves(panneau);
	                    }
	                });
	            	
	            }
            	
        	}
        }
	}
	
	//Promotion
	public void promotion(Pawn pawn, String color, int pawnRow, int pawnCol, JPanel panneau) {
		if (Menu2.color == "white") {
			if((color == "white" && pawnRow == 0) || (color == "black" && pawnRow == 7)) {
	        	panneau.remove(pawn);
	        	String[] choixPromotion = {"Reine", "Tour", "Fou", "Cavalier"};
	        	int choix = JOptionPane.showOptionDialog(
	        		    null,
	        		    "Choisissez une pièce pour la promotion :",
	        		    "Promotion du pion",
	        		    JOptionPane.DEFAULT_OPTION,
	        		    JOptionPane.PLAIN_MESSAGE,
	        		    null,
	        		    choixPromotion,
	        		    choixPromotion[0]
	        		);
	        	
	        	switch (choix) {
	            case 0: // Reine
	            	addQueen(pawnRow, pawnCol + 1, color, panneau);
	            	if (pawn.getColor().equals("black")) {
	            		String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
                		
	            		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	            		
						joueur2Score += 8;
						joueur1Score -= 8;
						updateScoreLabels();
					}else {
						String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
                		
						try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						joueur1Score += 8;
						joueur2Score -= 8;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            case 1: // Tour
	            	addRook(pawnRow, pawnCol + 1, color, panneau); 
	            	if (pawn.getColor().equals("black")) {
	            		String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
	            		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	            		
	            		joueur2Score += 5;
						joueur1Score -= 5;
						updateScoreLabels();
					}else {
						String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
						try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						joueur1Score += 5;
						joueur2Score -= 5;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            case 2: // Fou
	            	addBishop(pawnRow, pawnCol + 1, color, panneau);
	            	if (pawn.getColor().equals("black")) {
	            		String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
	            		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	            		
						joueur2Score += 3;
						joueur1Score -= 3;
				        updateScoreLabels();
					}else {
						String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
						try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						joueur1Score += 3;
						joueur2Score -= 3;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            case 3: // Cavalier
	            	addKnight(pawnRow, pawnCol + 1, color, panneau); 
	            	if (pawn.getColor().equals("black")) {
	            		String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
	            		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	            		
	            		joueur2Score += 3;
						joueur1Score -= 3;
				        updateScoreLabels();
					}else {
						String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
						try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						joueur1Score += 3;
						joueur2Score -= 3;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            default:
	                // Annulé ou fermé, mets Reine par défaut
	            	addQueen(pawnRow, pawnCol + 1, color, panneau);
	            	if (pawn.getColor().equals("black")) {
	            		String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
	            		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
	            		
						joueur2Score += 8;
						joueur1Score -= 8;
						updateScoreLabels();
					}else {
						String from = coordToNotation(pawnRow, pawnCol);
                		String to = coordToNotation(pawnRow,  pawnCol + 1);        		
                		String move = "P:" + from + "->" + to;
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
						try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						
						joueur1Score += 8;
						joueur2Score -= 8;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
	            	break;
	        	}
	        	
	        }
		}
		else if (Menu2.color == "black") {
			if((color == "white" && pawnRow == 7) || (color == "black" && pawnRow == 0)) {
	        	panneau.remove(pawn);
	        	String[] choixPromotion = {"Reine", "Tour", "Fou", "Cavalier"};
	        	int choix = JOptionPane.showOptionDialog(
	        		    null,
	        		    "Choisissez une pièce pour la promotion :",
	        		    "Promotion du pion",
	        		    JOptionPane.DEFAULT_OPTION,
	        		    JOptionPane.PLAIN_MESSAGE,
	        		    null,
	        		    choixPromotion,
	        		    choixPromotion[0]
	        		);
	        	
	        	switch (choix) {
	            case 0: // Reine
	            	addQueen(pawnRow, pawnCol + 1, color, panneau);
	            	if (pawn.getColor().equals("black")) {
						joueur2Score += 8;
						joueur1Score -= 8;
						updateScoreLabels();
					}else {
						joueur1Score += 8;
						joueur2Score -= 8;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            case 1: // Tour
	            	addRook(pawnRow, pawnCol + 1, color, panneau); 
	            	if (pawn.getColor().equals("black")) {
	            		joueur2Score += 5;
						joueur1Score -= 5;
						updateScoreLabels();
					}else {
						joueur1Score += 5;
						joueur2Score -= 5;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            case 2: // Fou
	            	addBishop(pawnRow, pawnCol + 1, color, panneau);
	            	if (pawn.getColor().equals("black")) {
						joueur2Score += 3;
						joueur1Score -= 3;
				        updateScoreLabels();
					}else {
						joueur1Score += 3;
						joueur2Score -= 3;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            case 3: // Cavalier
	            	addKnight(pawnRow, pawnCol + 1, color, panneau); 
	            	if (pawn.getColor().equals("black")) {
	            		joueur2Score += 3;
						joueur1Score -= 3;
				        updateScoreLabels();
					}else {
						joueur1Score += 3;
						joueur2Score -= 3;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
					break;
	            default:
	                // Annulé ou fermé, mets Reine par défaut
	            	addQueen(pawnRow, pawnCol + 1, color, panneau);
	            	if (pawn.getColor().equals("black")) {
						joueur2Score += 8;
						joueur1Score -= 8;
						updateScoreLabels();
					}else {
						joueur1Score += 8;
						joueur2Score -= 8;
						updateScoreLabels();
					}
	            	
	            	addLegalMoves(panneau);
	            	break;
	        	}
	        	
	        }
		}
        
	}
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void selectedActivation(JPanel panneau) {
	    for (Component c : panneau.getComponents()) {
	        c.setEnabled(true);
	    }
	}
	private void clearSelections(JPanel panneau) {
	    if (selectedPiece != null) {
	        selectedPiece.setBorderPainted(false);
	        selectedPiece.setContentAreaFilled(false);
	        selectedPiece.setBackground(null);
	        selectedPiece = null;
	    }

	    // Supprime tous les Choice (déplacement ou capture)
	    for (Component comp : panneau.getComponents()) {
	        if (comp instanceof Choice) {
	            panneau.remove(comp);
	        }
	    }

	    selectedPieceChoice = null;
	    selectedPieceChoice2 = null;
	    captureChoice = null;

	    panneau.repaint();
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	private void addBishop(int ligne, int colonne , String color, JPanel panneau) {
		Bishop bishop = new Bishop(color,ligne,colonne);
	    bishop.setBorderPainted(false);
	    bishop.setContentAreaFilled(false);
	    bishop.setFocusPainted(false);
	    bishop.setEnabled(true);
	    final int[] bishopRow = {ligne};
	    final int[] bishopCol = {colonne - 1};
	    
	    bishop.addActionListener(new ActionListener() {
	        public void actionPerformed(ActionEvent e) {
	        	if (selectedPiece == bishop) {
	        		clearSelections(panneau);
	        	} else {
	        		clearSelections(panneau);
	        		
	        		selectedPiece = bishop;
	                bishop.setBorderPainted(true);
	                bishop.setContentAreaFilled(true);
	                bishop.setBackground(Color.CYAN);
	                
	                if (!bishop.getColor().equals(gameState.getCurrentPlayerColor())) {
	                    // Si ce n'est pas au tour de cette couleur, on ne montre pas les déplacements.
	                	selectedPieceChoice = null;
                        selectedPieceChoice2 = null;
                        captureChoice = null;
                        panneau.repaint();
	                    return;
	                }
	                
	                for (Component c : panneau.getComponents()) {
	                	if (c instanceof ColoredPiece) {
	                		ColoredPiece piece = (ColoredPiece) c;
	                		if (!Menu2.trainingMode && !piece.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	                    // Pas le bon tour, on ignore
	    	                    return;
	    	                }

	    	                showBishopMoves(bishop,bishopRow,bishopCol,panneau);
	                	}
	                }
	        	}

	        }
	    });
	    
	    panneau.add(bishop);
	}
	
	private void showBishopMoves (Bishop bishop, int[] bishopRow, int[] bishopCol, JPanel panneau) {
        int[][] directions = {
        	    {-1, -1}, // haut gauche
        	    {-1,  1}, // haut droite
        	    { 1, -1}, // bas gauche
        	    { 1,  1}  // bas droite
        	};

        LongDistancePieceMoves (bishop, bishopRow, bishopCol, directions, panneau);

    	panneau.repaint();
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	private void addKnight(int ligne, int colonne , String color, JPanel panneau) {
		Knight knight = new Knight(color,ligne,colonne);
		knight.setBorderPainted(false);
		knight.setContentAreaFilled(false);
		knight.setFocusPainted(false);
		knight.setEnabled(true);
	    final int[] knightRow = {ligne};
	    final int[] knightCol = {colonne - 1};
	    
	    knight.addActionListener(new ActionListener(){
	    	public void actionPerformed (ActionEvent e) {
	    		if (selectedPiece == knight) {
	    			clearSelections(panneau);
	    		} else {
	    			clearSelections(panneau);
	    			
	    			selectedPiece = knight;
	    			knight.setBorderPainted(true);
	    			knight.setContentAreaFilled(true);
	    			knight.setBackground(Color.CYAN);
	    			
	    			if (!knight.getColor().equals(gameState.getCurrentPlayerColor())) {
	                    // Si ce n'est pas au tour de cette couleur, on ne montre pas les déplacements.
	                	selectedPieceChoice = null;
                        selectedPieceChoice2 = null;
                        captureChoice = null;
                        panneau.repaint();
	                    return;
	                }
	                
	    			for (Component c : panneau.getComponents()) {
	                	if (c instanceof ColoredPiece) {
	                		ColoredPiece piece = (ColoredPiece) c;
	                		if (!Menu2.trainingMode && !piece.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	                    // Pas le bon tour, on ignore
	    	                    return;
	    	                }
	                		
	    	                showKnightMoves(knight,knightRow,knightCol,panneau);
	                	}
	                }
	    		}
	    	}
	    });
	    panneau.add(knight);
	}
	
	private void showKnightMoves(Knight knight, int[] knightRow, int[] knightCol, JPanel panneau) {
		int currentRow = knightRow[0];
		int currentCol = knightCol[0];
		
		int[][] directions = {
				{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
			    { 1, -2}, { 1, 2}, { 2, -1}, { 2, 1}
        	};

            	for (int[] dir : directions) {
            		int nextRow = currentRow + dir[0];
            		int nextCol = currentCol + dir[1];
            		
            		if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) continue;
            		            		
                    if (willKingBeInCheck(knight, nextRow, nextCol, panneau)) {
                        continue;
                    }
                    
                    knightMoves(knight, knightRow, knightCol, nextRow, nextCol, panneau);
            	}
    	panneau.repaint();
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	public void addRook (int ligne, int colonne , String color, JPanel panneau) {
		Rook rook = new Rook(color,ligne,colonne);
		rook.setBorderPainted(false);
		rook.setContentAreaFilled(false);
		rook.setFocusPainted(false);
		rook.setEnabled(true);
	    final int[] rookRow = {ligne};
	    final int[] rookCol = {colonne - 1};
	    
	    rook.addActionListener(new ActionListener(){
	    	public void actionPerformed (ActionEvent e) {
	    		if (selectedPiece == rook) {
	    			clearSelections(panneau);
	    		} else {
	    			clearSelections(panneau);
	    			
	    			selectedPiece = rook;
	    			rook.setBorderPainted(true);
	    			rook.setContentAreaFilled(true);
	    			rook.setBackground(Color.CYAN);
	    			
	    			if (!rook.getColor().equals(gameState.getCurrentPlayerColor())) {
	                    // Si ce n'est pas au tour de cette couleur, on ne montre pas les déplacements.
	                	selectedPieceChoice = null;
                        selectedPieceChoice2 = null;
                        captureChoice = null;
                        panneau.repaint();
	                    return;
	                }
	    			
	    			for (Component c : panneau.getComponents()) {
	                	if (c instanceof ColoredPiece) {
	                		ColoredPiece piece = (ColoredPiece) c;
	                		if (!Menu2.trainingMode && !piece.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	                    // Pas le bon tour, on ignore
	    	                    return;
	    	                }

	    	                showRookMoves(rook,rookRow,rookCol,panneau);
	                	}
	                }
	    		}
	    	}
	    });
	    panneau.add(rook);
	}
	
	private void showRookMoves (Rook rook, int[] rookRow, int[] rookCol, JPanel panneau) {
        
        int[][] directions = {
        	    {-1, 0}, // haut
        	    {0,  1}, // droite
        	    {0, -1}, // gauche
        	    {1,  0}  // bas
        	};
        LongDistancePieceMoves (rook, rookRow, rookCol, directions, panneau);

    	panneau.repaint();
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	public void addQueen (int ligne, int colonne , String color, JPanel panneau) {
		Queen queen = new Queen(color,ligne,colonne);
		queen.setBorderPainted(false);
		queen.setContentAreaFilled(false);
		queen.setFocusPainted(false);
		queen.setEnabled(true);
	    final int[] queenRow = {ligne};
	    final int[] queenCol = {colonne - 1};
	    
	    queen.addActionListener(new ActionListener(){
	    	public void actionPerformed (ActionEvent e) {
	    		if (selectedPiece == queen) {
	    			clearSelections(panneau);
	    		} else {
	    			clearSelections(panneau);
	    			
	    			selectedPiece = queen;
	    			queen.setBorderPainted(true);
	    			queen.setContentAreaFilled(true);
	    			queen.setBackground(Color.CYAN);
	    			
	    			if (!queen.getColor().equals(gameState.getCurrentPlayerColor())) {
	                    // Si ce n'est pas au tour de cette couleur, on ne montre pas les déplacements.
	                	selectedPieceChoice = null;
                        selectedPieceChoice2 = null;
                        captureChoice = null;
                        panneau.repaint();
	                    return;
	                }
	                
	    			for (Component c : panneau.getComponents()) {
	                	if (c instanceof ColoredPiece) {
	                		ColoredPiece piece = (ColoredPiece) c;
	                		if (!Menu2.trainingMode && !piece.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	                    // Pas le bon tour, on ignore
	    	                    return;
	    	                }
	                		
	    	                showQueenMoves(queen,queenRow,queenCol,panneau);
	                	}
	                }
	                
	    		}
	    	}
	    });
	    panneau.add(queen);
	}
	
	private void showQueenMoves(Queen queen, int[] queenRow, int[] queenCol, JPanel panneau) {
        
        int[][] directions = {
        	    {-1, 0}, // haut
        	    {0,  1}, // droite
        	    {0, -1}, // gauche
        	    {1,  0}, // bas
        	    {-1, -1}, // haut gauche
        	    {-1,  1}, // haut droite
        	    { 1, -1}, // bas gauche
        	    { 1,  1}  // bas droite
        	};
        LongDistancePieceMoves (queen, queenRow, queenCol, directions, panneau);

    	panneau.repaint();
	}
	
	
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	public void addKing (int ligne, int colonne , String color, JPanel panneau) {
		King king = new King(color,ligne,colonne);
		king.setBorderPainted(false);
		king.setContentAreaFilled(false);
		king.setFocusPainted(false);
		king.setEnabled(true);
	    final int[] kingRow = {ligne};
	    final int[] kingCol = {colonne - 1};
	    
	    king.addActionListener(new ActionListener(){
	    	public void actionPerformed (ActionEvent e) {
	    		if (selectedPiece == king) {
	    			clearSelections(panneau);
	    		} else {
	    			clearSelections(panneau);
	    			
	    			selectedPiece = king;
	    			king.setBorderPainted(true);
	    			king.setContentAreaFilled(true);
	    			king.setBackground(Color.CYAN);
	    			
	    			if (!king.getColor().equals(gameState.getCurrentPlayerColor())) {
	                    // Si ce n'est pas au tour de cette couleur, on ne montre pas les déplacements.
	                	selectedPieceChoice = null;
                        selectedPieceChoice2 = null;
                        captureChoice = null;
                        panneau.repaint();
	                    return;
	                }
	                
	    			for (Component c : panneau.getComponents()) {
	                	if (c instanceof ColoredPiece) {
	                		ColoredPiece piece = (ColoredPiece) c;
	                		if (!Menu2.trainingMode && !piece.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	                    // Pas le bon tour, on ignore
	    	                    return;
	    	                }

	    	                showKingMoves(king,kingRow,kingCol,panneau);
	                	}
	                }	                
	    		}
	    	}
	    });
	    
	    if (king.getColor().equals(gameState.getCurrentPlayerColor())) {
	    	for (Component c : panneau.getComponents()){
	    		if (c instanceof ColoredPiece) {
	    			
	    		}
	    	}
	    }
	    panneau.add(king);
	}
	
	private void showKingMoves(King king, int[] kingRow, int[] kingCol, JPanel panneau) {
		int currentRow = kingRow[0];
        int currentCol = kingCol[0];
        
        int[][] directions = {
        	    {-1, 0}, // haut
        	    {0,  1}, // droite
        	    {0, -1}, // gauche
        	    {1,  0}, // bas
        	    {-1, -1}, // haut gauche
        	    {-1,  1}, // haut droite
        	    { 1, -1}, // bas gauche
        	    { 1,  1}  // bas droite
        	};
        for (int[] dir : directions) {
    	    int nextRow = currentRow + dir[0];
    	    int nextCol = currentCol + dir[1];
    	    
    	    if (willKingBeInCheck(king, nextRow, nextCol, panneau)) {
    	    	continue;
    	    }
    	    
    	    boolean isLineAndDiagOccupied = false;
	        
	        for (Component c : panneau.getComponents()) {
	            if (c instanceof JButton && c != king) {
	                Rectangle r = c.getBounds();
	                int cRow = r.y / 100;
	                int cCol = r.x / 100;
	                
	                if (cRow == nextRow && cCol == nextCol) {
	                	isLineAndDiagOccupied = true;
	                		//prise d'un pion par un roi
		                	if (c instanceof Pawn) {
	    	                	Pawn otherPawn = (Pawn) c;
		                		if (!otherPawn.getColor().equals(king.getColor())) {
		                			captureChoice = new Choice();
		                			captureChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                			captureChoice.setBorderPainted(false);
		                			captureChoice.setContentAreaFilled(false);
		                			captureChoice.setFocusPainted(false);
		                			captureChoice.setEnabled(true);
		                			captureChoice.setVisible(true);
		                			panneau.add(captureChoice,0);
		                			
		                			captureChoice.addActionListener(new ActionListener () {
		                				public void actionPerformed (ActionEvent e) {
		                					panneau.remove(otherPawn);
		                					king.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                					
		                					String from = coordToNotation(kingRow[0], kingCol[0]);
		        	                		String to = coordToNotation(nextRow, nextCol);        		
		        	                		String move = "K:" + from + "x" + to;
		        	                		
		        	                		
		        	                		System.out.println(move);
		        	                		saveMove(gameId, joueur, move);
		        	                		
		                					kingRow[0] = nextRow;
		                					kingCol[0] = nextCol;
		                					king.sethasMoved(true);
		                					
		                					if (otherPawn.getColor().equals("white")) {
		                						joueur2Score += otherPawn.getValue();
		                						joueur1Score -= otherPawn.getValue();
		                				        updateScoreLabels();
		                					}else {
		                						joueur1Score += otherPawn.getValue();
		                						joueur2Score -= otherPawn.getValue();
		                						updateScoreLabels();
		                					}
		                					
		                					clearSelections(panneau);
		                	        		
		                	        		gameState.switchTurn();
		                                    gameState.resetLastDoubleStepPawn();
		                                    selectedActivation(panneau);
		                                    
		                                    addLegalMoves(panneau);
		                				}
		                			});
		                		}
		                	}
		                	
		                	//prise d'un fou par un roi
		                	if (c instanceof Bishop) {
	    	                	Bishop otherBishop = (Bishop) c;
		                		if (!otherBishop.getColor().equals(king.getColor())) {
		                			captureChoice = new Choice();
		                			captureChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                			captureChoice.setBorderPainted(false);
		                			captureChoice.setContentAreaFilled(false);
		                			captureChoice.setFocusPainted(false);
		                			captureChoice.setEnabled(true);
		                			captureChoice.setVisible(true);
		                			panneau.add(captureChoice,0);
		                			
		                			captureChoice.addActionListener(new ActionListener () {
		                				public void actionPerformed (ActionEvent e) {
		                					panneau.remove(otherBishop);
		                					king.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                					
		                					String from = coordToNotation(kingRow[0], kingCol[0]);
		        	                		String to = coordToNotation(nextRow, nextCol);        		
		        	                		String move = "K:" + from + "x" + to;
		        	                		
		        	                		
		        	                		System.out.println(move);
		        	                		saveMove(gameId, joueur, move);
		        	                		
		        	                		try {
												sendMove( String.valueOf(gameId), move, joueur);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
		        	                		
		                					kingRow[0] = nextRow;
		                					kingCol[0] = nextCol;
		                					king.sethasMoved(true);
		                					
		                					if (otherBishop.getColor().equals("white")) {
		                						joueur2Score += otherBishop.getValue();
		                						joueur1Score -= otherBishop.getValue();
		                						updateScoreLabels();
		                					}else {
		                						joueur1Score += otherBishop.getValue();
		                						joueur2Score -= otherBishop.getValue();
		                						updateScoreLabels();
		                					}
		                					
		                					clearSelections(panneau);
		                	        		
		                	        		gameState.switchTurn();
		                                    gameState.resetLastDoubleStepPawn();
		                                    selectedActivation(panneau);
		                                    
		                                    addLegalMoves(panneau);
		                				}
		                			});
		                		}
		                	}
		                	
		                	//prise d'un cavalier par un roi
		                	if (c instanceof Knight) {
	    	                	Knight otherKnight = (Knight) c;
		                		if (!otherKnight.getColor().equals(king.getColor())) {
		                			captureChoice = new Choice();
		                			captureChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                			captureChoice.setBorderPainted(false);
		                			captureChoice.setContentAreaFilled(false);
		                			captureChoice.setFocusPainted(false);
		                			captureChoice.setEnabled(true);
		                			captureChoice.setVisible(true);
		                			panneau.add(captureChoice,0);
		                			
		                			captureChoice.addActionListener(new ActionListener () {
		                				public void actionPerformed (ActionEvent e) {
		                					panneau.remove(otherKnight);
		                					king.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                					
		                					String from = coordToNotation(kingRow[0], kingCol[0]);
		        	                		String to = coordToNotation(nextRow, nextCol);        		
		        	                		String move = "K:" + from + "x" + to;
		        	                		
		        	                		
		        	                		System.out.println(move);
		        	                		saveMove(gameId, joueur, move);
		        	                		
		        	                		try {
												sendMove( String.valueOf(gameId), move, joueur);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
		        	                		
		                					kingRow[0] = nextRow;
		                					kingCol[0] = nextCol;
		                					king.sethasMoved(true);
		                					
		                					if (otherKnight.getColor().equals("white")) {
		                						joueur2Score += otherKnight.getValue();
		                						joueur1Score -= otherKnight.getValue();
		                				        updateScoreLabels();
		                					}else {
		                						joueur1Score += otherKnight.getValue();
		                						joueur2Score -= otherKnight.getValue();
		                						updateScoreLabels();
		                					}
		                					
		                					clearSelections(panneau);
		                	        		
		                	        		gameState.switchTurn();
		                                    gameState.resetLastDoubleStepPawn();
		                                    selectedActivation(panneau);
		                                    
		                                    addLegalMoves(panneau);
		                				}
		                			});
		                		}
		                	}
		                	
		                	//prise d'une tour par un roi
		                	if (c instanceof Rook) {
	    	                	Rook otherRook = (Rook) c;
		                		if (!otherRook.getColor().equals(king.getColor())) {
		                			captureChoice = new Choice();
		                			captureChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                			captureChoice.setBorderPainted(false);
		                			captureChoice.setContentAreaFilled(false);
		                			captureChoice.setFocusPainted(false);
		                			captureChoice.setEnabled(true);
		                			captureChoice.setVisible(true);
		                			panneau.add(captureChoice,0);
		                			
		                			captureChoice.addActionListener(new ActionListener () {
		                				public void actionPerformed (ActionEvent e) {
		                					panneau.remove(otherRook);
		                					king.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                					
		                					String from = coordToNotation(kingRow[0], kingCol[0]);
		        	                		String to = coordToNotation(nextRow, nextCol);        		
		        	                		String move = "K:" + from + "x" + to;
		        	                		
		        	                		
		        	                		System.out.println(move);
		        	                		saveMove(gameId, joueur, move);
		        	                		
		        	                		try {
												sendMove( String.valueOf(gameId), move, joueur);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
		        	                		
		                					kingRow[0] = nextRow;
		                					kingCol[0] = nextCol;
		                					king.sethasMoved(true);
		                					
		                					if (otherRook.getColor().equals("white")) {
		                						joueur2Score += otherRook.getValue();
		                						joueur1Score -= otherRook.getValue();
		                				        updateScoreLabels();
		                					}else {
		                						joueur1Score += otherRook.getValue();
		                						joueur2Score -= otherRook.getValue();
		                						updateScoreLabels();
		                					}
		                					
		                					clearSelections(panneau);
		                	        		
		                	        		gameState.switchTurn();
		                                    gameState.resetLastDoubleStepPawn();
		                                    selectedActivation(panneau);
		                                    
		                                    addLegalMoves(panneau);
		                				}
		                			});
		                		}
		                	}
		                	
		                	//prise d'une reine par un roi
		                	if (c instanceof Queen) {
		                		Queen otherqueen = (Queen) c;
		                		if (!otherqueen.getColor().equals(king.getColor())) {
		                			captureChoice = new Choice();
		                			captureChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                			captureChoice.setBorderPainted(false);
		                			captureChoice.setContentAreaFilled(false);
		                			captureChoice.setFocusPainted(false);
		                			captureChoice.setEnabled(true);
		                			captureChoice.setVisible(true);
		                			panneau.add(captureChoice,0);
		                			
		                			captureChoice.addActionListener(new ActionListener () {
		                				public void actionPerformed (ActionEvent e) {
		                					panneau.remove(otherqueen);
		                					king.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		                					
		                					String from = coordToNotation(kingRow[0], kingCol[0]);
		        	                		String to = coordToNotation(nextRow, nextCol);        		
		        	                		String move = "K:" + from + "x" + to;
		        	                		
		        	                		
		        	                		System.out.println(move);
		        	                		saveMove(gameId, joueur, move);
		        	                		
		        	                		try {
												sendMove( String.valueOf(gameId), move, joueur);
											} catch (IOException e1) {
												// TODO Auto-generated catch block
												e1.printStackTrace();
											}
		        	                		
		                					kingRow[0] = nextRow;
		                					kingCol[0] = nextCol;
		                					king.sethasMoved(true);
		                					
		                					if (otherqueen.getColor().equals("white")) {
		                						joueur2Score += otherqueen.getValue();
		                						joueur1Score -= otherqueen.getValue();
		                				        updateScoreLabels();
		                					}else {
		                						joueur1Score += otherqueen.getValue();
		                						joueur2Score -= otherqueen.getValue();
		                						updateScoreLabels();
		                					}
		                					
		                					clearSelections(panneau);
		                	        		
		                	        		gameState.switchTurn();
		                                    gameState.resetLastDoubleStepPawn();
		                                    selectedActivation(panneau);
		                                    
		                                    addLegalMoves(panneau);
		                				}
		                			});
		                		}
		                	}
	                	
	                	
	                	break;
	                }
	            }
	        }
	        
	        if (!isLineAndDiagOccupied) {
	        	selectedPieceChoice = new Choice();
	    	    selectedPieceChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
	    	    selectedPieceChoice.setBorderPainted(false);
	    	    selectedPieceChoice.setContentAreaFilled(false);
	    	    selectedPieceChoice.setFocusPainted(false);
	            selectedPieceChoice.setEnabled(true);
	            selectedPieceChoice.setVisible(true);
		        panneau.add(selectedPieceChoice);
	    	    
		        selectedPieceChoice.addActionListener(new ActionListener(){
		        	public void actionPerformed(ActionEvent e) {
		        		king.setBounds(nextCol * 100, nextRow * 100, 100, 100);
		        		
		        		String from = coordToNotation(kingRow[0], kingCol[0]);
                		String to = coordToNotation(nextRow, nextCol);        		
                		String move = "K:" + from + "->" + to;
                		
                		
                		System.out.println(move);
                		saveMove(gameId, joueur, move);
                		
                		try {
							sendMove( String.valueOf(gameId), move, joueur);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
                		
		        		kingRow[0] =nextRow;
		        		kingCol[0] = nextCol;
		        		king.sethasMoved(true);

		        		clearSelections(panneau);
		        		
		        		gameState.switchTurn();
	                    gameState.resetLastDoubleStepPawn();
	                    selectedActivation(panneau);
	                    
	                    addLegalMoves(panneau);
		        	}
		        });
	        }
	        
	        //Castle/Roque
	        for (Component c : panneau.getComponents()) {
	        	if (c instanceof Rook) {
	        		Rectangle r = c.getBounds();
	        		int rRow = r.y / 100;
	        		int rCol = r.x / 100;
	        		
	        		Rook rook = (Rook) c;
	        		//Vérifier que le roi et la tour sont de la même couleur, qu'ils n'ont pas bougés et qu'ils sont sur la même ligne
	        		if(rRow == kingRow[0] && !rook.hasMoved() && !king.hasMoved() && rook.getColor().equals(king.getColor())) {
	        			//Petit Roque
	        			if (rCol == 7) {
	        				boolean isEntreOccupied = false;
	        				for (int i = 1; i <= 2; i++) {
		        				int entre = kingCol[0] + i;
		        				
		        				// verifie qu'il n'y a rien entre le roi et la tour
		        				for (Component comp : panneau.getComponents()) {
		        					if (comp instanceof ColoredPiece) {
			        					ColoredPiece piece = (ColoredPiece) comp;
			        					int pieceRow = piece.getBounds().y / 100;
			        					int pieceCol = piece.getBounds().x /100;
			        					
			        					if(pieceRow == kingRow[0] && pieceCol == entre) {
			        						isEntreOccupied = true;
			        						break;
			        					}
			        				}
		        				}
		        				
		        				if (!isEntreOccupied && willKingBeInCheck(king, kingRow[0], entre, panneau)) {
		        					isEntreOccupied = true;
		        				}
		        				
		        			}
	        				
	        				if (!isEntreOccupied) {
	        					selectedPieceChoice = new Choice();
	        		    	    selectedPieceChoice.setBounds((kingCol[0] + 2) * 100, kingRow[0] * 100, 100, 100);
	        		    	    selectedPieceChoice.setBorderPainted(false);
	        		    	    selectedPieceChoice.setContentAreaFilled(false);
	        		    	    selectedPieceChoice.setFocusPainted(false);
	        		            selectedPieceChoice.setEnabled(true);
	        		            selectedPieceChoice.setVisible(true);
	        			        panneau.add(selectedPieceChoice);
	        		    	    
	        			        selectedPieceChoice.addActionListener(new ActionListener(){
	        			        	public void actionPerformed(ActionEvent e) {
	        			        		king.setBounds((kingCol[0] + 2) * 100, kingRow[0] * 100, 100, 100);
	        			        		       		
	        	                		String move = "Castel: O-O";
	        	                		
	        	                		
	        	                		System.out.println(move);
	        	                		saveMove(gameId, joueur, move);
	        	                		
	        	                		try {
											sendMove( String.valueOf(gameId), move, joueur);
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
	        	                		
	        			        		kingRow[0] = kingRow[0];
	        			        		kingCol[0] = kingCol[0] + 2;
	        			        		
	        			        		panneau.remove(rook);
	        			        		addRook(kingRow[0], kingCol[0], rook.getColor(), panneau);
	        			        		
	        			        		king.sethasMoved(true);
	        			        		rook.sethasMoved(true);
	        			        		
	        			        		clearSelections(panneau);
	        			        		
	        			        		gameState.switchTurn();
	        		                    gameState.resetLastDoubleStepPawn();
	        		                    selectedActivation(panneau);
	        		                    
	        		                    addLegalMoves(panneau);
	        			        	}
	        			        });
	        				}
	        			}
	        			
	        			//Grand Roque
	        			if (rCol == 0) {
	        				boolean isEntreOccupied = false;
	        				for (int i = 1; i <= 3; i++) {
		        				int entre = kingCol[0] - i;
		        				
		        				// verifie qu'il n'y a rien entre le roi et la tour
		        				for (Component comp : panneau.getComponents()) {
		        					if (comp instanceof ColoredPiece) {
			        					ColoredPiece piece = (ColoredPiece) comp;
			        					int pieceRow = piece.getBounds().y / 100;
			        					int pieceCol = piece.getBounds().x /100;
			        					
			        					if(pieceRow == kingRow[0] && pieceCol == entre) {
			        						isEntreOccupied = true;
			        						break;
			        					}
			        				}
		        				}
		        				
		        				if (!isEntreOccupied && willKingBeInCheck(king, kingRow[0], entre + 1, panneau)) {
		        					isEntreOccupied = true;
		        				}
		        			}

	        				if (!isEntreOccupied) {
	        					selectedPieceChoice = new Choice();
	        		    	    selectedPieceChoice.setBounds((kingCol[0] - 2) * 100, kingRow[0] * 100, 100, 100);
	        		    	    selectedPieceChoice.setBorderPainted(false);
	        		    	    selectedPieceChoice.setContentAreaFilled(false);
	        		    	    selectedPieceChoice.setFocusPainted(false);
	        		            selectedPieceChoice.setEnabled(true);
	        		            selectedPieceChoice.setVisible(true);
	        			        panneau.add(selectedPieceChoice);
	        		    	    
	        			        selectedPieceChoice.addActionListener(new ActionListener(){
	        			        	public void actionPerformed(ActionEvent e) {
	        			        		king.setBounds((kingCol[0] - 2) * 100, kingRow[0] * 100, 100, 100);
	        			        		       		
	        	                		String move = "Castle: O-O-O";
	        	                		
	        	                		
	        	                		System.out.println(move);
	        	                		saveMove(gameId, joueur, move);
	        	                		
	        	                		try {
											sendMove( String.valueOf(gameId), move, joueur);
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();
										}
	        	                		
	        			        		kingRow[0] = kingRow[0];
	        			        		kingCol[0] = kingCol[0] - 2;
	        			        		
	        			        		panneau.remove(rook);
	        			        		addRook(kingRow[0], kingCol[0] + 2, rook.getColor(), panneau);
	        			        		
	        			        		king.sethasMoved(true);
	        			        		rook.sethasMoved(true);
	        			        		
	        			        		clearSelections(panneau);
	        			        		
	        			        		gameState.switchTurn();
	        		                    gameState.resetLastDoubleStepPawn();
	        		                    selectedActivation(panneau);
	        		                    
	        		                    addLegalMoves(panneau);
	        			        	}
	        			        });
	        				}
	        			}
	        		}
	        	}
	        }
        }
	}
	

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public boolean isKingInCheck(String color, JPanel panneau) {
	    int kingRow = -1;
	    int kingCol = -1;
	    King targetKing = null;

	    // 1. Trouver le roi de la couleur donnée
	    for (Component c : panneau.getComponents()) {
	        if (c instanceof King) {
	            King k = (King) c;
	            if (k.getColor().equals(color)) {
	                Rectangle r = k.getBounds();
	                kingRow = r.y / 100;
	                kingCol = r.x / 100;
	                targetKing = k;
	                break;
	            }
	        }
	    }

	    if (targetKing == null) return false;
	    
	    // 2. Parcourir les pièces adverses et vérifier si elles peuvent atteindre le roi
	    for (Component c : panneau.getComponents()) {
	        if (c instanceof ColoredPiece) {
	        	
	        	ColoredPiece piece = (ColoredPiece) c;

	            if (!piece.getColor().equals(color)) {
	                if (canKingBeReached(piece, kingRow, kingCol, panneau, piece.getColor())) {
	                	targetKing.setContentAreaFilled(true);
	                	targetKing.setBackground(Color.RED);
	                    return true;
	                }
	            }
	        }
	    }

	    targetKing.setContentAreaFilled(false);
	    return false; // Aucun danger détecté
	}
	//Verifie que les déplacements de la piece (allié) met son roi en echec
	public boolean willKingBeInCheck(ColoredPiece piece, int nextRow, int nextCol, JPanel panneau) {
		int oldX = piece.getBounds().x;
	    int oldY = piece.getBounds().y;
	    Component capturedPiece = null;
	    
	    // Vérifie s'il y a une pièce adverse sur la case cible
	    for (Component c : panneau.getComponents()) {
	        if (c.getBounds().x == nextCol * 100 && c.getBounds().y == nextRow * 100 && c instanceof ColoredPiece) {
	            ColoredPiece other = (ColoredPiece) c;
	            if (!other.getColor().equals(piece.getColor())) {
	                capturedPiece = c;
	                panneau.remove(c); // On "capture" la pièce
	                break;
	            }
	        }
	    }
	    
	    

	    // Déplace temporairement la pièce
	    piece.setBounds(nextCol * 100, nextRow * 100, 100, 100);
	    panneau.revalidate();
	    panneau.repaint();

	    // Pause pour forcer Swing à appliquer les changements (important en debug)
	    Toolkit.getDefaultToolkit().sync(); // synchrone pour forcer le rendu

	    // Vérifie si le roi est en échec
	    boolean exposesKing = isKingInCheck(piece.getColor(), panneau);

	    // On remet la pièce à sa position initiale
	    piece.setBounds(oldX, oldY, 100, 100);

	    // On restaure la pièce capturée si besoin
	    if (capturedPiece != null) {
	        panneau.add(capturedPiece);
	    }

	    panneau.revalidate();
	    panneau.repaint();

	    return exposesKing;
	}
	
	//Verifie que la piece (attaquant) peut atteindre les coordonées du roi
	public boolean canKingBeReached(ColoredPiece piece, int kingRow, int kingCol, JPanel panneau, String color) {
			Rectangle r = piece.getBounds();
			int pieceRow = r.y / 100;
			int pieceCol = r.x / 100;
			
			// Mise en echec par un pion
			if (piece instanceof Pawn) {
				int nextRow = color.equals("white") ? pieceRow - 1 : pieceRow + 1;
				if (nextRow == kingRow && (pieceCol + 1 == kingCol || pieceCol - 1 == kingCol)) {
					return true;
				}
			}
			
			//Mise en echec par un fou
			else if (piece instanceof Bishop) {
				int[][] directions = {
		        	    {-1, -1}, // haut gauche
		        	    {-1,  1}, // haut droite
		        	    { 1, -1}, // bas gauche
		        	    { 1,  1}  // bas droite
		        	};

            	for (int[] dir : directions) {
            	    int nextRow = pieceRow;
            	    int nextCol = pieceCol;

            	    while (true) {
            	        nextRow += dir[0];
            	        nextCol += dir[1];

            	        // Vérifie si on reste dans le plateau
            	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
            	            break;
            	        }
            	        
            	        // Vérifie s'il y a une pièce qui bloque le chemin
            	        boolean isBlocked = false;
            	        for (Component c : panneau.getComponents()) {
            	            Rectangle otherR = c.getBounds();
            	            int cRow = otherR.y / 100;
            	            int cCol = otherR.x / 100;

            	            if (cRow == nextRow && cCol == nextCol) {
            	                // Si cest le roi ciblé, retour true
            	                if (cRow == kingRow && cCol == kingCol && c instanceof King && !((ColoredPiece)c).getColor().equals(piece.getColor())) {
            	                    return true;
            	                } else {
            	                    isBlocked = true;
            	                    break;
            	                }
            	            }
            	        }

            	        if (isBlocked) break;

            	    }
            	}
			}
			
			//Mise en echec par un cavalier
			else if (piece instanceof Knight) {
				int[][] directions = {
						{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
					    { 1, -2}, { 1, 2}, { 2, -1}, { 2, 1}
		        	};

            	for (int[] dir : directions) {
            		int nextRow = pieceRow + dir[0];
            		int nextCol = pieceCol + dir[1];
            		
            		if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) continue;
            		
            		if ((nextRow == kingRow && nextCol == kingCol)) {
            			return true;
            		}
            	}
			}
			
			//Mise en echec par une tour
			else if (piece instanceof Rook) {
				int[][] directions = {
		        	    {-1, 0}, // haut
		        	    {0,  1}, // droite
		        	    {0, -1}, // gauche
		        	    {1,  0}  // bas
		        	};
				for (int[] dir : directions) {
		    	    int nextRow = pieceRow;
		    	    int nextCol = pieceCol;

		    	    while (true) {
		    	        nextRow += dir[0];
		    	        nextCol += dir[1];

		    	        // Vérifie si on reste dans le plateau
		    	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		    	            break;
		    	        }
		    	        
		    	     // Vérifie s'il y a une pièce qui bloque le chemin
		    	        boolean isBlocked = false;
		    	        for (Component c : panneau.getComponents()) {
		    	            Rectangle otherR = c.getBounds();
		    	            int cRow = otherR.y / 100;
		    	            int cCol = otherR.x / 100;

		    	            if (cRow == nextRow && cCol == nextCol) {
		    	                // Si cest le roi ciblé, retour true
		    	                if (cRow == kingRow && cCol == kingCol && c instanceof King && !((ColoredPiece)c).getColor().equals(piece.getColor())) {
		    	                    return true;
		    	                } else {
		    	                    isBlocked = true;
		    	                    break;
		    	                }
		    	            }
		    	        }

		    	        if (isBlocked) break;

		    	    }
				}
			}
			
			//Mise en echec par une reine
			else if(piece instanceof Queen) {
				int[][] directions = {
		        	    {-1, 0}, // haut
		        	    {0,  1}, // droite
		        	    {0, -1}, // gauche
		        	    {1,  0}, // bas
		        	    {-1, -1}, // haut gauche
		        	    {-1,  1}, // haut droite
		        	    { 1, -1}, // bas gauche
		        	    { 1,  1}  // bas droite
		        	};
		        for (int[] dir : directions) {
		    	    int nextRow = pieceRow;
		    	    int nextCol = pieceCol;

		    	    while (true) {
		    	        nextRow += dir[0];
		    	        nextCol += dir[1];

		    	        // Vérifie si on reste dans le plateau
		    	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		    	            break;
		    	        }
		    	        
		    	     // Vérifie s'il y a une pièce qui bloque le chemin
		    	        boolean isBlocked = false;
		    	        for (Component c : panneau.getComponents()) {
		    	            Rectangle otherR = c.getBounds();
		    	            int cRow = otherR.y / 100;
		    	            int cCol = otherR.x / 100;

		    	            if (cRow == nextRow && cCol == nextCol) {
		    	                // Si cest le roi ciblé, retour true
		    	                if (cRow == kingRow && cCol == kingCol && c instanceof King && !((ColoredPiece)c).getColor().equals(piece.getColor())) {
		    	                    return true;
		    	                } else {
		    	                    isBlocked = true;
		    	                    break;
		    	                }
		    	            }
		    	        }

		    	        if (isBlocked) break;

		    	    }
		        }
			}
		
		return false;
	}
	
	
/*//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	//Vérifie si les déplacements d'une piece alliée sont sur la trajectoire d'un echec
	public boolean willPieceBeInTrajectory(ColoredPiece piece, int nextRow, int nextCol, JPanel panneau) {
	    int oldX = piece.getBounds().x;
	    int oldY = piece.getBounds().y;
	    Component capturedPiece= null;

	 // Vérifie s'il y a une pièce adverse sur la case cible
	    for (Component c : panneau.getComponents()) {
	        if (c.getBounds().x == nextCol * 100 && c.getBounds().y == nextRow * 100 && c instanceof ColoredPiece) {
	            ColoredPiece other = (ColoredPiece) c;
	            if (!other.getColor().equals(piece.getColor())) {
	                capturedPiece = c;
	                panneau.remove(c); // On "capture" la pièce
	                break;
	            }
	        }
	    }
	    
	    // Déplacement temporaire de la pièce
	    piece.setBounds(nextCol * 100, nextRow * 100, 100, 100);

	    // Vérifie sil existe une pièce adverse menaçante pour le roi,
	    // et si cette pièce alliée se trouve sur sa trajectoire
	    boolean isBlocking = isPieceInTrajectory(piece, panneau);

	    // Restaure la position de la pièce
	    piece.setBounds(oldX, oldY, 100, 100);
	    
	    // On restaure la pièce capturée si besoin
	    if (capturedPiece != null) {
	        panneau.add(capturedPiece);
	    }

	    return isBlocking;
	}

	//Vérifie si une piece alliée est sur la trajectoire d'un echec
	public boolean isPieceInTrajectory(ColoredPiece allyPiece, JPanel panneau) {
	    Rectangle allyRect = allyPiece.getBounds();
	    int allyRow = allyRect.y / 100;
	    int allyCol = allyRect.x / 100;

	    ColoredPiece attacker;
	    King king = null;

	    // Trouver le roi allié
	    for (Component c : panneau.getComponents()) {
	        if (c instanceof King && ((ColoredPiece) c).getColor().equals(allyPiece.getColor())) {
	            king = (King) c;
	            break;
	        }
	    }
	    if (king == null) return false;

	    Rectangle kingRect = king.getBounds();
	    int kingRow = kingRect.y / 100;
	    int kingCol = kingRect.x / 100;

	    // Parcourt toutes les pièces adverses
	    for (Component c : panneau.getComponents()) {
	        if (!(c instanceof ColoredPiece)) continue;

	        ColoredPiece p = (ColoredPiece) c;
	        if (p.getColor().equals(allyPiece.getColor())) continue; // On veut les pièces adverses

	        Rectangle r = p.getBounds();
	        int row = r.y / 100;
	        int col = r.x / 100;

	        // Vérifie si cette pièce menace le roi
	        if (isThreateningKing(p, row, col, kingRow, kingCol, panneau)) {
	            // Vérifie si allyPiece est alignée avec l'attaquant et le roi
	            if (areAligned(row, col, kingRow, kingCol, allyRow, allyCol)) {
	                // Vérifie si allyPiece est entre l'attaquant et le roi
	                if (isBetween(row, col, kingRow, kingCol, allyRow, allyCol)) {
	                    return true;
	                }
	            }
	        }
	    }

	    return false;
	}

	
	private boolean isThreateningKing(ColoredPiece piece, int pieceRow, int pieceCol, int kingRow, int kingCol, JPanel panneau) {
	    int[][] directions;

	    if (piece instanceof Rook) {
	        directions = new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1} };
	    } else if (piece instanceof Bishop) {
	        directions = new int[][] { {-1, -1}, {-1, 1}, {1, -1}, {1, 1} };
	    } else if (piece instanceof Queen) {
	        directions = new int[][] { {-1, 0}, {1, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 1}, {1, -1}, {1, 1} };
	    } else {
	        return false;
	    }

	    for (int[] dir : directions) {
	        int r = pieceRow;
	        int c = pieceCol;

	        while (true) {
	            r += dir[0];
	            c += dir[1];
	            if (r < 0 || r >= 8 || c < 0 || c >= 8) break;

	            Component comp = getPieceAt(r, c, panneau);
	            if (comp != null) {
	                if (comp instanceof King && ((ColoredPiece) comp).getColor() != piece.getColor()) {
	                    return true; // attaque le roi
	                } else {
	                    break; // bloqué par une autre pièce
	                }
	            }
	        }
	    }
	    return false;
	}
	
	private Component getPieceAt(int row, int col, JPanel panneau) {
	    for (Component c : panneau.getComponents()) {
	        Rectangle bounds = c.getBounds();
	        if (bounds.x / 100 == col && bounds.y / 100 == row) {
	            return c;
	        }
	    }
	    return null;
	}

	
	private boolean areAligned(int r1, int c1, int r2, int c2, int r3, int c3) {
	    // même ligne
	    if (r1 == r2 && r2 == r3) return true;
	    // même colonne
	    if (c1 == c2 && c2 == c3) return true;
	    // même diagonale
	    if (Math.abs(r1 - r2) == Math.abs(c1 - c2) &&
	        Math.abs(r2 - r3) == Math.abs(c2 - c3) &&
	        ((r1 - r2) * (r2 - r3) >= 0) && ((c1 - c2) * (c2 - c3) >= 0)) {
	        return true;
	    }
	    return false;
	}

	private boolean isBetween(int r1, int c1, int r2, int c2, int r, int c) {
	    // Vérifie si (r,c) est entre (r1,c1) et (r2,c2)
	    return ((r >= Math.min(r1, r2) && r <= Math.max(r1, r2)) &&
	            (c >= Math.min(c1, c2) && c <= Math.max(c1, c2)));
	}

*///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
	public void updateScoreLabels() {
		// Si Menu.joueur n'est pas défini (null ou vide), on essaie de récupérer celui sauvegardé précédemment :
		if (joueur == null || joueur.isEmpty()) {
		    joueur = prefs.get("joueur", "Invité");
		}
		
		// Si Menu.opponent n'est pas défini (null ou vide), on essaie de récupérer celui sauvegardé précédemment :
		if (opponent == null || opponent.isEmpty()) {
			opponent = prefs.get("opponent", "Invité");
		}
		
		if (opponent != null && !Menu2.trainingMode) {
			if (Menu2.color == "white") {
				if(joueur1Score <= 0) {
					
					joueur1Label.setText(joueur + " - Score: ");
				}else {
					joueur1Label.setText(joueur + " - Score: +" + joueur1Score);
				}
				
				if(joueur2Score <= 0) {
					joueur2Label.setText(opponent + " - Score: ");
				}else {
					joueur2Label.setText(opponent + " - Score: +" + joueur2Score);
				}
			} else {
				if(joueur1Score <= 0) {
					
					joueur1Label.setText(opponent + " - Score: ");
				}else {
					joueur1Label.setText(opponent + " - Score: +" + joueur1Score);
				}
				
				if(joueur2Score <= 0) {
					joueur2Label.setText(joueur + " - Score: ");
				}else {
					joueur2Label.setText(joueur + " - Score: +" + joueur2Score);
				}
			}
		} else {
			if (Menu2.color == "white") {
				if(joueur1Score <= 0) {
					
					joueur1Label.setText(joueur + " - Score: ");
				}else {
					joueur1Label.setText(joueur + " - Score: +" + joueur1Score);
				}
				
				if(joueur2Score <= 0) {
					joueur2Label.setText("Joueur 2 (Noir) - Score: ");
				}else {
					joueur2Label.setText("Joueur 2 (Noir) - Score: +" + joueur2Score);
				}
			} else {
				if(joueur1Score <= 0) {
					
					joueur1Label.setText("Joueur 1 (Blanc) - Score: ");
				}else {
					joueur1Label.setText("Joueur 1 (Blanc) - Score: +" + joueur1Score);
				}
				
				if(joueur2Score <= 0) {
					joueur2Label.setText(joueur + " - Score: ");
				}else {
					joueur2Label.setText(joueur + " - Score: +" + joueur2Score);
				}
			}
		}		
	}
	
	private void addLegalMoves(JPanel panneau) {
		legalMoves.clear();
		if (isKingInCheck("white", panneau)) {
		    for (Component c : panneau.getComponents()) {
		    	if (c instanceof Pawn && ((Pawn) c).getColor().equals("white")) {
	           		Pawn pawn = (Pawn) c;
	           		Rectangle r = pawn.getBounds();
	           		int pawnRow = r.y / 100;
	           		int pawnCol = r.x / 100;
	           		
	           		boolean isNextRow1Occupied = false;
	                boolean isNextRow2Occupied = false;
	                
	           		for (Component a : panneau.getComponents()) {
	           			ColoredPiece otherPiece = (ColoredPiece) a;
	           			int row = otherPiece.getBounds().y / 100;
	           			int col = otherPiece.getBounds().x / 100;
	           			
	           			if (row == pawnRow - 1 && col ==pawnCol) {
	           				isNextRow1Occupied = true;
	           			}
	           			if (row == pawnRow - 2 && col ==pawnCol) {
	           				isNextRow2Occupied = true;
	           			}
	           		}
	           		
	           		if (!isNextRow1Occupied) {
	           			if (!willKingBeInCheck(pawn, pawnRow - 1, pawnCol, panneau)) {
	            			legalMoves.add(new int[]{pawnRow - 1, pawnCol});
	            		}
	            		else if (!willKingBeInCheck(pawn, pawnRow - 2, pawnCol, panneau)) {
	            			if (pawn.hasMoved() && !isNextRow2Occupied) {
	            				legalMoves.add(new int[]{pawnRow - 2, pawnCol});
		            		}
	            		}
	           		}       					    		
		    	}
		    	
		    	if (c instanceof Knight && ((Knight) c).getColor().equals("white")) {
		    		Knight knight = (Knight) c;
		    		boolean isAlly = false;
		    		int[][] directions = {
		    				{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
		    			    { 1, -2}, { 1, 2}, { 2, -1}, { 2, 1}
		            	};
		    		
                	for (int[] dir : directions) {
                		int nextRow = (knight.getBounds().y / 100) + dir[0];
                		int nextCol = (knight.getBounds().x / 100) + dir[1];
                		
                		if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) continue;
                		
                		for (Component a : panneau.getComponents()) {
	                		if (a instanceof ColoredPiece && a != knight) {
	                			ColoredPiece ennemy = (ColoredPiece) a;
	                			if (!ennemy.getColor().equals("black")) {
	                				Rectangle r = a.getBounds();
			    	                int cRow = r.y / 100;
			    	                int cCol = r.x / 100;
		
			    	                if ((cRow == nextRow && cCol == nextCol)) {
			    	                	isAlly = true;
			    	                }
	                			}
		        	        }
                		}
                		
                		if (!isAlly) {
                    		if (willKingBeInCheck(knight, nextRow, nextCol, panneau)) {
                				continue;
                			}
                			
    			    		legalMoves.add(new int[]{nextRow, nextCol});            			
                		}
                	}
		    	}
		    	
		    	if (c instanceof Bishop && ((Bishop) c).getColor().equals("white")) {
		    		Bishop bishop = (Bishop) c;
		    		int[][] directions = {
		            	    {-1, -1}, // haut gauche
		            	    {-1,  1}, // haut droite
		            	    { 1, -1}, // bas gauche
		            	    { 1,  1}  // bas droite
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = bishop.getBounds().y / 100;
		        	    int nextCol = bishop.getBounds().x / 100;

		        	    while (true) {
		        	        nextRow += dir[0];
		        	        nextCol += dir[1];

		        	        // Vérifie si on reste dans le plateau
		        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		        	            break;
		        	        }
		        	        
		        	        final int targetRow = nextRow;
		        	        final int targetCol = nextCol;
		        	        boolean isDiagOccupied = false;
		        	        
		        	        for (Component a : panneau.getComponents()) {
		        	        	if (a instanceof ColoredPiece && a != bishop) {
	    	    	                Rectangle r = a.getBounds();
	    	    	                int cRow = r.y / 100;
	    	    	                int cCol = r.x / 100;

	    	    	                if (cRow == nextRow && cCol == nextCol) {
	    	    	                	isDiagOccupied = true;
	    	    	                }
			        	        }
		        	        }
		        	        
		        	        if (!isDiagOccupied) {
		        	        	if (willKingBeInCheck(bishop, targetRow, targetCol, panneau)) {
		    	    	            continue;
		    	    	        }
		    		            legalMoves.add(new int[]{targetRow, targetCol});
		        	        }
		        	    }
		        	}            					    	        	
		    	}
		    	
		    	if (c instanceof Rook && ((Rook) c).getColor().equals("white")) {
		    		Rook rook = (Rook) c;
		    		int[][] directions = {
		            	    {-1,  0}, // haut
		            	    { 0,  1}, // droite
		            	    { 0, -1}, // gauche
		            	    { 1,  0}  // bas
		            	    
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = rook.getBounds().y / 100;
		        	    int nextCol = rook.getBounds().x / 100;

		        	    while (true) {
		        	        nextRow += dir[0];
		        	        nextCol += dir[1];

		        	        // Vérifie si on reste dans le plateau
		        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		        	            break;
		        	        }
		        	        
		        	        final int targetRow = nextRow;
		        	        final int targetCol = nextCol;
		        	        boolean isLineOccupied = false;
		        	        
		        	        for (Component a : panneau.getComponents()) {
		        	        	if (a instanceof JButton && a != rook) {
	    	    	                Rectangle r = a.getBounds();
	    	    	                int cRow = r.y / 100;
	    	    	                int cCol = r.x / 100;

	    	    	                if (cRow == nextRow && cCol == nextCol) {
	    	    	                	isLineOccupied = true;
	    	    	                }
			        	        }
		        	        }
		        	        
		        	        if (!isLineOccupied) {
		        	        	if (willKingBeInCheck(rook, targetRow, targetCol, panneau)) {
		    	    	            continue;
		    	    	        }
		    		            legalMoves.add(new int[]{targetRow, targetCol});
		        	        }
		        	    }
		        	}            					    	        	
		    	}
		    	
		    	if (c instanceof Queen && ((Queen) c).getColor().equals("white")) {
		    		Queen queen = (Queen) c;
		    		int[][] directions = {
		            	    {-1,  0}, // haut
		            	    { 0,  1}, // droite
		            	    { 0, -1}, // gauche
		            	    { 1,  0},  // bas
		            	    {-1, -1}, // haut gauche
		            	    {-1,  1}, // haut droite
		            	    { 1, -1}, // bas gauche
		            	    { 1,  1}  // bas droite
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = queen.getBounds().y / 100;
		        	    int nextCol = queen.getBounds().x / 100;

		        	    while (true) {
		        	        nextRow += dir[0];
		        	        nextCol += dir[1];

		        	        // Vérifie si on reste dans le plateau
		        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		        	            break;
		        	        }
		        	        
		        	        final int targetRow = nextRow;
		        	        final int targetCol = nextCol;
		        	        boolean isDiagAndLineOccupied = false;
		        	        
		        	        for (Component a : panneau.getComponents()) {
		        	        	if (a instanceof ColoredPiece && a != queen) {
	    	    	                Rectangle r = a.getBounds();
	    	    	                int cRow = r.y / 100;
	    	    	                int cCol = r.x / 100;

	    	    	                if (cRow == nextRow && cCol == nextCol) {
	    	    	                	isDiagAndLineOccupied = true;
	    	    	                }
			        	        }
		        	        }
		        	        
		        	        if (!isDiagAndLineOccupied) {
		        	        	if (willKingBeInCheck(queen, targetRow, targetCol, panneau)) {
		    	    	            continue;
		    	    	        }
		    		            legalMoves.add(new int[]{targetRow, targetCol});
		        	        }
		        	    }
		        	}            					    	        	
		    	}
		    	
		    	if (c instanceof King && ((King) c).getColor().equals("white")) {
		    		King king = (King) c;
		    		int[][] directions = {
		            	    {-1,  0}, // haut
		            	    { 0,  1}, // droite
		            	    { 0, -1}, // gauche
		            	    { 1,  0},  // bas
		            	    {-1, -1}, // haut gauche
		            	    {-1,  1}, // haut droite
		            	    { 1, -1}, // bas gauche
		            	    { 1,  1}  // bas droite
		            	};

		    		for (int[] dir : directions) {
		        	    int nextRow = king.getBounds().y / 100;
		        	    int nextCol = king.getBounds().x / 100;
		        	    
	        	        nextRow += dir[0];
	        	        nextCol += dir[1];

	        	        // Vérifie si on reste dans le plateau
	        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
	        	            continue;
	        	        }
	        	        
	        	        final int targetRow = nextRow;
	        	        final int targetCol = nextCol;
	        	        boolean isDiagAndLineOccupied = false;
	        	        
	        	        for (Component a : panneau.getComponents()) {
	        	        	if (a instanceof ColoredPiece && a != king && ((ColoredPiece)a).getColor().equals(king.getColor())) {
    	    	                Rectangle r = a.getBounds();
    	    	                int cRow = r.y / 100;
    	    	                int cCol = r.x / 100;

    	    	                if (cRow == nextRow && cCol == nextCol) {
    	    	                	isDiagAndLineOccupied = true;
    	    	                }
		        	        }
	        	        }
	        	        
	        	        if (!isDiagAndLineOccupied) {
	        	        	if (willKingBeInCheck(king, targetRow, targetCol, panneau)) {
	    	    	            continue;
	    	    	        }
	    		            legalMoves.add(new int[]{targetRow, targetCol});
	        	        }		        	    
		        	}           					    	        	
		    	}
		    }
		    
		    checkMate("noir",panneau);		    		    
		}
		if (isKingInCheck("black", panneau)) {
		    for (Component c : panneau.getComponents()) {
		    	if (c instanceof Pawn && ((Pawn) c).getColor().equals("black")) {
	           		Pawn pawn = (Pawn) c;
	           		Rectangle r = pawn.getBounds();
	           		int pawnRow = r.y / 100;
	           		int pawnCol = r.x / 100;
	           		
	           		boolean isNextRow1Occupied = false;
	                boolean isNextRow2Occupied = false;
	                
	           		for (Component a : panneau.getComponents()) {
	           			ColoredPiece otherPiece = (ColoredPiece) a;
	           			int row = otherPiece.getBounds().y / 100;
	           			int col = otherPiece.getBounds().x / 100;
	           			
	           			if (row == pawnRow + 1 && col ==pawnCol) {
	           				isNextRow1Occupied = true;
	           			}
	           			if (row == pawnRow + 2 && col ==pawnCol) {
	           				isNextRow2Occupied = true;
	           			}
	           		}
	           		
	           		if (!isNextRow1Occupied) {
	           			if (!willKingBeInCheck(pawn, pawnRow + 1, pawnCol, panneau)) {
	            			legalMoves.add(new int[]{pawnRow + 1, pawnCol});
	            		}
	            		else if (!willKingBeInCheck(pawn, pawnRow + 2, pawnCol, panneau)) {
	            			if (pawn.hasMoved() && !isNextRow2Occupied) {
	            				legalMoves.add(new int[]{pawnRow + 2, pawnCol});
		            		}
	            		}
	           		}       					    		
		    	}
		    	
		    	if (c instanceof Knight && ((Knight) c).getColor().equals("black")) {
		    		Knight knight = (Knight) c;
		    		boolean isAlly = false;
		    		int[][] directions = {
		    				{-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
		    			    { 1, -2}, { 1, 2}, { 2, -1}, { 2, 1}
		            	};
		    		
                	for (int[] dir : directions) {
                		int nextRow = (knight.getBounds().y / 100) + dir[0];
                		int nextCol = (knight.getBounds().x / 100) + dir[1];
                		
                		if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) continue;
                		
                		for (Component a : panneau.getComponents()) {
	                		if (a instanceof ColoredPiece && a != knight) {
	                			ColoredPiece ennemy = (ColoredPiece) a;
	                			if (!ennemy.getColor().equals("white")) {
	                				Rectangle r = a.getBounds();
			    	                int cRow = r.y / 100;
			    	                int cCol = r.x / 100;
		
			    	                if ((cRow == nextRow && cCol == nextCol)) {
			    	                	isAlly = true;
			    	                }
	                			}
		        	        }
                		}
                		
                		if (!isAlly) {
                    		if (willKingBeInCheck(knight, nextRow, nextCol, panneau)) {
                				continue;
                			}
                			
    			    		legalMoves.add(new int[]{nextRow, nextCol});               			
                		}
                	}
		    	}
		    	
		    	if (c instanceof Bishop && ((Bishop) c).getColor().equals("black")) {
		    		Bishop bishop = (Bishop) c;
		    		int[][] directions = {
		            	    {-1, -1}, // haut gauche
		            	    {-1,  1}, // haut droite
		            	    { 1, -1}, // bas gauche
		            	    { 1,  1}  // bas droite
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = bishop.getBounds().y / 100;
		        	    int nextCol = bishop.getBounds().x / 100;

		        	    while (true) {
		        	        nextRow += dir[0];
		        	        nextCol += dir[1];

		        	        // Vérifie si on reste dans le plateau
		        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		        	            break;
		        	        }
		        	        
		        	        final int targetRow = nextRow;
		        	        final int targetCol = nextCol;
		        	        boolean isDiagOccupied = false;
		        	        
		        	        for (Component a : panneau.getComponents()) {
		        	        	if (a instanceof ColoredPiece && a != bishop) {
	    	    	                Rectangle r = a.getBounds();
	    	    	                int cRow = r.y / 100;
	    	    	                int cCol = r.x / 100;

	    	    	                if (cRow == nextRow && cCol == nextCol) {
	    	    	                	isDiagOccupied = true;
	    	    	                }
			        	        }
		        	        }
		        	        
		        	        if (!isDiagOccupied) {
		        	        	if (willKingBeInCheck(bishop, targetRow, targetCol, panneau)) {
		    	    	            continue;
		    	    	        }
		    		            legalMoves.add(new int[]{targetRow, targetCol});
		        	        }
		        	    }
		        	}            					    	        	
		    	}
		    	
		    	if (c instanceof Rook && ((Rook) c).getColor().equals("black")) {
		    		Rook rook = (Rook) c;
		    		int[][] directions = {
		            	    {-1,  0}, // haut
		            	    { 0,  1}, // droite
		            	    { 0, -1}, // gauche
		            	    { 1,  0}  // bas
		            	    
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = rook.getBounds().y / 100;
		        	    int nextCol = rook.getBounds().x / 100;

		        	    while (true) {
		        	        nextRow += dir[0];
		        	        nextCol += dir[1];

		        	        // Vérifie si on reste dans le plateau
		        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		        	            break;
		        	        }
		        	        
		        	        final int targetRow = nextRow;
		        	        final int targetCol = nextCol;
		        	        boolean isLineOccupied = false;
		        	        
		        	        for (Component a : panneau.getComponents()) {
		        	        	if (a instanceof ColoredPiece && a != rook) {
	    	    	                Rectangle r = a.getBounds();
	    	    	                int cRow = r.y / 100;
	    	    	                int cCol = r.x / 100;

	    	    	                if (cRow == nextRow && cCol == nextCol) {
	    	    	                	isLineOccupied = true;
	    	    	                }
			        	        }
		        	        }
		        	        
		        	        if (!isLineOccupied) {
		        	        	if (willKingBeInCheck(rook, targetRow, targetCol, panneau)) {
		    	    	            continue;
		    	    	        }
		    		            legalMoves.add(new int[]{targetRow, targetCol});
		        	        }
		        	    }
		        	}            					    	        	
		    	}
		    	
		    	if (c instanceof Queen && ((Queen) c).getColor().equals("black")) {
		    		Queen queen = (Queen) c;
		    		int[][] directions = {
		            	    {-1,  0}, // haut
		            	    { 0,  1}, // droite
		            	    { 0, -1}, // gauche
		            	    { 1,  0},  // bas
		            	    {-1, -1}, // haut gauche
		            	    {-1,  1}, // haut droite
		            	    { 1, -1}, // bas gauche
		            	    { 1,  1}  // bas droite
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = queen.getBounds().y / 100;
		        	    int nextCol = queen.getBounds().x / 100;

		        	    while (true) {
		        	        nextRow += dir[0];
		        	        nextCol += dir[1];

		        	        // Vérifie si on reste dans le plateau
		        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
		        	            break;
		        	        }
		        	        
		        	        final int targetRow = nextRow;
		        	        final int targetCol = nextCol;
		        	        boolean isDiagAndLineOccupied = false;
		        	        
		        	        for (Component a : panneau.getComponents()) {
		        	        	if (a instanceof ColoredPiece && a != queen) {
	    	    	                Rectangle r = a.getBounds();
	    	    	                int cRow = r.y / 100;
	    	    	                int cCol = r.x / 100;

	    	    	                if (cRow == nextRow && cCol == nextCol) {
	    	    	                	isDiagAndLineOccupied = true;
	    	    	                }
			        	        }
		        	        }
		        	        
		        	        if (!isDiagAndLineOccupied) {
		        	        	if (willKingBeInCheck(queen, targetRow, targetCol, panneau)) {
		    	    	            continue;
		    	    	        }
		    		            legalMoves.add(new int[]{targetRow, targetCol});
		        	        }
		        	    }
		        	}            					    	        	
		    	}
		    	
		    	if (c instanceof King && ((King) c).getColor().equals("black")) {
		    		King king = (King) c;
		    		int[][] directions = {
		            	    {-1,  0}, // haut
		            	    { 0,  1}, // droite
		            	    { 0, -1}, // gauche
		            	    { 1,  0},  // bas
		            	    {-1, -1}, // haut gauche
		            	    {-1,  1}, // haut droite
		            	    { 1, -1}, // bas gauche
		            	    { 1,  1}  // bas droite
		            	};

		        	for (int[] dir : directions) {
		        	    int nextRow = king.getBounds().y / 100;
		        	    int nextCol = king.getBounds().x / 100;
		        	    
	        	        nextRow += dir[0];
	        	        nextCol += dir[1];

	        	        // Vérifie si on reste dans le plateau
	        	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
	        	            continue;
	        	        }
	        	        
	        	        final int targetRow = nextRow;
	        	        final int targetCol = nextCol;
	        	        boolean isDiagAndLineOccupied = false;
	        	        
	        	        for (Component a : panneau.getComponents()) {
	        	        	if (a instanceof ColoredPiece && a != king && ((ColoredPiece)a).getColor().equals(king.getColor())) {
    	    	                Rectangle r = a.getBounds();
    	    	                int cRow = r.y / 100;
    	    	                int cCol = r.x / 100;

    	    	                if (cRow == nextRow && cCol == nextCol) {
    	    	                	isDiagAndLineOccupied = true;
    	    	                }
		        	        }
	        	        }
	        	        
	        	        if (!isDiagAndLineOccupied) {
	        	        	if (willKingBeInCheck(king, targetRow, targetCol, panneau)) {
	    	    	            continue;
	    	    	        }
	    		            legalMoves.add(new int[]{targetRow, targetCol});
	        	        }		        	    
		        	}            					    	        	
		    	}
		    }
		    
		    checkMate("blanc",panneau);
		}
	}
	
	private void checkMate(String color, JPanel panneau) {
		//legalMoves.forEach(move -> System.out.println(Arrays.toString(move)));
		if (legalMoves.isEmpty()) {
		    String[] choixFinDuJeu = {"Menu", "Rejouer"};
        	int choix = JOptionPane.showOptionDialog(
        		    null,
        		    "Les "+ color + " ont gagné",
        		    "echec et mat!",
        		    JOptionPane.DEFAULT_OPTION,
        		    JOptionPane.PLAIN_MESSAGE,
        		    null,
        		    choixFinDuJeu,
        		    choixFinDuJeu[0]
        		);
        	
        	switch (choix) {
            case 0: // Menu
            	Window window = SwingUtilities.getWindowAncestor(panneau);
            	Point location = window.getLocation();
                if (window != null) {
                    window.dispose();
                }
                new Menu2();
				break;
            case 1 : // Rejouer
            	panneau.removeAll();
            	joueur1Score = 0;
            	joueur2Score = 0;
            	updateScoreLabels();
            	Board board = new Board();
            	gameState = new GameState();
            	panneau.repaint();
            	for (int i=board.getCol() ; i>=1;i--) {
        			for (int j=1 ; j<=board.getRow();j++) {
        				initialize(i,j,panneau);
        			}
            	}
            	break;
        	default : //Menu
        		window = SwingUtilities.getWindowAncestor(panneau);
        		location = window.getLocation();
        	    if (window != null) {
        	        window.dispose();
        	    }
        	    new Menu2();
        		break;
        	}
		    checkMate = true;
		}
	}
    
	private void LongDistancePieceMoves (ColoredPiece piece, int[] pieceRow, int[] pieceCol, int[][] directions, JPanel panneau) {
		for (int[] dir : directions) {
    	    int nextRow = pieceRow[0];
    	    int nextCol = pieceCol[0];

    	    while (true) {
    	        nextRow += dir[0];
    	        nextCol += dir[1];

    	        // Vérifie si on reste dans le plateau
    	        if (nextRow < 0 || nextRow >= 8 || nextCol < 0 || nextCol >= 8) {
    	            break;
    	        }
    	        
    	        final int targetRow = nextRow;
    	        final int targetCol = nextCol;
    	        boolean isLongOccupied = false;

    	        if (isKingInCheck(piece.getColor(), panneau)) {
    		        if (willKingBeInCheck(piece, targetRow, targetCol, panneau)) {
    		            continue;
    		        }
    		        
    	            List<int[]> legalMoves = new ArrayList<>();
    	            legalMoves.add(new int[]{targetRow, targetCol});
    	            
    	            //System.out.println("Legal queen moves");
    	            //legalMoves.forEach(move -> System.out.println(Arrays.toString(move)));
    	            
    	            for (int[] moves : legalMoves) {
    	            	for (Component c : panneau.getComponents()) {
    	    	            if (c instanceof JButton && c != piece) {
    	    	                Rectangle r = c.getBounds();
    	    	                int cRow = r.y / 100;
    	    	                int cCol = r.x / 100;

    	    	                if (cRow == moves[0] && cCol == moves[1]) {
    	    	                	isLongOccupied = true;
    	    	                	// prise d'un fou par un fou
    	            	        	if (c instanceof Bishop) {
    	            	        		Bishop otherpiece = (Bishop) c;
    	            	                // Ajouter une case de capture si piece ennemie
    	            	        		if (!otherpiece.getColor().equals(piece.getColor())) {
    		            	                captureChoice = new Choice(); 
    		            	                captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
    		            	                captureChoice.setBorderPainted(false);
    		        	                    captureChoice.setContentAreaFilled(false);
    		        	                    captureChoice.setFocusPainted(false);
    		        	                    captureChoice.setEnabled(true);
    		        	                    captureChoice.setVisible(true);
    		            	                panneau.add(captureChoice, 0);
    		            	                
    		            	                
    		                    	        	
    		            	                captureChoice.addActionListener(new ActionListener(){
    		                    	        	public void actionPerformed(ActionEvent e) {
    		                    	        		panneau.remove(otherpiece);
    		                    	        		piece.setBounds(cCol * 100, cRow * 100, 100, 100);
    		                    	        		
    		                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
    		                    	        		String to = coordToNotation(cRow, cCol);
    		                    	        		String move = null;
    		                    	        		if (piece instanceof Bishop) {
    		                    	        			move = "B:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Rook) {
    		                    	        			move = "R:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Queen) {
    		                    	        			move = "Q:" + from + "x" + to;
    		                    	        		}
    		                    	        		System.out.println(move);
    		                    	        		saveMove(gameId, joueur, move);
    		                    	        		
    		                    	        		try {
														sendMove( String.valueOf(gameId), move, joueur);
													} catch (IOException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
    		                    	        		pieceRow[0] = cRow;
    		                    	        		pieceCol[0] = cCol;
    		                    	        		
    		                    	        		if (otherpiece.getColor().equals("white")) {
    		                    						joueur2Score += otherpiece.getValue();
    		                    						joueur1Score -= otherpiece.getValue();
    		                    						updateScoreLabels();
    		                    					}else {
    		                    						joueur1Score += otherpiece.getValue();
    		                    						joueur2Score -= otherpiece.getValue();
    		                    						updateScoreLabels();
    		                    					}
    		
    		                    	        		clearSelections(panneau);
    		                    	        		
    		                    	        		gameState.switchTurn();
    		        	                            gameState.resetLastDoubleStepPawn();
    		        	                            selectedActivation(panneau);
    		        	                            
    		        	                            addLegalMoves(panneau);
    		                    	        	}
    		                    	        });
    	            	        		}
    	            	            }
    	            	        	
    	    	                	// prise d'un pion par un fou
    	            	        	if (c instanceof Pawn) {
    	            	        		Pawn otherPawn = (Pawn) c;
    	            	                // Ajouter une case de capture si piece ennemie
    	            	        		if (!otherPawn.getColor().equals(piece.getColor())) {
    		            	                captureChoice = new Choice(); 
    		            	                captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
    		            	                captureChoice.setBorderPainted(false);
    		        	                    captureChoice.setContentAreaFilled(false);
    		        	                    captureChoice.setFocusPainted(false);
    		        	                    captureChoice.setEnabled(true);
    		        	                    captureChoice.setVisible(true);
    		            	                panneau.add(captureChoice, 0);
    		            	                
    		            	                
    		                    	        	
    		            	                captureChoice.addActionListener(new ActionListener(){
    		                    	        	public void actionPerformed(ActionEvent e) {
    		                    	        		panneau.remove(otherPawn);
    		                    	        		piece.setBounds(cCol * 100, cRow * 100, 100, 100);
    		                    	        		
    		                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
    		                    	        		String to = coordToNotation(cRow, cCol);
    		                    	        		String move = null;
    		                    	        		if (piece instanceof Bishop) {
    		                    	        			move = "B:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Rook) {
    		                    	        			move = "R:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Queen) {
    		                    	        			move = "Q:" + from + "x" + to;
    		                    	        		}
    		                    	        		
    		                    	        		System.out.println(move);
    		                    	        		saveMove(gameId, joueur, move);
    		                    	        		
    		                    	        		try {
														sendMove( String.valueOf(gameId), move, joueur);
													} catch (IOException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
    		                    	        		
    		                    	        		pieceRow[0] = cRow;
    		                    	        		pieceCol[0] = cCol;
    		                    	        		
    		                    	        		if (otherPawn.getColor().equals("white")) {
    		                    						joueur2Score += otherPawn.getValue();
    		                    						joueur1Score -= otherPawn.getValue();
    		                    				        updateScoreLabels();
    		                    					}else {
    		                    						joueur1Score += otherPawn.getValue();
    		                    						joueur2Score -= otherPawn.getValue();
    		                    						updateScoreLabels();
    		                    					}
    		 
    		                    	        		clearSelections(panneau);
    		                    	        		
    		                    	        		gameState.switchTurn();
    		        	                            gameState.resetLastDoubleStepPawn();
    		        	                            selectedActivation(panneau);
    		        	                            
    		        	                            addLegalMoves(panneau);
    		                    	        	}
    		                    	        });
    	            	        		}
    	            	            }
    	        	            
    	            	        	// prise d'un cavalier par un fou
    	            	        	if (c instanceof Knight) {
    	            	        		Knight otherKnight = (Knight) c;
    	            	                // Ajouter une case de capture si piece ennemie
    	            	        		if (!otherKnight.getColor().equals(piece.getColor())) {
    		            	                captureChoice = new Choice(); 
    		            	                captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
    		            	                captureChoice.setBorderPainted(false);
    		        	                    captureChoice.setContentAreaFilled(false);
    		        	                    captureChoice.setFocusPainted(false);
    		        	                    captureChoice.setEnabled(true);
    		        	                    captureChoice.setVisible(true);
    		            	                panneau.add(captureChoice, 0);
    		            	                
    		            	                
    		                    	        	
    		            	                captureChoice.addActionListener(new ActionListener(){
    		                    	        	public void actionPerformed(ActionEvent e) {
    		                    	        		panneau.remove(otherKnight);
    		                    	        		piece.setBounds(cCol * 100, cRow * 100, 100, 100);
    		                    	        		
    		                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
    		                    	        		String to = coordToNotation(cRow, cCol);
    		                    	        		String move = null;
    		                    	        		if (piece instanceof Bishop) {
    		                    	        			move = "B:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Rook) {
    		                    	        			move = "R:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Queen) {
    		                    	        			move = "Q:" + from + "x" + to;
    		                    	        		}
    		                    	        		
    		                    	        		System.out.println(move);
    		                    	        		saveMove(gameId, joueur, move);
    		                    	        		
    		                    	        		try {
														sendMove( String.valueOf(gameId), move, joueur);
													} catch (IOException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
    		                    	        		
    		                    	        		pieceRow[0] = cRow;
    		                    	        		pieceCol[0] = cCol;
    		                    	        		
    		                    	        		if (otherKnight.getColor().equals("white")) {
    		                    						joueur2Score += otherKnight.getValue();
    		                    						joueur1Score -= otherKnight.getValue();
    		                    				        updateScoreLabels();
    		                    					}else {
    		                    						joueur1Score += otherKnight.getValue();
    		                    						joueur2Score -= otherKnight.getValue();
    		                    						updateScoreLabels();
    		                    					}
    		
    		                    	        		clearSelections(panneau);
    		                    	        		
    		                    	        		gameState.switchTurn();
    		        	                            gameState.resetLastDoubleStepPawn();
    		        	                            selectedActivation(panneau);
    		        	                            
    		        	                            addLegalMoves(panneau);
    		                    	        	}
    		                    	        });
    	            	        		}
    	            	            }
    	            	        	
    	            	        	// prise d'une tour par un fou
    	            	        	if (c instanceof Rook) {
    	            	        		Rook otherRook = (Rook) c;
    	            	                // Ajouter une case de capture si piece ennemie
    	            	        		if (!otherRook.getColor().equals(piece.getColor())) {
    		            	                captureChoice = new Choice(); 
    		            	                captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
    		            	                captureChoice.setBorderPainted(false);
    		        	                    captureChoice.setContentAreaFilled(false);
    		        	                    captureChoice.setFocusPainted(false);
    		        	                    captureChoice.setEnabled(true);
    		        	                    captureChoice.setVisible(true);
    		            	                panneau.add(captureChoice, 0);
    		            	                
    		            	                
    		                    	        	
    		            	                captureChoice.addActionListener(new ActionListener(){
    		                    	        	public void actionPerformed(ActionEvent e) {
    		                    	        		panneau.remove(otherRook);
    		                    	        		piece.setBounds(cCol * 100, cRow * 100, 100, 100);
    		                    	        		
    		                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
    		                    	        		String to = coordToNotation(cRow, cCol);
    		                    	        		String move = null;
    		                    	        		if (piece instanceof Bishop) {
    		                    	        			move = "B:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Rook) {
    		                    	        			move = "R:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Queen) {
    		                    	        			move = "Q:" + from + "x" + to;
    		                    	        		}
    		                    	        		
    		                    	        		System.out.println(move);
    		                    	        		saveMove(gameId, joueur, move);
    		                    	        		
    		                    	        		try {
														sendMove( String.valueOf(gameId), move, joueur);
													} catch (IOException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
    		                    	        		
    		                    	        		pieceRow[0] = cRow;
    		                    	        		pieceCol[0] = cCol;
    		                    	        		
    		                    	        		if (otherRook.getColor().equals("white")) {
    		                    						joueur2Score += otherRook.getValue();
    		                    						joueur1Score -= otherRook.getValue();
    		                    				        updateScoreLabels();
    		                    					}else {
    		                    						joueur1Score += otherRook.getValue();
    		                    						joueur2Score -= otherRook.getValue();
    		                    						updateScoreLabels();
    		                    					}
    		
    		                    	        		clearSelections(panneau);
    		                    	        		
    		                    	        		gameState.switchTurn();
    		        	                            gameState.resetLastDoubleStepPawn();
    		        	                            selectedActivation(panneau);
    		        	                            
    		        	                            addLegalMoves(panneau);
    		                    	        	}
    		                    	        });
    	            	        		}
    	            	            }
    	            	        	
    	            	        	// prise d'une reine par un fou
    	            	        	if (c instanceof Queen) {
    	            	        		Queen otherQueen = (Queen) c;
    	            	                // Ajouter une case de capture si piece ennemie
    	            	        		if (!otherQueen.getColor().equals(piece.getColor())) {
    		            	                captureChoice = new Choice(); 
    		            	                captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
    		            	                captureChoice.setBorderPainted(false);
    		        	                    captureChoice.setContentAreaFilled(false);
    		        	                    captureChoice.setFocusPainted(false);
    		        	                    captureChoice.setEnabled(true);
    		        	                    captureChoice.setVisible(true);
    		            	                panneau.add(captureChoice, 0);
    		            	                
    		            	                
    		                    	        	
    		            	                captureChoice.addActionListener(new ActionListener(){
    		                    	        	public void actionPerformed(ActionEvent e) {
    		                    	        		panneau.remove(otherQueen);
    		                    	        		piece.setBounds(cCol * 100, cRow * 100, 100, 100);
    		                    	        		
    		                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
    		                    	        		String to = coordToNotation(cRow, cCol);
    		                    	        		String move = null;
    		                    	        		if (piece instanceof Bishop) {
    		                    	        			move = "B:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Rook) {
    		                    	        			move = "R:" + from + "x" + to;
    		                    	        		}
    		                    	        		if (piece instanceof Queen) {
    		                    	        			move = "Q:" + from + "x" + to;
    		                    	        		}
    		                    	        		
    		                    	        		System.out.println(move);
    		                    	        		saveMove(gameId, joueur, move);
    		                    	        		
    		                    	        		try {
														sendMove( String.valueOf(gameId), move, joueur);
													} catch (IOException e1) {
														// TODO Auto-generated catch block
														e1.printStackTrace();
													}
    		                    	        		
    		                    	        		pieceRow[0] = cRow;
    		                    	        		pieceCol[0] = cCol;
    		                    	        		
    		                    	        		if (otherQueen.getColor().equals("white")) {
    		                    						joueur2Score += otherQueen.getValue();
    		                    						joueur1Score -= otherQueen.getValue();
    		                    				        updateScoreLabels();
    		                    					}else {
    		                    						joueur1Score += otherQueen.getValue();
    		                    						joueur2Score -= otherQueen.getValue();
    		                    						updateScoreLabels();
    		                    					}
    		                    	        		
    		                    	        		gameState.switchTurn();
    		        	                            gameState.resetLastDoubleStepPawn();
    		        	                            selectedActivation(panneau);
    		        	                            
    		        	                            addLegalMoves(panneau);
    		                    	        	}
    		                    	        });
    	            	        		}
    	            	            }
    	    	                    break;
    	    	                }
    	    	            }
    	    	        }
    	            	
    	            	if (!isLongOccupied) {
    		            	// Ajoute un déplacement vide
    	        	        selectedPieceChoice = new Choice();
    	        	        selectedPieceChoice.setBounds(moves[1] * 100, moves[0] * 100, 100, 100);
    		                selectedPieceChoice.setBorderPainted(false);
    	                    selectedPieceChoice.setContentAreaFilled(false);
    	                    selectedPieceChoice.setFocusPainted(false);
    	                    selectedPieceChoice.setEnabled(true);
    	                    selectedPieceChoice.setVisible(true);
    	        	        panneau.add(selectedPieceChoice);
    	        	        
    	        	        selectedPieceChoice.addActionListener(new ActionListener(){
    	        	        	public void actionPerformed(ActionEvent e) {
    	        	        		piece.setBounds(moves[1] * 100, moves[0] * 100, 100, 100);
    	        	        		
    	        	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
                	        		String to = coordToNotation(moves[0], moves[1]);
                	        		String move = null;
                	        		if (piece instanceof Bishop) {
                	        			move = "B:" + from + "->" + to;
                	        		}
                	        		if (piece instanceof Rook) {
                	        			move = "R:" + from + "->" + to;
                	        		}
                	        		if (piece instanceof Queen) {
                	        			move = "Q:" + from + "->" + to;
                	        		}
                	        		
                	        		System.out.println(move);
                	        		saveMove(gameId, joueur, move);
                	        		
                	        		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
                	        		
    	        	        		pieceRow[0] = moves[0];
    	        	        		pieceCol[0] = moves[1];

    	        	        		clearSelections(panneau);
    	        	        		
    	        	        		gameState.switchTurn();
    	                            gameState.resetLastDoubleStepPawn();
    	                            selectedActivation(panneau);
    	                            
    	                            addLegalMoves(panneau);
    	        	        	}
    	        	        });   
    	            	} else {
    	            		break;
    	            	}
    	            }
    	        	break;
    	    	} else {
    	    		for (Component c : panneau.getComponents()) {
    		            if (c instanceof JButton && c != piece) {
    		                Rectangle r = c.getBounds();
    		                int cRow = r.y / 100;
    		                int cCol = r.x / 100;

    		                if (cRow == nextRow && cCol == nextCol) {
    		                	isLongOccupied = true;
    		                	// prise d'un fou par un fou
    	        	        	if (c instanceof Bishop) {
    	        	        		Bishop otherpiece = (Bishop) c;
    	        	                // Ajouter une case de capture si piece ennemie
    	        	        		if (!otherpiece.getColor().equals(piece.getColor())) {
    	            	                captureChoice = new Choice(); 
    	            	                captureChoice.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	            	                captureChoice.setBorderPainted(false);
    	        	                    captureChoice.setContentAreaFilled(false);
    	        	                    captureChoice.setFocusPainted(false);
    	        	                    captureChoice.setEnabled(true);
    	        	                    captureChoice.setVisible(true);
    	            	                panneau.add(captureChoice, 0);
    	            	                
    	            	                
    	                    	        	
    	            	                captureChoice.addActionListener(new ActionListener(){
    	                    	        	public void actionPerformed(ActionEvent e) {
    	                    	        		panneau.remove(otherpiece);
    	                    	        		piece.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	                    	        		
    	                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
		                    	        		String to = coordToNotation(targetRow, targetCol);
		                    	        		String move = null;
		                    	        		if (piece instanceof Bishop) {
		                    	        			move = "B:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Rook) {
		                    	        			move = "R:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Queen) {
		                    	        			move = "Q:" + from + "x" + to;
		                    	        		}
		                    	        		
		                    	        		System.out.println(move);
		                    	        		saveMove(gameId, joueur, move);
    	                    	        		
		                    	        		try {
													sendMove( String.valueOf(gameId), move, joueur);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
		                    	        		
    	                    	        		if (otherpiece.getColor().equals("white")) {
    	                    						joueur2Score += otherpiece.getValue();
    	                    						joueur1Score -= otherpiece.getValue();
    	                    						updateScoreLabels();
    	                    					}else {
    	                    						joueur1Score += otherpiece.getValue();
    	                    						joueur2Score -= otherpiece.getValue();
    	                    						updateScoreLabels();
    	                    					}

    	                    	        		clearSelections(panneau);
    	                    	        		
    	                    	        		gameState.switchTurn();
    	        	                            gameState.resetLastDoubleStepPawn();
    	        	                            selectedActivation(panneau);
    	        	                            
    	        	                            addLegalMoves(panneau);
    	                    	        	}
    	                    	        });
    	        	        		}
    	        	            }
    	        	        	
    		                	// prise d'un pion par un fou
    	        	        	if (c instanceof Pawn) {
    	        	        		Pawn otherPawn = (Pawn) c;
    	        	                // Ajouter une case de capture si piece ennemie
    	        	        		if (!otherPawn.getColor().equals(piece.getColor())) {
    	            	                captureChoice = new Choice(); 
    	            	                captureChoice.setBounds(targetCol * 100, targetRow * 100, 100, 100);                    	        		
    	            	                captureChoice.setBorderPainted(false);
    	        	                    captureChoice.setContentAreaFilled(false);
    	        	                    captureChoice.setFocusPainted(false);
    	        	                    captureChoice.setEnabled(true);
    	        	                    captureChoice.setVisible(true);
    	            	                panneau.add(captureChoice, 0);
    	            	                
    	            	                
    	                    	        	
    	            	                captureChoice.addActionListener(new ActionListener(){
    	                    	        	public void actionPerformed(ActionEvent e) {
    	                    	        		panneau.remove(otherPawn);
    	                    	        		piece.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	                    	        		
    	                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
		                    	        		String to = coordToNotation(targetRow, targetCol);
		                    	        		String move = null;
		                    	        		if (piece instanceof Bishop) {
		                    	        			move = "B:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Rook) {
		                    	        			move = "R:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Queen) {
		                    	        			move = "Q:" + from + "x" + to;
		                    	        		}
		                    	        		
		                    	        		System.out.println(move);
		                    	        		saveMove(gameId, joueur, move);
		                    	        		
		                    	        		try {
													sendMove( String.valueOf(gameId), move, joueur);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
		                    	        		
    	                    	        		pieceRow[0] = targetRow;
    	                    	        		pieceCol[0] = targetCol;
    	                    	        		
    	                    	        		if (otherPawn.getColor().equals("white")) {
    	                    						joueur2Score += otherPawn.getValue();
    	                    						joueur1Score -= otherPawn.getValue();
    	                    				        updateScoreLabels();
    	                    					}else {
    	                    						joueur1Score += otherPawn.getValue();
    	                    						joueur2Score -= otherPawn.getValue();
    	                    						updateScoreLabels();
    	                    					}
    	 
    	                    	        		clearSelections(panneau);
    	                    	        		
    	                    	        		gameState.switchTurn();
    	        	                            gameState.resetLastDoubleStepPawn();
    	        	                            selectedActivation(panneau);
    	        	                            
    	        	                            addLegalMoves(panneau);
    	                    	        	}
    	                    	        });
    	        	        		}
    	        	            }
    	    	            
    	        	        	// prise d'un cavalier par un fou
    	        	        	if (c instanceof Knight) {
    	        	        		Knight otherKnight = (Knight) c;
    	        	                // Ajouter une case de capture si piece ennemie
    	        	        		if (!otherKnight.getColor().equals(piece.getColor())) {
    	            	                captureChoice = new Choice(); 
    	            	                captureChoice.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	            	                captureChoice.setBorderPainted(false);
    	        	                    captureChoice.setContentAreaFilled(false);
    	        	                    captureChoice.setFocusPainted(false);
    	        	                    captureChoice.setEnabled(true);
    	        	                    captureChoice.setVisible(true);
    	            	                panneau.add(captureChoice, 0);
    	            	                
    	            	                
    	                    	        	
    	            	                captureChoice.addActionListener(new ActionListener(){
    	                    	        	public void actionPerformed(ActionEvent e) {
    	                    	        		panneau.remove(otherKnight);
    	                    	        		piece.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	                    	        		
    	                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
		                    	        		String to = coordToNotation(targetRow, targetCol);
		                    	        		String move = null;
		                    	        		if (piece instanceof Bishop) {
		                    	        			move = "B:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Rook) {
		                    	        			move = "R:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Queen) {
		                    	        			move = "Q:" + from + "x" + to;
		                    	        		}
		                    	        		
		                    	        		System.out.println(move);
		                    	        		saveMove(gameId, joueur, move);
		                    	        		
		                    	        		try {
													sendMove( String.valueOf(gameId), move, joueur);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
		                    	        		
    	                    	        		pieceRow[0] = targetRow;
    	                    	        		pieceCol[0] = targetCol;
    	                    	        		
    	                    	        		if (otherKnight.getColor().equals("white")) {
    	                    						joueur2Score += otherKnight.getValue();
    	                    						joueur1Score -= otherKnight.getValue();
    	                    				        updateScoreLabels();
    	                    					}else {
    	                    						joueur1Score += otherKnight.getValue();
    	                    						joueur2Score -= otherKnight.getValue();
    	                    						updateScoreLabels();
    	                    					}

    	                    	        		clearSelections(panneau);
    	                    	        		
    	                    	        		gameState.switchTurn();
    	        	                            gameState.resetLastDoubleStepPawn();
    	        	                            selectedActivation(panneau);
    	        	                            
    	        	                            addLegalMoves(panneau);
    	                    	        	}
    	                    	        });
    	        	        		}
    	        	            }
    	        	        	
    	        	        	// prise d'une tour par un fou
    	        	        	if (c instanceof Rook) {
    	        	        		Rook otherRook = (Rook) c;
    	        	                // Ajouter une case de capture si piece ennemie
    	        	        		if (!otherRook.getColor().equals(piece.getColor())) {
    	            	                captureChoice = new Choice(); 
    	            	                captureChoice.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	            	                captureChoice.setBorderPainted(false);
    	        	                    captureChoice.setContentAreaFilled(false);
    	        	                    captureChoice.setFocusPainted(false);
    	        	                    captureChoice.setEnabled(true);
    	        	                    captureChoice.setVisible(true);
    	            	                panneau.add(captureChoice, 0);
    	            	                
    	            	                
    	                    	        	
    	            	                captureChoice.addActionListener(new ActionListener(){
    	                    	        	public void actionPerformed(ActionEvent e) {
    	                    	        		panneau.remove(otherRook);
    	                    	        		piece.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	                    	        		
    	                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
		                    	        		String to = coordToNotation(targetRow, targetCol);
		                    	        		String move = null;
		                    	        		if (piece instanceof Bishop) {
		                    	        			move = "B:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Rook) {
		                    	        			move = "R:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Queen) {
		                    	        			move = "Q:" + from + "x" + to;
		                    	        		}
		                    	        		
		                    	        		System.out.println(move);
		                    	        		saveMove(gameId, joueur, move);
		                    	        		
		                    	        		try {
													sendMove( String.valueOf(gameId), move, joueur);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
		                    	        		
    	                    	        		pieceRow[0] = targetRow;
    	                    	        		pieceCol[0] = targetCol;
    	                    	        		
    	                    	        		if (otherRook.getColor().equals("white")) {
    	                    						joueur2Score += otherRook.getValue();
    	                    						joueur1Score -= otherRook.getValue();
    	                    				        updateScoreLabels();
    	                    					}else {
    	                    						joueur1Score += otherRook.getValue();
    	                    						joueur2Score -= otherRook.getValue();
    	                    						updateScoreLabels();
    	                    					}

    	                    	        		clearSelections(panneau);
    	                    	        		
    	                    	        		gameState.switchTurn();
    	        	                            gameState.resetLastDoubleStepPawn();
    	        	                            selectedActivation(panneau);
    	        	                            
    	        	                            addLegalMoves(panneau);
    	                    	        	}
    	                    	        });
    	        	        		}
    	        	            }
    	        	        	
    	        	        	// prise d'une reine par un fou
    	        	        	if (c instanceof Queen) {
    	        	        		Queen otherQueen = (Queen) c;
    	        	                // Ajouter une case de capture si piece ennemie
    	        	        		if (!otherQueen.getColor().equals(piece.getColor())) {
    	            	                captureChoice = new Choice(); 
    	            	                captureChoice.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	            	                captureChoice.setBorderPainted(false);
    	        	                    captureChoice.setContentAreaFilled(false);
    	        	                    captureChoice.setFocusPainted(false);
    	        	                    captureChoice.setEnabled(true);
    	        	                    captureChoice.setVisible(true);
    	            	                panneau.add(captureChoice, 0);
    	            	                
    	            	                
    	                    	        	
    	            	                captureChoice.addActionListener(new ActionListener(){
    	                    	        	public void actionPerformed(ActionEvent e) {
    	                    	        		panneau.remove(otherQueen);
    	                    	        		piece.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	                    	        		
    	                    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
		                    	        		String to = coordToNotation(targetRow, targetCol);
		                    	        		String move = null;
		                    	        		if (piece instanceof Bishop) {
		                    	        			move = "B:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Rook) {
		                    	        			move = "R:" + from + "x" + to;
		                    	        		}
		                    	        		if (piece instanceof Queen) {
		                    	        			move = "Q:" + from + "x" + to;
		                    	        		}
		                    	        		
		                    	        		System.out.println(move);
		                    	        		saveMove(gameId, joueur, move);
		                    	        		
		                    	        		try {
													sendMove( String.valueOf(gameId), move, joueur);
												} catch (IOException e1) {
													// TODO Auto-generated catch block
													e1.printStackTrace();
												}
		                    	        		
    	                    	        		pieceRow[0] = targetRow;
    	                    	        		pieceCol[0] = targetCol;
    	                    	        		
    	                    	        		if (otherQueen.getColor().equals("white")) {
    	                    						joueur2Score += otherQueen.getValue();
    	                    						joueur1Score -= otherQueen.getValue();
    	                    				        updateScoreLabels();
    	                    					}else {
    	                    						joueur1Score += otherQueen.getValue();
    	                    						joueur2Score -= otherQueen.getValue();
    	                    						updateScoreLabels();
    	                    					}

    	                    	        		clearSelections(panneau);
    	                    	        		
    	                    	        		gameState.switchTurn();
    	        	                            gameState.resetLastDoubleStepPawn();
    	        	                            selectedActivation(panneau);
    	        	                            
    	        	                            addLegalMoves(panneau);
    	                    	        	}
    	                    	        });
    	        	        		}
    	        	            }
    		                    break;
    		                }
    		            }
    		        }
    	    		
    	    		if (!isLongOccupied) {
    		        	// Ajoute un déplacement vide
    	    	        selectedPieceChoice = new Choice();
    	    	        selectedPieceChoice.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	                selectedPieceChoice.setBorderPainted(false);
    	                selectedPieceChoice.setContentAreaFilled(false);
    	                selectedPieceChoice.setFocusPainted(false);
    	                selectedPieceChoice.setEnabled(true);
    	                selectedPieceChoice.setVisible(true);
    	    	        panneau.add(selectedPieceChoice);
    	    	        
    	    	        selectedPieceChoice.addActionListener(new ActionListener(){
    	    	        	public void actionPerformed(ActionEvent e) {
    	    	        		piece.setBounds(targetCol * 100, targetRow * 100, 100, 100);
    	    	        		
    	    	        		String from = coordToNotation(pieceRow[0], pieceCol[0]);
            	        		String to = coordToNotation(targetRow, targetCol);
            	        		String move = null;
            	        		if (piece instanceof Bishop) {
            	        			move = "B:" + from + "->" + to;
            	        		}
            	        		if (piece instanceof Rook) {
            	        			move = "R:" + from + "->" + to;
            	        		}
            	        		if (piece instanceof Queen) {
            	        			move = "Q:" + from + "->" + to;
            	        		}
            	        		
            	        		System.out.println(move);
            	        		saveMove(gameId, joueur, move);
            	        		
            	        		try {
									sendMove( String.valueOf(gameId), move, joueur);
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
            	        		
    	    	        		pieceRow[0] = targetRow;
    	    	        		pieceCol[0] = targetCol;

    	    	        		clearSelections(panneau);
    	    	        		
    	    	        		gameState.switchTurn();
    	                        gameState.resetLastDoubleStepPawn();
    	                        selectedActivation(panneau);
    	                        
    	                        addLegalMoves(panneau);
    	    	        	}
    	    	        }); 
    	    		} else {
    	    			break;
    	    		}
    	    	}
    	    }
    	}
		
	}
	
	public void knightMoves (Knight knight, int[] knightRow, int[] knightCol, int nextRow, int nextCol, JPanel panneau) {
		selectedPieceChoice = new Choice ();
        selectedPieceChoice.setBounds(nextCol * 100, nextRow * 100, 100, 100);
        selectedPieceChoice.setBorderPainted(false);
        selectedPieceChoice.setContentAreaFilled(false);
        selectedPieceChoice.setFocusPainted(false);
        selectedPieceChoice.setEnabled(true);
        selectedPieceChoice.setVisible(true);
        panneau.add(selectedPieceChoice);
        
        selectedPieceChoice.addActionListener(new ActionListener() {
        	public void actionPerformed (ActionEvent e) {
        		knight.setBounds(nextCol * 100, nextRow * 100, 100, 100);
        		
        		String from = coordToNotation(knightRow[0], knightCol[0]);
        		String to = coordToNotation(nextRow, nextCol);        		
        		String move = "N:" + from + "->" + to;
        		
        		
        		System.out.println(move);
        		saveMove(gameId, joueur, move);
        		
        		try {
					sendMove( String.valueOf(gameId), move, joueur);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        		
        		knightRow[0] = nextRow;
        		knightCol[0] = nextCol;
        		
        		clearSelections(panneau);
        		
        		gameState.switchTurn();
                gameState.resetLastDoubleStepPawn();
                selectedActivation(panneau);
                
                addLegalMoves(panneau);
        	}
        });
        
        
        for (Component c : panneau.getComponents()) {
        	if (c instanceof JButton && c != knight) {
        		Rectangle r = c.getBounds();
        		int cRow = r.y / 100;
        		int cCol = r.x / 100;
        		
        		if (cRow == nextRow && cCol == nextCol) {
        			if (willKingBeInCheck(knight, cRow, cCol, panneau)) {
        				continue;
        			}
        			// prise d'un pion par un cavalier
        			if (c instanceof Pawn) {
        				Pawn otherPawn = (Pawn) c;
        				if (!otherPawn.getColor().equals(knight.getColor())) {
        					captureChoice = new Choice();
        					captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
        					captureChoice.setBorderPainted(false);
    	                    captureChoice.setContentAreaFilled(false);
    	                    captureChoice.setFocusPainted(false);
    	                    captureChoice.setEnabled(true);
    	                    captureChoice.setVisible(true);
        	                panneau.add(captureChoice, 0);
        	                
        	                captureChoice.addActionListener(new ActionListener() {
        	                	public void actionPerformed (ActionEvent e) {
        	                		panneau.remove(otherPawn);
        	                		knight.setBounds(cCol * 100, cRow * 100, 100, 100);
        	                		
        	                		String from = coordToNotation(knightRow[0], knightCol[0]);
        	                		String to = coordToNotation(cRow, cCol);        		
        	                		String move = "N:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
        	                		knightRow[0] = cRow;
                	        		knightCol[0] = cCol;
                	        		
                	        		if (otherPawn.getColor().equals("white")) {
                						joueur2Score += otherPawn.getValue();
                						joueur1Score -= otherPawn.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherPawn.getValue();
                						joueur2Score -= otherPawn.getValue();
                						updateScoreLabels();
                					}
                	        		
                	        		clearSelections(panneau);
                	        		
                	        		gameState.switchTurn();
    	                            gameState.resetLastDoubleStepPawn();
    	                            selectedActivation(panneau);
    	                            
    	                            addLegalMoves(panneau);
        	                	}
        	                });
        				}
        			}
        			
        			// prise d'un fou par un cavalier
        			if (c instanceof Bishop) {
        				Bishop otherBishop = (Bishop) c;
        				if (!otherBishop.getColor().equals(knight.getColor())) {
        					captureChoice = new Choice();
        					captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
        					captureChoice.setBorderPainted(false);
    	                    captureChoice.setContentAreaFilled(false);
    	                    captureChoice.setFocusPainted(false);
    	                    captureChoice.setEnabled(true);
    	                    captureChoice.setVisible(true);
        	                panneau.add(captureChoice, 0);
        	                
        	                captureChoice.addActionListener(new ActionListener() {
        	                	public void actionPerformed (ActionEvent e) {
        	                		panneau.remove(otherBishop);
        	                		knight.setBounds(cCol * 100, cRow * 100, 100, 100);
        	                		
        	                		String from = coordToNotation(knightRow[0], knightCol[0]);
        	                		String to = coordToNotation(cRow, cCol);        		
        	                		String move = "N:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
        	                		knightRow[0] = cRow;
                	        		knightCol[0] = cCol;
                	        		
                	        		if (otherBishop.getColor().equals("white")) {
                						joueur2Score += otherBishop.getValue();
                						joueur1Score -= otherBishop.getValue();
                						updateScoreLabels();
                					}else {
                						joueur1Score += otherBishop.getValue();
                						joueur2Score -= otherBishop.getValue();
                						updateScoreLabels();
                					}
                	        		
                	        		clearSelections(panneau);
                	        		
                	        		gameState.switchTurn();
    	                            gameState.resetLastDoubleStepPawn();
    	                            selectedActivation(panneau);
    	                            
    	                            addLegalMoves(panneau);
        	                	}
        	                });
        				}
        			}
        			
        			// prise d'un cavalier par un cavalier
        			if (c instanceof Knight) {
        				Knight otherKnight = (Knight) c;
        				if (!otherKnight.getColor().equals(knight.getColor())) {
        					captureChoice = new Choice();
        					captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
        					captureChoice.setBorderPainted(false);
    	                    captureChoice.setContentAreaFilled(false);
    	                    captureChoice.setFocusPainted(false);
    	                    captureChoice.setEnabled(true);
    	                    captureChoice.setVisible(true);
        	                panneau.add(captureChoice, 0);
        	                
        	                captureChoice.addActionListener(new ActionListener() {
        	                	public void actionPerformed (ActionEvent e) {
        	                		panneau.remove(otherKnight);
        	                		knight.setBounds(cCol * 100, cRow * 100, 100, 100);
        	                		
        	                		String from = coordToNotation(knightRow[0], knightCol[0]);
        	                		String to = coordToNotation(cRow, cCol);        		
        	                		String move = "N:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
        	                		knightRow[0] = cRow;
                	        		knightCol[0] = cCol;
                	        		
                	        		if (otherKnight.getColor().equals("white")) {
                						joueur2Score += otherKnight.getValue();
                						joueur1Score -= otherKnight.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherKnight.getValue();
                						joueur2Score -= otherKnight.getValue();
                						updateScoreLabels();
                					}
                	        		
                	        		clearSelections(panneau);
                	        		
                	        		gameState.switchTurn();
    	                            gameState.resetLastDoubleStepPawn();
    	                            selectedActivation(panneau);
    	                            
    	                            addLegalMoves(panneau);
        	                	}
        	                });
        				}
        			}
        			
        			// prise d'une tour par un cavalier
        			if (c instanceof Rook) {
        				Rook otherRook = (Rook) c;
        				if (!otherRook.getColor().equals(knight.getColor())) {
        					captureChoice = new Choice();
        					captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
        					captureChoice.setBorderPainted(false);
    	                    captureChoice.setContentAreaFilled(false);
    	                    captureChoice.setFocusPainted(false);
    	                    captureChoice.setEnabled(true);
    	                    captureChoice.setVisible(true);
        	                panneau.add(captureChoice, 0);
        	                
        	                captureChoice.addActionListener(new ActionListener() {
        	                	public void actionPerformed (ActionEvent e) {
        	                		panneau.remove(otherRook);
        	                		knight.setBounds(cCol * 100, cRow * 100, 100, 100);
        	                		
        	                		String from = coordToNotation(knightRow[0], knightCol[0]);
        	                		String to = coordToNotation(cRow, cCol);        		
        	                		String move = "N:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
        	                		knightRow[0] = cRow;
                	        		knightCol[0] = cCol;
                	        		
                	        		if (otherRook.getColor().equals("white")) {
                						joueur2Score += otherRook.getValue();
                						joueur1Score -= otherRook.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherRook.getValue();
                						joueur2Score -= otherRook.getValue();
                						updateScoreLabels();
                					}
                	        		
                	        		clearSelections(panneau);
                	        		
                	        		gameState.switchTurn();
    	                            gameState.resetLastDoubleStepPawn();
    	                            selectedActivation(panneau);
    	                            
    	                            addLegalMoves(panneau);
        	                	}
        	                });
        				}
        			}
        			
        			// prise d'une reine par un cavalier
        			if (c instanceof Queen) {
        				Queen otherQueen = (Queen) c;
        				if (!otherQueen.getColor().equals(knight.getColor())) {
        					captureChoice = new Choice();
        					captureChoice.setBounds(cCol * 100, cRow * 100, 100, 100);
        					captureChoice.setBorderPainted(false);
    	                    captureChoice.setContentAreaFilled(false);
    	                    captureChoice.setFocusPainted(false);
    	                    captureChoice.setEnabled(true);
    	                    captureChoice.setVisible(true);
        	                panneau.add(captureChoice, 0);
        	                
        	                captureChoice.addActionListener(new ActionListener() {
        	                	public void actionPerformed (ActionEvent e) {
        	                		panneau.remove(otherQueen);
        	                		knight.setBounds(cCol * 100, cRow * 100, 100, 100);
        	                		
        	                		String from = coordToNotation(knightRow[0], knightCol[0]);
        	                		String to = coordToNotation(cRow, cCol);        		
        	                		String move = "N:" + from + "x" + to;
        	                		
        	                		
        	                		System.out.println(move);
        	                		saveMove(gameId, joueur, move);
        	                		
        	                		try {
										sendMove( String.valueOf(gameId), move, joueur);
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
        	                		
        	                		knightRow[0] = cRow;
                	        		knightCol[0] = cCol;
                	        		
                	        		if (otherQueen.getColor().equals("white")) {
                						joueur2Score += otherQueen.getValue();
                						joueur1Score -= otherQueen.getValue();
                				        updateScoreLabels();
                					}else {
                						joueur1Score += otherQueen.getValue();
                						joueur2Score -= otherQueen.getValue();
                						updateScoreLabels();
                					}
                	        		
                	        		clearSelections(panneau);
                	        		
                	        		gameState.switchTurn();
    	                            gameState.resetLastDoubleStepPawn();
    	                            selectedActivation(panneau);
    	                            
    	                            addLegalMoves(panneau);
        	                	}
        	                });
        				}
        			}
        		}
        	}
        }
	}
	
	private void initialize (int i, int j, JPanel panneauArmée) {
		if (Menu2.color == "white") {
			// ajout des pions
	        if (i==6) {
	        	final int ligne = i;
	            final int colonne = j;
	            addPawn(ligne, colonne, "white", panneauArmée);                 
	            
	        }
	        
	        if (i==1) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addPawn(ligne, colonne, "black", panneauArmée);
	        }
	        
	        // ajout des fous
	        if (i==7 && j == 3) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==7 && j == 6) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==1 && j == 3) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "black", panneauArmée);
	        }
	        
	        if (i==1 && j == 6) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "black", panneauArmée);
	        }
	        
	        // ajout des cavaliers
	        if (i==7 && j == 2) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==7 && j == 7) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==1 && j == 2) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "black", panneauArmée);
	        }
	        
	        if (i==1 && j == 7) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "black", panneauArmée);
	        }
	        
	        // ajout des tours
	        if (i==7 && j == 1) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==7 && j == 8) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==1 && j == 1) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "black", panneauArmée);
	        }
	        
	        if (i==1 && j == 8) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "black", panneauArmée);
	        }
	        
	     // ajout des reines
	        if (i==7 && j == 4) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addQueen(ligne, colonne, "white", panneauArmée);
	        }
	                        
	        if (i==1 && j == 4) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addQueen(ligne, colonne, "black", panneauArmée);
	        }
	        
	     // ajout des rois
	        if (i==7 && j == 5) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addKing(ligne, colonne, "white", panneauArmée);
	        }
	                        
	        if (i==1 && j == 5) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addKing(ligne, colonne, "black", panneauArmée);
	        }
		} 
		else if (Menu2.color == "black") {
			// ajout des pions
	        if (i==1) {
	        	final int ligne = i;
	            final int colonne = j;
	            addPawn(ligne, colonne, "white", panneauArmée);                 
	            
	        }
	        
	        if (i==6) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addPawn(ligne, colonne, "black", panneauArmée);
	        }
	        
	        // ajout des fous
	        if (i==1 && j == 3) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==1 && j == 6) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==7 && j == 3) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "black", panneauArmée);
	        }
	        
	        if (i==7 && j == 6) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addBishop(ligne, colonne, "black", panneauArmée);
	        }
	        
	        // ajout des cavaliers
	        if (i==1 && j == 2) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==1 && j == 7) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==7 && j == 2) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "black", panneauArmée);
	        }
	        
	        if (i==7 && j == 7) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addKnight(ligne, colonne, "black", panneauArmée);
	        }
	        
	        // ajout des tours
	        if (i==1 && j == 1) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==1 && j == 8) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "white", panneauArmée);
	        }
	        
	        if (i==7 && j == 1) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "black", panneauArmée);
	        }
	        
	        if (i==7 && j == 8) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addRook(ligne, colonne, "black", panneauArmée);
	        }
	        
	     // ajout des reines
	        if (i==1 && j == 4) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addQueen(ligne, colonne, "white", panneauArmée);
	        }
	                        
	        if (i==7 && j == 4) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addQueen(ligne, colonne, "black", panneauArmée);
	        }
	        
	     // ajout des rois
	        if (i==1 && j == 5) {
	        	final int ligne = i-1;
	            final int colonne = j;
	
	            addKing(ligne, colonne, "white", panneauArmée);
	        }
	                        
	        if (i==7 && j == 5) {
	        	final int ligne = i;
	            final int colonne = j;
	
	            addKing(ligne, colonne, "black", panneauArmée);
	        }
		}
	}
	
	public static String coordToNotation(int row, int col) {
	    char file = (char) ('a' + col);       // colonne -> lettre
	    int rank = 8 - row;                   // ligne -> chiffre
	    return "" + file + rank;
	}
	
	public static void saveMove(int gameId, String player, String move) {
	    try (Connection conn = Database.getConnection()) {
	        PreparedStatement stmt = conn.prepareStatement(
	            "INSERT INTO moves (game_id, player, move) VALUES (?, ?, ?)"
	        );
	        stmt.setInt(1, gameId);
	        stmt.setString(2, player);
	        stmt.setString(3, move);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	public static String getLastMove(int gameId, String opponent) {
	    try (Connection conn = Database.getConnection()) {
	        PreparedStatement stmt = conn.prepareStatement(
	            "SELECT move FROM moves WHERE game_id = ? AND player = ? ORDER BY timestamp DESC LIMIT 1"
	        );
	        stmt.setInt(1, gameId);
	        stmt.setString(2, opponent);
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("move");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	
	public static int createGame(String joueur1, String joueur2) {
	    try (Connection conn = Database.getConnection()) {
	        PreparedStatement stmt = conn.prepareStatement(
	            "INSERT INTO games (player1, player2) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS
	        );
	        stmt.setString(1, joueur1);
	        stmt.setString(2, joueur2);
	        stmt.executeUpdate();
	        
	        ResultSet rs = stmt.getGeneratedKeys();
	        if (rs.next()) {
	            return rs.getInt(1); // retourne l'ID de la partie
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return -1;
	}
	
	private void applyMoveToBoard(String move, JPanel panneau) {
	    if (move.equals("O-O") || move.equals("O-O-O")) {
	        handleCastling(move, panneau);
	        return;
	    }

	    boolean isCapture = move.contains("x");
	    boolean isEnPassant = move.endsWith("e.p.");
	    
	    try {
	        String pieceType = move.substring(0, 1); // "P", "N", "B", "R", "Q", "K"
	        String from, to;

	        if (isEnPassant) {
	            from = move.substring(2, 4); 
	            to = move.substring(5, 7);   
	        } else if (isCapture) {
	            from = move.substring(2, 4); 
	            to = move.substring(5, 7);   
	        } else {
	            from = move.substring(2, 4);
	            to = move.substring(4, 6);
	        }

	        int fromRow = 8 - Character.getNumericValue(from.charAt(1));
	        int fromCol = from.charAt(0) - 'a';
	        int toRow = 8 - Character.getNumericValue(to.charAt(1));
	        int toCol = to.charAt(0) - 'a';

	        ColoredPiece pieceToMove = findPieceAt(fromRow, fromCol, panneau);
	        if (pieceToMove == null) return;

	        if (isCapture) {
	            for (Component c : panneau.getComponents()) {
	    	        if (c instanceof Pawn && !((Pawn) c).getColor().equals(Menu2.color)) {
	    	            Rectangle bounds = c.getBounds();
	    	            int x = bounds.x / 100;
	    	            int y = bounds.y / 100;
	    	            if (x == toCol && y == toRow) {
	    	                panneau.remove(c);
	    	            }
	    	        }
	    	        if (c instanceof Bishop && !((Bishop) c).getColor().equals(Menu2.color)) {
	    	            Rectangle bounds = c.getBounds();
	    	            int x = bounds.x / 100;
	    	            int y = bounds.y / 100;
	    	            if (x == toCol && y == toRow) {
	    	                panneau.remove(c);
	    	            }
	    	        }
	    	        if (c instanceof Knight && !((Knight) c).getColor().equals(Menu2.color)) {
	    	            Rectangle bounds = c.getBounds();
	    	            int x = bounds.x / 100;
	    	            int y = bounds.y / 100;
	    	            if (x == toCol && y == toRow) {
	    	                panneau.remove(c);
	    	            }
	    	        }
	    	        if (c instanceof Rook && !((Rook) c).getColor().equals(Menu2.color)) {
	    	            Rectangle bounds = c.getBounds();
	    	            int x = bounds.x / 100;
	    	            int y = bounds.y / 100;
	    	            if (x == toCol && y == toRow) {
	    	                panneau.remove(c);
	    	            }
	    	        }
	    	        if (c instanceof Queen && !((Queen) c).getColor().equals(Menu2.color)) {
	    	            Rectangle bounds = c.getBounds();
	    	            int x = bounds.x / 100;
	    	            int y = bounds.y / 100;
	    	            if (x == toCol && y == toRow) {
	    	                panneau.remove(c);
	    	            }
	    	        }
	    	    }

	            // Si prise en passant, retire le pion capturé derrière
	            if (isEnPassant && pieceType.equals("P")) {
	                int capturedRow = pieceToMove.getColor().equals("white") ? toRow + 1 : toRow - 1;
	                for (Component c : panneau.getComponents()) {
		    	        if (c instanceof Pawn && !((Pawn) c).getColor().equals(Menu2.color)) {
		    	            Rectangle bounds = c.getBounds();
		    	            int x = bounds.x / 100;
		    	            int y = bounds.y / 100;
		    	            if (x == toCol && y == capturedRow) {
		    	                panneau.remove(c);
		    	            }
		    	        }
		    	    }
	            }
	        }

	        // Déplacement de la pièce
	        pieceToMove.setBounds(toCol * 100, toRow * 100, 100, 100);
	        panneau.repaint();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	private void handleCastling(String move, JPanel panneau) {
		for (Component c : panneau.getComponents()) {
			if (c instanceof King && !((King) c).getColor().equals(Menu2.color)) {
				King king = (King) c;
				
				Rectangle k = c.getBounds();
				int kingRow = k.y / 100;
				int kingCol = k.x / 100;
				if (move.equals("O-O")) {
					for (Component a : panneau.getComponents()) {
						if (a instanceof Rook && !((Rook) a).getColor().equals(Menu2.color)) {
							Rook rook = (Rook) a;
							int rookRow = a.getBounds().y / 100;
							int rookCol = a.getBounds().x / 100;
							
							if (rookCol == 7 && rookRow == kingRow) {
								king.setBounds((kingCol + 2) * 100, kingRow * 100, 100, 100);
								rook.setBounds((rookCol - 2) * 100, rookRow * 100, 100, 100);
							}
						}
					}
				}
				
				if (move.equals("O-O-O")) {
					for (Component a : panneau.getComponents()) {
						if (a instanceof Rook && !((Rook) a).getColor().equals(Menu2.color)) {
							Rook rook = (Rook) a;
							int rookRow = a.getBounds().y / 100;
							int rookCol = a.getBounds().x / 100;
							
							if (rookCol == 0 && rookRow == kingRow) {
								king.setBounds((kingCol - 2) * 100, kingRow * 100, 100, 100);
								rook.setBounds((rookCol + 3) * 100, rookRow * 100, 100, 100);
							}
						}
					}
				}
			}
		}
	}

	private ColoredPiece findPieceAt(int row, int col, JPanel panneau) {
	    for (Component c : panneau.getComponents()) {
	        if (c instanceof ColoredPiece) {
	            Rectangle bounds = c.getBounds();
	            int x = bounds.x / 100;
	            int y = bounds.y / 100;
	            if (x == col && y == row) {
	                return (ColoredPiece) c;
	            }
	        }
	    }
	    return null;
	}
	
	public static void sendMove(String gameId, String move, String player) throws IOException {
	    URL url = new URL("http://localhost:8080/moves"); //mettre l'url render quand ca voudra bien fonctionner
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("POST");
	    conn.setRequestProperty("Content-Type", "application/json");
	    conn.setDoOutput(true);

	    String json = String.format("{\"gameId\":\"%s\", \"move\":\"%s\", \"player\":\"%s\"}", gameId, move, player);

	    try (OutputStream os = conn.getOutputStream()) {
	        byte[] input = json.getBytes("utf-8");
	        os.write(input, 0, input.length);
	    }

	    int responseCode = conn.getResponseCode();
	    if (responseCode != 200 && responseCode != 201) {
	        throw new IOException("Erreur lors de l'envoi du coup : HTTP " + responseCode);
	    }

	    conn.disconnect();
	}
	
	public static String getLastOpponentMove(String gameId, String player) throws IOException {
	    URL url = new URL("http://localhost:8080/moves/" + gameId + "/last/" + player); //mettre l'url render quand ca voudra bien fonctionner
	    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	    conn.setRequestMethod("GET");
	    conn.setRequestProperty("Accept", "application/json");

	    int responseCode = conn.getResponseCode();
	    if (responseCode != 200) {
	        throw new IOException("Erreur lors de la récupération du coup : HTTP " + responseCode);
	    }

	    try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"))) {
	        StringBuilder response = new StringBuilder();
	        String responseLine;
	        while ((responseLine = br.readLine()) != null) {
	            response.append(responseLine.trim());
	        }
	        
	        return response.toString();
	    }
	}
}

