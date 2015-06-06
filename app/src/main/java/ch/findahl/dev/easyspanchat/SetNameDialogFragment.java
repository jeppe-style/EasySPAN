package ch.findahl.dev.easyspanchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by jesper on 27/04/15.
 */
public class SetNameDialogFragment extends DialogFragment {

    public interface ChangeNameDialogListener{
        void onDialogPositiveClick(DialogFragment dialog);
    }

    private ChangeNameDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {

            mListener = (ChangeNameDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement " +
                    "ChangeNameDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_name, null);

        String currentValue = getActivity().getPreferences(Context.MODE_PRIVATE).getString
                (getString(R.string
                        .preference_device_name), "no name");

        ((EditText) view.findViewById(R.id.name)).setText(currentValue);

        builder.setView(view);

        builder.setTitle("Set Name");

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mListener.onDialogPositiveClick(SetNameDialogFragment.this);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SetNameDialogFragment.this.getDialog().cancel();
            }
        });

        return builder.create();
    }
}
