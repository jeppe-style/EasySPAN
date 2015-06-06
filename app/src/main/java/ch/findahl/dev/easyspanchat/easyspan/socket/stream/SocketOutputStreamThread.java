package ch.findahl.dev.easyspanchat.easyspan.socket.stream;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.InterruptedException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import ch.findahl.dev.easyspanchat.easyspan.router.RouterMessage;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageListener;

/**
 * Created by jesper on 05/04/15.
 */
public class SocketOutputStreamThread extends SocketStreamThread {

    private static final String TAG = SocketOutputStreamThread.class.getSimpleName();

    private final boolean debug = true;

    private ObjectOutputStream outputStream;

    private ArrayBlockingQueue<Object> messageQueue;

    public SocketOutputStreamThread(Socket socket, SocketMessageListener chatMessageListener,
                                    CountDownLatch doneSignal) {
        super(socket, chatMessageListener, doneSignal);
    }

    @Override
    public void beforeRun() {

        Socket socket = getSocket();

        try {

            outputStream = new ObjectOutputStream(socket.getOutputStream());

            messageQueue = new ArrayBlockingQueue<>(1, true);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void afterRun() {

        try {
            outputStream.close();
            outputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "counting down signal");
        getDoneSignal().countDown();

    }

    @Override
    public void doWork() {


        try {

            Object message = messageQueue.take();

            if (debug && message instanceof RouterMessage) {
                Log.d(TAG, "writing router message \n" + message + "\n" + this.toString());
            }

            outputStream.writeObject(message);

            outputStream.reset();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            if (e.getClass().equals(EOFException.class) || e.getClass().equals(SocketException.class)) {
                Log.d(TAG, e.getClass() + ". Stopping thread.");
                this.stopWork();
            } else {
                e.printStackTrace();
            }
        }

    }

    public synchronized void sendMessage(Object message) throws InterruptedException {

        if (debug && message instanceof RouterMessage) {
            Log.d(TAG, "putting router message \n" + message + "\n" + this.toString());
        }

        messageQueue.put(message);

    }
}
