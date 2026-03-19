package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;

public class RootHandler implements HttpHandler {

	@Override
    public void handle(HttpExchange exchange) throws IOException {
        // 构建简单的法语欢迎界面
        String response = "<html><body style='font-family: sans-serif; text-align: center; padding-top: 50px;'>" +
                          "<h1>🚀 Système de Paiement RFID</h1>" +
                          "<p>Statut du serveur : <b>En ligne</b></p>" +
                          "<hr style='width: 50%;'>" +
                          "<p>Utilisez les interfaces API pour le paiement ou l'enregistrement.</p>" +
                          "</body></html>";

        // 调用 SimpleHttpServer 的静态公共方法发送响应
        SimpleHttpServer.sendResponse(exchange, response);
    }
}
