package host;

import timer.SRTimer;
import timer.moudle;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-14 11:41
 */
public class sr_sender extends myhost implements sender {
    private final int N; // 窗口大小 8
    private final int TimeLimit; // 超时时间 3
    private final int Seq_num; // 序号空间大小 16
    private final int chunk_num; // 要发送的分组个数 30
    protected InetAddress destAddress;
    protected int destPort = 10240;
    protected int base;
    protected int nextSeq;
    protected int sent_num;

    private final moudle[] timerMoudle = new moudle[16];
    private final SRTimer[] timer = new SRTimer[16];
    private final Set<Integer> commitedACK = new HashSet<>();


    public sr_sender(String name, int n, int timeLimit, int seq_num, int chunk_num) throws SocketException {
        super(new DatagramSocket(), name);
        N = n;
        TimeLimit = timeLimit;
        Seq_num = seq_num;
        this.chunk_num = chunk_num;

        for (int i = 0; i < 16; i++) {
            timerMoudle[i] = new moudle();
            timer[i] = new SRTimer(timerMoudle[i], this, i);
        }
    }

    public void send() throws IOException {
        // 初始化计时器
        for (int i = 0; i < Seq_num; i++) {
            timerMoudle[i].setTime(0);
            timer[i].start();
        }

        base = 0;
        nextSeq = 0;
        sent_num = 0;
        while (sent_num < chunk_num) {
            sendData();
            recvACk();
        }
        for (int i = 0; i < Seq_num; i++) {
            timerMoudle[i].setTime(0);
        }
        socket.close();
        System.out.println(getHostName() + "所有分组接收完毕, " + getHostName() + "发送端关闭");
    }

    public void timeOut(int seq) throws IOException {
        synchronized (this) {
            String resendData = getHostName()
                    + ": Resending to port " + destPort + ", Seq = " + seq;

            byte[] data = resendData.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(data,
                    data.length, destAddress, destPort);
            socket.send(datagramPacket);

            System.out.println(getHostName()
                    + "重新发送发送到" + destPort + "端口， Seq = " + seq);
        }
    }

    @Override
    // 改变nextSeq
    public void sendData() throws IOException {
        int step = nextSeq - base;
        step = (step >= 0) ? step : (step + Seq_num);
        while (step < N && sent_num < chunk_num) {
            // 计时开始
            timerMoudle[nextSeq].setTime(TimeLimit);

            // 模拟丢包
            if (nextSeq % 5 == 0) { // 序号是5的倍数的包都被丢掉, 丢包率0.2
                ;
            } else {
                String sendData = getHostName()
                        + ": Sending to port " + destPort + ", Seq = " + nextSeq;
                byte[] data = sendData.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(data,
                        data.length, destAddress, destPort);
                socket.send(datagramPacket);
            }
            System.out.println(getHostName()
                    + "发送到" + destPort + "端口, Seq = " + nextSeq);
            synchronized (this) {
                nextSeq = (nextSeq + 1) % Seq_num;
            }
            step = nextSeq - base;
            step = (step >= 0) ? step : (step + Seq_num);
        }
    }

    @Override
    // 改变base sent_num
    public void recvACk() throws IOException {
        byte[] bytes = new byte[4096]; // 4k 数据
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        socket.setSoTimeout(5000);
        while (true) {
            // 接收报文
            try {
                socket.receive(datagramPacket);
            } catch (SocketTimeoutException ex) {
                System.out.println(getHostName() + " 等待分组超时, 消息队列中没有ACK报文");
                break;
            }

            // 解析报文
            String fromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            System.out.println(getHostName() + "收到报文: \n=========\n" + fromServer + "\n=========");
            int ack = Integer.parseInt(fromServer.substring(fromServer.indexOf("ACK: ") + 5).trim());
            // 落在窗口内, 则标记分组
            if (base < Seq_num - N) {
                if (ack < base + N && ack >= base) {
                    commitedACK.add(ack);
                    timerMoudle[ack].setTime(0);
                    sent_num++;
                }
            } else {
                if (ack >= base || ack < (base + N) % Seq_num) {
                    commitedACK.add(ack);
                    timerMoudle[ack].setTime(0);
                    sent_num++;
                }
            }
            System.out.println(getHostName() + "已收到分组数: " + sent_num);
            // 滑窗
            while (commitedACK.contains(base)) {
                commitedACK.remove(base);
                base = (base + 1) % Seq_num;
            }
            System.out.println("after while base" + base + " nextSeq "+ nextSeq);

            // socket的receive函数导致长度被设置为接受数据长度, 这里更正回来
            datagramPacket.setLength(4096);
        }
    }

    @Override
    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    @Override
    public void setDestAddress(InetAddress destAddress) {
        this.destAddress = destAddress;
    }
}
