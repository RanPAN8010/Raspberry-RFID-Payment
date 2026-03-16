package Raspberry;

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
    }
}