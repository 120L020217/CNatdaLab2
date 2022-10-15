package host;

import java.io.IOException;

/**
 * @author: 120L020217崔家铭
 * @description:
 * @date: 2022-10-14 11:43
 */
public class sr_receiver implements receiver{
    @Override
    public void sendACK(int ack) throws IOException {

    }

    @Override
    public int recvData() throws IOException {

        return 0;
    }
}
