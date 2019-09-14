package com.asav.flexis

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment

class FragmentAddObjectiveOptions : DialogFragment(), TextView.OnEditorActionListener {

    private var tvNew: TextView? = null
    private var tvFromExisting: TextView? = null
    internal var tbId = ""

    // 1. Defines the listener interface with a method passing back data result.

    interface FragmentAddObjectiveOptionsListener {

        fun onFinishEditDialog(inputText: String, tbId: String)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_objective_options, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Get field from view

        tvNew = view.findViewById(R.id.tv_from_new)
        tvFromExisting = view.findViewById(R.id.tv_from_existing)

        // Fetch arguments from bundle and set title
        val title = arguments!!.getString("title", "Enter Name")
        this.tbId = arguments!!.getString("tbId", "Enter Name")

        dialog.setTitle(title)

        // Show soft keyboard automatically and request focus to field
        //mEditText.requestFocus();

        dialog.window!!.setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        // 2. Setup a callback when the "Done" button is pressed on keyboard
        tvNew!!.setOnEditorActionListener(this)
        tvFromExisting!!.setOnEditorActionListener(this)

    }

    // Fires whenever the textfield has an action performed

    // In this case, when the "Done" button is pressed

    // REQUIRES a 'soft keyboard' (virtual keyboard)

    override fun onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean {

        val listener = activity as FragmentAddObjectiveOptionsListener?
        listener!!.onFinishEditDialog(v.text.toString(), tbId)

        dismiss()

        /*if (EditorInfo.IME_ACTION_DONE == actionId) {

            // Return input text back to activity through the implemented listener

            FragmentAddObjectiveOptionsListener listener = (FragmentAddObjectiveOptionsListener) getActivity();

            //listener.onFinishEditDialog(mEditText.getText().toString());

            // Close the dialog and return back to the parent activity

            return true;

        }*/
        return false
    }

    companion object {

        fun newInstance(title: String, tbId: String): FragmentAddObjectiveOptions {

            val frag = FragmentAddObjectiveOptions()
            val args = Bundle()
            args.putString("title", title)
            args.putString("tbId", tbId)
            frag.arguments = args

            return frag
        }
    }
}// Empty constructor is required for DialogFragment
// Make sure not to add arguments to the constructor
// Use `newInstance` instead as shown below
