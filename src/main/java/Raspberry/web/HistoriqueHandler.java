package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoriqueHandler implements HttpHandler {
	
	@Override
    public void handle(HttpExchange exchange) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><meta charset='UTF-8'><style>")
            .append("table { border-collapse: collapse; width: 90%; margin: auto; }")
            .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
            .append("th { background-color: #2196F3; color: white; }")
            .append("tr:nth-child(even){background-color: #f2f2f2;}")
            .append("</style></head><body>")
            .append("<h2 style='text-align:center;'>📜 Historique des Transactions</h2>")
            .append("<table><tr><th>ID</th><th>Tag</th><th>Type</th><th>Montant</th><th>Date</th></tr>");

        // 直接从数据库读取流水
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:data/payment_system.db");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM transactions ORDER BY id DESC")) {
            
            while (rs.next()) {
                html.append("<tr>")
                    .append("<td>").append(rs.getInt("id")).append("</td>")
                    .append("<td>").append(rs.getString("rfid_tag")).append("</td>")
                    .append("<td>").append(rs.getString("type")).append("</td>")
                    .append("<td>€").append(rs.getDouble("montant")).append("</td>")
                    .append("<td>").append(rs.getString("date_heure")).append("</td>")
                    .append("</tr>");
            }
        } catch (SQLException e) {
            html.append("<p>Erreur : ").append(e.getMessage()).append("</p>");
        }

        html.append("</table><br><div style='text-align:center;'><a href='/'>Retour</a></div></body></html>");
        SimpleHttpServer.sendResponse(exchange, html.toString());
    }
}
