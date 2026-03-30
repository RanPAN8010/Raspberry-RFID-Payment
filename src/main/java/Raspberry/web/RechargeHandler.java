package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.service.PaymentService;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire HTTP pour les opérations de recharge de compte.
 * Permet de créditer le solde d'un utilisateur via son tag RFID.
 */
public class RechargeHandler implements HttpHandler {
    private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
    private final String BASE_PATH = "src/main/webapp/";

    /**
     * Gère les requêtes HTTP pour le module de recharge.
     *
     * @param exchange L'objet HttpExchange contenant la requête et la réponse.
     * @throws IOException En cas d'erreur lors de la lecture des templates ou de l'envoi des données.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);

        String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

        String response;
        try {
            if (query == null || query.trim().isEmpty()) {
                response = loadTemplate("recharge.html");
            } else {
                String tag = params.get("tag");
                String amountStr = params.getOrDefault("amount", "0");
                double amount = Double.parseDouble(amountStr);

                if (tag != null && amount > 0) {
                    boolean succes = paymentService.rechargeAccount(tag, amount);
                    User user = userDAO.getUserByRfid(tag);

                    if (succes && user != null) {
                        response = processResponse(wantsJson, true, user, amount, "Recharge Réussie", "Le compte a été crédité.");
                    } else {
                        response = processResponse(wantsJson, false, user, amount, "Échec", "Utilisateur non trouvé.");
                    }
                } else {
                    response = processResponse(wantsJson, false, null, 0, "Invalide", "Paramètres incorrects.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response = processResponse(wantsJson, false, null, 0, "Erreur Serveur", e.getMessage());
            } catch (Exception fatal) {
                response = "<html><body><h1>Fatal Error</h1><p>Template missing.</p></body></html>";
            }
        }
        String contentType = wantsJson ? "application/json" : "text/html";
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, response);
    }

    /**
     * Formate la réponse selon le type de contenu demandé (JSON ou HTML).
     *
     * @param isJson    Vrai si une réponse JSON est attendue.
     * @param isSuccess État de la réussite de l'opération.
     * @param user      L'objet utilisateur concerné.
     * @param amount    Le montant rechargé.
     * @param title     Le titre de la page ou du statut.
     * @param msg       Le message de confirmation ou d'erreur.
     * @return La réponse formatée.
     * @throws IOException Si le template HTML est introuvable.
     */
    private String processResponse(boolean isJson, boolean isSuccess, User user, double amount, String title, String msg) throws IOException {
        if (isJson) {
            return String.format("{\"status\":\"%s\", \"message\":\"%s\", \"balance\":%.2f}",
                    isSuccess ? "success" : "error", msg, user != null ? user.getBalance() : 0);
        }
        String template = loadTemplate("resultat.html");

        return template
                .replace("{{CLASS}}", isSuccess ? "success" : "error")
                .replace("{{TITLE}}", title)
                .replace("{{MESSAGE}}", msg)
                .replace("{{USER}}", user != null ? user.getUsername() : "Inconnu")
                .replace("{{DETAIL}}", isSuccess ? ("Recharge de €" + String.format("%.2f", amount)) : "Action annulée")
                .replace("{{BALANCE}}", user != null ? String.format("%.2f", user.getBalance()) : "0.00");
    }

    private String loadTemplate(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(BASE_PATH + path)), "UTF-8");
    }

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