package timer;

import host.gbn_sender;

/**
 * @author: 120L020217崔家铭
 * @description:
 * @date: 2022-10-13 13:07
 */
public class GBNTimer extends Thread{
    private moudle timerMoudle;
    private gbn_sender host;

    public GBNTimer(moudle timerMoudle, gbn_sender host) {
        this.timerMoudle = timerMoudle;
        this.host = host;
    }

    @Override
    public void run() {
        while (true) {
            int time = timerMoudle.getTime();
            if (time > 0) {
                try {
                    Thread.sleep(time * 1000); // 计时time秒

                    System.out.println("\n");
                    if (host != null) {
                        System.out.println(host.getHostName() + "等待ACK超时");
                        host.timeOut();
                    }
                    timerMoudle.setTime(0);

                } catch (Exception e) {
                }
            }
        }
    }
}
