package Raspberry.web;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class RootHandler implements HttpHandler {

	@Override
    public void handle(HttpExchange exchange) throws IOException {
        // 在 Docker 容器中，文件会被放在 /app/index.html
        Path path = Paths.get("index.html");
        String content;

        if (Files.exists(path)) {
            // 读取 HTML 文件内容
            content = new String(Files.readAllBytes(path), "UTF-8");
        } else {
            // 如果找不到文件，显示一个备用的简单页面
            content = "<html><body><h1>Système RFID</h1>" +
                      "<p>index.html non trouvé dans " + path.toAbsolutePath() + "</p>" +
                      "<a href='/admin/liste'>Voir la liste</a></body></html>";
        }

        SimpleHttpServer.sendResponse(exchange, content);
    }
}
