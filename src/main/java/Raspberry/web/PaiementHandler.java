package Raspberry.web;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.service.PaymentService;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import java.io.IOException;

public class PaiementHandler implements HttpHandler {
	private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();
    
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String response;

        if (query != null) {
            String rfidTag = null;
            double amount = 0.0;

            // 解析参数: ?tag=xxx&amount=yyy
            String[] params = query.split("&");
            for (String param : params) {
                String[] pair = param.split("=");
                if (pair.length > 1) {
                    if (pair[0].equals("tag")) rfidTag = pair[1];
                    if (pair[0].equals("amount")) amount = Double.parseDouble(pair[1]);
                }
            }

            if (rfidTag != null) {
                // 执行扣款业务逻辑
                boolean succes = paymentService.processPayment(rfidTag, amount);
                
                // 重新查询用户信息以显示最新余额
                User user = userDAO.getUserByRfid(rfidTag);

                if (succes && user != null) {
                    response = "<h1>✅ Paiement Réussi</h1>" +
                               "<p>Utilisateur : " + user.getUsername() + "</p>" +
                               "<p>Nouveau Solde : <b>€" + user.getBalance() + "</b></p>";
                } else {
                    response = "<h1>❌ Échec du Paiement</h1>" +
                               "<p>Utilisateur non trouvé ou solde insuffisant.</p>";
                }
            } else {
                response = "<h1>⚠️ Paramètres Invalides</h1><p>Veuillez fournir un tag RFID.</p>";
            }
        } else {
            response = "<h1>🚫 Requête Invalide</h1><p>Aucun paramètre fourni.</p>";
        }

        // 调用 SimpleHttpServer 的静态公共方法发送响应
        SimpleHttpServer.sendResponse(exchange, response);
    }

}
