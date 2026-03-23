package Raspberry.DAO;

import Raspberry.database.DBConnection;
import Raspberry.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

	public UserDAO() {
		// TODO Auto-generated constructor stub
	}
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
        Connection conn = DBConnection.getConnection();
        
        // 核心修复：如果连接失败，直接返回 null，不要执行后续代码
        if (conn == null) {
            System.err.println("无法获取数据库连接，查询中止。");
            return null; 
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
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
    
	/**
     * 记录流水
     */
    public void addTransaction(String tag, String type, double montant) {
        String sql = "INSERT INTO transactions(rfid_tag, type, montant) VALUES(?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tag);
            pstmt.setString(2, type);
            pstmt.setDouble(3, montant);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur d'enregistrement de transaction : " + e.getMessage());
        }
    }
    /**
     * 获取所有用户列表 (用于管理界面展示)
     */
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC"; // 按 ID 倒序排列，新注册的在前面

        // 核心：手动检查连接
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            return userList; // 返回空列表而不是 null，防止外部调用报空指针
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setRfidTag(rs.getString("rfid_tag"));
                user.setBalance(rs.getDouble("balance"));
                user.setRole(rs.getString("role"));
                user.setActive(rs.getInt("active") == 1);
                
                userList.add(user);
            }
        } catch (SQLException e) {
            System.err.println("❌ 获取用户列表失败: " + e.getMessage());
        }
        
        return userList;
    }
    
}