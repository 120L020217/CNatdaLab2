package host;

import timer.GBNTimer;
import timer.moudle;

import java.io.IOException;
import java.net.*;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-14 11:41
 */
public class gbn_sender extends myhost implements sender {
    private final int N; // 窗口大小 8
    private final int TimeLimit; // 超时时间 3
    private final int Seq_num; // 序号空间大小 16
    private final int chunk_num; // 要发送的分组个数 30
    protected InetAddress destAddress;
    protected int destPort = 10240;
    protected int base;
    protected int nextSeq;
    protected int sent_num;
    protected int lastACK;

    private final moudle timerMoudle = new moudle();
    // TODO 构造器参数是this
    private final GBNTimer timer = new GBNTimer(timerMoudle, this);


    public gbn_sender(String name, int n, int timeLimit, int seq_num, int chunk_num) throws SocketException {
        super(new DatagramSocket(), name);
        N = n;
        TimeLimit = timeLimit;
        Seq_num = seq_num;
        this.chunk_num = chunk_num;
    }

    public void send() throws IOException {
        // 初始化计时器
        timerMoudle.setTime(0);
        timer.start();

        base = 0;
        nextSeq = 0;
        sent_num = 0;
        lastACK = -1;
        while (sent_num < chunk_num) {
            sendData();
            recvACk();
        }
        timerMoudle.setTime(0);
        socket.close();
        System.out.println(getHostName() + "所有分组接收完毕, " + getHostName() +"发送端关闭");
    }

    @Override
    public void timeOut() throws IOException {
        synchronized (this) {
            for (int i = base; i < nextSeq; i++) {
                String resendData = getHostName()
                        + ": Resending to port " + destPort + ", Seq = " + i;

                byte[] data = resendData.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(data,
                        data.length, destAddress, destPort);
                socket.send(datagramPacket);

                System.out.println(getHostName()
                        + "重新发送发送到" + destPort + "端口， Seq = " + i);
            }
        }
    }

    @Override
    // 改变nextSeq
    public void sendData() throws IOException {
        int step = nextSeq - base;
        step = (step >= 0) ? step : (step + Seq_num);
        while (step < N && sent_num < chunk_num) {
            // 计时开始
            if (base == nextSeq) {
                timerMoudle.setTime(TimeLimit);
            }

            // 模拟丢包
            if (nextSeq % 25 == 0) { // 序号是5的倍数的包都被丢掉, 丢包率0.2
                ;
            }
            else {
                // 发送报文
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
        // 接收报文
        byte[] bytes = new byte[4096]; // 4k 数据
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        socket.setSoTimeout(5000);
        try {
            socket.receive(datagramPacket);
        } catch (SocketTimeoutException ex) {
            System.out.println(getHostName() + " 等待分组超时, 消息队列中没有ACK报文");
            return;
        }

        // 解析报文
        String fromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        // TODO 观察接受端如何返回ACK 假设收到的是最新数据报 同时影响sent_num的计数
        System.out.println(getHostName() + "收到报文: \n=========\n" + fromServer + "\n=========");
        int ack = Integer.parseInt(fromServer.substring(fromServer.indexOf("ACK: ") + 5).trim());
        synchronized (this) {
            base = (ack + 1) % Seq_num;
        }

        int step = ack - lastACK;
        step = (step >= 0) ? step : (step + Seq_num);
        synchronized (this) {
            if (step != Seq_num) {
                sent_num += step;
            }
            lastACK = ack;
        }
        System.out.println(getHostName() + "已收到分组数: " + sent_num);
        if (base == nextSeq) {
            timerMoudle.setTime(0);
        } else {
            timerMoudle.setTime(TimeLimit);
        }
        //System.out.println(getHostName() + "接收到了ACK: " + ack);
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
