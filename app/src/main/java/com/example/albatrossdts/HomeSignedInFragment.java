package com.example.albatrossdts;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


//See https://www.youtube.com/watch?v=bjYstsO1PgI and https://www.youtube.com/watch?v=BMTNaPcPjdw on fragments

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeSignedInFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeSignedInFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeSignedInFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";

    // TODO: Rename and change types of parameters
    private String mUsername;

    private OnFragmentInteractionListener mListener;

    private TextView txtWelcome;
    private TextView txtDate;
    private TextView btnHelp;

    public HomeSignedInFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment HomeSignedInFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeSignedInFragment newInstance(String param1) {
        HomeSignedInFragment fragment = new HomeSignedInFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsername = getArguments().getString(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_signed_in, container, false);

        //When signed in, the welcome message should display the user's name and today's date
        txtWelcome = view.findViewById(R.id.txtWelcomeMessage);
        txtDate = view.findViewById(R.id.txtDate);
        btnHelp = view.findViewById(R.id.btnHelp);

        //Username should be captured in mUsername
        txtWelcome.setText("Welcome, "+mUsername+".");
        //Get the date using the getDate() method
        txtDate.setText(getDate());

        //Clicking btnHelp should open help documentation in Google Docs.
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://docs.google.com/document/d/1xFmmhKwzJUZKObq4KCuRgQt0m6w_SeEcY2-S_zBvT_w/edit?usp=sharing"));
                startActivity(intent);
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

    private String getDate(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("EEEE, MMMM d, y");
        String formattedDate = df.format(c);
        return formattedDate;
    }
}
