package timer;


import host.sr_sender;

/**
 * @author: 120L020217崔家铭
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
}
