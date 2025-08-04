import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {
	public static boolean stayConnected = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Preferences prefs = Preferences.userRoot().node("Chess");
        stayConnected = prefs.getBoolean("stayConnected", false); // false par défaut
        
        Database.getConnection();
        System.out.println("Chargé : stayConnected = " + stayConnected);
        if(stayConnected) {
        	new Menu2();
        } else {
        	new Menu();
        }
	}
}
