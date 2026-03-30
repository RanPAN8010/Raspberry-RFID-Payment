package Raspberry.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe utilitaire pour la gestion de la connexion à la base de données SQLite.
 * Fournit des méthodes pour obtenir une connexion et initialiser la structure des tables.
 */
public class DBConnection {
    // Chemin du fichier de la base de données, 
	// payment_system.db sera généré automatiquement à la racine du projet
    private static final String URL = "jdbc:sqlite:data/payment_system.db";
    private static Connection connection = null;

    /**
     * Obtient la connexion à la base de données.
     * Si la connexion est nulle ou fermée, elle est initialisée et les tables sont créées si nécessaire.
     *
     * @return L'objet Connection actif.
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                System.out.println("Connexion réussie à la SQLite !");
                
                initDatabase();
            }
        } catch (Exception e) {
            System.err.println("Échec de la connexion à la base de données : " + e.getMessage());
        }
        return connection;
    }
    
    /**
     * Initialise la base de données en créant les tables nécessaires.
     * Remplit les exigences du document : liaison RFID, gestion du solde et distinction des rôles.
     */
    public static void initDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL,"
                + "rfid_tag TEXT UNIQUE NOT NULL," // Correspond au badge RFID
                + "balance REAL DEFAULT 0.0,"      // Correspond aux fonctions de recharge et de paiement
                + "role TEXT CHECK(role IN ('ADMIN', 'USER', 'MERCHANT'))," // Correspond aux trois types de rôles
                + "active INTEGER DEFAULT 1"       // 1 pour actif, 0 pour désactivé
                + ");";

        String sqlTransactions = "CREATE TABLE IF NOT EXISTS transactions ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " rfid_tag TEXT NOT NULL,"
                + " type TEXT NOT NULL," // 'PAIEMENT' ou 'RECHARGE'
                + " montant REAL NOT NULL,"
                + " date_heure DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            stmt.execute(sqlTransactions);
            System.out.println("La structure des tables de la base de données est prête.");
        } catch (SQLException e) {
            System.err.println("Échec de la création des tables :" + e.getMessage());
        }      
    }

}
