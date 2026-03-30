package Raspberry.DAO;

import Raspberry.database.DBConnection;
import Raspberry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructeur par défaut de la classe UserDAO.
 */
public class UserDAO {

	/**
     * Constructeur par défaut de la classe UserDAO.
     */
	public UserDAO() {
		// TODO Auto-generated constructor stub
	}
	
	/**
     * Ajoute un nouvel utilisateur dans la base de données.
     *
     * @param user L'objet utilisateur à ajouter.
     * @return true si l'ajout est réussi, sinon false.
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users(username, rfid_tag, balance, role, active) VALUES(?, ?, ?, ?, ?)";
        // Syntaxe try-with-resources 
        //pour garantir la fermeture automatique de la connexion après utilisation
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRfidTag());
            pstmt.setDouble(3, user.getBalance());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.isActive() ? 1 : 0); // SQLite 没有 boolean，用 1 和 0 代替
            
            pstmt.executeUpdate();
            System.out.println("Ajout de l'utilisateur réussi : " + user.getUsername());
            return true;
            
        } catch (SQLException e) {
            System.err.println("Échec de l'ajout de l'utilisateur (peut-être que le RFID est déjà lié) : " + e.getMessage());
            return false;
        }
    }

    /**
     * Recherche un utilisateur par son tag RFID.
     *
     * @param rfidTag Le code du tag RFID.
     * @return L'objet User correspondant, ou null s'il n'est pas trouvé.
     */
    public User getUserByRfid(String rfidTag) {
        String sql = "SELECT * FROM users WHERE rfid_tag = ?";
        Connection conn = DBConnection.getConnection();
        
        // si la connexion échoue, retourner null directement et ne pas exécuter la suite
        if (conn == null) {
            System.err.println("Impossible d'obtenir la connexion à la base de données, requête interrompue.");
            return null; 
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rfidTag);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // Si ce RFID est trouvé dans la base de données, 
            	// l'encapsuler dans un objet User et le retourner
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRfidTag(rs.getString("rfid_tag"));
                user.setBalance(rs.getDouble("balance"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getInt("active") == 1);
                return user;
            }
            
        } catch (SQLException e) {
            System.err.println("Échec de la recherche de l'utilisateur : " + e.getMessage());
        }
        return null; 
    }
    
    /**
     * Met à jour le solde d'un utilisateur.
     *
     * @param rfidTag Le tag RFID de l'utilisateur.
     * @param newBalance Le nouveau montant du solde.
     * @return true si la mise à jour est réussie, sinon false.
     */
    public boolean updateBalance(String rfidTag, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE rfid_tag = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, rfidTag);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Échec de la mise à jour du solde : " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Enregistre une nouvelle transaction dans l'historique.
     *
     * @param tag Le tag RFID concerné.
     * @param type Le type de transaction (ex: recharge, débit).
     * @param montant Le montant de la transaction.
     */
    public void addTransaction(String tag, String type, double montant) {
        String sql = "INSERT INTO transactions(rfid_tag, type, montant) VALUES(?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            pstmt.setString(2, type);
            pstmt.setDouble(3, montant);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur d'enregistrement de transaction : " + e.getMessage());
        }
    }
    
    /**
     * Récupère la liste de tous les utilisateurs.
     *
     * @return Une liste d'objets User.
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
     // Classer par ID en ordre décroissant, les nouveaux inscrits apparaissent en premier
        String sql = "SELECT * FROM users ORDER BY id DESC"; 
        
        // vérifier manuellement la connexion
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
        	// Retourner une liste vide au lieu de null 
        	// pour éviter les erreurs de pointeur nul lors des appels externes
            return userList; 
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRfidTag(rs.getString("rfid_tag"));
                user.setBalance(rs.getDouble("balance"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getInt("active") == 1);
                
                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Échec de la récupération de la liste des utilisateurs : " + e.getMessage());
        }
        return userList;
    }
    
}