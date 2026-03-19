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
            server.createContext("/pay", new PaiementHandler());
            
            // 绑定注册接口：/admin/register
            
            // 添加充值接口的映射：/admin/recharge
            server.createContext("/admin/recharge", new RechargeHandler());
            
            //  路由配置：流水记录
            server.createContext("/admin/historique", new HistoriqueHandler());

            server.setExecutor(null); // 使用默认执行器
            server.start();

        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        }
    }
    // 封装发送响应的方法
    public static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

}
