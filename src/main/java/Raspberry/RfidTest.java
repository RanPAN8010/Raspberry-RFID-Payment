package Raspberry;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RfidTest {
    public static void main(String[] args) {
        System.out.println("=== 📡 RC522 硬件读取测试启动 (方案B: 混合模式) ===");
        System.out.println("请将 RFID 卡片靠近 RC522 模块...");

        try {
            // 调用你刚刚在根目录建好的 Python 脚本
            // 注意：如果在 Windows 本地测试可能需要写 "python"，但在树莓派上是 "python3"
            ProcessBuilder pb = new ProcessBuilder("python3", "read_rfid.py");
            Process process = pb.start();

            // 抓取 Python 脚本的控制台输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            // 只要 Python 脚本打印了内容，这里就能抓到
            while ((line = reader.readLine()) != null) {
                String rfidTag = line.trim();
                if (!rfidTag.isEmpty()) {
                    System.out.println("✅ 成功读取到物理卡号: [" + rfidTag + "]");
                }
            }
            
            process.waitFor(); // 等待进程结束
            
        } catch (Exception e) {
            System.err.println("❌ 调用 Python 脚本失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}