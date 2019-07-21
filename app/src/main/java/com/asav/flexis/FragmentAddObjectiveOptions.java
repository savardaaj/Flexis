package com.asav.flexis;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class FragmentAddObjectiveOptions extends DialogFragment implements TextView.OnEditorActionListener {

    private TextView tvNew, tvFromExisting;
    String tbId = "";

    public FragmentAddObjectiveOptions() {

        // Empty constructor is required for DialogFragment

        // Make sure not to add arguments to the constructor

        // Use `newInstance` instead as shown below
    }

    // 1. Defines the listener interface with a method passing back data result.

    public interface FragmentAddObjectiveOptionsListener {

        void onFinishEditDialog(String inputText, String tbId);

    }

    public static FragmentAddObjectiveOptions newInstance(String title, String tbId) {

        FragmentAddObjectiveOptions frag = new FragmentAddObjectiveOptions();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("tbId", tbId);
        frag.setArguments(args);

        return frag;
    }

    @Override

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_objective_options, container);
    }

    @Override

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get field from view

        tvNew = view.findViewById(R.id.tv_from_new);
        tvFromExisting = view.findViewById(R.id.tv_from_existing);

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Enter Name");
        this.tbId = getArguments().getString("tbId", "Enter Name");

        getDialog().setTitle(title);

        // Show soft keyboard automatically and request focus to field
        //mEditText.requestFocus();

        getDialog().getWindow().setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        // 2. Setup a callback when the "Done" button is pressed on keyboard
        tvNew.setOnEditorActionListener(this);
        tvFromExisting.setOnEditorActionListener(this);
    }

    // Fires whenever the textfield has an action performed

    // In this case, when the "Done" button is pressed

    // REQUIRES a 'soft keyboard' (virtual keyboard)

    @Override

    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

        FragmentAddObjectiveOptionsListener listener = (FragmentAddObjectiveOptionsListener) getActivity();
        listener.onFinishEditDialog(v.getText().toString(), tbId);

        dismiss();

        /*if (EditorInfo.IME_ACTION_DONE == actionId) {

            // Return input text back to activity through the implemented listener

            FragmentAddObjectiveOptionsListener listener = (FragmentAddObjectiveOptionsListener) getActivity();

            //listener.onFinishEditDialog(mEditText.getText().toString());

            // Close the dialog and return back to the parent activity

            return true;

        }*/
        return false;
    }
}
