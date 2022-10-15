package host;

import java.io.IOException;
import java.net.*;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-14 11:45
 */
public class gbn_receiver extends myhost implements receiver {
    private final int N; // 窗口大小 8
    private final int Seq_num; // 序号空间大小 16
    protected int expSeq;
    protected InetAddress retAddress;
    protected int retPort;
    protected boolean flag;

    public gbn_receiver(String name, int recvPort, int n, int seq_num) throws SocketException {
        super(new DatagramSocket(recvPort), name);
        N = n;
        Seq_num = seq_num;
    }

    public void recv() throws IOException {
        expSeq = 0;
        int ack;
        flag = true;
        while (flag) {
            ack = recvData();
            sendACK(ack);
        }
        socket.close();
    }

    @Override
    /**
     * @Description:
     * @Param: []
     * @return: 接受到数据返回ack, 否则返回-1
     * @Author: coldcodacode
     * @Date: 2022-10-15
     */
    public int recvData() throws IOException {
        // 收报文
        byte[] bytes = new byte[4096]; // 4k 数据
        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
        socket.setSoTimeout(10000);
        try {
            socket.receive(datagramPacket);
        } catch (SocketTimeoutException ex) {
            System.out.println(getHostName() + " 等待分组超时, " + getHostName() + "接收端关闭");
            flag = false;
            return -1;
        }

        // 解析报文
        String fromServer = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
        // TODO 观察接受端如何返回ACK 假设收到的是最新数据报 同时影响sent_num的计数
        System.out.println(getHostName() + "收到报文: \n=========\n" + fromServer + "\n=========");
        int ack = Integer.parseInt((fromServer.substring(fromServer.indexOf("Seq = ") + 6).trim().split(" "))[0]);
        retAddress = datagramPacket.getAddress();
        retPort = datagramPacket.getPort();
        return ack;
    }

    @Override
    /**
    * @Description:
    * @Param: [ack]
    * @return: void
    * @Author: coldcodacode
    * @Date: 2022-10-15
    */
    public void sendACK(int ack) throws IOException {
        if (ack == -1) { // 没收到报文, 什么也不做
            ;
        } else { // 收到报文
            System.out.println(getHostName() + "期待报文" + expSeq);
            if (ack == expSeq) { // 预期报文
                // 模拟丢包
                if (ack % 7 == 0){ // 丢包率: 1 / 7
                    ;
                }
                else{
                    String sendData = getHostName()
                            + ": Sending to port " + retPort + ", ACK: " + ack;
                    byte[] data = sendData.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(data,
                            data.length, retAddress, retPort);
                    socket.send(datagramPacket);
                }
                System.out.println(getHostName() + "收到期待报文");
                expSeq = (expSeq + 1) % Seq_num;
            } else { // 非预期报文
                String sendData = getHostName()
                        + ": Sending to port " + retPort + ", ACK: " + ((expSeq - 1 + Seq_num) % Seq_num);
                byte[] data = sendData.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(data,
                        data.length, retAddress, retPort);
                socket.send(datagramPacket);
                System.out.println(getHostName() + "未收到期待报文");
            }
        }
    }
}
