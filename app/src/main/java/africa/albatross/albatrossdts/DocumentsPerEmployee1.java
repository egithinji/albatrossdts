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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.apache.poi.java.awt.Color;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DocumentsPerEmployee1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DocumentsPerEmployee1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DocumentsPerEmployee1 extends Fragment {
    private static final String TAG = "DocumentsPerEmployee1";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Views
    private Spinner spinnerEmployee;
    private Button btnGenerate;
    private ProgressBar progressBar;
    private TextView txtStatus;
    private EditText editTextEnterEmail; //This is only for the demo version

    //Firestore
    private FirebaseFirestore db;

    //Array adapter for employee spinner
    private ArrayAdapter<String> adapter;

    //List to hold employees, needed by the adapter for the spinner
    private List<String> employeeList = new ArrayList<>();

    //HashMap for storing employee UID and names
    private HashMap<String, String> employees = new HashMap<>();

    //Spreadsheet objects
    private XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFSheet spreadsheet = workbook.createSheet("Items Per Employee");
    private int rowPosition = 0;

    //File object
    //private File file = new File(getContext().getExternalFilesDir(null),"createworkbook.xlsx");

    //Counter for use within the asynchronous task that gets the transaction data
    private int numberOfLoops = 0;


    public DocumentsPerEmployee1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DocumentsPerEmployee1.
     */
    // TODO: Rename and change types and number of parameters
    public static DocumentsPerEmployee1 newInstance(String param1, String param2) {
        DocumentsPerEmployee1 fragment = new DocumentsPerEmployee1();
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
        View view = inflater.inflate(R.layout.fragment_documents_per_employee1, container, false);

        //Instantiate views
        spinnerEmployee = view.findViewById(R.id.spinnerEmployee);
        btnGenerate = view.findViewById(R.id.btnGenerateDocumentsPerEmployeeReport);
        progressBar = view.findViewById(R.id.determinateBar);
        txtStatus = view.findViewById(R.id.txtStatus);
        editTextEnterEmail = view.findViewById(R.id.editTextEnterEmail);


        //Disable btnGenerate until after employees are populated
        btnGenerate.setEnabled(false);

        //Create the adapter
        adapter = new  ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, employeeList);

        populateEmployees();

        //Apply the adapters to the spinner
        spinnerEmployee.setAdapter(adapter);

        //Onclick for btnGenerate
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make the keyboard disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(editTextEnterEmail.getWindowToken(), 0);

                if (isValid(editTextEnterEmail.getText().toString())){
                    progressBar.setVisibility(View.VISIBLE);
                    txtStatus.setVisibility(View.VISIBLE);
                    generateReport();
                }else{
                    Toast.makeText(getContext(),"Please enter a valid email address.",Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    private boolean isValid(String email) { //See https://www.tutorialspoint.com/validate-email-address-in-java
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private void writeFirstPartOfSpreadsheet(){

        Log.i(TAG,"Writing first part of spreadsheet.");

        //Set column widths to fit the column titles almost exactly
        spreadsheet.setColumnWidth(0,2800);
        spreadsheet.setColumnWidth(1,4900);
        spreadsheet.setColumnWidth(2,5100);
        spreadsheet.setColumnWidth(3,2900);


        //Styling for title
        XSSFCellStyle titleStyle = workbook.createCellStyle();
        XSSFFont titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short)16);
        titleFont.setFontName("Segoe UI");
        XSSFColor titleColor = new XSSFColor(Color.decode("#613717"));
        titleFont.setColor(titleColor);
        titleStyle.setFont(titleFont);

        //Title should be on first cell of first row
        XSSFRow row = spreadsheet.createRow(rowPosition);
        XSSFCell titleCell = row.createCell(0);
        titleCell.setCellStyle(titleStyle);
        //Add the title. Cell should be first in first row from declaration at beginning of class
        titleCell.setCellValue("Items per Employee Report");

        //Jump to next row
        rowPosition++;
        row = spreadsheet.createRow(rowPosition);

        //Put date and time
        XSSFCellStyle dateStyle = workbook.createCellStyle();
        XSSFFont dateFont = workbook.createFont();
        dateFont.setFontName("Segoe UI");
        dateFont.setFontHeightInPoints((short)12);
        dateStyle.setFont(dateFont);
        XSSFCell dateCell = row.createCell(0);
        dateCell.setCellStyle(dateStyle);
        dateCell.setCellValue(getDateAndTime());

        //Put employee name and email (from spinner)
        rowPosition++;
        rowPosition++;
        row=spreadsheet.createRow(rowPosition);
        //Styling for employee name (same as for column header below)
        XSSFCellStyle styleEmployeeName = workbook.createCellStyle();
        XSSFColor employeeNameColor = new XSSFColor(Color.decode("#291801")); //See https://stackoverflow.com/questions/10912578/apache-poi-xssfcolor-from-hex-code
        XSSFFont employeeNameFont = workbook.createFont();
        employeeNameFont.setFontName("Segoe UI");
        employeeNameFont.setFontHeightInPoints((short)14);
        employeeNameFont.setColor(employeeNameColor);
        styleEmployeeName.setFont(employeeNameFont);
        //Write the cell
        XSSFCell employeeNameCell = row.createCell(0);
        employeeNameCell.setCellValue("Employee: "+spinnerEmployee.getSelectedItem().toString());
        //employeeNameCell.setCellValue("Employee: John Doe (jdoe@example.com)");//For demo. Remove and replace with commented line above for production.
        employeeNameCell.setCellStyle(styleEmployeeName);


        //Write column headers for this report
        //Jump two rows
        rowPosition++;
        rowPosition++;
        row=spreadsheet.createRow(rowPosition);

        //Styling for column headers
        XSSFCellStyle styleColumnHeaders = workbook.createCellStyle();
        styleColumnHeaders.setBorderBottom(BorderStyle.THICK);
        styleColumnHeaders.setBorderLeft(BorderStyle.THIN);
        styleColumnHeaders.setBorderRight(BorderStyle.THIN);
        styleColumnHeaders.setBorderTop(BorderStyle.THIN);
        XSSFColor columnColor = new XSSFColor(Color.decode("#291801")); //See https://stackoverflow.com/questions/10912578/apache-poi-xssfcolor-from-hex-code
        XSSFFont columnFont = workbook.createFont();
        columnFont.setFontName("Segoe UI");
        columnFont.setFontHeightInPoints((short)14);
        columnFont.setColor(columnColor);
        styleColumnHeaders.setFont(columnFont);

        //First column header
        XSSFCell headerCell = row.createCell(0);
        headerCell.setCellValue("Barcode");
        headerCell.setCellStyle(styleColumnHeaders);

        //Second column header
        headerCell = row.createCell(1);
        headerCell.setCellValue("Item Name");
        headerCell.setCellStyle(styleColumnHeaders);

        //Third column header
        headerCell = row.createCell(2);
        headerCell.setCellValue("Check Out Date");
        headerCell.setCellStyle(styleColumnHeaders);

        //Fourth column header
        headerCell = row.createCell(3);
        headerCell.setCellValue("Purpose");
        headerCell.setCellStyle(styleColumnHeaders);

        //Increment row position
        rowPosition++;
    }

    private void generateReport() {


        //Get all documents in documents collection where 'currently_checked_out_to' value is equal to the selected employee's uid.
        db.collection("documents")
                .whereEqualTo("currently_checked_out_to",employees.get(spinnerEmployee.getSelectedItem()))//This will query the employees hashmap for that key. The value will be equal to the employee's uid.
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //Commenting out below IF condition for demo. Needed in production

                            if(!(task.getResult().size()>0)) { //If no document found checked out to this user, just display toast and don't do the queries.
                                Toast.makeText(getContext(), "No item currently checked out by this employee.", Toast.LENGTH_LONG).show();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        //Hide progress bar and update status
                                        progressBar.setVisibility(View.INVISIBLE);
                                        txtStatus.setVisibility(View.INVISIBLE);
                                    }
                                });

                            }else {
                                //good tutorial here: https://www.tutorialspoint.com/apache_poi/index.htm
                                writeFirstPartOfSpreadsheet();

                                //Count for progress bar
                                int count = 0;

                                for (final QueryDocumentSnapshot document : task.getResult()) {
                                 /*
                                 *For each document checked out by this employee, use the document's last_transaction_id to retrieve
                                 * the transaction.
                                 * From that transaction, get the barcode, document title, date, and purpose and add these to the spreadsheet.
                                 * */

                                    //For testing:
                                    Log.i(TAG,"Value of count: "+count);

                                    //Increment count and update progress bar
                                    count++;
                                    progressBar.setProgress((count/task.getResult().size())*100);

                                    //Capture document data in a Document object for ease of retrieval
                                    final Document documentObject = document.toObject(Document.class);

                                    //Get a document reference to the specific transaction
                                    DocumentReference transactionDocumentReference = db.collection("transactions").document(documentObject.getLast_transaction_id());

                                    //Get the transaction data and update the spreadsheet
                                    transactionDocumentReference
                                            .get()
                                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                                    Log.i(TAG,"Fetching transaction and writing row.");

                                                    //Use Transaction object to hold the data
                                                    Transaction transaction = documentSnapshot.toObject(Transaction.class);

                                                    //Create a new row
                                                    XSSFRow row = spreadsheet.createRow(rowPosition);

                                                    //Styling
                                                    XSSFCellStyle cellStyle = workbook.createCellStyle();
                                                    cellStyle.setBorderLeft(BorderStyle.THIN);
                                                    cellStyle.setBorderRight(BorderStyle.THIN);
                                                    cellStyle.setBorderBottom(BorderStyle.THIN);
                                                    XSSFFont cellFont = workbook.createFont();
                                                    cellFont.setFontName("Segoe UI");
                                                    cellFont.setFontHeightInPoints((short)12);
                                                    cellStyle.setFont(cellFont);

                                                    XSSFCell cell;

                                                    //First column data
                                                    cell = row.createCell(0);
                                                    cell.setCellValue(transaction.getBarcode_number());
                                                    cell.setCellStyle(cellStyle);

                                                    //Second column data
                                                    cell = row.createCell(1);
                                                    cell.setCellValue(transaction.getDocument_title());
                                                    cell.setCellStyle(cellStyle);

                                                    //Third column data
                                                    //This is the date
                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                                                    String date = sfd.format(transaction.getDate().toDate()).toString();

                                                    cell = row.createCell(2);
                                                    cell.setCellValue(date); //The transaction.getDate returns a firebase timestamp
                                                    cell.setCellStyle(cellStyle);

                                                    //Fourth column data
                                                    cell = row.createCell(3);
                                                    cell.setCellValue(transaction.getPurpose());
                                                    cell.setCellStyle(cellStyle);

                                                    //Increment row
                                                    rowPosition++;

                                                    //Increment counter for deciding whether to generate file and send the email
                                                    numberOfLoops++;

                                                    if(numberOfLoops == task.getResult().size()){

                                                        //Create file using specific name
                                                        File file = new File(getContext().getExternalFilesDir(null),"createworkbook.xlsx");
                                                        FileOutputStream out = null;
                                                        try {
                                                            out = new FileOutputStream(file);
                                                            //write operation workbook using file out object
                                                            workbook.write(out);
                                                            out.close();
                                                            Log.i(TAG,"Created file: "+file.getPath());
                                                            //workbook = new XSSFWorkbook();

                                                            //Update status message
                                                            txtStatus.setText("Sending report via email...");
                                                            numberOfLoops = 0;//Reset numberOfLoops and rowPosition incase user generates another report
                                                            rowPosition = 0;
                                                            getEmail(file.getPath()); //getEmail kicks off the process that results in an email being sent using the approved email credentials from the database.


                                                        } catch (IOException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i(TAG,"Fetching transaction failed for reason: "+e.getMessage().toString());
                                                }
                                            });
                                }

                                //The following code is for the demo version.
                                //For production should remove it and uncomment above for loop


                                /*Transaction transaction1 = new Transaction("100", "Dewalt Drill", "1", "John Doe (jdoe@example.com)", "Check-out", "Taken to site", Timestamp.now());


                                //Increment count and update progress bar
                                count++;
                                progressBar.setProgress((count / 1) * 100); //I know this makes no sense, it's just for the demo version to ensure the progress bar makes an appearance

                                //Create a new row
                                XSSFRow row = spreadsheet.createRow(rowPosition);

                                //Styling
                                XSSFCellStyle cellStyle = workbook.createCellStyle();
                                cellStyle.setBorderLeft(BorderStyle.THIN);
                                cellStyle.setBorderRight(BorderStyle.THIN);
                                cellStyle.setBorderBottom(BorderStyle.THIN);
                                XSSFFont cellFont = workbook.createFont();
                                cellFont.setFontName("Segoe UI");
                                cellFont.setFontHeightInPoints((short) 12);
                                cellStyle.setFont(cellFont);

                                XSSFCell cell;

                                //First column data
                                cell = row.createCell(0);
                                cell.setCellValue(transaction1.getBarcode_number());
                                cell.setCellStyle(cellStyle);

                                //Second column data
                                cell = row.createCell(1);
                                cell.setCellValue(transaction1.getDocument_title());
                                cell.setCellStyle(cellStyle);

                                //Third column data
                                //This is the date

                                cell = row.createCell(2);
                                cell.setCellValue("01 October 2019 09:00");
                                cell.setCellStyle(cellStyle);

                                //Fourth column data
                                cell = row.createCell(3);
                                cell.setCellValue(transaction1.getPurpose());
                                cell.setCellStyle(cellStyle);

                                //Create file using specific name
                                File file = new File(getContext().getExternalFilesDir(null), "createworkbook.xlsx");
                                FileOutputStream out = null;
                                try {
                                    out = new FileOutputStream(file);
                                    //write operation workbook using file out object
                                    workbook.write(out);
                                    out.close();
                                    Log.i(TAG, "Created file: " + file.getPath());
                                    //workbook = new XSSFWorkbook();

                                    //Update status message
                                    txtStatus.setText("Sending report via email...");
                                    numberOfLoops = 0;//Reset numberOfLoops and rowPosition incase user generates another report
                                    rowPosition = 0;
                                    getEmail(file.getPath()); //getEmail kicks off the process that results in an email being sent using the approved email credentials from the database.

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }*/
                            }

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void getEmail(final String attachment) {
        //Set the value of EMAIL_ADDRESS to the address in the database then call getPassword()
        DocumentReference docRef = db.collection("credentials").document("email_address");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.EMAIL_ADDRESS = document.getString("value");
                        getPassword(attachment);
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void getPassword(final String attachment) {
        //Set the value of PASSWORD to the password in the database then fire the email.
        DocumentReference docRef = db.collection("credentials").document("password");
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MainActivity.PASSWORD = document.getString("value");
                        new DocumentsPerEmployee1.EmailTask().execute(attachment);
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
            sendEmail(strings[0]);
            return null;
        }
    }

    public void sendEmail(String attachment){
        try {
            GMailSender sender = new GMailSender(MainActivity.EMAIL_ADDRESS, MainActivity.PASSWORD);


            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");

            //Get user data from shared preferences
            SharedPreferences employeeSharedPref = getActivity().getSharedPreferences("EmployeeData",0);

            //Build the body
            String body = "Please find attached the Items per Employee Report."; //TODO: Replace with string resource

            //Name of attached file
            String fileName = "Items_per_Employee_Report.xlsx"; //TODO: Replace with string resource

            sender.sendMail("Items per Employee Report",//TODO:Need to replace some of these with either string resources or something else not hardcoded.
                    body,
                    MainActivity.EMAIL_ADDRESS,
                    //employeeSharedPref.getString("email_address",""),
                    editTextEnterEmail.getText().toString(),//For the demo version. In production, remove this and uncomment above line so that email goes to the signed-in user's email.
                    attachment,fileName);
            Log.i("SendMail","Email sent successfully");

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Hide progress bar and update status
                    progressBar.setVisibility(View.INVISIBLE);
                    txtStatus.setText("Report sent. Check your email.");
                }
            });



        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText("An error occurred sending the email. Please verify email credentials in Settings, or contact the administrator.");
                }
            });
        }

    }

    private String getDateAndTime(){
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        String formattedDate = df.format(c);
        return formattedDate;
    }



    private void populateEmployees(){
        //Get the employees from the employeeObjects (created at app startup) and update adapter with employee names
        //Also update the hashmap with employee names/ employee uid


        for(Employee e: MainActivity.employeeObjects){
            //Add entry to employee HashMap. The key is their name plus email address in brackets. The value is their uid.
            //This is to enable the user to uniquely identify the user, in case there is more than one person with the same first and last name.
            //The UID is used when querying the database for documents checked out by the specific employee.
            //employees.put(e.getFirst_name() + " " + e.getLast_name() + " (" + e.getEmail_address() + ")", e.getUid());


       //The following IF condition is for the demo. Not needed for production
            if(e.getFirst_name().equals("User")){
                employees.put(e.getFirst_name() + " " + e.getLast_name(), e.getUid());
                //Update the adapter
                adapter.add(e.getFirst_name()+" "+e.getLast_name());
            }

        }

        btnGenerate.setEnabled(true);

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
