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

public class PaiementHandler implements HttpHandler {
	private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	String query = exchange.getRequestURI().getQuery();
        Map<String, String> params = parseQuery(query);
        
        // 检测客户端是否需要 JSON
        String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

        String response;
        try {
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
        } catch (Exception e) {
            response = processResponse(wantsJson, false, null, 0, "Erreur Serveur", e.getMessage());
        }

        String contentType = wantsJson ? "application/json" : "text/html";
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, response);
    }

    private String processResponse(boolean isJson, boolean isSuccess, User user, double amount, String title, String msg) throws IOException {
        if (isJson) {
            return String.format("{\"status\":\"%s\", \"message\":\"%s\", \"balance\":%.2f}", 
                                 isSuccess ? "success" : "error", msg, user != null ? user.getBalance() : 0);
        }

        // 读取外部 HTML 模板
        String template = new String(Files.readAllBytes(Paths.get("paiement_resultat.html")));
        
        // 动态替换占位符
        return template
            .replace("{{CLASS}}", isSuccess ? "success" : "error")
            .replace("{{ICON}}", isSuccess ? "✅" : "❌")
            .replace("{{TITLE}}", title)
            .replace("{{MESSAGE}}", msg)
            .replace("{{USER}}", user != null ? user.getUsername() : "Inconnu")
            .replace("{{AMOUNT}}", String.format("%.2f", amount))
            .replace("{{BALANCE}}", user != null ? String.format("%.2f", user.getBalance()) : "0.00");
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
