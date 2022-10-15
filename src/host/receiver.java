package host;

import java.io.IOException;

public interface receiver {
    public void sendACK(int ack) throws IOException;

    public int recvData() throws IOException;
}
