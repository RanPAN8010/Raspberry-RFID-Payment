//package Raspberry;
//
//import Raspberry.DAO.UserDAO;
//import Raspberry.model.User;
//import Raspberry.service.PaymentService;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.Scanner;
//
//public class APP {
//    public static void main(String[] args) {
//        // 初始化组件
//        UserDAO userDAO = new UserDAO();
//        PaymentService paymentService = new PaymentService();
//        Scanner scanner = new Scanner(System.in);
//
//        System.out.println("=========================================");
//        System.out.println("   💳 欢迎使用 2026 树莓派智能支付终端   ");
//        System.out.println("=========================================");
//
//        while (true) {
//            System.out.println("\n[主菜单] 请选择操作：");
//            System.out.println("1. 🛒 刷卡消费");
//            System.out.println("2. 💵 余额充值");
//            System.out.println("3. 👤 注册新卡");
//            System.out.println("4. ❌ 退出系统");
//            System.out.print("👉 请输入选项 (1-4): ");
//
//            String choice = scanner.nextLine();
//
//            if ("4".equals(choice)) {
//                System.out.println("系统正在安全关闭，再见！");
//                break;
//            }
//
//            switch (choice) {
//                case "1":
//                    System.out.print("💰 请输入本次消费金额 (欧): ");
//                    double payAmount = Double.parseDouble(scanner.nextLine());
//                    String payRfid = waitForCardSwipe(scanner);
//                    if (payRfid != null) {
//                        paymentService.processPayment(payRfid, payAmount);
//                    }
//                    break;
//
//                case "2":
//                    System.out.print("💵 请输入充值金额 (欧): ");
//                    double rechargeAmount = Double.parseDouble(scanner.nextLine());
//                    String rechargeRfid = waitForCardSwipe(scanner);
//                    if (rechargeRfid != null) {
//                        paymentService.rechargeAccount(rechargeRfid, rechargeAmount);
//                    }
//                    break;
//
//                case "3":
//                    System.out.print("👤 请输入新用户的姓名: ");
//                    String name = scanner.nextLine();
//                    String newRfid = waitForCardSwipe(scanner);
//                    if (newRfid != null) {
//                        // ⚠️ 注意：如果这里报错，请把 getUserByRfid 换成 findByRfidTag，把 addUser 换成 saveUser
//                        if (userDAO.getUserByRfid(newRfid) == null) {
//                            User newUser = new User(name, newRfid, 0.0, "USER");
//                            userDAO.addUser(newUser);
//                            System.out.println("✅ 用户 [" + name + "] (卡号: " + newRfid + ") 注册成功！");
//                        } else {
//                            System.out.println("❌ 操作失败：该物理卡片已被注册！");
//                        }
//                    }
//                    break;
//
//                default:
//                    System.out.println("⚠️ 无效选项，请重新输入！");
//            }
//        }
//        scanner.close();
//    }
//
//    /**
//     * 核心融合方法：调用 Python 脚本等待硬件刷卡
//     * (附带了防崩溃的 Windows 本地模拟测试功能)
//     */
//    private static String waitForCardSwipe(Scanner scanner) {
//        System.out.println("⏳ 请将 RFID 卡片贴近读卡器...");
//        try {
//            ProcessBuilder pb = new ProcessBuilder("python3", "read_rfid.py");
//            Process process = pb.start();
//            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//
//            // 尝试读取 Python 脚本抓取到的硬件卡号
//            while ((line = reader.readLine()) != null) {
//                String rfidTag = line.trim();
//                if (!rfidTag.isEmpty()) {
//                    System.out.println("🔔 [硬件拦截] 成功读取到物理卡号: " + rfidTag);
//                    return rfidTag;
//                }
//            }
//            process.waitFor();
//
//        } catch (Exception e) {
//            // 忽略报错，交给下面的模拟模式处理
//        }
//
//        // 💡 开发者福利：如果你在 Windows 电脑上运行，Python 脚本会失败，此时会自动切换到手动输入模式
//        System.out.println("⚠️ [本地开发模式] 未检测到树莓派硬件环境。");
//        System.out.print("⌨️ 请通过键盘手动输入模拟卡号 (例如 8888): ");
//        return scanner.nextLine().trim();
//    }
//}