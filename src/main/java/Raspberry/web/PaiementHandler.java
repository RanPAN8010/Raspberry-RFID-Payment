package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import Raspberry.service.PaymentService;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import Raspberry.RFIDReader; // 引入我们新建的硬件读卡工具类

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PaiementHandler implements HttpHandler {
    private PaymentService paymentService = new PaymentService();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String acceptHeader = exchange.getRequestHeaders().getFirst("Accept");
        boolean wantsJson = acceptHeader != null && acceptHeader.contains("application/json");

        Map<String, String> params = parseQuery(exchange.getRequestURI().getQuery());
        String response;
        int statusCode = 200;

        try {
            // 💡 【纯软件测试模式】
            // 我们暂时不调用 RFIDReader.waitForCardSwipe();
            // 而是直接从网址里拿卡号 (tag) 和 金额 (amount)
            String rfidTag = params.get("tag");
            String amountStr = params.get("amount");

            if (amountStr == null || rfidTag == null) {
                response = renderError(wantsJson, "参数缺失", "请在网址中提供 tag 和 amount 参数。");
                statusCode = 400;
            } else {
                double amount = Double.parseDouble(amountStr);

                if (amount <= 0) {
                    response = renderError(wantsJson, "金额无效", "金额必须大于0。");
                    statusCode = 400;
                } else {
                    System.out.println("🌐 [测试模式] 收到网页扣款请求：卡号 [" + rfidTag + "]，金额 [" + amount + "] 欧。");

                    // 直接去查数据库并扣款，跳过硬件！
                    boolean succes = paymentService.processPayment(rfidTag, amount);
                    User user = userDAO.getUserByRfid(rfidTag);

                    if (succes && user != null) {
                        response = renderSuccess(wantsJson, user, amount);
                        System.out.println("✅ [测试模式] 数据库扣款成功！");
                    } else {
                        response = renderError(wantsJson, "支付失败",
                                (user == null) ? "数据库中找不到该卡号。" : "余额不足。");
                        System.out.println("❌ [测试模式] 扣款失败（查无此人或余额不足）。");
                    }
                }
            }
        } catch (NumberFormatException e) {
            response = renderError(wantsJson, "格式错误", "金额必须是数字。");
            statusCode = 400;
        } catch (Exception e) {
            response = renderError(wantsJson, "服务器错误", "内部系统错误。");
            statusCode = 500;
        }

        String contentType = wantsJson ? "application/json; charset=UTF-8" : "text/html; charset=UTF-8";
        exchange.getResponseHeaders().set("Content-Type", contentType);
        SimpleHttpServer.sendResponse(exchange, response);
    }

    // --- 渲染逻辑 (保持组员原本的漂亮样式不变) ---

    private String renderSuccess(boolean isJson, User user, double amount) {
        if (isJson) {
            return String.format("{\"status\":\"success\", \"user\":\"%s\", \"deducted\":%.2f, \"balance\":%.2f}",
                    user.getUsername(), amount, user.getBalance());
        }
        return "<html><body style='text-align:center; font-family:sans-serif;'>" +
                "<h1 style='color:green;'>✅ Succès (支付成功)</h1><p>Nouveau solde pour " + user.getUsername() +
                " : <b>€" + user.getBalance() + "</b></p><a href='/'>Retour</a></body></html>";
    }

    private String renderError(boolean isJson, String title, String msg) {
        if (isJson) {
            return String.format("{\"status\":\"error\", \"title\":\"%s\", \"message\":\"%s\"}", title, msg);
        }
        return "<html><body style='text-align:center; font-family:sans-serif;'>" +
                "<h1 style='color:red;'>❌ " + title + "</h1><p>" + msg + "</p><a href='/'>Retour</a></body></html>";
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) result.put(entry[0], entry[1]);
        }
        return result;
    }
}