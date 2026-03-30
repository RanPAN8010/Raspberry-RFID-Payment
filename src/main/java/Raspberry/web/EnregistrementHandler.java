package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire HTTP pour l'enregistrement des nouveaux utilisateurs.
 * Traite l'affichage du formulaire et la soumission des données d'inscription.
 */
public class EnregistrementHandler implements HttpHandler {
    private UserDAO userDAO = new UserDAO();

    /**
     * Gère les requêtes HTTP entrantes pour l'enregistrement.
     *
     * @param exchange L'objet HttpExchange contenant la requête et la réponse.
     * @throws IOException En cas d'erreur lors de la lecture des fichiers ou de l'envoi de la réponse.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            String query = exchange.getRequestURI().getQuery();

            // Si aucun paramètre n'est présent, afficher le formulaire d'enregistrement
            if (query == null || query.trim().isEmpty()) {
                response = loadHtmlTemplate("enregistrement.html");
            } else {
                // Traiter les données soumises
                response = handleRegistration(query);
            }
        } catch (Exception e) {
            // capturer toutes les erreurs possibles pour éviter ERR_EMPTY_RESPONSE
            e.printStackTrace(); 
            response = "<html><body><h1>Erreur Interne</h1><p>" + e.getMessage() + "</p></body></html>";
        }
        SimpleHttpServer.sendResponse(exchange, response);
    }

    /**
     * Charge le modèle HTML pour la page d'enregistrement.
     *
     * @param fileName Nom du fichier HTML à charger.
     * @return Le contenu du fichier HTML sous forme de chaîne de caractères.
     * @throws IOException Si le fichier ne peut pas être lu.
     */
    private String loadHtmlTemplate(String fileName) throws IOException {
    	// Lever directement une exception pour qu'elle soit gérée par le try-catch de handle(), 
    	// afin d'éviter de retourner une page incomplète
        String basePath = "src/main/webapp/";
        byte[] encoded = Files.readAllBytes(Paths.get(basePath+fileName));
        return new String(encoded, "UTF-8");
    }
    
    /**
     * Traite la logique spécifique de l'enregistrement d'un utilisateur.
     *
     * @param query La chaîne de requête contenant les paramètres nom et tag.
     * @return La page HTML de résultat formatée.
     * @throws IOException En cas d'erreur de chargement du template de résultat.
     */
    private String handleRegistration(String query) throws IOException {
        Map<String, String> params = parseQuery(query);

        String nom = params.getOrDefault("nom", "Inconnu");
        String tag = params.get("tag");

        // Validation de base : Le Tag ne peut pas être vide
        if (tag == null || tag.trim().isEmpty()) {
            return formatResultPage(false, "Paramètre Manquant", "Le tag RFID est obligatoire.", nom, "N/A", 0.0);
        }

        try {
            // Modification centrale : forcer le solde initial des nouveaux utilisateurs à 0.0
            double solde = 0.0;
            User user = new User(nom, tag, solde, "USER");
            boolean succes = userDAO.addUser(user);

            if (succes) {
                // Succès : afficher la page de réussite en vert
                return formatResultPage(true, "Enregistrement Réussi", "Nouvel utilisateur ajouté au système.", nom, "Compte créé", solde);
            } else {
                // Échec : Le Tag existe déjà
                return formatResultPage(false, "Échec de l'enregistrement", "Ce tag RFID est déjà associé à un autre compte.", nom, "Erreur de doublon", 0.0);
            }
        } catch (NumberFormatException e) {
            return formatResultPage(false, "Erreur de Saisie", "Le solde doit être un nombre valide.", nom, "Format invalide", 0.0);
        }
    }
    
    /**
     * Injecte les données dans le template resultat.html.
     *
     * @param isSuccess Indique si l'opération a réussi.
     * @param title Le titre de la page.
     * @param msg Le message principal.
     * @param user Le nom de l'utilisateur concerné.
     * @param detail Détails supplémentaires sur l'opération.
     * @param balance Le solde à afficher.
     * @return Le contenu HTML final.
     * @throws IOException Si le template ne peut pas être chargé.
     */
    private String formatResultPage(boolean isSuccess, String title, String msg, String user, String detail, double balance) throws IOException {
        String template = loadHtmlTemplate("resultat.html");

        return template
            .replace("{{CLASS}}", isSuccess ? "success" : "error")
            .replace("{{TITLE}}", title)
            .replace("{{MESSAGE}}", msg)
            .replace("{{USER}}", user)
            .replace("{{DETAIL}}", detail)
            .replace("{{BALANCE}}", String.format("%.2f", balance));
    }
    
    /**
     * Analyse et convertit les paramètres de la requête URL en Map.
     *
     * @param query La chaîne de requête brute.
     * @return Une Map contenant les paires clé-valeur.
     */
    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                }
            }
        }
        return result;
    }
}