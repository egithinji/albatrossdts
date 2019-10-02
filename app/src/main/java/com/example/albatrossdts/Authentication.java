package com.example.albatrossdts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class Authentication extends AppCompatActivity {

    private static final String TAG = "Information: ";
    private static final int RC_SIGN_IN = 1;

    //Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        //Iniitialize the firestore object
        db = FirebaseFirestore.getInstance();

        /*
        * Capture and respond to the dynamic link based on these instructions on passwordless email link sign in:
        * https://firebase.google.com/docs/auth/android/firebaseui
        * https://firebase.google.com/docs/auth/android/email-link-auth
        * https://firebase.google.com/docs/dynamic-links/android/receive
        * */

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }

                        // Handle the deep link. For example, open the linked
                        // content, or apply promotional credit to the user's
                        // account.
                        // ...

                        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                                .setAndroidPackageName(/* yourPackageName= */ "com.example.albatrossdts",/* installIfNotAvailable= */ true, /* minimumVersion= */ null)
                                .setHandleCodeInApp(true) // This must be set to true
                                .setUrl("https://albatrossdts.page.link") // This URL needs to be whitelisted
                                .setDynamicLinkDomain("albatrossdts.page.link")
                                .build();

                        // Create and launch sign-in intent. It will be caught by onActivityResult
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder()
                                        .setEmailLink(deepLink.toString())
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.EmailBuilder().enableEmailLinkSignIn()
                                                        .setActionCodeSettings(actionCodeSettings).build()))
                                        .build(),
                                RC_SIGN_IN);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "getDynamicLink:onFailure", e);
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Log.i(TAG,"Successfully signed in "+user.getUid());
                updateUid(user.getEmail(), user.getUid());
            }
            else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                //Make the authenticating please wait message invisible
                TextView txtAuthenticating = findViewById(R.id.txtAuthenticatingPleaseWait);
                txtAuthenticating.setVisibility(View.INVISIBLE);
                //Display the textview informing user of unsuccessful sign in
                TextView tv = findViewById(R.id.txtAuthenticationError);
                tv.setVisibility(View.VISIBLE);
                Toast.makeText(this,"Unsuccessful sign in: "+response.getError().toString(),Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateUid(String email, final String uid) {
        //Find the employee with the authenticated user's email address.
        //Update the uid field of that employee with the authenticated user's uid.
        //Store the employee's data in stored preferences
        //Send the user to main activity.

        //Get the specific employee using their email address.
        db.collection("employees")
                .whereEqualTo("email_address", email)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentReference dc;
                            if(task.getResult().size()==0){
                                //Email address not found in database
                                //Sign the person out of firebase
                                FirebaseAuth.getInstance().signOut();
                                TextView txtAuthenticating = findViewById(R.id.txtAuthenticatingPleaseWait);
                                txtAuthenticating.setVisibility(View.INVISIBLE);
                                //Display the textview informing user of unsuccessful sign in
                                TextView tv = findViewById(R.id.txtAuthenticationError);
                                tv.setVisibility(View.VISIBLE);
                            }else{
                                //Email address was found in database
                                for (final QueryDocumentSnapshot document : task.getResult()) {//Should only be one record
                                    dc = document.getReference(); //Get the reference to the employee document
                                    //Run the update:
                                    dc
                                            .update("uid",uid)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.d(TAG, "Document successfully updated!");

                                                    //Store the employee's data in shared preferences for use throughout the app
                                                    Employee employee = document.toObject(Employee.class);

                                                    SharedPreferences sharedPref = getSharedPreferences("EmployeeData",0);
                                                    SharedPreferences.Editor editor = sharedPref.edit();
                                                    editor.putString("uid",employee.getUid());
                                                    editor.putString("email_address",employee.getEmail_address());
                                                    editor.putString("first_name",employee.getFirst_name());
                                                    editor.putString("last_name",employee.getLast_name());
                                                    editor.putString("group",employee.getGroup());
                                                    editor.apply();

                                                    //Send the signed-in user to the main activity
                                                    Intent intent = new Intent(Authentication.this,MainActivity.class);
                                                    startActivity(intent);
                                                    finish();//Needed so the user can't come back track here after successful sign-in. See https://stackoverflow.com/questions/8631095/how-to-prevent-going-back-to-the-previous-activity
                                                }

                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.w(TAG, "Error updating document", e);
                                                }
                                            });
                                }
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intent = new Intent(Authentication.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
