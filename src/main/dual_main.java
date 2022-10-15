package main;

import host.gbn_receiver;
import host.gbn_sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author: 120L020217崔家铭
 * @description:
 * @date: 2022-10-15 12:10
 */
public class dual_main {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        gbn_sender clientSender = new gbn_sender("clientSender", 8, 3, 16, 30);
        clientSender.setDestAddress(InetAddress.getLocalHost()); // 服务器主机地址
        clientSender.setDestPort(10240);
        gbn_receiver serverReceiver = new gbn_receiver("serverReceiver", 10240, 8, 16);

        gbn_sender serverSender = new gbn_sender("serverSender", 8, 3, 16, 50);
        serverSender.setDestAddress(InetAddress.getLocalHost()); // 客户端主机地址
        serverSender.setDestPort(20480);
        gbn_receiver clientReceiver = new gbn_receiver("clientReceiver", 20480, 8, 16);

        new Thread(() -> {
            try {
                clientSender.send();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                serverReceiver.recv();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                serverSender.send();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                clientReceiver.recv();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
