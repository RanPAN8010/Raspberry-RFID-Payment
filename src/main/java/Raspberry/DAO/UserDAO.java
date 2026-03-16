package Raspberry.DAO;

import Raspberry.database.DBConnection;
import Raspberry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /**
     * 1. 添加新用户 (对应文档中管理员的功能)
     */
    public boolean addUser(User user) {
        String sql = "INSERT INTO users(username, rfid_tag, balance, role, active) VALUES(?, ?, ?, ?, ?)";
        
        // try-with-resources 语法，确保数据库连接用完后自动关闭
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRfidTag());
            pstmt.setDouble(3, user.getBalance());
            pstmt.setString(4, user.getRole());
            pstmt.setInt(5, user.isActive() ? 1 : 0); // SQLite 没有 boolean，用 1 和 0 代替
            
            pstmt.executeUpdate();
            System.out.println("成功添加用户: " + user.getUsername());
            return true;
            
        } catch (SQLException e) {
            System.err.println("添加用户失败 (可能是 RFID 已被绑定): " + e.getMessage());
            return false;
        }
    }

    /**
     * 2. 通过 RFID 查找用户 (对应文档中刷卡身份验证的核心机制)
     */
    public User getUserByRfid(String rfidTag) {
        String sql = "SELECT * FROM users WHERE rfid_tag = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rfidTag);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                // 如果在数据库找到了这个 RFID，就把它包装成一个 User 对象返回
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRfidTag(rs.getString("rfid_tag"));
                user.setBalance(rs.getDouble("balance"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getInt("active") == 1);
                return user;
            }
            
        } catch (SQLException e) {
            System.err.println("查询用户失败: " + e.getMessage());
        }
        
        // 如果没找到，返回 null
        return null; 
    }

	/**
     * 更新用户余额 (用于充值和扣款)
     */
    public boolean updateBalance(String rfidTag, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE rfid_tag = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, rfidTag);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("更新余额失败: " + e.getMessage());
            return false;
        }
    }
	
}
