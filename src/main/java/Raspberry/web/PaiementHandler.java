package Raspberry.web;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.service.PaymentService;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaiementHandler implements HttpHandler {
	private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
    	// 1. 判断客户端想要什么格式 (HTML 或 JSON)
        String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String response;
        int statusCode = 200;

        try {
            String rfidTag = params.get("tag");
            String amountStr = params.get("amount");

            if (rfidTag == null || amountStr == null) {
                response = renderError(wantsJson, "Parametres manquants", "Tag ou montant absent.");
                statusCode = 400;
            } else {
                double amount = Double.parseDouble(amountStr);
                
                if (amount <= 0) {
                    response = renderError(wantsJson, "Montant invalide", "Le montant doit être > 0.");
                    statusCode = 400;
                } else {
                    boolean succes = paymentService.processPayment(rfidTag, amount);
                    User user = userDAO.getUserByRfid(rfidTag);

                    if (succes && user != null) {
                        response = renderSuccess(wantsJson, user, amount);
                    } else {
                        response = renderError(wantsJson, "Echec du paiement", 
                            (user == null) ? "Utilisateur inconnu." : "Solde insuffisant.");
                    }
                }
            }
        } catch (NumberFormatException e) {
            response = renderError(wantsJson, "Erreur de format", "Le montant doit être un nombre.");
            statusCode = 400;
        } catch (Exception e) {
            response = renderError(wantsJson, "Erreur Serveur", "Erreur interne.");
            statusCode = 500;
        }

        // 设置正确的 Content-Type
        String contentType = wantsJson ? "application/json; charset=UTF-8" : "text/html; charset=UTF-8";
        exchange.getResponseHeaders().set("Content-Type", contentType);
        SimpleHttpServer.sendResponse(exchange, response);
    }

    // --- 渲染逻辑 ---

    private String renderSuccess(boolean isJson, User user, double amount) {
        if (isJson) {
            return String.format("{\"status\":\"success\", \"user\":\"%s\", \"deducted\":%.2f, \"balance\":%.2f}",
                                 user.getUsername(), amount, user.getBalance());
        }
        return "<html><body style='text-align:center; font-family:sans-serif;'>" +
               "<h1 style='color:green;'>✅ Succès</h1><p>Nouveau solde pour " + user.getUsername() + 
               " : <b>€" + user.getBalance() + "</b></p><a href='/'>Retour</a></body></html>";
    }

    private String renderError(boolean isJson, String title, String msg) {
        if (isJson) {
            return String.format("{\"status\":\"error\", \"title\":\"%s\", \"message\":\"%s\"}", title, msg);
        }
        return "<html><body style='text-align:center; font-family:sans-serif;'>" +
               "<h1 style='color:red;'>❌ " + title + "</h1><p>" + msg + "</p><a href='/'>Retour</a></body></html>";
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
