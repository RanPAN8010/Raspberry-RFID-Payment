package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.*;

public class HistoriqueHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>")
                .append("body { font-family: sans-serif; background: #f4f7f6; padding: 20px; text-align: center; }")
                .append("table { border-collapse: collapse; width: 90%; margin: 20px auto; background: white; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }")
                .append("th, td { border: 1px solid #ddd; padding: 12px; text-align: center; }")
                .append("th { background-color: #3498db; color: white; }")
                .append("tr:nth-child(even){background-color: #f2f2f2;}")
                .append("a { text-decoration: none; color: #3498db; font-weight: bold; }")
                .append("</style></head><body>")
                .append("<h2>📜 Historique des Transactions</h2>")
                .append("<table><tr><th>ID</th><th>Tag</th><th>Type</th><th>Montant</th><th>Date</th></tr>");

        // 直接从数据库读取流水
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/payment_system.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY id DESC")) {

            while (rs.next()) {
                html.append("<tr>")
                        .append("<td>").append(rs.getInt("id")).append("</td>")
                        .append("<td><code>").append(rs.getString("rfid_tag")).append("</code></td>")
                        .append("<td>").append(rs.getString("type")).append("</td>")
                        .append("<td><strong>€").append(rs.getDouble("montant")).append("</strong></td>")
                        .append("<td>").append(rs.getString("date_heure")).append("</td>")
                        .append("</tr>");
            }
        } catch (SQLException e) {
            html.append("<tr><td colspan='5' style='color:red;'>Erreur : ").append(e.getMessage()).append("</td></tr>");
        }

        html.append("</table><br><div><a href='/'>⬅ Retour à l'accueil</a></div></body></html>");

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, html.toString());
    }
}