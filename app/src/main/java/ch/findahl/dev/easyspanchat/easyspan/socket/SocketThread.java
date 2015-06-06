package ch.findahl.dev.easyspanchat.easyspan.socket;

import java.lang.Override;
import java.lang.Thread;

/**
 * Created by jesper on 06/04/15.
 */
public abstract class SocketThread extends Thread {


    private volatile boolean isRunning = false;
    private final SocketMessageListener messageListener;

    protected SocketThread(SocketMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    protected abstract void beforeRun();

    protected abstract void afterRun();

    protected abstract void doWork();

    @Override
    public void run() {

        isRunning = true;

        beforeRun();

        while (isRunning) {

            doWork();

        }

        afterRun();

    }

    public void stopWork() {

        isRunning = false;

    }

    protected SocketMessageListener getMessageListener() {
        return messageListener;
    }

}
