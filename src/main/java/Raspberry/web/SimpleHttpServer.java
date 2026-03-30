package Raspberry.web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import Raspberry.service.PaymentService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Classe principale du serveur HTTP.
 * Configure les routes, initialise les services et démarre le serveur web pour le terminal de paiement.
 */
public class SimpleHttpServer {
    private PaymentService paymentService = new PaymentService();

    /**
     * Démarre le serveur HTTP sur le port spécifié.
     *
     * @param port Le port d'écoute du serveur (par exemple 8080).
     */
    public static void start(int port) {
        try {
            // Créer le serveur, écouter sur le port spécifié
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Serveur HTTP démarré, écoute sur le port : " + port);

            // Configuration des routes : Page d'accueil
            server.createContext("/", new RootHandler());

            // Configuration des routes : Fonctionnalité de paiement
            server.createContext("/pay", new PaiementHandler());

            // Configuration des routes : Interface d'enregistrement
            server.createContext("/admin/enregistrement", new EnregistrementHandler());
         
            // Configuration des routes : Liste des utilisateurs
            server.createContext("/admin/liste", new UserListHandler());
            
            // Interface API dédiée au déclenchement du scan physique depuis le web
            // Cette interface est spécifiquement responsable de la liaison 
            // avec le bouton "Scanner la carte" sur la page web
            server.createContext("/api/scan", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    System.out.println("[Requête Web] En attente du passage de la carte sur la machine...");

                    // Appeler la classe utilitaire sous-jacente 
                    // pour exécuter le script Python et attendre le scan
                    String rfidTag = Raspberry.RFIDReader.waitForCardSwipe();

                    // Empêcher un crash si la carte n'est pas scannée 
                    // ou si le délai expire (retourne null)
                    if (rfidTag == null) {
                        rfidTag = "";
                    }

                    // Retourner le numéro de carte lu 
                    // en tant que texte brut au JavaScript du frontend web
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(200, rfidTag.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(rfidTag.getBytes());
                    os.close();
                }
            });

            // Configuration des routes : Interface de recharge
            server.createContext("/admin/recharge", new RechargeHandler());

            // Configuration des routes : Historique des transactions
            server.createContext("/admin/historique", new HistoriqueHandler());

            // Utilisation d'un pool de threads (CachedThreadPool)
            // Ainsi, pendant que le backend attend le scan physique, 
            // il peut toujours répondre instantanément aux autres requêtes web
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();

        } catch (IOException e) {
            System.err.println("Échec du démarrage du serveur : " + e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour envoyer une réponse HTML.
     *
     * @param exchange L'objet HttpExchange.
     * @param response Le contenu HTML de la réponse.
     * @throws IOException En cas d'erreur d'envoi.
     */
    public static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    
    
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   Terminal de Paiement Intelligent 2026  ");
        System.out.println("=========================================");

        // Réveil de la base de données
        Raspberry.database.DBConnection.getConnection();

        // Démarrer le serveur web sur le port 8080
        start(8080);

        System.out.println("Système central prêt ! Veuillez maintenant contrôler cette machine via le navigateur web.");
    }
}