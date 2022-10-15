package main;

import host.sr_receiver;
import host.sr_sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * @author: coldcodacode
 * @description:
 * @date: 2022-10-15 17:00
 */
public class sr_main {
    public static void main(String[] args) throws SocketException, UnknownHostException {
        sr_sender hostSender = new sr_sender("hostSender", 8, 3, 16, 30);
        hostSender.setDestAddress(InetAddress.getLocalHost());
        hostSender.setDestPort(8000);
        sr_receiver hostReceiver = new sr_receiver("hostReceiver", 8000, 8, 16);

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
