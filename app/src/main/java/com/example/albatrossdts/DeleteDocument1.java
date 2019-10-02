package com.example.albatrossdts;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeleteDocument1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeleteDocument1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeleteDocument1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    //The views
    EditText txtEditBarcodeNumber;
    ImageView imgSearch;
    ProgressBar progressBar;


    //Shared preferences
    SharedPreferences sharedPref;

    //Firestore
    private FirebaseFirestore db;


    private OnFragmentInteractionListener mListener;

    public DeleteDocument1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeleteDocument1.
     */
    // TODO: Rename and change types and number of parameters
    public static DeleteDocument1 newInstance(String param1, String param2) {
        DeleteDocument1 fragment = new DeleteDocument1();
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

        //Initiate firestore instance
        db = FirebaseFirestore.getInstance();

        //Get the shared preferences
        sharedPref = getContext().getSharedPreferences("DeleteDocumentData",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_document1, container, false);

        //Instantiate the views
        txtEditBarcodeNumber = view.findViewById(R.id.txtEditBarcodeNumber);
        imgSearch = view.findViewById(R.id.imgSearch);
        progressBar = view.findViewById(R.id.progressBar);

        //Search button onClick listener
        imgSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if nothing has been entered for barcode number, prompt the user.
                if(txtEditBarcodeNumber.getText().toString().equals("")){
                    txtEditBarcodeNumber.setBackgroundResource(R.drawable.edit_text_error);
                }else {
                    clearSharedPreferences();//Clear any previously downloaded shared preferences
                    progressBar.setVisibility(View.VISIBLE);
                    getDocumentData((txtEditBarcodeNumber.getText().toString()).toUpperCase()); //Needs to be capitalized because of format of text on physical barcodes.
                }
            }
        });

        //Barcode edittext onclick listener to return the normal background after user
        //gets error and then enters text.
        txtEditBarcodeNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtEditBarcodeNumber.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        return view;
    }

    private void clearSharedPreferences() {
        //Clear the shared preferences holding previously downloaded document data
        //Also disable the next button
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
    }

    private void getDocumentData(String barcode_number) {
        //Query the database for the barcode that has been input.
        //If found, go to DeleteDocument2 fragment.
        //If not found, inform the user.

        db.collection("documents")
                .whereEqualTo("barcode_number",barcode_number)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            if (task.getResult().size()>0){
                                //The document was found
                                //Save the data in a Document object
                                Document document;
                                for(QueryDocumentSnapshot qds: task.getResult()){
                                    document = qds.toObject(Document.class);
                                    //Add the document's details to shared preferences
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString("document_barcode_number",document.getBarcode_number());
                                    editor.putString("document_title",document.getTitle());
                                    editor.putString("document_description",document.getDescription());
                                    editor.putString("document_photo_url",document.getPhoto_url());
                                    editor.commit();

                                    //Make the keyboard disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    mgr.hideSoftInputFromWindow(txtEditBarcodeNumber.getWindowToken(), 0);

                                    launchNextFragment();
                                }


                            }else{
                                //Document not found
                                Toast.makeText(getContext(),"Document not found",Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }

                        }else{
                            //TODO: Add error handling.
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
    }



    private void launchNextFragment() {
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((MainActivity)getActivity()).replaceFragment("DeleteDocument2",true);
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
