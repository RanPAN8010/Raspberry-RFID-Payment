package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.service.PaymentService;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;

public class RechargeHandler implements HttpHandler {
	private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String response;

        if (query != null) {
            String tag = null;
            double montant = 0.0;

            // 解析参数: ?tag=xxx&amount=yyy
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    if (pair[0].equals("tag")) tag = pair[1];
                    if (pair[0].equals("amount")) montant = Double.parseDouble(pair[1]);
                }
            }

            if (tag != null && montant > 0) {
                // 执行充值逻辑
                boolean succes = paymentService.rechargeAccount(tag, montant);
                User user = userDAO.getUserByRfid(tag);

                if (succes && user != null) {
                    response = "<h1>✅ Recharge Réussie</h1>" +
                               "<p>Utilisateur : " + user.getUsername() + "</p>" +
                               "<p>Montant ajouté : €" + montant + "</p>" +
                               "<p>Nouveau Solde : <b>€" + user.getBalance() + "</b></p>";
                } else {
                    response = "<h1>❌ Échec de la recharge</h1><p>Utilisateur non trouvé.</p>";
                }
            } else {
                response = "<h1>⚠️ Paramètres Invalides</h1><p>Veuillez fournir un tag et un montant positif.</p>";
            }
        } else {
            response = "<h1>🚫 Requête Invalide</h1>";
        }

        // 调用 SimpleHttpServer 的静态公共方法发送响应
        SimpleHttpServer.sendResponse(exchange, response);
    }

}
