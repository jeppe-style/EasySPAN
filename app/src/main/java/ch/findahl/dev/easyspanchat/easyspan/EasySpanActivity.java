package ch.findahl.dev.easyspanchat.easyspan;

import android.support.v7.app.AppCompatActivity;

import ch.findahl.dev.easyspanchat.easyspan.messaging.EasySpanMessage;

/**
 * Created by jesper on 25/05/15.
 */
public abstract class EasySpanActivity extends AppCompatActivity {

    /**
     * Called when Easy SPAN messaging service changes status.
     * @param enabled the status of the messaging service
     */
    public abstract void messageServiceAvailable(boolean enabled);

    /**
     * Called when Easy SPAN receives a message from a connected peer.
     *
     * @param message the message object
     * @param messageId the identifier of the conversation
     */
    public abstract void onMessageReceived(EasySpanMessage message, String messageId);

    /**
     * Called when a new device is connected to the network group
     *
     * @param name the name of the device
     * @param messageId the id of the device, can be used to identify conversation
     */
    public abstract void onDeviceAdded(String name, String messageId);

    /**
     * Called when a device has disconnected from the network group
     *
     * @param name the name of the device
     */
    public abstract void onDeviceRemoved(String name);

    // TODO - this method should not pass a string but the status of the devices
    public abstract void updateDevicesTextView(String text);
}
