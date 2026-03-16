package Raspberry.service;
import Raspberry.DAO.UserDAO;
import Raspberry.model.User;


public class PaymentService {
    private UserDAO userDAO;
    
    public PaymentService() {
        this.userDAO = new UserDAO();
    }

    /**
     * 处理支付请求 (商家功能)
     */
    public boolean processPayment(String rfidTag, double amount) {
        User user = userDAO.getUserByRfid(rfidTag);

        if (user == null) {
            System.out.println("❌ 支付失败：未找到该 RFID 对应的账户！");
            return false;
        }
        
        if (!user.isActive()) {
            System.out.println("❌ 支付失败：该账户已被禁用！");
            return false;
        }

        if (user.getBalance() < amount) {
            System.out.println("❌ 支付失败：余额不足！当前余额: €" + user.getBalance());
            return false;
        }

        // 扣款逻辑
        double newBalance = user.getBalance() - amount;
        if (userDAO.updateBalance(rfidTag, newBalance)) {
            System.out.println("✅ 支付成功！扣除: €" + amount + "，剩余余额: €" + newBalance);
            return true;
        } else {
            System.out.println("❌ 支付失败：系统错误。");
            return false;
        }
    }

    /**
     * 处理充值请求 (用户/管理员功能)
     */
    public boolean rechargeAccount(String rfidTag, double amount) {
        if (amount <= 0) {
            System.out.println("❌ 充值金额必须大于 0！");
            return false;
        }

        User user = userDAO.getUserByRfid(rfidTag);
        if (user != null) {
            double newBalance = user.getBalance() + amount;
            if (userDAO.updateBalance(rfidTag, newBalance)) {
                System.out.println("✅ 充值成功！当前余额: €" + newBalance);
                return true;
            }
        }
        System.out.println("❌ 充值失败：未找到该用户。");
        return false;
    }
}
