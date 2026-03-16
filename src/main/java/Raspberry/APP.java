package Raspberry;
import java.sql.Connection;

import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import Raspberry.database.DBConnection;
import Raspberry.web.SimpleHttpServer;

public class APP {
	public static void main(String[] args) {
		System.out.println("RFID Payment System is starting...");
        // 调用获取连接的方法，这会触发 DBConnection 里的 initDatabase()
        Connection conn = DBConnection.getConnection();
        
        if (conn != null) {
            System.out.println("数据库初始化成功！");
        } else {
            System.out.println("数据库初始化失败，请检查依赖配置。");
        }		        
        UserDAO dao = new UserDAO();
        if (dao.findByRfidTag("8888") == null) {
            User testUser = new User("Alpha_Tester", "8888", 100.0, "ADMIN");
            dao.saveUser(testUser);
            System.out.println("已创建测试账户: 卡号 8888"); 
        }
        
        // 启动 Web 服务器
		SimpleHttpServer server = new SimpleHttpServer();
		server.start(8080); // 这里的端口要和 docker-compose.yml 里的端口对应
		        
		// 保持程序运行
		while(true) {
		    try { Thread.sleep(10000); } catch (InterruptedException e) {}
		}
    }
}