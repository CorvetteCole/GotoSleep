package com.corvettecole.gotosleep;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NativeDialogPrompt.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NativeDialogPrompt#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NativeDialogPrompt extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_YES_ACTIONS = "yes_actions";
    private static final String ARG_NO_ACTIONS = "no_actions";

    private String[] mYesActions;
    private String[] mNoActions;

    private OnFragmentInteractionListener mListener;

    private Button rateYesButton;
    private Button rateNoButton;
    private TextView rateTextView;
    private ConstraintLayout rateLayout;

    public NativeDialogPrompt() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param yesActions Link to open if user says yes. "dismiss" will close the prompt, "next" will go to the next prompt level
     * @param noActions Link to open if users says no. "dismiss" will close the prompt, "next" will go to the next prompt level
     * @return A new instance of fragment NativeDialogPrompt.
     */
    public static NativeDialogPrompt newInstance(String[] yesActions, String[] noActions) {
        NativeDialogPrompt fragment = new NativeDialogPrompt();
        Bundle args = new Bundle();
        args.putStringArray(ARG_YES_ACTIONS, yesActions);
        args.putStringArray(ARG_NO_ACTIONS, noActions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mYesActions = getArguments().getStringArray(ARG_YES_ACTIONS);
            mNoActions = getArguments().getStringArray(ARG_NO_ACTIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rateLayout = container.findViewById(R.id.native_dialog);
        rateNoButton = container.findViewById(R.id.rateNoButton);
        rateYesButton = container.findViewById(R.id.rateYesButton);
        rateTextView = container.findViewById(R.id.rateText);



        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.native_dialog_prompt, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
