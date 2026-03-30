package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Gestionnaire HTTP pour l'affichage de la liste des utilisateurs.
 * Récupère tous les utilisateurs de la base de données et les injecte dans un template HTML.
 */
public class UserListHandler implements HttpHandler {
    private final UserDAO userDAO = new UserDAO();

    /**
     * Gère la requête HTTP pour afficher la liste des utilisateurs enregistrés.
     *
     * @param exchange L'objet HttpExchange contenant la requête et la réponse.
     * @throws IOException En cas d'erreur de lecture de fichier ou d'envoi de données.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response;
        try {
            // Récupérer tous les utilisateurs depuis la base de données
            List<User> users = userDAO.getAllUsers();

            // Générer les lignes du tableau HTML
            StringBuilder rows = new StringBuilder();
            if (users == null || users.isEmpty()) {
                rows.append("<tr><td colspan='4' style='text-align:center;'>Aucun utilisateur trouvé.</td></tr>");
            } else {
                for (User u : users) {
                    rows.append("<tr>");
                    rows.append("<td>").append(u.getUsername()).append("</td>");
                    rows.append("<td><code>").append(u.getRfidTag()).append("</code></td>");
                    rows.append("<td><strong>€").append(String.format("%.2f", u.getBalance())).append("</strong></td>");

                    // Afficher différents styles de badges selon le rôle
                    String badgeClass = "ADMIN".equals(u.getRole()) ? "badge-admin" : "badge-user";
                    rows.append("<td><span class='badge ").append(badgeClass).append("'>")
                            .append(u.getRole()).append("</span></td>");

                    rows.append("</tr>");
                }
            }

            // Réparation du chemin : lire le template et effectuer le remplacement
            String templatePath = "src/main/webapp/liste.html";
            String template = new String(Files.readAllBytes(Paths.get(templatePath)), "UTF-8");
            response = template.replace("{{TABLE_ROWS}}", rows.toString());

        } catch (Exception e) {
            e.printStackTrace();
            response = "<html><body><h1>Erreur lors du chargement de la liste</h1><p>" + e.getMessage() + "</p></body></html>";
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, response);
    }
}