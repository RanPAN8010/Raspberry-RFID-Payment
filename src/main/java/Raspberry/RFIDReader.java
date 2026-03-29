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
            // 🚨 修复 1：加上 "-u" 参数，强制 Python 无缓存实时输出！
            ProcessBuilder pb = new ProcessBuilder("python3", "-u", "read_rfid.py");
            Process process = pb.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            while ((line = reader.readLine()) != null) {
                String rfidTag = line.trim();

                if (!rfidTag.isEmpty() && rfidTag.matches("\\d+")) {
                    System.out.println("🔔 [硬件层] 成功拦截纯净物理卡号: " + rfidTag);

                    // 读完后立刻强杀 Python 进程，释放硬件引脚！
                    process.destroy();
                    return rfidTag;
                } else {
                    // 如果读到的是 RuntimeWarning 等杂音，跳过它，继续等下一行
                    System.out.println("⚠️ [过滤掉非卡号杂音]: " + rfidTag);
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("❌ 底层读卡模块调用失败: " + e.getMessage());
        }
        return null;
    }
}