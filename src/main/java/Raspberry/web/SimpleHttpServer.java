package Raspberry.web;
import com.sun.net.httpserver.HttpServer;

import Raspberry.DAO.UserDAO;
import Raspberry.model.User;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
public class SimpleHttpServer {
	private static UserDAO userDAO = new UserDAO();
	
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
    static class PayHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
        	String query = exchange.getRequestURI().getQuery(); // 获取 URL 参数，如 tag=123
            String response;

            if (query != null && query.startsWith("tag=")) {
                String rfidTag = query.split("=")[1];
                
                // 调用组员写的 DAO 方法
                User user = userDAO.findByRfidTag(rfidTag);

                if (user != null) {
                    response = "<h1>User Found</h1>" +
                               "<p>Username: " + user.getUsername() + "</p>" +
                               "<p>Balance: $" + user.getBalance() + "</p>";
                } else {
                    response = "<h1>Error</h1><p>User with Tag [" + rfidTag + "] not found.</p>";
                }
            } else {
                response = "<h1>Invalid Request</h1><p>Please use /pay?tag=YOUR_ID</p>";
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
