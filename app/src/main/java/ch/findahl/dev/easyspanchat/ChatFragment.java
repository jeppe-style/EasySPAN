package ch.findahl.dev.easyspanchat;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 */
public class ChatFragment extends Fragment {

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public static final String ARG_SECTION_NUMBER = "section_number";

    private Button sendButton;
    private EditText chatMessageEditText;

    private MainActivity mActivity;
    private OnSendButtonClickedListener mSendButtonListener;
    private List<ChatMessage> messages;

    private int sectionNumber;

    public interface OnSendButtonClickedListener {
        void onSendButtonClicked(String msg);
    }

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ListAdapter mAdapter;

    public static ChatFragment newInstance(int sectionNumber) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ChatFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = (MainActivity) getActivity();

        messages = new ArrayList<>();
        sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);

        if (mActivity.getMessages(sectionNumber) != null) {
            messages.addAll(mActivity.getMessages(sectionNumber));
        }

        mAdapter = new ChatMessageAdapter(getActivity(),
                messages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        chatMessageEditText = (EditText) mActivity.findViewById(R.id.chatMessageEditText);
        sendButton = (Button) mActivity.findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSendButtonPressed();
            }
        });

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        mSendButtonListener = (OnSendButtonClickedListener) activity;
    }

    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link Activity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();
        mActivity.onFragmentResumed(sectionNumber);
    }

    public void setInputEnabled(Boolean enabled) {

        sendButton.setEnabled(enabled);
        chatMessageEditText.setEnabled(enabled);

        if (enabled) {
            chatMessageEditText.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
            chatMessageEditText.requestFocus();
        } else {
            chatMessageEditText.setInputType(InputType.TYPE_NULL);
            chatMessageEditText.clearFocus();
        }

    }

    public void displayChatMessage(ChatMessage message) {
        messages.add(message);
        ((ChatMessageAdapter) mAdapter).notifyDataSetChanged();
    }

    private void onSendButtonPressed() {

        mSendButtonListener.onSendButtonClicked(chatMessageEditText.getText().toString());

        chatMessageEditText.getText().clear();

        InputMethodManager inputManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);

    }

}
