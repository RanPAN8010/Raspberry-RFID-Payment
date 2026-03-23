package Raspberry;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class RFIDReader {
    /**
     * 阻塞并调用 Python 脚本，等待真实物理刷卡
     */
    public static String waitForCardSwipe() {
        System.out.println("⏳ [硬件层] 正在等待实体卡片靠近读卡器...");
        try {
            ProcessBuilder pb = new ProcessBuilder("python3", "read_rfid.py");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String rfidTag = line.trim();
                if (!rfidTag.isEmpty()) {
                    System.out.println("🔔 [硬件层] 成功拦截物理卡号: " + rfidTag);
                    return rfidTag; // 读到就立刻返回
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("❌ 底层读卡模块调用失败: " + e.getMessage());
        }
        return null;
    }
}
