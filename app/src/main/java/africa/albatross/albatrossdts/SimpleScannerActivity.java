package africa.albatross.albatrossdts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class SimpleScannerActivity extends FragmentActivity implements ZXingScannerView.ResultHandler {
    private static final String TAG = "Simple Scanner";
    private ZXingScannerView mScannerView;
    private String fromFragment; //Which fragment started the scanner

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        Intent intent = getIntent();
        fromFragment = intent.getStringExtra("fromFragment");

    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getText()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        Toast.makeText(SimpleScannerActivity.this,"Scan successful. Barcode number: "+rawResult.getText(),Toast.LENGTH_LONG).show();

        //Add the barcode number to shared preferences
        SharedPreferences sharedPref = getSharedPreferences("DocumentData",0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("barcode_number",rawResult.getText());
        editor.apply();

        launchFragment();



        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    private void launchFragment() {
        //Send user back to AddNewDocument3 fragment
        //AddNewDocument3 fragment = AddNewDocument3.newInstance(null,null);

        //FragmentManager fragmentManager = getSupportFragmentManager();
        //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        //fragmentTransaction.replace(R.id.fragment_container, fragment).commit();

        Intent intent = new Intent(SimpleScannerActivity.this,MainActivity.class);
        intent.putExtra("nameOfFragment",fromFragment);
        startActivity(intent);
    }

}
