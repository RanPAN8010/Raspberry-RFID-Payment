package Raspberry;

public class APP {
	public static void main(String[] args) {
        System.out.println("RFID Payment System is starting...");
        // 模拟一个持续运行的服务，防止容器退出
        while(true) {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
