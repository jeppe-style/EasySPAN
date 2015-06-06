package ch.findahl.dev.easyspanchat.easyspan.socket;

/**
 * Created by jesper on 20/05/15.
 */
public interface SocketErrorListener {

    void onNetworkDown(int error);
}
