package ch.findahl.dev.easyspanchat;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.findahl.dev.easyspanchat.easyspan.EasySpanActivity;
import ch.findahl.dev.easyspanchat.easyspan.EasySpanManager;
import ch.findahl.dev.easyspanchat.easyspan.messaging.EasySpanMessage;
import ch.findahl.dev.easyspanchat.easyspan.messaging.MessageManager;


public class MainActivity extends EasySpanActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
        ChatFragment
        .OnSendButtonClickedListener, SetNameDialogFragment.ChangeNameDialogListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String devicesText;
    private String fragmentToBeRemoved;

    private void logDebugMessage(String msg) {
        boolean debug = true;
        if (debug)
            Log.d(TAG, msg);
    }

    private Map<String, String> fragmentToDeviceMap; // <"fragment name", "messageId">,
                                                     // messageId=devieId for unicast messages

    private Map<String, List<ChatMessage>> messages; // <"messageId", List<Object> >

    /*--- Easy SPAN ---*/

    private EasySpanManager spanManager;

    /*--- Navigation Drawer ---*/

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String deviceName;
    private boolean chatEnabled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragmentToDeviceMap = new HashMap<>();
        messages = new HashMap<>();

        /* --- Easy SPAN --- */

        deviceName = getPreferences(Context.MODE_PRIVATE).getString(getString(R.string.preference_device_name), "no name");

        spanManager = new EasySpanManager(this, deviceName);
        spanManager.initialize();

        /* --- Navigation Drawer --- */

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // add the default fragment options
        fragmentToDeviceMap.put(getString(R.string.title_public_chat), MessageManager.BROADCAST_MESSAGE_ID);
        mNavigationDrawerFragment.addFragment(getString(R.string
                .title_public_chat));

    }


    @Override
    protected void onResume() {
        super.onResume();

        spanManager.resume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        spanManager.pause();

    }

    @Override
    protected void onStop() {
        super.onStop();
        spanManager.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spanManager.destroy();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, ChatFragment.newInstance(position))
                .commit();

    }


    private void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){

            case R.id.action_name:
                return changeName();

            case R.id.action_discover:
                return spanManager.discoverPeers();

            case R.id.action_stop_discovery:
                return spanManager.stopPeerDiscovery();

            case R.id.action_create_group:
                return spanManager.createGroup();

            case R.id.action_remove_group:
                return spanManager.removeCurrentGroup();

            default:
                return super.onOptionsItemSelected(item);

        }
    }


    @SuppressWarnings("SameReturnValue")
    private boolean changeName() {

        DialogFragment nameDialogFragment = new SetNameDialogFragment();
        nameDialogFragment.show(getFragmentManager(), "name");

        return true;
    }

    public String getDeviceName() {
        return deviceName;
    }

    @Override
    public void onSendButtonClicked(String msg) {

        FragmentManager fragmentManager = getFragmentManager();
        final ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentById(R.id
                .container);
        int fragmentNumber = chatFragment.getArguments().getInt(ChatFragment.ARG_SECTION_NUMBER);
        String fragmentName = mNavigationDrawerFragment.getItem(fragmentNumber);

        String toId = fragmentToDeviceMap.get(fragmentName);

        ChatMessage message = new ChatMessage((toId.equals(MessageManager.BROADCAST_MESSAGE_ID)),
                spanManager.getThisDeviceName(),
                spanManager.getThisDeviceId(), toId, msg);

        spanManager.onSendMessage(message);
    }


    @Override
    public void onMessageReceived(final EasySpanMessage message, String messageId) {

        if (! (message instanceof ChatMessage)) return;

        logDebugMessage("onMessageReceived: " + message);

        saveMessage((ChatMessage) message, messageId);

        FragmentManager fragmentManager = getFragmentManager();
        final ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentById(R.id
                .container);
        int fragmentNumber = chatFragment.getArguments().getInt(ChatFragment.ARG_SECTION_NUMBER);
        String fragmentName = mNavigationDrawerFragment.getItem(fragmentNumber);

        logDebugMessage("fragment: " + fragmentName);

        if (fragmentToDeviceMap.get(fragmentName).equals(messageId)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatFragment.displayChatMessage((ChatMessage) message);
                }
            });
        }

        logDebugMessage("Displaying chat message from " + messageId);

    }


    private void saveMessage(ChatMessage message, String messageId) {

        List<ChatMessage> messageList;

        messageList = messages.get(messageId);

        if (messageList == null) {

            messageList = new ArrayList<>();

            messages.put(messageId, messageList);

        }

        messageList.add(message);

    }

    @Override
    public void updateDevicesTextView(final String text) {

        devicesText = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView devicesTextView = (TextView) findViewById(R.id.devicesTextView2);
                devicesTextView.setText(text);
            }
        });

    }

    @Override
    public void messageServiceAvailable(final boolean enabled) {

        logDebugMessage("enabling chat input? " + enabled);

        chatEnabled = enabled;

        // TODO - handle even of chat not available
        if (!chatEnabled) {
            // remove all messages first (so fragments cannot add)
            messages.clear();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNavigationDrawerFragment.selectItem(0);
                }
            });

            for (final String name : fragmentToDeviceMap.keySet()) {

                if  (!name.equals(getString(R.string.title_public_chat))) {
                    logDebugMessage("removing fragment: " + name);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mNavigationDrawerFragment.removeFragment(name);
                        }
                    });
                }
            }

        }

        setChatInput();
    }

    private void setChatInput() {
        FragmentManager fragmentManager = getFragmentManager();
        final ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentById(R.id
                .container);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (chatFragment != null)
                    chatFragment.setInputEnabled(chatEnabled);
            }
        });
    }

    public List<ChatMessage> getMessages(int fragmentNumber) {

        String fragmentId = mNavigationDrawerFragment.getItem(fragmentNumber);
        String messageId = fragmentToDeviceMap.get(fragmentId);

        return messages.get(messageId);

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        this.deviceName = ((EditText) dialog.getDialog().findViewById(R.id.name)).getText().toString();

        logDebugMessage("Name changed to " + this.deviceName);

        SharedPreferences sharedPreferences = getPreferences(Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.preference_device_name), deviceName);
        editor.apply();

        spanManager.setThisDeviceName(this.deviceName);
    }


    @Override
    public void onDeviceAdded(final String name, final String messageId) {

        logDebugMessage("Device added: " + name);

        fragmentToDeviceMap.put(name, messageId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mNavigationDrawerFragment.addFragment(name);
            }
        });

    }

    @Override
    public void onDeviceRemoved(final String name) {

        logDebugMessage("Device Removed: " + name);

        FragmentManager fragmentManager = getFragmentManager();
        final ChatFragment chatFragment = (ChatFragment) fragmentManager.findFragmentById(R.id
                .container);
        int fragmentNumber = chatFragment.getArguments().getInt(ChatFragment.ARG_SECTION_NUMBER);
        String currentFragment = mNavigationDrawerFragment.getItem(fragmentNumber);
        fragmentToBeRemoved = name;

        if (fragmentToBeRemoved.equals(currentFragment)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNavigationDrawerFragment.selectItem(0);
                }
            });

        } else {
            String id = fragmentToDeviceMap.remove(fragmentToBeRemoved);
            messages.remove(id);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNavigationDrawerFragment.removeFragment(fragmentToBeRemoved);
                    fragmentToBeRemoved = null;
                }
            });
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, name + " is no longer available", Toast
                        .LENGTH_SHORT)
                        .show();
            }
        });

    }

    public void onFragmentResumed(int number) {
        logDebugMessage("onFragmentResumed");
        if (devicesText != null) updateDevicesTextView(devicesText);
        mTitle = mNavigationDrawerFragment.getItem(number);
        logDebugMessage("title: " + mTitle);
        logDebugMessage("position: " + number);
        restoreActionBar();
        setChatInput();

        // if we need to remove a fragment
        if (fragmentToBeRemoved != null) {
            String id = fragmentToDeviceMap.remove(fragmentToBeRemoved);
            messages.remove(id);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mNavigationDrawerFragment.removeFragment(fragmentToBeRemoved);
                    fragmentToBeRemoved = null;
                }
            });
            messages.remove(fragmentToBeRemoved);
        }

    }
}
