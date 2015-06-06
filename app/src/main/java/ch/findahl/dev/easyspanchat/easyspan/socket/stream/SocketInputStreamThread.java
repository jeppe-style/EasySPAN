package ch.findahl.dev.easyspanchat.easyspan.socket.stream;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ClassNotFoundException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.CountDownLatch;

import ch.findahl.dev.easyspanchat.easyspan.router.RouterMessage;
import ch.findahl.dev.easyspanchat.easyspan.socket.SocketMessageListener;

/**
 * Created by jesper on 05/04/15.
 */
public class SocketInputStreamThread extends SocketStreamThread {

    private static final String TAG = SocketInputStreamThread.class.getSimpleName();

    private final boolean debug = true;

    private ObjectInputStream inputStream;

    public SocketInputStreamThread(Socket socket, SocketMessageListener messageListener,
                                   CountDownLatch doneSignal) {
        super(socket, messageListener, doneSignal);
    }

    @Override
    public void beforeRun() {

        Socket socket = getSocket();

        try {
            assert socket != null;
            inputStream = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void afterRun() {

        try {
            inputStream.close();
            inputStream = null;
        } catch (IOException e) {
            e.printStackTrace();
        }


        Log.d(TAG, "counting down signal");
        getDoneSignal().countDown();

    }

    /**
     *
     */
    @Override
    public void doWork() {

        try {

            Object message = inputStream.readObject();

            if (debug && message instanceof RouterMessage) {
                Log.d(TAG, "reading router message \n" + message + "\n" + this.toString());
            }

            getMessageListener().onMessage(message);

        } catch (ClassNotFoundException e) {
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
}
