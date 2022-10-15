package host;

import java.io.IOException;
import java.net.InetAddress;

public interface sender {
    public void sendData() throws IOException;

    public void recvACk() throws IOException;

    public void setDestPort(int destPort);

    public void setDestAddress(InetAddress destAddress);
}
