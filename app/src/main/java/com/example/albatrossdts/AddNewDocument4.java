package com.example.albatrossdts;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNewDocument4.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNewDocument4#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewDocument4 extends Fragment {
    private static final String TAG = "AddNewDocument4";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView txtSummaryDocumentTitle;
    private TextView txtSummaryDocumentDescription;
    private TextView txtSummaryLocation;
    private ImageView imgSummaryDocumentPhoto;
    private TextView txtSummaryBarcodeNumber;
    private Button btnBackSummary;
    private Button btnSubmit;
    private ProgressBar progressBar;

    //Firestore
    private FirebaseFirestore db;

    //Firebase auth
    private FirebaseAuth mAuth;

    //Shared preferences
    SharedPreferences sharedPref;





    public AddNewDocument4() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewDocument4.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNewDocument4 newInstance(String param1, String param2) {
        AddNewDocument4 fragment = new AddNewDocument4();
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

        //Iniitialize the firestore object
        db = FirebaseFirestore.getInstance();

        //Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new_document4, container, false);

        txtSummaryDocumentTitle = view.findViewById(R.id.txtSummaryDocumentTitle);
        txtSummaryDocumentDescription = view.findViewById(R.id.txtSummaryDocumentDescription);
        txtSummaryLocation = view.findViewById(R.id.txtSummaryDocumentLocation);
        imgSummaryDocumentPhoto = view.findViewById(R.id.imgSummaryDocumentPhoto);
        txtSummaryBarcodeNumber = view.findViewById(R.id.txtSummaryBarcodeNumber);
        btnBackSummary = view.findViewById(R.id.btnBack4AddNew);
        btnSubmit = view.findViewById(R.id.btnSubmitAddNew);
        progressBar = view.findViewById(R.id.progressBarSummary);
        progressBar.setVisibility(View.INVISIBLE);

        //Set the values as per the shared preferences
        sharedPref = getActivity().getSharedPreferences("AddNewDocumentData",0);

        txtSummaryDocumentTitle.setText("Document title: "+sharedPref.getString("title",""));
        txtSummaryDocumentDescription.setText("Document description: "+sharedPref.getString("description",""));
        txtSummaryLocation.setText("Permanent storage location: "+sharedPref.getString("location",""));
        Glide.with(getContext())
                .asBitmap()
                .load(sharedPref.getString("photo_url",""))
                .into(imgSummaryDocumentPhoto);
        txtSummaryBarcodeNumber.setText("Barcode number: "+sharedPref.getString("barcode_number",""));

        //Set OnClick for back button
        btnBackSummary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        //Set OnClick fr submit button
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make the progress bar visible
                progressBar.setVisibility(View.VISIBLE);

                //First make sure a document with that barcode does not exist in the database
                //Once this is confirmed, the submit() method will be called.
                //Otherwise user will be informed by toast that a document with that barcode already exists
                confirmBarcodeUnique();

            }
        });


        return view;
    }

    private void confirmBarcodeUnique() {
        //The size of task.getResult will be 0 if the barcode is unique.

        Query query = db.collection("documents").whereEqualTo("barcode_number",sharedPref.getString("barcode_number",""));
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()){

                    if(task.getResult().size()>0){
                        //The barcode number already exists in the database. Inform the user.

                        //Make progress bar invisible
                        progressBar.setVisibility(View.INVISIBLE);

                        Toast.makeText(getContext(),"An item with that barcode number already exists in the database.",Toast.LENGTH_LONG).show();
                    }else{
                        //The barcode number is unique. Submit
                        submit();
                        //demoAdd();
                    }

                }else{
                    //TODO:Need error handling here
                }
            }
        });
    }


    private void submit() {

        Document document = getDocumentFromSharedPreferences();

        db.collection("documents")
                .add(document)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                          submitTransaction(documentReference);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        //Make progress bar invisible
                        progressBar.setVisibility(View.INVISIBLE);

                        //Display Toast
                        Toast.makeText(getContext(),"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();

                    }
                });

    }

    private void submitTransaction(DocumentReference dr) {
        //Add an entry to the Transactions collection in the database
        //Get all the variables needed to create a Transaction object

        //Get user data from shared preferences
        SharedPreferences employeeSharedPref = getActivity().getSharedPreferences("EmployeeData",0);

        String barcode_number = sharedPref.getString("barcode_number","");
        String document_title = sharedPref.getString("title","");
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = employeeSharedPref.getString("uid","");
        String employee_name = employeeSharedPref.getString("first_name","")+" "+employeeSharedPref.getString("last_name","");
        String transaction_type = MainActivity.TRANSACTION_TYPE_ADD;
        String purpose = null; //Adding a new document does not require a purpose
        Timestamp timestamp = Timestamp.now();
        final DocumentReference documentRef = dr;

        //Create a Transaction object
        Transaction transaction = new Transaction(barcode_number,document_title,uid,employee_name,transaction_type,purpose,timestamp);

        //Upload the transaction
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {

                        //Update the 'last_transaction_id' value of this document
                        documentRef.update("last_transaction_id",documentReference.getId())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        getEmail(); //getEmail kicks off the process that results in an email being sent using the approved email credentials from the database.
                                    }
                                });


                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Make progress bar invisible
                        progressBar.setVisibility(View.INVISIBLE);

                        //Display Toast
                        Toast.makeText(getContext(),"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void getEmail() {
        //Set the value of EMAIL_ADDRESS to the address in the database then call getPassword()
        DocumentReference docRef = db.collection("credentials").document("email_address");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.EMAIL_ADDRESS = document.getString("value");
                        getPassword();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getPassword() {
        //Set the value of PASSWORD to the password in the database then fire the email.
        DocumentReference docRef = db.collection("credentials").document("password");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.PASSWORD = document.getString("value");
                        new AddNewDocument4.EmailTask().execute();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private class EmailTask extends AsyncTask<String,Void,Void> {
        //Good explanation of AsyncTask here: https://stackoverflow.com/questions/9671546/asynctask-android-example
        @Override
        protected Void doInBackground(String... strings) {
            sendEmail();
            return null;
        }
    }

    public void sendEmail(){
        try {
            GMailSender sender = new GMailSender(MainActivity.EMAIL_ADDRESS, MainActivity.PASSWORD);

            String dateAndTime;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
            dateAndTime = dateFormat.format(calendar.getTime());

            //Get user data from shared preferences
            SharedPreferences employeeSharedPref = getActivity().getSharedPreferences("EmployeeData",0);

            //Build the body
            String body = "Item with barcode number '"+sharedPref.getString("barcode_number","")+"' and name '"+sharedPref.getString("title","")+"' was added to the database by "+employeeSharedPref.getString("first_name","")+" "+employeeSharedPref.getString("last_name","")+" on "+dateAndTime+".\nStorage Location: "+sharedPref.getString("location","");

            sender.sendMail("New Item Added",//TODO:Need to replace some of these with either string resources or something else not hardcoded.
                    body,
                    MainActivity.EMAIL_ADDRESS,
                    MainActivity.EMAIL_FOR_NOTIFICATIONS,
                    null,
                    null);
            Log.i("SendMail","Email sent successfully");
            clearSharedPreferences();



            //Go back to first fragment of Add New Document to allow addition of new document.
            goToBeginning();

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
            //TODO: Need to find a way to notify user if email notification not sent
        }

    }

    private void clearSharedPreferences() {
        //Clear the shared preferences
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();

        SharedPreferences scannerSharedPref = getActivity().getSharedPreferences("DocumentData",0);
        editor = scannerSharedPref.edit();
        editor.clear();
        editor.commit();
    }

    private void goToBeginning() {//For the demo, I want the user to go to the home page, not the first fragment for adding an item.

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Display Toast
                Toast.makeText(getContext(),"Well done! Item submitted successfully.",Toast.LENGTH_LONG).show();
            }
        });

        //Make progress bar invisible
        progressBar.setVisibility(View.INVISIBLE);

        ((MainActivity)getActivity()).replaceFragment("HomeSignedInFragment",false); //We don't want the user to click back and come back here.
    }

    private Document getDocumentFromSharedPreferences() {
        //create a document object using contents of shared preferences
        //get the user's uid from the firebase auth
        //return this document

        //Get user data from shared preferences
        SharedPreferences employeeSharedPref = getActivity().getSharedPreferences("EmployeeData",0);


        String barcode_number = sharedPref.getString("barcode_number","");
        String title = sharedPref.getString("title","");
        String description = sharedPref.getString("description","");
        String permanent_location = sharedPref.getString("location","");
        String currently_checked_out_to = null;
        String photo_url = sharedPref.getString("photo_url","");
        String added_by = employeeSharedPref.getString("uid","");

        Document d = new Document(barcode_number,title,description,permanent_location,currently_checked_out_to,null,null,photo_url,added_by);

        return d;

    }

    private void goBack() {
        //launch the previous fragment, AddNewDocument3
        //AddNewDocument3 fragment = AddNewDocument3.newInstance(null,null);
        ((MainActivity)getActivity()).replaceFragment("AddNewDocument3",true);
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
