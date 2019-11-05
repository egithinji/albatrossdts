package africa.albatross.albatrossdts;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import africa.albatross.albatrossdts.R;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CheckOut2.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CheckOut2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckOut2 extends Fragment {
    private static final String TAG = "CheckOut2";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    //Views
    Spinner spinnerReason;
    Button btnCheckOut;
    ProgressBar progressBar;

    //Shared preferences
    SharedPreferences sharedPreferences;
    SharedPreferences employeeSharedPreferences;

    //Firestore
    FirebaseFirestore db;

    //Firebase auth
    FirebaseAuth mAuth;
    FirebaseUser user;

    //List to hold locations, needed by the adapter for the spinner
    private List<String> reasonsList = new ArrayList<>();

    //Array adapter for reasons spinner
    private ArrayAdapter<String> adapter;


    private OnFragmentInteractionListener mListener;

    public CheckOut2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckOut2.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckOut2 newInstance(String param1, String param2) {
        CheckOut2 fragment = new CheckOut2();
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

        Log.i(TAG,"In CheckOut2");

        sharedPreferences = getContext().getSharedPreferences("CheckOutDocumentData",0);
        employeeSharedPreferences = getContext().getSharedPreferences("EmployeeData",0);

        //Initialize firestore
        db = FirebaseFirestore.getInstance();

        //Initialize firebase user
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_out2, container, false);

        //Instantiate the views
        spinnerReason = view.findViewById(R.id.spinnerReason);
        btnCheckOut = view.findViewById(R.id.btnCheckOut);
        progressBar = view.findViewById(R.id.progressBarCheckOut);

        btnCheckOut.setEnabled(false);//It will be enabled after the spinner has been populated by getReasons().

        //Create the adapter
        adapter = new  ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, reasonsList);

        //Get the reasons. At the same time update the adapter
        getReasons();

        //Apply the adapter to the spinner
        spinnerReason.setAdapter(adapter);

        //Specify layouts to use:
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


        //OnClickListener for btnCheckOut
        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    progressBar.setVisibility(View.VISIBLE);
                    checkOutDocument();
                    //demoCheckOut();
            }
        });


        return view;
    }

    private void demoCheckOut() {
        //Dummy checkout for demonstration purposes
        getEmail();


    }

    private void getReasons() {
        //Get the reason documents from firestore and store in a Reason object
        //Update the adapter with the reasons.

        db.collection("reasons_for_checkout")
                .orderBy("reason")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "CheckOut2" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reason reason = document.toObject(Reason.class);
                                //Update the adapter
                                adapter.add(reason.getReason());
                            }
                            btnCheckOut.setEnabled(true);
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void checkOutDocument() {
        //Get the document
        db.collection("documents")
                .whereEqualTo("barcode_number",sharedPreferences.getString("document_barcode_number",""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            //Update this document
                            for (QueryDocumentSnapshot qds: task.getResult()){

                                //Store the document in a document object
                                Document document = qds.toObject(Document.class);

                                //Compare the 'currently_checked_out_to' value with the user's uid. If the same, inform user
                                //that document currently checked out to them and don't do anything else.
                                //TODO: The below technique uses boilerplate and should be replaced with something more efficient.

                                if(document.getCurrently_checked_out_to()==null){//If I don't do this I get a null pointer exception when comparing the document's checked_out_to value.
                                    final DocumentReference dr = qds.getReference();
                                    //Update the 'checked out to'value of this document
                                    dr
                                            .update("currently_checked_out_to",employeeSharedPreferences.getString("uid","")) //Had to get uid from the user object instead of employeeSharedPreferences to avoid issues
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    //Update the purpose
                                                    dr.update("purpose",spinnerReason.getSelectedItem().toString()) //Add the purpose for checkout
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    submitTransaction(spinnerReason.getSelectedItem().toString(), dr);
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(),"Error updating item.",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                                //Commenting out following for demo version. Since all demo users are the same user, they would get informed the item is already checked out to them
                                //else if(document.getCurrently_checked_out_to().equals(user.getUid())){//Had to get uid from the user object instead of employeeSharedPreferences to avoid issues
                                //    Toast.makeText(getContext(),"This item is already checked out to you.",Toast.LENGTH_LONG).show();
                                //    progressBar.setVisibility(View.INVISIBLE);
                                //    ((MainActivity)getActivity()).replaceFragment("CheckOut1",false);
                                //}
                                else{
                                     final DocumentReference dr = qds.getReference();
                                    //Update the 'checked out to'value of this document
                                    dr
                                            .update("currently_checked_out_to",employeeSharedPreferences.getString("uid",""))//Had to get uid from the user object instead of employeeSharedPreferences to avoid issues
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    //Update the purpose
                                                    dr.update("purpose",spinnerReason.getSelectedItem().toString()) //Add the purpose for checkout
                                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    submitTransaction(spinnerReason.getSelectedItem().toString(), dr);
                                                                }
                                                            });
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getContext(),"Error updating item.",Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            }

                        }else {
                            //TODO: Error handling
                        }
                    }

                });
    }

    private void submitTransaction(String purpose, DocumentReference dr){
        //Get all the variables needed to create a Transaction object
        String barcode_number = sharedPreferences.getString("document_barcode_number","");
        String document_title = sharedPreferences.getString("document_title","");
        String uid = employeeSharedPreferences.getString("uid","");
        String employee_name = employeeSharedPreferences.getString("first_name","")+" "+employeeSharedPreferences.getString("last_name","");
        String transaction_type = MainActivity.TRANSACTION_TYPE_CHECKOUT;
        //Purpose is obtained from parameter passed into this method
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
                        documentRef.update("last_transaction_id",documentReference.getId()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                getEmail(); //getEmail kicks off the process that results in an email being sent using the approved email credentials from the database.
                                //launchNextFragment();
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getActivity(),"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                        launchNextFragment();
                    }
                });
    }


    public void sendEmail(){
        try {
            GMailSender sender = new GMailSender(MainActivity.EMAIL_ADDRESS, MainActivity.PASSWORD);

            String dateAndTime;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
            dateAndTime = dateFormat.format(calendar.getTime());

            //Build the body
            String body = "Item with barcode number '"+sharedPreferences.getString("document_barcode_number","")+"' and name '"+sharedPreferences.getString("document_title","")+"' was checked out by "+employeeSharedPreferences.getString("first_name","")+" "+employeeSharedPreferences.getString("last_name","")+" on "+dateAndTime+".\nPurpose: '"+spinnerReason.getSelectedItem().toString()+"'";

            sender.sendMail("Item Check-out",//TODO:Need to replace some of these with either string resources or something else not hardcoded.
                    body,
                    MainActivity.EMAIL_ADDRESS,
                    MainActivity.EMAIL_FOR_NOTIFICATIONS,
                    null,
                    null);
            Log.i("SendMail","Email sent successfully");


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressBar.setVisibility(View.INVISIBLE);
                    //Display Toast
                    Log.i(TAG,"Displaying toast.");
                    Toast.makeText(getActivity(),"Fantastic! Item checked out.",Toast.LENGTH_LONG).show();
                }
            });

            launchNextFragment();


        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
            //TODO: Need to find a way to notify user if email notification not sent
        }

    }


    private class EmailTask extends AsyncTask<String,Void,Void> {
        //Good explanation of AsyncTask here: https://stackoverflow.com/questions/9671546/asynctask-android-example
        @Override
        protected Void doInBackground(String... strings) {
            sendEmail();
            return null;
        }
    }

    private void getEmail(){
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

    private void getPassword(){
        //Set the value of PASSWORD to the password in the database then fire the email.
        DocumentReference docRef = db.collection("credentials").document("password");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.PASSWORD = document.getString("value");
                        new EmailTask().execute();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void clearSharedPreferences(){
        //Clear the shared preferences
        //SharedPreferences.Editor editor = sharedPreferences.edit();
        //editor.clear();
        //editor.commit();

        SharedPreferences scannerSharedPreferences = getActivity().getSharedPreferences("DocumentData",0);
        SharedPreferences.Editor editor = scannerSharedPreferences.edit();
        editor.clear();
        editor.commit();

    }

    private void launchNextFragment() {
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        clearSharedPreferences();

        progressBar.setVisibility(View.INVISIBLE);

        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((MainActivity)getActivity()).replaceFragment("HomeSignedInFragment",false);

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
