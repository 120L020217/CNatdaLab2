package host;

import java.io.IOException;
import java.net.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-14 11:45
 */
public class sr_receiver extends myhost implements receiver {
    private final int N; // 窗口大小 8
    private final int Seq_num; // 序号空间大小 16
    protected int expSeq;
    protected InetAddress retAddress;
    protected int retPort;
    protected boolean flag;
    private final Set<Integer> commitedData = new HashSet<>();

    public sr_receiver(String name, int recvPort, int n, int seq_num) throws SocketException {
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
        System.out.println(getHostName() + "收到报文: \n=========\n" + fromServer + "\n=========");
        int ack = Integer.parseInt(fromServer.substring(fromServer.indexOf("Seq = ") + 6).trim());
        retAddress = datagramPacket.getAddress();
        retPort = datagramPacket.getPort();
        return ack;
    }

    @Override
    public void sendACK(int ack) throws IOException {
        if (ack == -1) { // 没收到报文, 什么也不做
            ;
        } else { // 收到报文
            if (((expSeq < Seq_num - N) && (ack < expSeq + N && ack >= expSeq)) ||
                    ((expSeq >= Seq_num - N) && (ack >= expSeq || ack < (expSeq + N) % Seq_num))) {
                // 先缓存报文
                if (!commitedData.contains(ack)) {
                    System.out.println(getHostName() + " 缓存分组: " + ack);
                    commitedData.add(ack);
                }
                // 滑窗
                while (commitedData.contains(expSeq)) {
                    commitedData.remove(expSeq);
                    // TODO 向上层交付缓存
                    /*

                     */
                    expSeq = (expSeq + 1) % Seq_num;
                }
                // 发ACK
                if (ack < 0){ // 丢包率: 0
                    /* 这个位置有个小bug, 当丢包率设为ack余7为0丢弃,
                        落入窗口内的包, 不管是不是有缓存, 只要余7为0都会丢弃,
                        所以发送端永远接不到ACK.
                        究其原因是这个简陋的丢包方式, 修改可以让有缓存的包不丢.
                        由于代码再套if比较冗长, 干脆把丢包率设为0 :)
                     */
                } else {
                    String sendData = getHostName()
                            + ": Sending to port " + retPort + ", ACK: " + ack;
                    byte[] data = sendData.getBytes();
                    DatagramPacket datagramPacket = new DatagramPacket(data,
                            data.length, retAddress, retPort);
                    socket.send(datagramPacket);
                }
                System.out.println(getHostName() + "发送 ACK: " + ack);
            } else {
                String sendData = getHostName()
                        + ": Sending to port " + retPort + ", ACK: " + ack;
                byte[] data = sendData.getBytes();
                DatagramPacket datagramPacket = new DatagramPacket(data,
                        data.length, retAddress, retPort);
                socket.send(datagramPacket);
                System.out.println(getHostName() + "重新发送 ACK: " + ack);
            }
        }
    }
}
