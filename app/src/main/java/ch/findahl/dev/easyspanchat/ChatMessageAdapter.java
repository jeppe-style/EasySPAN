package ch.findahl.dev.easyspanchat;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by jesper on 01/06/15.
 */
class ChatMessageAdapter extends ArrayAdapter<ChatMessage> {

    /**
     * Constructor
     *  @param context  The current context.
     * @param objects  The objects to represent in the ListView.
     */
    public ChatMessageAdapter(Context context, List<ChatMessage> objects) {
        super(context, android.R.layout.simple_list_item_1, objects);

    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder{
        TextView message;
        TextView name;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ChatMessage chatMessage = getItem(position);

        String thisDeviceName = ((MainActivity) getContext()).getDeviceName();
        boolean isSelf = chatMessage.getFromName().equals(thisDeviceName);

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) getContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        View viewToUse = mInflater.inflate(R.layout.chatmessage_list_item, null);
        ViewHolder holder = new ViewHolder();

        if (isSelf) {
            holder.message = (TextView)viewToUse.findViewById(R.id.chatMessageTextYou);
            holder.name = (TextView) viewToUse.findViewById(R.id.chatMessageNameYou);
            viewToUse.findViewById(R.id.chatMessageContainer).setVisibility(View.GONE);
        } else {
            holder.message = (TextView)viewToUse.findViewById(R.id.chatMessageText);
            holder.name = (TextView) viewToUse.findViewById(R.id.chatMessageName);
            viewToUse.findViewById(R.id.chatMessageContainerYou).setVisibility(View.GONE);
        }

        viewToUse.setTag(holder);

        holder.message.setText(chatMessage.getText());
        holder.name.setText(chatMessage.getFromName());
        if (isSelf) holder.name.setText("You");

        return viewToUse;
    }

    @Override
    public boolean isEnabled(int position) {
        // make items not selectableChat
        return false;
    }
}
