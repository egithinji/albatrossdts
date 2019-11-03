package com.example.albatrossdts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        HomeSignedOutFragment.OnFragmentInteractionListener,
        HomeSignedInFragment.OnFragmentInteractionListener,
        AddNewDocument1.OnFragmentInteractionListener,
        AddNewDocument2.OnFragmentInteractionListener,
        AddNewDocument3.OnFragmentInteractionListener,
        AddNewDocument4.OnFragmentInteractionListener,
        DeleteDocument1.OnFragmentInteractionListener,
        DeleteDocument2.OnFragmentInteractionListener,
        CheckIn1.OnFragmentInteractionListener,
        CheckIn2.OnFragmentInteractionListener,
        CheckOut1.OnFragmentInteractionListener,
        CheckOut2.OnFragmentInteractionListener,
        DocumentsSummary1.OnFragmentInteractionListener,
        DocumentsPerEmployee1.OnFragmentInteractionListener,
        TransactionsSummary1.OnFragmentInteractionListener,
        TransactionsByDocument1.OnFragmentInteractionListener,
        Settings.OnFragmentInteractionListener,
        ContactUs.OnFragmentInteractionListener{

    private static final String TAG = "Info:";
    private static String CURRENT_FRAGMENT = ""; //TODO: This was an attempt at fixing the issue when activity is destroyed upon screen rotation but didn't work too well. I've temporarily disabled landscape orientation.

    //Static variables to hold transaction types
    public static final String TRANSACTION_TYPE_ADD = "Add Item";
    public static final String TRANSACTION_TYPE_DELETE = "Delete Item";
    public static final String TRANSACTION_TYPE_CHECKOUT = "Check-out";
    public static final String TRANSACTION_TYPE_CHECKIN = "Check-in";

    //Email credentials
    public static String EMAIL_ADDRESS;
    public static String PASSWORD;

    //Email for receiving notifications
    public static String EMAIL_FOR_NOTIFICATIONS;

    //Firebase Auth
    private FirebaseAuth mAuth;

    //Firestore
    private FirebaseFirestore db;

    private static final int RC_SIGN_IN = 1;
    /*The MENU_ID will depend on whether the user is a regular user or admin.
    * It will be stored in this static variable and used when inflating the navigation view's menu.
    * Only admin users should have access to the reporting features.
    * */

    public static int MENU_ID = R.menu.activity_main_drawer_signedout; //This is for the demo version of the app. Production should be set to drawer_signedout

    //An arraylist used for holding employee objects. Used in DocumentsPerEmployee fragment
    public static ArrayList<Employee> employeeObjects = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //For apache poi see https://github.com/centic9/poi-on-android
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        //Don't want a title here for now. Maybe call it home?
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        //Initialize mAuth
        mAuth = FirebaseAuth.getInstance();

        //replaceFragment(getNameOfFragmentToLoad(),false);

        updateEmailNotification();

        getEmployees();

        //By default the HomeSignedOutFragment should be loaded
        /*See https://www.youtube.com/watch?v=bjYstsO1PgI and https://www.youtube.com/watch?v=BMTNaPcPjdw for fragments*/
        HomeSignedOutFragment homeSignedOutFragment = HomeSignedOutFragment.newInstance(null,null);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_container,homeSignedOutFragment).commit();

        //Check the logged in user's credentials and set the MENU_ID to the correct value
        //Also load the correct fragment
        checkIfCurrentUserSignedIn();

        //Set the navigationView's menu:
        navigationView.getMenu().clear();
        navigationView.inflateMenu(MENU_ID);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
    }

    public static void getEmployees(){
        //Get the employees from firestore and store in a Employee object
        //Update the adapter with the employee names.

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("employees")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    private static final String TAG = "MainActivity" ;

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            employeeObjects.clear(); //First clear what was there
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Employee employee = document.toObject(Employee.class);
                                //Add employee object to employeeObjects arraylist
                                if(employee.getUid()!= null) { //don't add any unauthenticated users to the array list.
                                    employeeObjects.add(employee);
                                }
                            }
                        } else {
                            Log.w(TAG, "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    private void updateEmailNotification() {
        //Get email used for receiving notifications from firestore
        //Get Firestore instance
        db = FirebaseFirestore.getInstance();

        DocumentReference documentReference = db.collection("credentials").document("email_address_notifications");
        documentReference
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        MainActivity.EMAIL_FOR_NOTIFICATIONS = documentSnapshot.getString("value");
                    }
                });
    }

    private String getNameOfFragmentToLoad() {
        //Checks whether the name of a fragment has been passed from an intent that started MainActivity.
        //Returns that fragment.

        //Fragment fragment;

        //Check whether there is any data from an intent that started this acvtivity
        //E.g. when coming from SimpleScannerActivity
        String nameOfFragment;
        Intent intent = getIntent();
        if(intent.getExtras() == null){
            //The activity has not been started by an intent from another activity.
            Log.i(TAG,"Intent.getExtras is null");

            if(CURRENT_FRAGMENT == "") {
                //if CURRENT_FRAGMENT is empty, this is a fresh loading of the app.
                nameOfFragment = "HomeSignedInFragment";
                Log.i(TAG,"CURRENT_FRAGMENT is empty");
            }else{
                //if CURRENT_FRAGMENT has a value, it means it was saved in onSavedInstanceState and the app was rotated. Load the CURRENT_FRAGMENT fragment
                nameOfFragment = CURRENT_FRAGMENT;
                Log.i(TAG,"CURRENT_FRAGMENT is "+CURRENT_FRAGMENT);
            }

        }else{
            Log.i(TAG,"Intent.getExtras is not null");
            //The activity was launched by an intent that passed a value indicating fragment to load
            nameOfFragment = intent.getStringExtra("nameOfFragment");
        }

        CURRENT_FRAGMENT = nameOfFragment; //this is needed in case the screen is rotated. The variable will be passed on through saved instance state.

        //fragment = getFragmentFromName(nameOfFragment);

        return nameOfFragment;
    }

    private Fragment getFragmentFromName(String nameOfFragment){

        Fragment fragment = null;

        switch (nameOfFragment)
        {
            case "HomeSignedInFragment":

                //Get user data from shared preferences
                SharedPreferences sharedPref = getSharedPreferences("EmployeeData",0);
                //Get the first name of the signed in user
                String fname = sharedPref.getString("first_name","");
                //Pass the user's first name as parameter to the HomeSignedInFragment
                fragment = HomeSignedInFragment.newInstance(fname);
                CURRENT_FRAGMENT = "HomeSignedInFragment"; //This is needed in case the screen is rotated. The variable will be passed on through saved instance state.
                break;

            case "AddNewDocument1":

                Log.i(TAG,"CURRENT_FRAGMENT has been set to "+CURRENT_FRAGMENT);
                fragment = AddNewDocument1.newInstance(null,null);
                CURRENT_FRAGMENT = "AddNewDocument1";
                break;

            case "AddNewDocument2":
                fragment = AddNewDocument2.newInstance(null,null);
                CURRENT_FRAGMENT = "AddNewDocument2";
                break;

            case "AddNewDocument3":
                fragment = AddNewDocument3.newInstance(null,null);
                CURRENT_FRAGMENT = "AddNewDocument3";
                break;

            case "AddNewDocument4":
                fragment = AddNewDocument4.newInstance(null,null);
                CURRENT_FRAGMENT = "AddNewDocument4";
                break;

            case "DeleteDocument1":
                fragment = DeleteDocument1.newInstance(null,null);
                CURRENT_FRAGMENT = "DeleteDocument1";
                break;

           case "DeleteDocument2":
                fragment = DeleteDocument2.newInstance(null,null);
                CURRENT_FRAGMENT = "DeleteDocument2";
                break;

            case "CheckIn1":
                fragment = CheckIn1.newInstance(null,null);
                CURRENT_FRAGMENT = "CheckIn1";
                break;

            case "CheckIn2":
                fragment = CheckIn2.newInstance(null,null);
                CURRENT_FRAGMENT = "CheckIn2";
                break;

            case "CheckOut1":
                fragment = CheckOut1.newInstance(null,null);
                CURRENT_FRAGMENT = "CheckOut1";
                break;

            case "CheckOut2":
                fragment = CheckOut2.newInstance(null,null);
                CURRENT_FRAGMENT = "CheckOut2";
                break;

            case "DocumentsSummary1":
                fragment = DocumentsSummary1.newInstance(null,null);
                CURRENT_FRAGMENT = "DocumentsSummary1";
                break;

            case "DocumentsPerEmployee1":
                fragment = DocumentsPerEmployee1.newInstance(null,null);
                CURRENT_FRAGMENT = "DocumentsPerEmployee1";
                break;

            case "TransactionsSummary1":
                fragment = TransactionsSummary1.newInstance(null,null);
                CURRENT_FRAGMENT = "TransactionsSummary1";
                break;

            case "TransactionsByDocument1":
                fragment = TransactionsByDocument1.newInstance(null,null);
                CURRENT_FRAGMENT = "TransactionsByDocument1";
                break;

            case "Settings":
                fragment = Settings.newInstance(null,null);
                CURRENT_FRAGMENT = "Settings";

            case "ContactUs":
                fragment = ContactUs.newInstance(null,null);
                CURRENT_FRAGMENT = "ContactUs";
        }

        return fragment;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_privacy_policy) {

            //Open privacy policy google doc

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://docs.google.com/document/d/1sgw_91bhErpNfZ-aN9jXona81LIEVNu570ZBnj0P8SY/edit?usp=sharing"));
            startActivity(intent);

        }else if(id == R.id.action_help){
            //Open help google doc

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setData(Uri.parse("https://docs.google.com/document/d/1xFmmhKwzJUZKObq4KCuRgQt0m6w_SeEcY2-S_zBvT_w/edit?usp=sharing"));
            startActivity(intent);

        }else if(id == R.id.action_sign_out){

            //Sign out the user
            //Clear the shared preferences for the employee
            SharedPreferences sharedPref = getSharedPreferences("EmployeeData",0);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.clear();
            editor.commit();

            //Clear the shared preferences for adding new document
            SharedPreferences sharedPreferencesAddNew = getSharedPreferences("AddNewDocumentData",0);
            editor = sharedPreferencesAddNew.edit();
            editor.clear();
            editor.commit();

            //Clear the CURRENT_FRAGMENT variable so it starts afresh on sign in
            CURRENT_FRAGMENT = "";

            //Firebase sign out
            FirebaseAuth.getInstance().signOut();
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home){
            //Start HomeSignedInFragment
            replaceFragment("HomeSignedInFragment",true);

        } else if (id == R.id.nav_action1) {
            //Start CheckIn1 fragment
            replaceFragment("CheckIn1", true);

        } else if (id == R.id.nav_action2) {
            //Start CheckOut1 fragment
            replaceFragment("CheckOut1", true);

        } else if (id == R.id.nav_action3) {
            //Start AddNewDocument1 fragment
            replaceFragment("AddNewDocument1", true);
        } else if (id == R.id.nav_action4) {
            //Start DeleteDocument1 fragment
            replaceFragment("DeleteDocument1", true);

        } else if (id == R.id.nav_report1) {
            //Start DocumentsSummary1 fragment
            replaceFragment("DocumentsSummary1", true);

        } else if (id == R.id.nav_report2) {
            //Start DocumentsPerEmployee1 fragment
            replaceFragment("DocumentsPerEmployee1", true);

        } else if (id == R.id.nav_report3) {
            //Start TransactionsSummary1
            replaceFragment("TransactionsSummary1", true);

        } else if (id == R.id.nav_report4) {
            //Start TransactionsByDocument1
            replaceFragment("TransactionsByDocument1", true);

        } else if (id == R.id.nav_settings){
            //Start Settings
            replaceFragment("Settings",true);

        } else if (id == R.id.nav_contact_us){
            //Start ContactUs
            replaceFragment("ContactUs",true);
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void checkIfCurrentUserSignedIn(){

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // User is signed in.
            Log.i(TAG,"User is signed in.");

            replaceFragment(getNameOfFragmentToLoad(),false);

            //Check shared preferences to see if this user is admin.
            //Adjust the MENU_ID accordingly

            //Get user data from shared preferences
            SharedPreferences sharedPref = getSharedPreferences("EmployeeData",0);

                MENU_ID = R.menu.activity_main_drawer_demo;


        } else {
            // User not signed in.
            Log.i(TAG,"User is not signed in.");
            //Set the menu to the signedout menu
            MENU_ID = R.menu.activity_main_drawer_signedout;
        }

    }



    public void replaceFragment(String fragmentName, boolean allowBack){
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        /*
         * allowBack is used to decide whether or not to do addToBackStack.
         * I don't want this to be done during the sign in fragment because
         * after successful sign in the user can click the back button and go back to the sign in box again
         * which I don't want.
         * But for majority of cases I want the user to be able to back track with
         * the back button.
         * */

        Fragment fragment = getFragmentFromName(fragmentName);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //TODO:Commenting this out for now to conserve memory. I've disabled back button
        //TODO: on the app until I figure out how to properly manage the backstack and prevent weird
        //TODO: behaviour from occuring.
        /*if(allowBack){
            fragmentTransaction.addToBackStack(null);
        }*/
        fragmentTransaction.replace(R.id.fragment_container, fragment).commit();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
            outState.putString("current_fragment",CURRENT_FRAGMENT);
        }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        CURRENT_FRAGMENT = savedInstanceState.getString("current_fragment");
    }




}
