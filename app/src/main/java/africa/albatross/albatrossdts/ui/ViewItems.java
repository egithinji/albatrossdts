package africa.albatross.albatrossdts.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import africa.albatross.albatrossdts.Document;
import africa.albatross.albatrossdts.Employee;
import africa.albatross.albatrossdts.MainActivity;
import africa.albatross.albatrossdts.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ViewItems.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ViewItems#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewItems extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private TextView txtBarcode;
    private TextView txtTitle;
    private TextView txtDescription;
    private TextView txtLocation;
    private TextView txtCurrentlyCheckedOut;
    private ImageView imgPhoto;
    private List<Document> items = new ArrayList<>();
    private Button btnBack;
    private Button btnNext;
    private ProgressBar progressBar;

    //Firestore
    private FirebaseFirestore db;

    private int listPosition = 0; //Position in the list that holds the items

    //HashMap for storing employee UID and names
    private HashMap<String, String> employees = new HashMap<>();





    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    public ViewItems() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewItems.
     */
    // TODO: Rename and change types and number of parameters
    public static ViewItems newInstance(String param1, String param2) {
        ViewItems fragment = new ViewItems();
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


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_items, container, false);

        //Instantiate views
        txtBarcode = view.findViewById(R.id.txtBarcode);
        txtTitle = view.findViewById(R.id.txtTitle);
        txtDescription = view.findViewById(R.id.txtDescription);
        txtLocation = view.findViewById(R.id.txtLocation);
        txtCurrentlyCheckedOut = view.findViewById(R.id.txtCurrentlyCheckedOut);
        imgPhoto = view.findViewById(R.id.imgPhoto);
        btnBack = view.findViewById(R.id.btnBackViewItems);
        btnNext = view.findViewById(R.id.btnNextViewItems);
        progressBar = view.findViewById(R.id.progressBar);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(listPosition == 0){//If we're at the beggining of the list and user clicks PREV, take him back to home page.
                    ((MainActivity)getActivity()).replaceFragment("HomeSignedInFragment",false);
                }else{
                    listPosition--;
                    btnNext.setEnabled(true);
                    refreshViews();
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPosition++;
                btnBack.setEnabled(true);
                refreshViews();
            }
        });


        getItems();//Get items from the database and store them in the items list


        return view;
    }


    private void getItems() {

        db.collection("documents")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()){
                            Document document;
                            for(QueryDocumentSnapshot qds: task.getResult()){
                                document = qds.toObject(Document.class);
                                items.add(document);
                            }
                            getEmployeeNames();
                        }
                    }
                });

    }

    private void getEmployeeNames() {
        //Create a hashmap mapping employee uid's to employee names. This hashmap will be used while creating the report
        //to print the employee names.

        db.collection("employees")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Employee employee = document.toObject(Employee.class);

                                if(!(employee.getUid()==null)) { //don't add any unauthenticated users to the list.
                                    //The below commented out line should be the one used during production
                                    //employees.put(employee.getUid(), employee.getFirst_name() + " " + employee.getLast_name() + " (" + employee.getEmail_address() + ")");
                                    employees.put(employee.getUid(), employee.getFirst_name() + " " + employee.getLast_name());
                                }
                            }
                            //Now that hashmap is populated, call refreshViews() method
                            btnBack.setEnabled(true);
                            btnNext.setEnabled(true);
                            refreshViews();
                        }

                    }
                });
    }

    private void refreshViews() {
        //If the items list is empty or the position is the last item (size-1), disable the next button

        if(items.size() == 0 || listPosition == items.size()-1){
            btnNext.setEnabled(false);
        }

        if(items.size() != 0){
            txtBarcode.setText(items.get(listPosition).getBarcode_number());
            txtTitle.setText(items.get(listPosition).getTitle());
            txtDescription.setText(items.get(listPosition).getDescription());
            txtLocation.setText(items.get(listPosition).getPermanent_location());
            if(items.get(listPosition).getCurrently_checked_out_to() == null){//If currently_checked_out_to field is null, it means the item is in storage.
                txtCurrentlyCheckedOut.setText("In storage");
            }else{
                txtCurrentlyCheckedOut.setText(employees.get(items.get(listPosition).getCurrently_checked_out_to()));
            }
            progressBar.setVisibility(View.VISIBLE);
            Glide.with(getContext())
                    .asBitmap()
                    .load(items.get(listPosition).getPhoto_url())
                    .into(imgPhoto);
            //If the user clicks the link, open larger image in browser
            imgPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse(items.get(listPosition).getPhoto_url()));
                    startActivity(intent);
                }
            });
            progressBar.setVisibility(View.INVISIBLE);
        }

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
