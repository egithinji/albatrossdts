package com.example.albatrossdts;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNewDocument1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNewDocument1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewDocument1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    //Firestore
    private FirebaseFirestore db;

    //List to hold locations, needed by the adapter for the spinner
    private List<String> locationList = new ArrayList<>();

    //Array adapter for locations spinner
    private ArrayAdapter<String> adapter;

    //Document Title edit text
    EditText txtDocumentTitle;

    //Document Description edit text
    EditText txtDocumentDescription;

    //Spinner widget
    Spinner spinner;

    //Next button
    Button btnNext;

    private OnFragmentInteractionListener mListener;

    public AddNewDocument1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewDocument1.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNewDocument1 newInstance(String param1, String param2) {
        AddNewDocument1 fragment = new AddNewDocument1();
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
            //mParam1 = getArguments().getString(ARG_PARAM1);
            //mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_new_document1, container, false);

        //Get Firestore instance
        db = FirebaseFirestore.getInstance();

        //Get the shared preferences. The two edit texts will be set with whatever is in the edit text
        //in case the user is navigating back to make a change. They should see what they had previously entered.
        SharedPreferences sharedPref = getActivity().getSharedPreferences("AddNewDocumentData",0);

        //Initialize document title editbox
        txtDocumentTitle = view.findViewById(R.id.txtEditDocumentTitle);
        txtDocumentTitle.setText(sharedPref.getString("title",""));
        txtDocumentTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    txtDocumentTitle.setBackgroundResource(R.drawable.edit_text_normal);
                }
            }
        });


        //Initialize document description editbox
        txtDocumentDescription = view.findViewById(R.id.txtEditDocumentDescription);
        txtDocumentDescription.setText(sharedPref.getString("description",""));
        txtDocumentDescription.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(!b){
                    txtDocumentDescription.setBackgroundResource(R.drawable.edit_text_normal);
                }
            }
        });

        //Initialize spinner
        spinner = view.findViewById(R.id.spinnerPermanentLocation);

        //Initialize next button
        btnNext = view.findViewById(R.id.btnNext1AddNew);
        btnNext.setEnabled(false); //It will be enabled after the spinner has been populated by getLocations().

        //Create the adapter
        adapter = new  ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, locationList);

        //Get the locations. At the same time update the adapter
        getLocations();

        //Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        //Specify layouts to use:
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        //Set onClick for next button
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });

        return view;
    }

    private void validate() {
        //Check if title and description have been entered. If not, highlight in red.

        if(txtDocumentTitle.getText().toString().equals("")){
            txtDocumentTitle.setBackgroundResource(R.drawable.edit_text_error);
        }
        else if(txtDocumentDescription.getText().toString().equals("")){
            txtDocumentDescription.setBackgroundResource(R.drawable.edit_text_error);
        }else{
            goNext();
        }

    }

    private void goNext() {
        //Add the entered values into shared preferences, then load the AddNewDocument2 fragment.

        SharedPreferences sharedPref = getContext().getSharedPreferences("AddNewDocumentData",0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("title",txtDocumentTitle.getText().toString());
        editor.putString("description",txtDocumentDescription.getText().toString());
        editor.putString("location",spinner.getSelectedItem().toString());
        editor.apply();

        launchNextFragment();
    }

    private void launchNextFragment() {
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((MainActivity)getActivity()).replaceFragment("AddNewDocument2",true);
    }

    private void getLocations() {
        //Get the location documents from firestore and store in a Location object
        //Update the adapter with the location names.

        db.collection("locations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Location location = document.toObject(Location.class);
                                //Update the adapter
                                adapter.add(location.getName());
                            }
                            btnNext.setEnabled(true);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
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
