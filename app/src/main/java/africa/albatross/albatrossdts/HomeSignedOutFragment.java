package com.example.albatrossdts;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.ActionCodeSettings;

import java.util.Arrays;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeSignedOutFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeSignedOutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeSignedOutFragment extends Fragment {
    private static final int RC_SIGN_IN = 1;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    // TODO: Rename and change types of parameters
    //TODO: Commenting out for now but remove eventually if no arguments needed.
    //private String mParam1;
    //private String mParam2;

    Button btnSignIn;

    private OnFragmentInteractionListener mListener;

    public HomeSignedOutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeSignedOutFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeSignedOutFragment newInstance(String param1, String param2) {
        HomeSignedOutFragment fragment = new HomeSignedOutFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //TODO: Commenting out for now but remove eventually if no arguments needed.
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_signed_out, container, false);

        btnSignIn = view.findViewById(R.id.btn_sign_in);

        //Set up onclicklistener for sign in button
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInUser();
            }
        });

        return view;
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

    private void signInUser(){
        // See https://firebase.google.com/docs/auth/android/firebaseui
        // I am following the option of catching the link in a different actvity, specifically the Authentication.java activity.
        // The appropriate intent filter has been put in the androidmanifest.
        //See also: https://firebase.google.com/docs/auth/android/email-link-auth
        // and https://firebase.google.com/docs/dynamic-links/android/receive.

        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(/* yourPackageName= */ "com.example.albatrossdts",/* installIfNotAvailable= */ true, /* minimumVersion= */ null)
                .setHandleCodeInApp(true) // This must be set to true
                .setUrl("https://albatrossdts.page.link") // This URL needs to be whitelisted
                .setDynamicLinkDomain("albatrossdts.page.link")
                .build();

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                new AuthUI.IdpConfig.EmailBuilder().enableEmailLinkSignIn()
                                        .setActionCodeSettings(actionCodeSettings).build()))
                        .build(),
                RC_SIGN_IN);

        getActivity().finish(); //Needed so the user can't come back track here after successful sign-in. See https://stackoverflow.com/questions/8631095/how-to-prevent-going-back-to-the-previous-activity

    }
}
