package africa.albatross.albatrossdts;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
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
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DeleteDocument2.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DeleteDocument2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeleteDocument2 extends Fragment {
    private static final String TAG = "DeleteDocument2";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Views
    TextView txtBarcodeNumberContent;
    EditText txtEditReason;
    Button btnDelete;
    Button btnBack;
    ImageView imgDocumentPhoto;
    TextView txtDocumentTitleContent;
    TextView txtDocumentDescriptionContent;
    ProgressBar progressBar;
    RelativeLayout layoutSearchResults;

    //Shared preferences
    SharedPreferences sharedPref;

    //Dialog
    private DialogFragment deleteDialog;



    public DeleteDocument2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeleteDocument2.
     */
    // TODO: Rename and change types and number of parameters
    public static DeleteDocument2 newInstance(String param1, String param2) {
        DeleteDocument2 fragment = new DeleteDocument2();
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
            // mParam2 = getArguments().getString(ARG_PARAM2);
        }

        sharedPref = getContext().getSharedPreferences("DeleteDocumentData",0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete_document2, container, false);

        //Instantiate the views
        txtBarcodeNumberContent = view.findViewById(R.id.txtBarcodeNumberContent);
        txtEditReason = view.findViewById(R.id.txtEditReason);
        btnBack = view.findViewById(R.id.btnBackDeleteDocument2);
        btnDelete = view.findViewById(R.id.btnDelete);
        imgDocumentPhoto = view.findViewById(R.id.imgDocumentPhoto);
        txtBarcodeNumberContent = view.findViewById(R.id.txtBarcodeNumberContent);
        txtDocumentTitleContent = view.findViewById(R.id.txtDocumentTitleContent);
        txtDocumentDescriptionContent = view.findViewById(R.id.txtDocumentDescriptionContent);
        progressBar = view.findViewById(R.id.progressBar);
        layoutSearchResults = view.findViewById(R.id.layoutSearchResults);

        //OnClickListener for btnDelete
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(txtEditReason.getText().toString().equals("")) {
                    txtEditReason.setBackgroundResource(R.drawable.edit_text_error);
                }else {
                    displayConfirmationDialog();
                }
            }
        });

        //txtEditReason onclick listener to return the normal background after user
        //gets error and then enters text.
        txtEditReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                txtEditReason.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        //OnClickListener for btnBack
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchPreviousFragment();
            }
        });

        //Display the search results from the previous fragment
        displaySearchResults();

        //Add onclick listener to imageview to allow user to
        //open the image url in a browser.
        imgDocumentPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(sharedPref.getString("document_photo_url","")));
                startActivity(intent);
            }
        });

        return view;
    }

    private void displayConfirmationDialog() {

        FragmentManager fm = getActivity().getSupportFragmentManager();
        deleteDialog = new DeleteDialog();
        ((DeleteDialog) deleteDialog).document_barcode_number = sharedPref.getString("document_barcode_number",""); //Setting the correct supplier id.
        ((DeleteDialog) deleteDialog).purpose = txtEditReason.getText().toString();
        deleteDialog.show(fm,"dialog");

    }


    //Dialog that appears when user clicks the Delete button
    //See https://developer.android.com/guide/topics/ui/dialogs
    public static class DeleteDialog extends DialogFragment {
        private String document_barcode_number;
        private String purpose;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to delete document "+document_barcode_number+"?") //TODO:This and the other strings should be replaced with a string resource
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //delete from database
                            deleteDocument(getContext(),purpose);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    private static void deleteDocument(final Context context, final String purpose) {

        //Firestore
        FirebaseFirestore db;

        //Initialize firestore
        db = FirebaseFirestore.getInstance();

        //Shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences("DeleteDocumentData",0);

        //Query to return the document with the correct barcode number
        db.collection("documents")
                .whereEqualTo("barcode_number",sharedPref.getString("document_barcode_number",""))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            //Delete this document
                            for (QueryDocumentSnapshot qds: task.getResult()){
                                DocumentReference dr = qds.getReference();
                                dr.delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                submitTransaction(context,purpose);
                                            }
                                        });
                            }

                        }else {
                            //TODO: Error handling
                        }
                    }
                });
    }

    private static void submitTransaction(final Context context, String purpose) {
        //Add an entry to the Transactions collection in the database

        //Firestore
        FirebaseFirestore db;


        db = FirebaseFirestore.getInstance();

        //Firebase user
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        //Shared preferences for document
        SharedPreferences sharedPref = context.getSharedPreferences("DeleteDocumentData",0);
        //Add purpose to sharedpref to be used later in sendMail()
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("purpose",purpose);
        editor.commit();

        //Shared preferences for user
        SharedPreferences employeeSharedPref = context.getSharedPreferences("EmployeeData",0);

        //Get all the variables needed to create a Transaction object
        String barcode_number = sharedPref.getString("document_barcode_number","");
        String document_title = sharedPref.getString("document_title","");
        String uid = employeeSharedPref.getString("uid","");
        String employee_name = employeeSharedPref.getString("first_name","")+" "+employeeSharedPref.getString("last_name","");
        String transaction_type = MainActivity.TRANSACTION_TYPE_DELETE;
        //Purpose is obtained from parameter passed into this method
        Timestamp timestamp = Timestamp.now();

        //Create a Transaction object
        Transaction transaction = new Transaction(barcode_number,document_title,uid,employee_name,transaction_type,purpose,timestamp);

        //Upload the transaction
        db.collection("transactions")
                .add(transaction)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(context,"Item deleted.",Toast.LENGTH_LONG).show();
                        getEmail(context); //getEmail kicks off the process that results in an email being sent using the approved email credentials from the database.
                        launchNextFragment(context);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Display Toast
                        Toast.makeText(context,"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();

                        launchNextFragment(context);
                    }
                });

    }

    private static void getEmail(final Context context) {
        //Firestore
        FirebaseFirestore db;

        //Get Firestore instance
        db = FirebaseFirestore.getInstance();

        //Set the value of EMAIL_ADDRESS to the address in the database then call getPassword()
        DocumentReference docRef = db.collection("credentials").document("email_address");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.EMAIL_ADDRESS = document.getString("value");
                        getPassword(context);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private static void getPassword(final Context context) {
        //Firestore
        FirebaseFirestore db;

        //Get Firestore instance
        db = FirebaseFirestore.getInstance();

        //Set the value of PASSWORD to the password in the database then fire the email.
        DocumentReference docRef = db.collection("credentials").document("password");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.PASSWORD = document.getString("value");
                        new DeleteDocument2.EmailTask(context).execute();
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private static class EmailTask extends AsyncTask<String,Void,Void> {
        //Good explanation of AsyncTask here: https://stackoverflow.com/questions/9671546/asynctask-android-example
        //For this particular fragment wanted to try a different technique for passing a value to the async task as described here:
        //https://stackoverflow.com/questions/16920942/getting-context-in-asynctask/16921076

        private Context mContext;

        public EmailTask(Context context){
            mContext = context;
        }

        @Override
        protected Void doInBackground(String... strings) {
            sendEmail(mContext);
            return null;
        }

    }

    public static void sendEmail(Context context){
        try {
            GMailSender sender = new GMailSender(MainActivity.EMAIL_ADDRESS, MainActivity.PASSWORD);

            String dateAndTime;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
            dateAndTime = dateFormat.format(calendar.getTime());

            //Shared preferences
            SharedPreferences sharedPreferences = context.getSharedPreferences("DeleteDocumentData",0);
            SharedPreferences employeeSharedPreferences = context.getSharedPreferences("EmployeeData",0);

            //Build the body
            String body = "Item with barcode number '"+sharedPreferences.getString("document_barcode_number","")+"' and name '"+sharedPreferences.getString("document_title","")+"' was deleted by "+employeeSharedPreferences.getString("first_name","")+" "+employeeSharedPreferences.getString("last_name","")+" on "+dateAndTime+".\nReason: '"+sharedPreferences.getString("purpose","")+"'";

            sender.sendMail("Item Deleted",//TODO:Need to replace some of these with either string resources or something else not hardcoded.
                    body,
                    MainActivity.EMAIL_ADDRESS,
                    MainActivity.EMAIL_FOR_NOTIFICATIONS,
                    null,
                    null);
            Log.i("SendMail","Email sent successfully");
            clearSharedPreferences(context);
        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);
            //TODO: Need to find a way to notify user if email notification not sent
        }

    }

    private void displaySearchResults() {
        //Display search results using data in shared preferences
        //Also enable next button

        txtBarcodeNumberContent.setText(sharedPref.getString("document_barcode_number",""));
        txtDocumentTitleContent.setText(sharedPref.getString("document_title",""));
        txtDocumentDescriptionContent.setText(sharedPref.getString("document_description",""));
        Glide.with(getContext())
                .asBitmap()
                .load(sharedPref.getString("document_photo_url",""))
                .listener(new RequestListener<Bitmap>() {//This is an oncomplete listener that allows me to make the progress bar invisible and the layout visible once glide completes
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.INVISIBLE);
                        layoutSearchResults.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imgDocumentPhoto);
    }

    private void launchPreviousFragment() {
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((MainActivity)getActivity()).replaceFragment("DeleteDocument1",false);
    }

    private static void launchNextFragment(Context c) {
        //Same as launchPreviousFragment but static so as to be accessible from inside static method deleteDocument
        //See https://stackoverflow.com/questions/22990158/access-getactivity-inside-static-method

        //Replaces the fragment in the frame_layout in app_bar_main.xml

        MainActivity activity = (MainActivity)c;
        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((activity)).replaceFragment("HomeSignedInFragment",false);
    }

    private static void clearSharedPreferences(Context context){
        //Clear the shared preferences
        SharedPreferences sharedPref = context.getSharedPreferences("DeleteDocumentData",0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
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
