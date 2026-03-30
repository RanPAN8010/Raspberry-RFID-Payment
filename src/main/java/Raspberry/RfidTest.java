package Raspberry;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Classe de test pour valider le fonctionnement du matériel RC522.
 * Utilise une approche hybride en appelant un script Python pour lire les tags RFID.
 */
public class RfidTest {
	
	/**
     * Point d'entrée principal pour le test du lecteur RFID.
     *
     * @param args Arguments de la ligne de commande.
     */
    public static void main(String[] args) {
        System.out.println("=== Démarrage du test de lecture matérielle RC522 (Mode Hybride) ===");
        System.out.println("Veuillez approcher la carte RFID du module RC522...");

        try {
        	// Appeler le script Python dans le répertoire racine
            ProcessBuilder pb = new ProcessBuilder("python3", "read_rfid.py");
            Process process = pb.start();

            // Capturer la sortie de la console du script Python
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            // Tant que le script Python affiche du contenu, il sera capturé ici
            while ((line = reader.readLine()) != null) {
                String rfidTag = line.trim();
                if (!rfidTag.isEmpty()) {
                    System.out.println("Numéro de carte physique lu avec succès : [" + rfidTag + "]");
                }
            }
            
            process.waitFor(); // Attendre la fin du processus
            
        } catch (Exception e) {
            System.err.println("Échec de l'appel du script Python : " + e.getMessage());
            e.printStackTrace();
        }
    }
}