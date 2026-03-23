package Raspberry;

import Raspberry.DAO.UserDAO;
import Raspberry.database.DBConnection;
import Raspberry.model.User;
import Raspberry.service.PaymentService;
import Raspberry.web.SimpleHttpServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.Scanner;

public class APP {
    public static void main(String[] args) {
		System.out.println("RFID Payment System is starting...");
		int port = 8080;

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
            
            try {
                SimpleHttpServer.start(port);
                System.out.println("🌐 Web 服务器已启动，监听端口: 8080");
                
                // 防止主线程退出
                Thread.currentThread().join();
            } catch (Exception e) {
                System.err.println("❌ Web 服务器启动失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}