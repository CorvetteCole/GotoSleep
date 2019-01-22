package com.corvettecole.gotosleep;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
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
    private static final String ARG_DIALOG_TEXTS = "dialog_texts";

    private static final String TAG = "NativeDialogPrompt";

    private String[] mYesActions;
    private String[] mNoActions;
    private String[] mDialogTexts;

    private OnFragmentInteractionListener mListener;

    private Button dialogYesButton;
    private Button dialogNoButton;
    private TextView dialogTextView;
    private ConstraintLayout dialogLayout;

    private int dialogPromptLevel = 0;

    public NativeDialogPrompt() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param yesActions Link to open if user says yes. "dismiss" will close the prompt, "continue" will go to the next prompt level
     * @param noActions Link to open if users says no. "dismiss" will close the prompt, "continue" will go to the next prompt level
     * @return A new instance of fragment NativeDialogPrompt.
     */
    public static NativeDialogPrompt newInstance(String[] yesActions, String[] noActions, String[] dialogTexts) {
        NativeDialogPrompt fragment = new NativeDialogPrompt();
        Bundle args = new Bundle();
        args.putStringArray(ARG_YES_ACTIONS, yesActions);
        args.putStringArray(ARG_NO_ACTIONS, noActions);
        args.putStringArray(ARG_DIALOG_TEXTS, dialogTexts);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            mYesActions = getArguments().getStringArray(ARG_YES_ACTIONS);
            mNoActions = getArguments().getStringArray(ARG_NO_ACTIONS);
            mDialogTexts = getArguments().getStringArray(ARG_DIALOG_TEXTS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dialogLayout = container.findViewById(R.id.native_dialog_layout);
        dialogNoButton = container.findViewById(R.id.rateNoButton);
        dialogYesButton = container.findViewById(R.id.rateYesButton);
        dialogTextView = container.findViewById(R.id.rateText);

        // Enable smooth animations on lower API devices
        ((ViewGroup) container.findViewById(R.id.native_dialog_layout)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        dialogTextView.setText(mDialogTexts[dialogPromptLevel]);

        dialogNoButton.setOnClickListener(v -> {
            if (mNoActions[dialogPromptLevel].equalsIgnoreCase("dismiss")){
                //TODO additional dismiss action (heads up to MainActivity)
                dialogLayout.setVisibility(View.GONE);

            } else if (mNoActions[dialogPromptLevel].equalsIgnoreCase("continue")){
                dialogPromptLevel++;
                dialogTextView.setText(mDialogTexts[dialogPromptLevel]);
            } else {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mNoActions[dialogPromptLevel]));
                    startActivity(browserIntent);
                } catch (Exception e){
                    Log.e(TAG, "ERROR PARSING URI: " + e.toString());
                }
                //TODO dismiss action
            }

        });

        dialogYesButton.setOnClickListener(v -> {
            if (mYesActions[dialogPromptLevel].equalsIgnoreCase("dismiss")){
                //TODO additional dismiss action (heads up to MainActivity)
                dialogLayout.setVisibility(View.GONE);

            } else if (mYesActions[dialogPromptLevel].equalsIgnoreCase("continue")){
                dialogPromptLevel++;
                dialogTextView.setText(mDialogTexts[dialogPromptLevel]);
            } else {
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mYesActions[dialogPromptLevel]));
                    startActivity(browserIntent);
                } catch (Exception e){
                    Log.e(TAG, "ERROR PARSING URI: " + e.toString());
                }
                //TODO dismiss action
            }

        });


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.native_dialog_prompt, container, false);
    }

    /*
        Pre-fragment code from MainActivity, for reference

        private void initiateRatingDialogue(){
        dialogLayout.setVisibility(View.VISIBLE);
        Log.d(TAG, "set dialogLayout to visible");
        //initial state, TextView displays "Are you enjoying Go to Sleep?"

        ((ViewGroup) findViewById(R.id.native_dialog_frame)).getLayoutTransition()
                .enableTransitionType(LayoutTransition.CHANGING);

        dialogNoButton.setOnClickListener(v -> {
            if (!isRequestingFeedback && !isRequestingRating) {
                isRequestingFeedback = true;
                dialogTextView.setText(getString(R.string.request_feedback));
                dialogNoButton.setText(getString(R.string.no_thanks));
                dialogYesButton.setText(getString(R.string.ok_sure));
            } else {
                dialogLayout.setVisibility(View.GONE);
                Log.d(TAG, "ads will re-enable after onResume called");
                adsInitialized = false;
            }
            getPrefs.edit().putBoolean(RATING_PROMPT_SHOWN_KEY, true).apply();
        });

        dialogYesButton.setOnClickListener(v -> {
            if (!isRequestingRating && !isRequestingFeedback){
                isRequestingRating = true;
                dialogTextView.setText(getString(R.string.rating_request));
                dialogYesButton.setText(getString(R.string.ok_sure));
                dialogNoButton.setText(getString(R.string.no_thanks));
            } else if (isRequestingFeedback){
                sendFeedback();

            } else {
                sendToPlayStore();
            }
            getPrefs.edit().putBoolean(RATING_PROMPT_SHOWN_KEY, true).apply();
        });

    }
     */

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
