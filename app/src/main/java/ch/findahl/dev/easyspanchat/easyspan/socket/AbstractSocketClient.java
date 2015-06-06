package ch.findahl.dev.easyspanchat.easyspan.socket;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

import ch.findahl.dev.easyspanchat.easyspan.socket.stream.SocketInputStreamThread;
import ch.findahl.dev.easyspanchat.easyspan.socket.stream.SocketOutputStreamThread;

/**
 * Created by jesper on 26/05/15.
 */
public class AbstractSocketClient extends SocketMessageThread {

    Socket socket;
    private SocketInputStreamThread inputStreamThread;
    private SocketOutputStreamThread outputStreamThread;

    private final CountDownLatch doneSignal = new CountDownLatch(1);

    AbstractSocketClient(SocketMessageListener messageListener) {
        super(messageListener);
    }

    @Override
    protected void beforeRun() {

        inputStreamThread = new SocketInputStreamThread(socket, getMessageListener(), doneSignal);
        outputStreamThread = new SocketOutputStreamThread(socket, getMessageListener(), doneSignal);

        inputStreamThread.start();
        outputStreamThread.start();

    }

    @Override
    public void afterRun() {

        if (inputStreamThread != null)
            inputStreamThread.stopWork();
        if (outputStreamThread != null)
            outputStreamThread.stopWork();

        if (socket != null) {
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void doWork() {

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        stopWork();

    }

    @Override
    public void stopWork() {

        doneSignal.countDown();

        super.stopWork();
    }

    @Override
    public void sendMessage(Object message) {
        try {
            if (outputStreamThread != null)
                outputStreamThread.sendMessage(message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
