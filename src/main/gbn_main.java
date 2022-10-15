package main;

import host.gbn_receiver;
import host.gbn_sender;
import host.myhost;
import host.sender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-14 21:12
 */
public class gbn_main {
    public static void main(String[] args) throws IOException {
        File file = new File("test.txt");
        FileInputStream inputStream = new FileInputStream(file);
        int length = inputStream.available();
        byte bytes[] = new byte[length];
        inputStream.read(bytes);
        inputStream.close();
        String str =new String(bytes, StandardCharsets.UTF_8);

        gbn_sender hostSender = new gbn_sender(str, "hostSender", 8, 3, 16, 10);
        hostSender.setDestAddress(InetAddress.getLocalHost());
        hostSender.setDestPort(10240);
        gbn_receiver hostReceiver = new gbn_receiver("hostReceiver", 10240, 8, 16);

        new Thread(() -> {
            try {
                hostSender.send();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        new Thread(() -> {
            try {
                hostReceiver.recv();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
