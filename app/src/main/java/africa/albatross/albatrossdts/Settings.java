package africa.albatross.albatrossdts;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import africa.albatross.albatrossdts.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Settings.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Settings#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Settings extends Fragment {
    private static final String TAG = "Settings";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Firestore
    private FirebaseFirestore db;

    //Views
    private Spinner spinnerEmployees;
    private Spinner spinnerGroups;
    private Button btnDeleteEmployee;
    private EditText editTextAddEmployeeEmail;
    private EditText editTextAddEmployeeFirstName;
    private EditText editTextAddEmployeeLastName;
    private CheckBox checkBoxAddEmployeeAdmin;
    private Button btnAddEmployee;
    private Spinner spinnerLocations;
    private Button btnDeleteLocation;
    private EditText editTextAddLocation;
    private Button btnAddLocation;
    private Spinner spinnerReasons;
    private Button btnDeleteReason;
    private EditText editTextAddReason;
    private Button btnAddReason;
    private EditText editTextEmailAddress;
    private EditText editTextEmailPassword;
    private Button btnUpdateCredentials;
    private EditText editTextEmailNotifications;
    private Button btnUpdateEmail;

    //HashMaps for linking entities (employee,location,reason) with document id for easy deletion
    private HashMap<String, String> employees = new HashMap<>();
    private HashMap<String, String> locations = new HashMap<>();
    private HashMap<String, String> reasons = new HashMap<>();

    //Array adapters for spinners
    private ArrayAdapter<String> adapterEmployees;
    private ArrayAdapter<String> adapterLocations;
    private ArrayAdapter<String> adapterReasons;
    private ArrayAdapter<String> adapterGroups;

    //Lists for adapters
    private List<String> listEmployees = new ArrayList<>();
    private List<String> listLocations = new ArrayList<>();
    private List<String> listReasons = new ArrayList<>();
    private List<String> listGroups = new ArrayList<>();

    //Dialog
    private DialogFragment deleteDialog;


    public Settings() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Settings.
     */
    // TODO: Rename and change types and number of parameters
    public static Settings newInstance(String param1, String param2) {
        Settings fragment = new Settings();
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

        //Get Firestore instance
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        //Instantiate views

        spinnerEmployees = view.findViewById(R.id.spinnerEmployee);
        btnDeleteEmployee = view.findViewById(R.id.btnDeleteEmployee);
        editTextAddEmployeeEmail = view.findViewById(R.id.editTextNewEmployeeEmail);
        editTextAddEmployeeFirstName = view.findViewById(R.id.editTextNewEmployeeFirstName);
        editTextAddEmployeeLastName = view.findViewById(R.id.editTextNewEmployeeLastName);
        spinnerGroups = view.findViewById(R.id.spinnerGroup);
        btnAddEmployee = view.findViewById(R.id.btnAddEmployee);
        spinnerLocations = view.findViewById(R.id.spinnerPermanentLocation);
        btnDeleteLocation = view.findViewById(R.id.btnDeleteLocation);
        editTextAddLocation = view.findViewById(R.id.editTextNewLocation);
        btnAddLocation = view.findViewById(R.id.btnAddLocation);
        spinnerReasons = view.findViewById(R.id.spinnerReason);
        btnDeleteReason = view.findViewById(R.id.btnDeleteReason);
        editTextAddReason = view.findViewById(R.id.editTextNewReason);
        btnAddReason = view.findViewById(R.id.btnAddReason);
        editTextEmailAddress = view.findViewById(R.id.editTextEmailAddress);
        editTextEmailPassword = view.findViewById(R.id.editTextEmailPassword);
        btnUpdateCredentials = view.findViewById(R.id.btnUpdateCredentials);
        editTextEmailNotifications = view.findViewById(R.id.editTextEmailAddressNotifications);
        btnUpdateEmail = view.findViewById(R.id.btnUpdateEmail);

        //Create adapters
        adapterEmployees = new  ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listEmployees);
        adapterLocations = new  ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listLocations);
        adapterReasons = new  ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, listReasons);
        adapterGroups = new ArrayAdapter<>(getActivity(),android.R.layout.simple_spinner_item, listGroups);

        //Populate spinners
        getEmployees();

        //Apply the adapters to the spinners
        spinnerEmployees.setAdapter(adapterEmployees);
        spinnerLocations.setAdapter(adapterLocations);
        spinnerReasons.setAdapter(adapterReasons);
        spinnerGroups.setAdapter(adapterGroups);

        //Delete Employee button onClickListener
        btnDeleteEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayConfirmationDialog("employees",employees.get(spinnerEmployees.getSelectedItem().toString()),spinnerEmployees.getSelectedItem().toString());
            }
        });

        //Delete Location button onClickListener
        btnDeleteLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayConfirmationDialog("locations",locations.get(spinnerLocations.getSelectedItem().toString()),spinnerLocations.getSelectedItem().toString());
            }
        });

        //Delete Reason button onClickListener
        btnDeleteReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayConfirmationDialog("reasons_for_checkout",reasons.get(spinnerReasons.getSelectedItem().toString()),spinnerReasons.getSelectedItem().toString());
            }
        });

        //Add Employee button onClickListener
        btnAddEmployee.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextAddEmployeeEmail.getText().toString().equals("")){
                    editTextAddEmployeeEmail.setBackgroundResource(R.drawable.edit_text_error);
                }else if(editTextAddEmployeeFirstName.getText().toString().equals("")){
                    editTextAddEmployeeFirstName.setBackgroundResource(R.drawable.edit_text_error);
                }else if(editTextAddEmployeeLastName.getText().toString().equals("")){
                    editTextAddEmployeeLastName.setBackgroundResource(R.drawable.edit_text_error);
                }else{
                    //Make the keyboards disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editTextAddEmployeeEmail.getWindowToken(), 0);
                    mgr.hideSoftInputFromWindow(editTextAddEmployeeFirstName.getWindowToken(), 0);
                    mgr.hideSoftInputFromWindow(editTextAddEmployeeLastName.getWindowToken(), 0);

                    addEmployee(editTextAddEmployeeEmail.getText().toString(),editTextAddEmployeeFirstName.getText().toString(),editTextAddEmployeeLastName.getText().toString(),spinnerGroups.getSelectedItem().toString());
                }
            }
        });

        //Listener to return employee edittexts to normal once user clicks on it
        editTextAddEmployeeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextAddEmployeeEmail.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        editTextAddEmployeeFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextAddEmployeeFirstName.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        editTextAddEmployeeLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextAddEmployeeLastName.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        //Add Location button onClickListener
        btnAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextAddLocation.getText().toString().equals("")){
                    editTextAddLocation.setBackgroundResource(R.drawable.edit_text_error);
                }else{
                    //Make the keyboards disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editTextAddLocation.getWindowToken(), 0);

                    addLocation(editTextAddLocation.getText().toString());
                }
            }
        });

        //Listener to return location edittext to normal once user clicks on it
        editTextAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextAddLocation.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        //Add Reason button onClickListener
        btnAddReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextAddReason.getText().toString().equals("")){
                    editTextAddReason.setBackgroundResource(R.drawable.edit_text_error);
                }else{
                    //Make the keyboards disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editTextAddReason.getWindowToken(), 0);

                    addReason(editTextAddReason.getText().toString());
                }
            }
        });

        //Listener to return reason edittext to normal once user clicks on it
        editTextAddReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextAddReason.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        //OnClick listener for Update credentials button
        btnUpdateCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextEmailAddress.getText().toString().equals("")){
                    editTextEmailAddress.setBackgroundResource(R.drawable.edit_text_error);
                }else if(editTextEmailPassword.getText().toString().equals("")){
                    editTextEmailPassword.setBackgroundResource(R.drawable.edit_text_error);
                }else{
                    //Make the keyboards disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editTextEmailAddress.getWindowToken(), 0);
                    mgr.hideSoftInputFromWindow(editTextEmailPassword.getWindowToken(), 0);

                    updateEmail();
                }
            }
        });

        //OnClick listener for Update notifications email button
        btnUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(editTextEmailNotifications.getText().toString().equals("")){
                    editTextEmailNotifications.setBackgroundResource(R.drawable.edit_text_error);
                }else{
                    //Make the keyboard disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                    InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    mgr.hideSoftInputFromWindow(editTextEmailNotifications.getWindowToken(), 0);
                    updateEmailNotification();
                }
            }
        });


        return view;
    }


    private void getEmployees() {
        //Get the employees from firestore and store in an Employee object
        //Update the adapter with the employee names.
        //Then call getLocations().

        db.collection("employees")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Employee employee = document.toObject(Employee.class);

                                //Add entry to employee HashMap. The key is their name plus email address in brackets. The value is the auto-generated unique firestore document id for this employee.
                                //This is to enable the user to uniquely identify the user, in case there is more than one person with the same first and last name.
                                //The auto-generated id is used when deleting the employee.

                                    employees.put(employee.getFirst_name() + " " + employee.getLast_name() + " (" + employee.getEmail_address() + ")", document.getId());
                                    //Update the adapter
                                    adapterEmployees.add(employee.getFirst_name()+" "+employee.getLast_name()+" ("+employee.getEmail_address()+")");
                                    //Update the employeeObjects arraylist which is used by DocumentsPerEmployee

                            }
                            MainActivity.getEmployees();//this is for updating the employeeObjects arraylist created upon app startup in the main activity. Used to populate spinner in DocumentsPerEmployee fragment.
                            btnDeleteEmployee.setEnabled(true);
                            getLocations();

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    private void getLocations() {
        //Get the locations from firestore and store in a Location object
        //Update the adapter with the location names.
        //Then call getReasons().

        db.collection("locations")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Location location = document.toObject(Location.class);

                                //Add entry to locations HashMap. The key is the location name. The value is the auto-generated unique firestore document id for this location.
                                //The auto-generated id is used when deleting the location.

                                locations.put(location.getName(), document.getId());
                                //Update the adapter
                                adapterLocations.add(location.getName());
                            }
                            btnDeleteLocation.setEnabled(true);
                            getReasons();

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void getReasons() {
        //Get the check out reasons from firestore and store in a Reason object
        //Update the adapter with the reasons.
        //Then call getEmailAddress()


        db.collection("reasons_for_checkout")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Reason reason = document.toObject(Reason.class);

                                //Add entry to reasons HashMap. The key is the reason. The value is the auto-generated unique firestore document id for this reason.
                                //The auto-generated id is used when deleting the reason.

                                reasons.put(reason.getReason(), document.getId());
                                //Update the adapter
                                adapterReasons.add(reason.getReason());
                            }
                            btnDeleteReason.setEnabled(true);
                            getEmailAddress();
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void getEmailAddress() {
        //Get the email address from firestore and update the edittext accordingly
        //Then call getPassword

        final DocumentReference documentReference = db.collection("credentials").document("email_address");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        String emailAddress = documentSnapshot.getString("value");
                        editTextEmailAddress.setText(emailAddress);
                        getPassword();
                    }else{
                        Log.d(TAG, "No such document");
                    }

                }else {
                    Log.d(TAG, "get failed with ", task.getException());
                }


            }
        });
    }

    private void getPassword() {
        //Get the password from firestore and update the edittext accordingly
        //then call getEmailNotification()

        final DocumentReference documentReference = db.collection("credentials").document("password");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        String password = documentSnapshot.getString("value");
                        editTextEmailPassword.setText(password);
                        btnUpdateCredentials.setEnabled(true);
                        getEmailNotification();
                    }else{
                        Log.d(TAG, "No such document");
                    }

                }else {
                    Log.d(TAG, "get failed with ", task.getException());
                }


            }
        });
    }

    private void getEmailNotification() {
        //Get the email address from firestore
        //then call getGroups()

        final DocumentReference documentReference = db.collection("credentials").document("email_address_notifications");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if(documentSnapshot.exists()){
                        String emailAddress = documentSnapshot.getString("value");
                        editTextEmailNotifications.setText(emailAddress);
                        btnUpdateEmail.setEnabled(true);
                        getGroups();
                    }else{
                        Log.d(TAG, "No such document");
                    }

                }else {
                    Log.d(TAG, "get failed with ", task.getException());
                }


            }
        });


    }

    private void getGroups() {
        //Get the groups from firestore and store in a group object and add to adapter

        db.collection("groups")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "Groups" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Group group = document.toObject(Group.class);

                                //Update the adapter
                                adapterGroups.add(group.getName());
                            }
                            btnAddEmployee.setEnabled(true);

                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });



    }

    private void displayConfirmationDialog(String collectionName, String documentName, String information) {

        FragmentManager fm = getActivity().getSupportFragmentManager();
        deleteDialog = new Settings.DeleteDialog();
        ((DeleteDialog) deleteDialog).collectionName = collectionName; //Setting the correct supplier id.
        ((DeleteDialog) deleteDialog).documentName = documentName;
        ((DeleteDialog)deleteDialog).information = information;
        deleteDialog.show(fm,"dialog");

    }

    //Dialog that appears when user clicks the Delete button
    //See https://developer.android.com/guide/topics/ui/dialogs
    public static class DeleteDialog extends DialogFragment {
        private String collectionName;
        private String documentName;
        private String information;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Are you sure you want to delete "+information+" from the database?")
                    .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //delete from database
                            deleteDocument(getContext(),collectionName,documentName);
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

    private static void deleteDocument(final Context context,final String collectionName, final String documentName) {
        //Deletes a document from firestore when given a collection name and document name.

        //Firestore
        FirebaseFirestore db;

        //Initialize firestore
        db = FirebaseFirestore.getInstance();

        db.collection(collectionName).document(documentName)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(context,"Deletion successful.",Toast.LENGTH_LONG).show();
                        relaunchFragment(context);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void addEmployee(String email, String firstName, String lastName, String group){
        //add a new employee record to firestore database

        //first create an employee object
        Employee employee = new Employee(null,email,firstName,lastName,group);

        db.collection("employees")
                .add(employee)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"New employee added.",Toast.LENGTH_LONG).show();
                        Settings.relaunchFragment(getContext());
                    }
                });

    }

    private void addLocation(String locationName){
        //add a new location to firestore database

        //first create a location object
        Location location = new Location(locationName);

        db.collection("locations")
                .add(location)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"New location added.",Toast.LENGTH_LONG).show();
                        Settings.relaunchFragment(getContext());
                    }
                });
    }

    private void addReason(String reasonDescription) {
        //add a new reason to firestore database

        //first create a reason object
        Reason reason = new Reason(reasonDescription);

        db.collection("reasons_for_checkout")
                .add(reason)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getContext(),"New reason added.",Toast.LENGTH_LONG).show();
                        Settings.relaunchFragment(getContext());
                    }
                });
    }

    private void updateEmail() {
        //update email address credentials with value in the email address edittext
        //then call updatePassword()
        db.collection("credentials").document("email_address")
                .update("value",editTextEmailAddress.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        updatePassword();                        
                    }
                });
    }

    private void updatePassword() {
        //update email password credentials with value in password edittext
        db.collection("credentials").document("password")
                .update("value",editTextEmailPassword.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Intent intent = getActivity().getIntent();
                        getActivity().finish();
                        startActivity(intent);

                        MainActivity.EMAIL_ADDRESS = editTextEmailAddress.getText().toString();
                        MainActivity.PASSWORD = editTextEmailPassword.getText().toString();

                        Toast.makeText(getContext(),"Email credentials updated. PASSWORD: "+MainActivity.PASSWORD, Toast.LENGTH_LONG).show();
                        Settings.relaunchFragment(getContext());
                    }
                });
    }

    private void updateEmailNotification() {
        //update notification email in firestore with value in editTextEmailNotifications
        db.collection("credentials").document("email_address_notifications")
                .update("value",editTextEmailNotifications.getText().toString())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        MainActivity.EMAIL_FOR_NOTIFICATIONS = editTextEmailNotifications.getText().toString();
                        Toast.makeText(getContext(),"Notification email updated.", Toast.LENGTH_LONG).show();
                        Settings.relaunchFragment(getContext());
                    }
                });
    }

    private static void relaunchFragment(Context c) {
        //Static so as to be accessible from inside static method deleteDocument
        //See https://stackoverflow.com/questions/22990158/access-getactivity-inside-static-method

        //Replaces the fragment in the frame_layout in app_bar_main.xml

        MainActivity activity = (MainActivity)c;
        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((activity)).replaceFragment("Settings",false);
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
