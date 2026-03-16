package Raspberry.DAO;
import Raspberry.database.DBConnection;
import Raspberry.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
public class UserDAO {
	/**
     * 根据 RFID 卡号查询用户
     * 用于刷卡时的身份验证和余额检查
     */
    public User findByRfidTag(String rfidTag) {
        String sql = "SELECT * FROM users WHERE rfid_tag = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, rfidTag);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRfidTag(rs.getString("rfid_tag"));
                user.setBalance(rs.getDouble("balance"));
                user.setActive(rs.getInt("status") == 1);
                user.setRole(rs.getString("role"));
                return user;
            }
        } catch (SQLException e) {
            System.err.println("查询用户失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 保存新用户到数据库
     * 用于管理员创建账户
     */
    public boolean saveUser(User user) {
        String sql = "INSERT INTO users(username, rfid_tag, balance, status, role) VALUES(?,?,?,?,?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getRfidTag());
            pstmt.setDouble(3, user.getBalance());
            pstmt.setInt(4, user.isActive() ? 1 : 0);
            pstmt.setString(5, user.getRole());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("保存用户失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 更新用户余额
     * 用于支付成功后扣款或充值
     */
    public boolean updateBalance(String rfidTag, double newBalance) {
        String sql = "UPDATE users SET balance = ? WHERE rfid_tag = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, newBalance);
            pstmt.setString(2, rfidTag);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("更新余额失败: " + e.getMessage());
            return false;
        }
    }
}
