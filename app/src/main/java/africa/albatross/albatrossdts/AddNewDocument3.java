package africa.albatross.albatrossdts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import africa.albatross.albatrossdts.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNewDocument3.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNewDocument3#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewDocument3 extends Fragment {
    private static final int MY_CAMERA_REQUEST_CODE = 100 ;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    //Back button
    private Button btnBack;

    //Next button
    private Button btnNext;

    //Text for barcode number
    private TextView txtBarcodeNumber;

    //Barcode button
    private Button btnBarcodeButton;

    //Shared preferences
    SharedPreferences sharedPref;

    private OnFragmentInteractionListener mListener;

    public AddNewDocument3() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewDocument3.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNewDocument3 newInstance(String param1, String param2) {
        AddNewDocument3 fragment = new AddNewDocument3();
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
        View view = inflater.inflate(R.layout.fragment_add_new_document3, container, false);

        btnBack = view.findViewById(R.id.btnBack3AddNew);
        btnNext = view.findViewById(R.id.btnNext3AddNew);
        btnBarcodeButton = view.findViewById(R.id.btnScanBarcode);
        txtBarcodeNumber = view.findViewById(R.id.txtBarcodeScan);

        //Set text for barcode number.
        //Get it from the shared preference created by SimpleScannerActivity.
        //And add it to the AddNewDocumentData shared preferences.
        SharedPreferences scannerSharedPref = getActivity().getSharedPreferences("DocumentData",0);
        sharedPref = getActivity().getSharedPreferences("AddNewDocumentData",0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("barcode_number",scannerSharedPref.getString("barcode_number",""));
        editor.commit();

        //Clear the DocumentData shared preference
        //editor = scannerSharedPref.edit();
        //editor.clear();
        //editor.commit();


        txtBarcodeNumber.setText("Barcode number: "+sharedPref.getString("barcode_number",""));

        //Set OnClick for back button
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        //Set OnClick for next button
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goNext();
            }
        });

        //Set OnClick for barcode button
        btnBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchBarcodeScanner();
            }
        });

        return view;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.addToBackStack(null);
                Intent intent = new Intent(getContext(),SimpleScannerActivity.class);
                intent.putExtra("fromFragment","AddNewDocument3");
                startActivity(intent);

            } else {

                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_LONG).show();

            }

        }
    }

    private void launchBarcodeScanner() {

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            requestPermissions(new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        else{
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            Intent intent = new Intent(getContext(),SimpleScannerActivity.class);
            intent.putExtra("fromFragment","AddNewDocument3");
            startActivity(intent);
        }
    }

    private void goNext() {
        //Just to keep the methods consistent with the other AddNewDocument fragments.
        //Usually would add shared preferences here, but this was already done
        //by the SimpleScannerActivity. So just calling the launchNextFragment method.

        if(sharedPref.getString("barcode_number","").equals("")){
            //The user must scan a barcode
            Toast.makeText(getContext(),"Please scan the item's barcode.",Toast.LENGTH_LONG).show();
        }else {
            launchNextFragment();
        }

    }

    private void launchNextFragment() {
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        //The next fragment to launch
        //AddNewDocument4 fragment = AddNewDocument4.newInstance(null,null);

        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((MainActivity)getActivity()).replaceFragment("AddNewDocument4",true);
    }

    private void goBack() {
        //launch the previous fragment, AddNewDocument2
        //AddNewDocument2 fragment = AddNewDocument2.newInstance(null,null);
        ((MainActivity)getActivity()).replaceFragment("AddNewDocument2",true);
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
