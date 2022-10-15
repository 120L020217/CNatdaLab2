package host;

import java.io.IOException;
import java.net.DatagramSocket;

/**
 * @author: 120L020217崔家铭
 * @description:
 * @date: 2022-10-13 23:11
 */
public abstract class myhost {
    protected DatagramSocket socket;
    private final String name;

    public myhost(DatagramSocket socket, String name) {
        this.socket = socket;
        this.name = name;
    }

    public String getHostName() {
        return name;
    }
}
