package com.alex.weatherapp.UIDynamic;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.alex.weatherapp.R;

/**
 * Created by Alex on 08.12.2015.
 */
public class EditPlaceNameDialog extends DialogFragment {
    private static final String ARG_SUGGESTED_PLACE_NAME = "suggested_place_name";
    public interface IEditPlaceNameDialogListener {
        void onPositiveButtonClicked(String confirmedPlaceName);
        void onNegativeButtonClicked();
    }

    public static EditPlaceNameDialog newInstance(String nameSuggestion){
        EditPlaceNameDialog dialogFragment = new EditPlaceNameDialog();
        Bundle args = new Bundle();
        args.putString(ARG_SUGGESTED_PLACE_NAME, nameSuggestion);
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    public EditPlaceNameDialog() {
        super();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.add_new_place_edit_dlg_layout, null);

        EditText nameEdit = (EditText) view.findViewById(R.id.idc_anp_place_name);
        final String nameSuggestion = getArguments().getString(ARG_SUGGESTED_PLACE_NAME);
        nameEdit.setText(nameSuggestion);

        builder.setView(view);
        builder.setPositiveButton(R.string.ids_dialog_add_place_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                EditText nameEdit = (EditText) getDialog().findViewById(R.id.idc_anp_place_name);
                String placeName = nameEdit.getText().toString();
                mListener.onPositiveButtonClicked(placeName);
                Toast.makeText(getActivity(), "Place name: " + placeName, Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.ids_dialog_add_place_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onNegativeButtonClicked();
            }
        });

        return builder.create();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (IEditPlaceNameDialogListener) activity;
        }catch (ClassCastException cce){
            throw new ClassCastException("Activity must implement IEditPlaceNameDialogListener" +
                    " interface");
        }
    }

    IEditPlaceNameDialogListener mListener;
}
