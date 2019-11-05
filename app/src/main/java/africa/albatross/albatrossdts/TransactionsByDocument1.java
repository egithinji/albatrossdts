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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import africa.albatross.albatrossdts.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransactionsByDocument1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransactionsByDocument1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionsByDocument1 extends Fragment {
    private static final String TAG = "TransactionsByDocument1";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Views
    private Button btnGenerate;
    private ProgressBar progressBar;
    private TextView txtStatus;
    private EditText editTextDocument;
    private EditText editTextEnterEmail; //This is only for the demo version

    //Firestore
    private FirebaseFirestore db;

    //Spreadsheet objects
    private XSSFWorkbook workbook = new XSSFWorkbook();
    private XSSFSheet spreadsheet = workbook.createSheet("Transactions by Item");
    private int rowPosition = 0;

    //Hashmap for storing doc barcodes and titles
    private HashMap<String,String> docTitles = new HashMap<>();

    public TransactionsByDocument1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionsByDocument1.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionsByDocument1 newInstance(String param1, String param2) {
        TransactionsByDocument1 fragment = new TransactionsByDocument1();
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
        View view = inflater.inflate(R.layout.fragment_transactions_by_document1, container, false);

        btnGenerate = view.findViewById(R.id.btnTransactionsByDocumentReport);
        progressBar = view.findViewById(R.id.determinateBar);
        txtStatus = view.findViewById(R.id.txtStatus);
        editTextDocument = view.findViewById(R.id.editTextDocument);
        editTextEnterEmail = view.findViewById(R.id.editTextEnterEmail);

        //Onclick for btnGenerate
        btnGenerate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //if nothing has been entered for barcode number, prompt the user.
                if(editTextDocument.getText().toString().equals("")) {
                    editTextDocument.setBackgroundResource(R.drawable.edit_text_error);
                }else {

                    if(isValid(editTextEnterEmail.getText().toString())){
                        //Make the keyboard disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                        InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        mgr.hideSoftInputFromWindow(editTextDocument.getWindowToken(), 0);
                        progressBar.setVisibility(View.VISIBLE);
                        txtStatus.setVisibility(View.VISIBLE);
                        getDocumentTitles();//this will load document barcodes and titles into a hashmap then launch the getBarcodes method

                    }else{
                        Toast.makeText(getContext(),"Please enter a valid email address.",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });

        //Barcode edittext onclick listener to return the normal background after user
        //gets error and then enters text.
        editTextDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextDocument.setBackgroundResource(R.drawable.edit_text_normal);
            }
        });

        return view;
    }

    private boolean isValid(String email) { //See https://www.tutorialspoint.com/validate-email-address-in-java
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


    private void getDocumentTitles(){
        //Needed to get the title of the document that the user searches for.
        //Creates a hashmap of barcode-title objects
        //We'll pull this data from the transactions collection because
        // the document may have been deleted already from the documents collection but the user still needs to generate a transactions report.
        //It goes through transactions and adds values to the hash.
        //There should be no problem if a particular barcode is reused on a new document because the hashmap should overwrite
        //TODO: The last point assumes documents are pulled from firestore ordered by documentID and that this is also chronological

        db.collection("transactions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(QueryDocumentSnapshot documentSnapshot:task.getResult()){
                                Transaction transaction = documentSnapshot.toObject(Transaction.class);
                                docTitles.put(transaction.getBarcode_number(),transaction.getDocument_title());
                            }
                            //now generate the report
                            generateReport();
                        }
                    }
                });

    }

    private void generateReport() {

        //good tutorial here: https://www.tutorialspoint.com/apache_poi/index.htm

        //Get all documents in transactions collection where 'barcode' is equal to the value entered in the editText.
        db.collection("transactions")
                .orderBy("date")
                .whereEqualTo("barcode_number",editTextDocument.getText().toString().toUpperCase())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            if(!(task.getResult().size()>0)) { //If no transaction found with that barcode just display toast and don't do the queries.
                                Toast.makeText(getContext(), "Item with that barcode not found.", Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                txtStatus.setVisibility(View.INVISIBLE);
                            }else{
                                Log.i(TAG,"Document title being passed: "+editTextDocument.getText().toString().toUpperCase());
                                writeFirstPartOfSpreadsheet(editTextDocument.getText().toString().toUpperCase(),docTitles.get(editTextDocument.getText().toString().toUpperCase()));

                                //Count for progress bar
                                int count = 0;

                                for (final QueryDocumentSnapshot document : task.getResult()) {

                                    //Increment count and update progress bar
                                    count++;
                                    progressBar.setProgress((count/task.getResult().size())*100);

                                    //Capture transaction in Transaction object for ease of retrieval
                                    Transaction transactionObject = document.toObject(Transaction.class);


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
                                    cell.setCellValue(transactionObject.getEmployee_name());
                                    cell.setCellStyle(cellStyle);

                                    //Second column data
                                    cell = row.createCell(1);
                                    cell.setCellValue(transactionObject.getTransaction_type());
                                    cell.setCellStyle(cellStyle);

                                    //Third column data
                                    cell = row.createCell(2);
                                    cell.setCellValue(transactionObject.getPurpose());
                                    cell.setCellStyle(cellStyle);

                                    //Fourth column data
                                    //This is the date
                                    SimpleDateFormat sfd = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                                    String date = sfd.format(transactionObject.getDate().toDate());

                                    cell = row.createCell(3);
                                    cell.setCellValue(date); //The transaction.getDate returns a firebase timestamp
                                    cell.setCellStyle(cellStyle);

                                    //Increment row
                                    rowPosition++;
                                }

                                //Create file using specific name
                                File file = new File(getContext().getExternalFilesDir(null),"createworkbook.xlsx");
                                FileOutputStream out = null;
                                try {
                                    out = new FileOutputStream(file);
                                    //write operation workbook using file out object
                                    workbook.write(out);
                                    out.close();
                                    Log.i(TAG,"Created file: "+file.getPath());
                                    //Update status message
                                    txtStatus.setText("Sending report via email...");
                                    getEmail(file.getPath()); //getEmail kicks off the process that results in an email being sent using the approved email credentials from the database.

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }

                        }else {
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

    private void writeFirstPartOfSpreadsheet(String barcode_number, String document_title) {

        Log.i(TAG,"document_title set to: "+document_title);


        //Set column widths to fit the column titles almost exactly
        spreadsheet.setColumnWidth(0,6000);
        spreadsheet.setColumnWidth(1,4900);
        spreadsheet.setColumnWidth(2,5100);
        spreadsheet.setColumnWidth(3,7000);


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
        titleCell.setCellValue("Transactions by Item Report");

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

        //Put document barcode number and title
        rowPosition++;
        rowPosition++;
        row=spreadsheet.createRow(rowPosition);

        //Styling for barcode number and document title (sub-titles)
        XSSFCellStyle styleSubTitle = workbook.createCellStyle();
        XSSFColor subTitleColor = new XSSFColor(Color.decode("#291801")); //See https://stackoverflow.com/questions/10912578/apache-poi-xssfcolor-from-hex-code
        XSSFFont subTitleFont = workbook.createFont();
        subTitleFont.setFontName("Segoe UI");
        subTitleFont.setFontHeightInPoints((short)14);
        subTitleFont.setColor(subTitleColor);
        styleSubTitle.setFont(subTitleFont);

        //Styling for barcode number and document title (values)
        XSSFCellStyle styleValues = workbook.createCellStyle();
        XSSFColor valuesColor = new XSSFColor(Color.decode("#613717"));
        XSSFFont valuesFont = workbook.createFont();
        valuesFont.setFontName("Segoe UI");
        valuesFont.setFontHeightInPoints((short)14);
        valuesFont.setColor(valuesColor);
        styleValues.setFont(valuesFont);

        //Write the cells
        XSSFCell subTitleCell = row.createCell(0);
        subTitleCell.setCellValue("Barcode:");
        subTitleCell.setCellStyle(styleSubTitle);

        XSSFCell valuesCell = row.createCell(1);
        valuesCell.setCellValue(barcode_number);
        valuesCell.setCellStyle(styleValues);

        rowPosition++;

        row = spreadsheet.createRow(rowPosition);
        subTitleCell = row.createCell(0);
        subTitleCell.setCellValue("Item Name:");
        subTitleCell.setCellStyle(styleSubTitle);

        valuesCell = row.createCell(1);
        valuesCell.setCellValue(document_title);
        valuesCell.setCellStyle(styleValues);


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
        headerCell.setCellValue("Employee Name");
        headerCell.setCellStyle(styleColumnHeaders);

        //Second column header
        headerCell = row.createCell(1);
        headerCell.setCellValue("Transaction");
        headerCell.setCellStyle(styleColumnHeaders);

        //Third column header
        headerCell = row.createCell(2);
        headerCell.setCellValue("Purpose");
        headerCell.setCellStyle(styleColumnHeaders);

        //Fourth column header
        headerCell = row.createCell(3);
        headerCell.setCellValue("Date");
        headerCell.setCellStyle(styleColumnHeaders);

        //Increment row position
        rowPosition++;
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
                        new TransactionsByDocument1.EmailTask().execute(attachment);
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
            String body = "Please find attached the Transactions By Item Report."; //TODO: Replace with string resource

            //Name of attached file
            String fileName = "Transactions_By_Item_Report.xlsx"; //TODO: Replace with string resource

            sender.sendMail("Transactions By Item Report",//TODO:Need to replace some of these with either string resources or something else not hardcoded.
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
            workbook = new XSSFWorkbook();
            spreadsheet = workbook.createSheet("Transactions by Item");
            rowPosition = 0;

        } catch (Exception e) {
            Log.e("SendMail", e.getMessage(), e);

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtStatus.setText("An error occurred sending the email. Please make sure you are connected to the Internet, or contact the administrator.");
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
