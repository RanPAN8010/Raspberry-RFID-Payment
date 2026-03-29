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
        // 🚨 修复路径，指向 webapp 文件夹
        Path path = Paths.get("src/main/webapp/index.html");
        String content;

        if (Files.exists(path)) {
            // 读取 HTML 文件内容
            content = new String(Files.readAllBytes(path), "UTF-8");
        } else {
            // 如果找不到文件，显示一个备用的简单页面
            content = "<html><body style='text-align:center; padding:50px; font-family:sans-serif;'>" +
                    "<h1>⚠️ Erreur de configuration</h1>" +
                    "<p>Le fichier <b>index.html</b> est introuvable dans <i>" + path.toAbsolutePath() + "</i></p>" +
                    "<p>Veuillez vérifier que vous exécutez le programme depuis le dossier racine du projet.</p>" +
                    "</body></html>";
        }

        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        SimpleHttpServer.sendResponse(exchange, content);
    }
}