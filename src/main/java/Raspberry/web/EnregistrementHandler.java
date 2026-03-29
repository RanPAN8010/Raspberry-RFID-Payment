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

public class EnregistrementHandler implements HttpHandler {
    private UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            String query = exchange.getRequestURI().getQuery();

            // 1. 如果没有参数，显示注册表单
            if (query == null || query.trim().isEmpty()) {
                response = loadHtmlTemplate("enregistrement.html");
            } else {
                // 2. 处理提交的数据
                response = handleRegistration(query);
            }
        } catch (Exception e) {
            // 关键：捕获所有可能的错误，防止 ERR_EMPTY_RESPONSE
            e.printStackTrace(); // 这会让错误出现在 docker logs 里
            response = "<html><body><h1>Erreur Interne</h1><p>" + e.getMessage() + "</p></body></html>";
        }

        SimpleHttpServer.sendResponse(exchange, response);
    }

    /**
     * 读取注册页面的 HTML 模板
     */
    private String loadHtmlTemplate(String fileName) throws IOException {
    	// 直接抛出异常让 handle() 的 try-catch 处理，避免返回半成品页面
        String basePath = "src/main/webapp/";
        byte[] encoded = Files.readAllBytes(Paths.get(basePath+fileName));
        return new String(encoded, "UTF-8");
    }
    /**
     * 处理具体的注册逻辑
     */
    private String handleRegistration(String query) throws IOException {
        Map<String, String> params = parseQuery(query);

        String nom = params.getOrDefault("nom", "Inconnu");
        String tag = params.get("tag");

        // 基础验证：Tag 不能为空
        if (tag == null || tag.trim().isEmpty()) {
            return formatResultPage(false, "Paramètre Manquant", "Le tag RFID est obligatoire.", nom, "N/A", 0.0);
        }

        try {
            // 🚨 核心修改：强制将新用户的初始金额设为 0.0
            double solde = 0.0;
            User user = new User(nom, tag, solde, "USER");
            boolean succes = userDAO.addUser(user);

            if (succes) {
                // 成功：显示绿色成功页
                return formatResultPage(true, "Enregistrement Réussi", "Nouvel utilisateur ajouté au système.", nom, "Compte créé", solde);
            } else {
                // 失败：Tag 已存在
                return formatResultPage(false, "Échec de l'enregistrement", "Ce tag RFID est déjà associé à un autre compte.", nom, "Erreur de doublon", 0.0);
            }
        } catch (NumberFormatException e) {
            return formatResultPage(false, "Erreur de Saisie", "Le solde doit être un nombre valide.", nom, "Format invalide", 0.0);
        }
    }

    /**
     * 核心逻辑：读取 resultat.html 并注入注册相关的数据
     */
    private String formatResultPage(boolean isSuccess, String title, String msg, String user, String detail, double balance) throws IOException {
        String template = loadHtmlTemplate("resultat.html");

        return template
            .replace("{{CLASS}}", isSuccess ? "success" : "error")
            .replace("{{ICON}}", isSuccess ? "👤" : "❌") // 注册成功用用户图标
            .replace("{{TITLE}}", title)
            .replace("{{MESSAGE}}", msg)
            .replace("{{USER}}", user)
            .replace("{{DETAIL}}", detail)
            .replace("{{BALANCE}}", String.format("%.2f", balance));
    }
    
    /**
     * 解析 URL 参数
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