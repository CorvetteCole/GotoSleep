package com.corvettecole.gotosleep;

import android.animation.LayoutTransition;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NativeDialogPrompt} interface
 * to handle interaction events.
 * Use the {@link NativeDialogPrompt#newInstance} factory method to
 * create an instance of this fragment.
 *
 */

//NativeDialogPrompt.OnFragmentInteractionListener

public class NativeDialogPrompt extends androidx.fragment.app.Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_YES_ACTIONS = "yes_actions";
    private static final String ARG_NO_ACTIONS = "no_actions";

    private static final String ARG_DIALOG_TEXTS = "dialog_texts";
    private static final String ARG_YES_TEXTS = "yes_texts";
    private static final String ARG_NO_TEXTS = "no_texts";

    private static final String TAG = "NativeDialogPrompt";

    private String[][] mYesActions;
    private String[][] mNoActions;
    private String[][] mDialogTexts;
    private String[][] mYesTexts;
    private String[][] mNoTexts;

    private OnFragmentInteractionListener mListener;

    private Button dialogYesButton;
    private Button dialogNoButton;
    private TextView dialogTextView;
    private ConstraintLayout dialogLayout;

    private int dialogPromptLevel = 0;
    private int dialogPromptBranch = 0;

    boolean mCustomButtonTexts = false;

    public NativeDialogPrompt() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param yesActions Link to open if user says yes. "dismiss" will close the prompt,
     *                   "nextLevel" will go to the next prompt level, "branch1" will go to the specified branch,
     *                   "purchase:$id" will issue a callback to the parent activity interface with the parameter as the id
     * @param noActions Link to open if users says no. "dismiss" will close the prompt,
     *                  "nextLevel" will go to the next prompt level,  "branch1" will go to the specified branch,
     *                  "purchase:$id" will issue a callback to the parent activity interface with the parameter as the id
     * @return A new instance of fragment NativeDialogPrompt.
     */
    public static NativeDialogPrompt newInstance(String[][] yesActions, String[][] noActions, String[][] dialogTexts) {
        NativeDialogPrompt fragment = new NativeDialogPrompt();
        Bundle args = new Bundle();
        args.putSerializable(ARG_YES_ACTIONS, yesActions);
        args.putSerializable(ARG_NO_ACTIONS, noActions);
        args.putSerializable(ARG_DIALOG_TEXTS, dialogTexts);
        fragment.setArguments(args);
        return fragment;
    }

    public static NativeDialogPrompt newInstance(String[][] yesActions, String[][] noActions, String[][] dialogTexts, String[][] yesTexts, String[][] noTexts) {
        NativeDialogPrompt fragment = new NativeDialogPrompt();
        Bundle args = new Bundle();
        args.putSerializable(ARG_YES_ACTIONS, yesActions);
        args.putSerializable(ARG_NO_ACTIONS, noActions);
        args.putSerializable(ARG_DIALOG_TEXTS, dialogTexts);
        args.putSerializable(ARG_NO_TEXTS, noTexts);
        args.putSerializable(ARG_YES_TEXTS, yesTexts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            mYesActions = (String[][]) getArguments().getSerializable(ARG_YES_ACTIONS);
            mNoActions = (String[][]) getArguments().getSerializable(ARG_NO_ACTIONS);
            mDialogTexts = (String[][]) getArguments().getSerializable(ARG_DIALOG_TEXTS);

            mYesTexts = (String[][]) getArguments().getSerializable(ARG_YES_TEXTS);
            mNoTexts = (String[][]) getArguments().getSerializable(ARG_NO_TEXTS);
            if (mYesTexts != null){
                mCustomButtonTexts = true;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.native_dialog_prompt, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState){
        dialogLayout = view.findViewById(R.id.native_dialog_layout);
        dialogNoButton = view.findViewById(R.id.rateNoButton);
        dialogYesButton = view.findViewById(R.id.rateYesButton);
        dialogTextView = view.findViewById(R.id.rateText);

        // Enable smooth animations on lower API devices
        ((ViewGroup) view.findViewById(R.id.native_dialog_layout)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        updateDialogText(dialogPromptBranch, dialogPromptLevel);

        dialogNoButton.setOnClickListener(v -> {


            if (mNoActions[dialogPromptBranch][dialogPromptLevel].contains("dismiss")) {
                transmitToActivity("dismissed");
            } else if (!(mNoActions[dialogPromptBranch][dialogPromptLevel].contains("nextLevel") || mNoActions[dialogPromptBranch][dialogPromptLevel].contains("branch"))){
                if (mNoActions[dialogPromptBranch][dialogPromptLevel].contains("purchase:")){
                    transmitToActivity(mNoActions[dialogPromptBranch][dialogPromptLevel].substring(mNoActions[dialogPromptBranch][dialogPromptLevel].indexOf("purchase:") + "purchase:".length()));
                } else {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNoActions[dialogPromptBranch][dialogPromptLevel]));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "ERROR PARSING URI: " + e.toString());
                        Toast.makeText(getContext(), "Parsing Error", Toast.LENGTH_LONG).show();
                    }
                }
                transmitToActivity("dismissed");
            }

            if (mNoActions[dialogPromptBranch][dialogPromptLevel].contains("nextLevel") || mNoActions[dialogPromptBranch][dialogPromptLevel].contains("branch")){

                if (mNoActions[dialogPromptBranch][dialogPromptLevel].contains("branch")){
                    //TODO explain this
                    dialogPromptBranch = Integer.parseInt(mNoActions[dialogPromptBranch][dialogPromptLevel]
                            .substring(mNoActions[dialogPromptBranch][dialogPromptLevel].indexOf("branch") + "branch".length(),
                                    mNoActions[dialogPromptBranch][dialogPromptLevel].indexOf("branch") + "branch".length() + 1));
                    dialogPromptLevel = 0;
                } else {
                    dialogPromptLevel++;
                }

                updateDialogText(dialogPromptBranch, dialogPromptLevel);
            }

        });

        dialogYesButton.setOnClickListener(v -> {

            if (mYesActions[dialogPromptBranch][dialogPromptLevel].contains("dismiss")) {
                transmitToActivity("dismissed");
            } else if (!(mYesActions[dialogPromptBranch][dialogPromptLevel].contains("nextLevel") || mYesActions[dialogPromptBranch][dialogPromptLevel].contains("branch"))){
                if (mYesActions[dialogPromptBranch][dialogPromptLevel].contains("purchase:")){
                    transmitToActivity(mYesActions[dialogPromptBranch][dialogPromptLevel].substring(mYesActions[dialogPromptBranch][dialogPromptLevel].indexOf("purchase:") + "purchase:".length()));
                } else {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mYesActions[dialogPromptBranch][dialogPromptLevel]));
                        startActivity(browserIntent);
                    } catch (Exception e) {
                        Log.e(TAG, "ERROR PARSING URI: " + e.toString());
                        Toast.makeText(getContext(), "Parsing Error", Toast.LENGTH_LONG).show();
                    }
                }
                transmitToActivity("dismissed");
            }

            if (mYesActions[dialogPromptBranch][dialogPromptLevel].contains("nextLevel") || mYesActions[dialogPromptBranch][dialogPromptLevel].contains("branch")){

                if (mYesActions[dialogPromptBranch][dialogPromptLevel].contains("branch")){
                    //TODO explain this
                    dialogPromptBranch = Integer.parseInt(mYesActions[dialogPromptBranch][dialogPromptLevel]
                            .substring(mYesActions[dialogPromptBranch][dialogPromptLevel].indexOf("branch") + "branch".length(),
                                    mYesActions[dialogPromptBranch][dialogPromptLevel].indexOf("branch") + "branch".length() + 1));
                    dialogPromptLevel = 0;
                } else {
                    dialogPromptLevel++;
                }
                updateDialogText(dialogPromptBranch, dialogPromptLevel);
            }
        });
    }

    private void updateDialogText(int dialogPromptBranch, int dialogPromptLevel){
        dialogTextView.setText(mDialogTexts[dialogPromptBranch][dialogPromptLevel]);
        if (mCustomButtonTexts){
            dialogYesButton.setText(mYesTexts[dialogPromptBranch][dialogPromptLevel]);
            dialogNoButton.setText(mNoTexts[dialogPromptBranch][dialogPromptLevel]);
        }
    }


    public void transmitToActivity(String string) {
        Log.d(TAG, string);
        if (mListener != null) {
            mListener.onFragmentInteraction(string);
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
        void onFragmentInteraction(String string);
    }
}
