package timer;

/**
 * @author: 120L020217崔家铭
 * @description:
 * @date: 2022-10-13 20:35
 */
public class moudle {
    // volatile 禁止指令重排; 保证变量在多线程下, 对其他线程可见(对它的值修改, 其他线程可见)
    private volatile int time;

    // synchronized 防止多线程调用冲突
    public synchronized int getTime(){
        return time;
    }

    public synchronized void setTime(int time){
        this.time = time;
    }
}
