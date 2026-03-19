package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class EnregistrementHandler implements HttpHandler {
	private UserDAO userDAO = new UserDAO();

	@Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String response;

        if (query != null) {
            String nom = "Utilisateur_Anonyme";
            String tag = null;
            double solde = 0.0;

            // 解析参数
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    if (pair[0].equals("nom")) nom = pair[1];
                    if (pair[0].equals("tag")) tag = pair[1];
                    if (pair[0].equals("solde")) solde = Double.parseDouble(pair[1]);
                }
            }

            if (tag != null) {
                User nouvelUtilisateur = new User(nom, tag, solde, "USER");
                boolean succes = userDAO.addUser(nouvelUtilisateur);
                if (succes) {
                    response = "<h1>✅ Succès de l'enregistrement</h1>" +
                               "<p>Nom : " + nom + "</p><p>Solde : €" + solde + "</p>";
                } else {
                    response = "<h1>❌ Échec</h1><p>Le tag [" + tag + "] existe déjà.</p>";
                }
            } else {
                response = "<h1>Erreur</h1><p>Tag manquant.</p>";
            }
        } else {
            response = "<h1>Requête Invalide</h1>";
        }
        sendResponse(exchange, response);
    }

    private void sendResponse(HttpExchange exchange, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
