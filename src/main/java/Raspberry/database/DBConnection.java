package Raspberry.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnection {
    // 数据库文件路径，项目根目录下会自动生成 payment_system.db
    private static final String URL = "jdbc:sqlite:data/payment_system.db";
    private static Connection connection = null;

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                // 加载 SQLite 驱动（现代 JDBC 驱动通常会自动加载，但显式写出更保险）
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(URL);
                System.out.println("成功连接到 SQLite 数据库！");
                
                // 首次连接时自动创建表
                initDatabase();
            }
        } catch (Exception e) {
            System.err.println("数据库连接失败: " + e.getMessage());
        }
        return connection;
    }

    /**
     * 初始化数据库：创建用户表
     * 满足文档要求：RFID 绑定、余额管理、角色区分 [cite: 3, 6, 12]
     */
    private static void initDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL,"
                + "rfid_tag TEXT UNIQUE NOT NULL," // 对应文档的 badge RFID [cite: 3]
                + "balance REAL DEFAULT 0.0,"      // 对应充值和支付功能 [cite: 9, 10]
                + "role TEXT CHECK(role IN ('ADMIN', 'USER', 'MERCHANT'))," // 对应三类角色 [cite: 5, 7, 12]
                + "active INTEGER DEFAULT 1"       // 1 为激活，0 为禁用 
                + ");";

        String sqlTransactions = "CREATE TABLE IF NOT EXISTS transactions ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " rfid_tag TEXT NOT NULL,"
                + " type TEXT NOT NULL," // 'PAIEMENT' 或 'RECHARGE'
                + " montant REAL NOT NULL,"
                + " date_heure DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSQL);
            stmt.execute(sqlTransactions);
            System.out.println("数据库表结构已准备就绪。");
        } catch (SQLException e) {
            System.err.println("创建表失败: " + e.getMessage());
        }

        
    }
}
