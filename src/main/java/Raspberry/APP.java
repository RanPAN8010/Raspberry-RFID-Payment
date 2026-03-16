package Raspberry;

<<<<<<< HEAD
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
=======
import Raspberry.database.DBConnection;
import java.sql.Connection;

public class APP {
    public static void main(String[] args) {
        System.out.println("正在启动支付系统...");
        
        // 调用获取连接的方法，这会触发 DBConnection 里的 initDatabase()
        Connection conn = DBConnection.getConnection();
        
        if (conn != null) {
            System.out.println("数据库初始化成功！");
        } else {
            System.out.println("数据库初始化失败，请检查依赖配置。");
        }
>>>>>>> e3f62e08e0051b1d0378dd06a9a27e3cfad40f43
    }
}