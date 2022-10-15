package timer;


import host.sr_sender;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-14 11:37
 */
public class SRTimer extends Thread{
    private moudle timerMoudle;
    private sr_sender host;
    private int seq;

    public SRTimer(moudle timerMoudle, sr_sender host, int seq) {
        this.timerMoudle = timerMoudle;
        this.host = host;
        this.seq = seq;
    }

    @Override
    public void run() {
        while (true) {
            if (timerMoudle.getTime() > 0) {
                try {
                    Thread.sleep(timerMoudle.getTime() * 1000); // 计时timerMoudle.getTime(秒
                    if (timerMoudle.getTime() > 0) {
                        System.out.println("\n");
                        if (host != null) {
                            System.out.println(host.getHostName() + "分组" + getSeq() + "等待ACK超时");
                            host.timeOut(getSeq());
                        }
                    }
                    timerMoudle.setTime(0);

                } catch (Exception e) {
                }
            }
        }
    }

    public int getSeq(){
        return seq;
    }
}
