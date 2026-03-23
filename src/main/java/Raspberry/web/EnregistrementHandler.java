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
            if (query == null || query.isEmpty()) {
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
    private String loadHtmlTemplate(String fileName) {
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            return new String(encoded, "UTF-8");
        } catch (IOException e) {
            System.err.println("❌ Fichier manquant : " + fileName);
            return "<html><body><h1>Erreur 404</h1><p>Le fichier ["
                    + fileName
                    + "] est introuvable sur le serveur.</p></body></html>";
        }
    }
    /**
     * 处理具体的注册逻辑
     */
    private String handleRegistration(String query) {
        Map<String, String> params = parseQuery(query);

        String nom = params.getOrDefault("nom", "Inconnu");
        String tag = params.get("tag");
        String soldeStr = params.getOrDefault("solde", "0");

        if (tag == null || tag.trim().isEmpty()) {
            return "<h1> Erreur</h1><p>Le tag RFID est obligatoire.</p><a href='/admin/enregistrement'>Réessayer</a>";
        }

        try {
            double solde = Double.parseDouble(soldeStr);
            User user = new User(nom, tag, solde, "USER");

            boolean succes = userDAO.addUser(user);

            if (succes) {
                // 注册成功，提示并提供返回链接
                return "<h1>✅ Succès</h1><p>Utilisateur <b>" + nom + "</b> (Tag: " + tag + ") enregistré.</p>" +
                        "<a href='/admin/enregistrement'>Nouvel enregistrement</a> | <a href='/'>Accueil</a>";
            } else {
                return "<h1> Échec</h1><p>Le tag [" + tag + "] existe déjà dans la base.</p><a href='/admin/enregistrement'>Retour</a>";
            }
        } catch (NumberFormatException e) {
            return "<h1>❌ Erreur</h1><p>Le solde doit être un nombre valide.</p><a href='/admin/enregistrement'>Retour</a>";
        }
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