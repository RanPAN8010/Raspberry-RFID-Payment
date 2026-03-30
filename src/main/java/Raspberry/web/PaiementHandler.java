package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.service.PaymentService;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Gestionnaire HTTP pour les opérations de paiement.
 * Permet d'afficher l'interface de paiement et de traiter les transactions via RFID.
 */
public class PaiementHandler implements HttpHandler {
    private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
 // Préfixe unifié pour les chemins des fichiers
    private final String BASE_PATH = "src/main/webapp/";

    /**
     * Gère les requêtes HTTP pour le module de paiement.
     * Supporte à la fois les réponses HTML et les réponses JSON pour les appels AJAX.
     *
     * @param exchange L'objet HttpExchange.
     * @throws IOException En cas d'erreur de lecture de fichier ou d'envoi de réponse.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();

        String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

        String response;

        try {
            if (query == null || query.trim().isEmpty()) {
                // Réparation du chemin d'accès
                response = new String(Files.readAllBytes(Paths.get(BASE_PATH + "paiement.html")), "UTF-8");
            } else {
                Map<String, String> params = parseQuery(query);
                String rfidTag = params.get("tag");
                double amount = Double.parseDouble(params.getOrDefault("amount", "0"));

                if (rfidTag != null && amount > 0) {
                    boolean succes = paymentService.processPayment(rfidTag, amount);
                    User user = userDAO.getUserByRfid(rfidTag);

                    if (succes && user != null) {
                        response = processResponse(wantsJson, true, user, amount, "Paiement Réussi", "Transaction effectuée avec succès.");
                    } else {
                        response = processResponse(wantsJson, false, user, amount, "Échec du Paiement", "Solde insuffisant ou utilisateur inconnu.");
                    }
                } else {
                    response = processResponse(wantsJson, false, null, 0, "Paramètres Invalides", "Le tag ou le montant est incorrect.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response = processResponse(wantsJson, false, null, 0, "Erreur Serveur", e.getMessage());
            } catch (Exception fatal) {
                response = "<h1>Fatal Error</h1><p>HTML template missing.</p>";
            }
        }
        String contentType = wantsJson ? "application/json" : "text/html";
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, response);
    }

    /**
     * Formate la réponse en fonction du format demandé (JSON ou HTML).
     *
     * @param isJson    Indique si la réponse doit être en JSON.
     * @param isSuccess État de la transaction.
     * @param user      L'utilisateur concerné.
     * @param amount    Le montant de la transaction.
     * @param title     Le titre de la réponse.
     * @param msg       Le message de détail.
     * @return La chaîne de caractères formatée pour la réponse.
     * @throws IOException Si le template HTML est introuvable.
     */
    private String processResponse(boolean isJson, boolean isSuccess, User user, double amount, String title, String msg) throws IOException {
        if (isJson) {
            return String.format("{\"status\":\"%s\", \"message\":\"%s\", \"balance\":%.2f}",
                    isSuccess ? "success" : "error", msg, user != null ? user.getBalance() : 0);
        }
        // Réparation du chemin d'accès
        String template = new String(Files.readAllBytes(Paths.get(BASE_PATH + "resultat.html")), "UTF-8");

        return template
                .replace("{{CLASS}}", isSuccess ? "success" : "error")
                .replace("{{TITLE}}", title)
                .replace("{{MESSAGE}}", msg)
                .replace("{{USER}}", user != null ? user.getUsername() : "Inconnu")
                .replace("{{AMOUNT}}", String.format("%.2f", amount))
                .replace("{{BALANCE}}", user != null ? String.format("%.2f", user.getBalance()) : "0.00");
    }

    /**
     * Analyse la chaîne de requête URL pour extraire les paramètres.
     *
     * @param query La chaîne de requête brute.
     * @return Une Map des paramètres.
     */
    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) result.put(entry[0], entry[1]);
        }
        return result;
    }
}