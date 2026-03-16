package Raspberry;

import Raspberry.web.SimpleHttpServer;

public class APP {
	public static void main(String[] args) {
		System.out.println("RFID Payment System is starting...");
		        
		        // 启动 Web 服务器
		        SimpleHttpServer server = new SimpleHttpServer();
		        server.start(8080); // 这里的端口要和 docker-compose.yml 里的端口对应
		        
		        // 保持程序运行
		        while(true) {
		            try { Thread.sleep(10000); } catch (InterruptedException e) {}
		        }
    }
}
