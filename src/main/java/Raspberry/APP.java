package Raspberry;
import java.sql.Connection;

import Raspberry.DAO.UserDAO;
import Raspberry.model.User;
import Raspberry.database.DBConnection;
import Raspberry.web.SimpleHttpServer;

public class APP {
	public static void main(String[] args) {
		System.out.println("RFID Payment System is starting...");

        // 1. 初始化数据库并获取连接
        Connection conn = DBConnection.getConnection();
        
        if (conn != null) {
            System.out.println("✅ 数据库连接并初始化成功！");
            
            // 2. 检查并创建测试数据
            UserDAO dao = new UserDAO();
            if (dao.getUserByRfid("8888") == null) {
                User testUser = new User("Alpha_Tester", "8888", 100.0, "ADMIN");
                dao.addUser(testUser);
                System.out.println("✅ 已创建默认测试账户: 卡号 8888");
            }
        } else {
            System.err.println("❌ 严重错误：数据库无法连接，请检查 Docker 挂载路径！");
            // 数据库连不上时，为了防止空指针，可以选择停止程序
            System.exit(1);
        }

        // 3. 启动 Web 服务器
        SimpleHttpServer server = new SimpleHttpServer();
        // 确保你的端口与 docker-compose.yml 一致
        server.start(8080);
        
        // 4. 保持主线程运行
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                System.out.println("系统退出...");
                break;
            }
        }
	}
}