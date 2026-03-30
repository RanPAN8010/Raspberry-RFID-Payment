package Raspberry;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Classe utilitaire pour l'interaction avec le lecteur de cartes RFID physique.
 * Appelle un script Python externe pour capturer les données du matériel.
 */
public class RFIDReader {

	/**
     * Bloque le thread actuel et appelle un script Python en attendant un scan de carte physique.
     *
     * @return Le numéro du tag RFID sous forme de chaîne de caractères, ou null en cas d'échec.
     */
    public static String waitForCardSwipe() {
        System.out.println("En attente de l'approche d'une carte physique du lecteur...");
        try {
            // ajout de l'argument "-u" 
        	// pour forcer une sortie Python en temps réel sans cache !
            ProcessBuilder pb = new ProcessBuilder("python3", "-u", "read_rfid.py");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String rfidTag = line.trim();

                if (!rfidTag.isEmpty() && rfidTag.matches("\\d+")) {
                    System.out.println("Numéro de carte intercepté avec succès : " + rfidTag);

                    // Tuer immédiatement le processus Python après lecture 
                    // pour libérer les broches matérielles
                    process.destroy();
                    return rfidTag;
                } else {
                    // Si un bruit tel que RuntimeWarning est lu, 
                	// l'ignorer et attendre la ligne suivante
                    System.out.println("Bruit filtré, non-numéro de carte : " + rfidTag);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Échec de l'appel au module de lecture : " + e.getMessage());
        }
        return null;
    }
}