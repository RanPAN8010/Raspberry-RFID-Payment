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

public class SimpleHttpServer {
    private PaymentService paymentService = new PaymentService();

    public static void start(int port) {
        try {
            // 1. 创建服务器，监听指定端口
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("HTTP Server 已启动，监听端口: " + port);

            // 2. 路由配置：首页
            server.createContext("/", new RootHandler());

            // 3. 路由配置：支付功能
            server.createContext("/pay", new PaiementHandler());

            // 4. 路由配置：注册接口
            server.createContext("/admin/enregistrement", new EnregistrementHandler());
         
            // 5. 路由配置：用户列表
            server.createContext("/admin/liste", new UserListHandler());
            
            // ==========================================
            // 💡 新增：专用于网页端触发物理刷卡的 API 接口
            // 这个接口专门负责和网页上的 "Scanner la carte" 按钮对接
            // ==========================================
            server.createContext("/api/scan", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    System.out.println("🌐 [Web端请求] 正在等待机器刷卡...");

                    // 呼叫底层的独立工具类去跑 Python 脚本等待刷卡
                    String rfidTag = Raspberry.RFIDReader.waitForCardSwipe();

                    // 防止没刷上卡或者超时返回 null 导致崩溃
                    if (rfidTag == null) {
                        rfidTag = "";
                    }

                    // 将读到的卡号作为纯文本返回给网页前端的 JavaScript
                    exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
                    exchange.sendResponseHeaders(200, rfidTag.getBytes().length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(rfidTag.getBytes());
                    os.close();
                }
            });

            // 5. 路由配置：充值接口
            server.createContext("/admin/recharge", new RechargeHandler());

            // 6. 路由配置：流水记录
            server.createContext("/admin/historique", new HistoriqueHandler());

            // 🚨 核心修改：使用多线程池！
            // 这样后台在死等物理刷卡的时候，依然可以秒速响应其他网页的访问请求
            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
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