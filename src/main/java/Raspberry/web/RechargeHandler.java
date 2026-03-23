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

public class RechargeHandler implements HttpHandler {
	private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String response;        
        
        // 判断客户端需求 (JSON 还是 HTML)
        String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");
        try {
            if (query == null || query.trim().isEmpty()) {
                response = loadTemplate("recharge.html");
            } else {
            	Map<String, String> params = parseQuery(query);
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
            // 【关键修复】：捕获异常并打印日志，防止 ERR_EMPTY_RESPONSE
            e.printStackTrace();
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
        // 复用支付结果的模板 (或者你新建一个 recharge_resultat.html)
        String template = loadTemplate("paiement_resultat.html");
        return template
            .replace("{{CLASS}}", isSuccess ? "success" : "recharge") // 可以自定义 CSS 类名
            .replace("{{ICON}}", isSuccess ? "💰" : "❌")
            .replace("{{TITLE}}", title)
            .replace("{{MESSAGE}}", msg)
            .replace("{{USER}}", user != null ? user.getUsername() : "Inconnu")
            .replace("{{AMOUNT}}", "+" + String.format("%.2f", amount))
            .replace("{{BALANCE}}", user != null ? String.format("%.2f", user.getBalance()) : "0.00");
    }
    
    private String loadTemplate(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), "UTF-8");
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
