package Raspberry.web;
import com.sun.net.httpserver.HttpServer;

import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import Raspberry.service.PaymentService;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
public class SimpleHttpServer {
	private PaymentService paymentService = new PaymentService();
	
	public void start(int port) {
        try {
            // 1. 创建服务器，监听指定端口
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("HTTP Server 已启动，监听端口: " + port);

            // 2. 路由配置：首页
            server.createContext("/", new RootHandler());

            // 3. 路由配置：支付 API 测试 (例如: /pay?id=123)
            server.createContext("/pay", new PayHandler());

            server.setExecutor(null); // 使用默认执行器
            server.start();

        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }

    // 处理首页请求 (localhost:8080/)
    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = "<h1>Welcome to RFID Payment System</h1><p>Status: Running</p>";
            sendResponse(exchange, response);
        }
    }

    // 处理支付请求 (localhost:8080/pay)
    class PayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String response;

            // 改进后的解析逻辑
            if (query != null) {
                String rfidTag = null;
                double amount = 0.0;

                // 将 query 按 & 分割，例如 ["tag=8888", "amount=10"]
                String[] params = query.split("&");
                for (String param : params) {
                    String[] pair = param.split("=");
                    if (pair.length > 1) {
                        if (pair[0].equals("tag")) rfidTag = pair[1];
                        if (pair[0].equals("amount")) amount = Double.parseDouble(pair[1]);
                    }
                }

                if (rfidTag != null) {
					// 调用 PaymentService 进行处理
                    boolean success = paymentService.processPayment(rfidTag, amount);
                    
                    // 重新获取用户信息来显示最新余额
                    User user = new UserDAO().getUserByRfid(rfidTag);

                    if (success && user != null) {
                        response = "<h1>✅ Payment Success</h1>" +
                                   "<p>User: " + user.getUsername() + "</p>" +
                                   "<p>New Balance: €" + user.getBalance() + "</p>";
                    } else {
                        response = "<h1>❌ Error</h1><p>User [" + rfidTag + "] not found or balance insufficient.</p>";
                    }
                } else {
                    response = "<h1>Invalid Parameters</h1><p>Usage: /pay?tag=8888&amount=10</p>";
                }
            } else {
                response = "<h1>No Parameters Provided</h1>";
            }
            sendResponse(exchange, response);
        }
    }

    // 封装发送响应的方法
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
