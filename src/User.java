import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class User implements Serializable {
	private String email;
	private String Id;
	private String Mdp;
	private int victoire;
	private int defaite;
	private int nul;
	private int Elo;
	private int victoireVS;
	private int defaiteVS;
	private int nulVS;
	private boolean connected;
	private static final long serialVersionUID = 1L;
	
	public User (String email, String Id, String Mdp) {
		this.email = email;
		this.Id = Id;
		this.Mdp = Mdp;
		this.victoire = 0;
		this.defaite = 0;
		this.nul = 0;
		this.Elo = 100;
		this.victoireVS = 0;
		this.defaiteVS = 0;
		this.nulVS = 0;
	}
	
	public static boolean saveUser(User user) {
	    String checkSql = "SELECT COUNT(*) FROM user WHERE email = ? OR identifiant = ?";
	    String insertSql = "INSERT INTO user (email, identifiant, mot_de_passe) VALUES (?, ?, ?)";

	    try (Connection conn = Database.getConnection();
	         PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

	        checkStmt.setString(1, user.getEmail());
	        checkStmt.setString(2, user.getId());

	        ResultSet rs = checkStmt.executeQuery();
	        if (rs.next() && rs.getInt(1) > 0) {
	            System.out.println("Utilisateur déjà existant.");
	            return false; // utilisateur déjà en base
	        }

	        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
	            insertStmt.setString(1, user.getEmail());
	            insertStmt.setString(2, user.getId());
	            insertStmt.setString(3, user.getMdp());

	            insertStmt.executeUpdate();
	            System.out.println("Utilisateur ajouté avec succès.");
	            return true;
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        return false;
	    }
	}

	
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getMdp() {
		return Mdp;
	}

	public void setMdp(String mdp) {
		Mdp = mdp;
	}

	public int getVictoire() {
		return victoire;
	}

	public void setVictoire(int victoire) {
		this.victoire = victoire;
	}

	public int getDefaite() {
		return defaite;
	}

	public void setDefaite(int defaite) {
		this.defaite = defaite;
	}

	public int getNul() {
		return nul;
	}

	public void setNul(int nul) {
		this.nul = nul;
	}

	public int getElo() {
		return Elo;
	}

	public void setElo(int elo) {
		Elo = elo;
	}

	public int getVictoireVS() {
		return victoireVS;
	}

	public void setVictoireVS(int victoireVS) {
		this.victoireVS = victoireVS;
	}

	public int getDefaiteVS() {
		return defaiteVS;
	}

	public void setDefaiteVS(int defaiteVS) {
		this.defaiteVS = defaiteVS;
	}

	public int getNulVS() {
		return nulVS;
	}

	public void setNulVS(int nulVS) {
		this.nulVS = nulVS;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}
