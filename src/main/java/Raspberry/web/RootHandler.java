package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gestionnaire HTTP pour la page d'accueil (racine).
 * Charge et sert le fichier index.html principal du projet.
 */
public class RootHandler implements HttpHandler {

	/**
     * Gère la requête HTTP pour l'accès à la racine du serveur.
     *
     * @param exchange L'objet HttpExchange contenant la requête et la réponse.
     * @throws IOException En cas d'erreur de lecture de fichier ou d'envoi de la réponse.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Path path = Paths.get("src/main/webapp/index.html");
        String content;

        if (Files.exists(path)) {
            // Lire le contenu du fichier HTML
            content = new String(Files.readAllBytes(path), "UTF-8");
        } else {
            // Si le fichier est introuvable, afficher une page simple de secours
            content = "<html><body style='text-align:center; padding:50px; font-family:sans-serif;'>" +
                    "<h1>Erreur de configuration</h1>" +
                    "<p>Le fichier <b>index.html</b> est introuvable dans <i>" + path.toAbsolutePath() + "</i></p>" +
                    "<p>Veuillez vérifier que vous exécutez le programme depuis le dossier racine du projet.</p>" +
                    "</body></html>";
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, content);
    }
}