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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
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


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TransactionsSummary1.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TransactionsSummary1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TransactionsSummary1 extends Fragment {
    private static final String TAG = "TransactionsSummary1";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Views
    private Button btnGenerateReport;
    private ProgressBar progressBar;
    private TextView txtStatus;
    private EditText editTextEnterEmail; //This is only for the demo version

    //Firestore
    private FirebaseFirestore db;



    public TransactionsSummary1() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TransactionsSummary1.
     */
    // TODO: Rename and change types and number of parameters
    public static TransactionsSummary1 newInstance(String param1, String param2) {
        TransactionsSummary1 fragment = new TransactionsSummary1();
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

        //Iniitialize the firestore object
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_transactions_summary1, container, false);


        //Instantiate views
        btnGenerateReport = view.findViewById(R.id.btnTransactionsSummaryReport);
        progressBar = view.findViewById(R.id.determinateBar);
        txtStatus = view.findViewById(R.id.txtStatus);
        editTextEnterEmail = view.findViewById(R.id.editTextEnterEmail);


        //Set onClickListener for generateReport button
        btnGenerateReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Make the keyboard disappear. See https://stackoverflow.com/questions/4841228/after-type-in-edittext-how-to-make-keyboard-disappear
                InputMethodManager mgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                mgr.hideSoftInputFromWindow(editTextEnterEmail.getWindowToken(), 0);

                if(isValid(editTextEnterEmail.getText().toString())){
                    try {
                        progressBar.setVisibility(View.VISIBLE);
                        txtStatus.setVisibility(View.VISIBLE);
                        generateReport();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else {
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

    private void generateReport() throws IOException{
        //good tutorial here: https://www.tutorialspoint.com/apache_poi/index.htm

        //Get all documents in documents collection
        db.collection("transactions")
                .orderBy("date")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            //Create Blank workbook
                            XSSFWorkbook workbook = new XSSFWorkbook();
                            File file = new File(getContext().getExternalFilesDir(null),"createworkbook.xlsx");

                            //Create a spreadsheet with correct name
                            XSSFSheet spreadsheet = workbook.createSheet("Transactions Summary");

                            //Set column widths to fit the column titles almost exactly
                            spreadsheet.setColumnWidth(0,3000);
                            spreadsheet.setColumnWidth(1,5200);
                            spreadsheet.setColumnWidth(2,6000);
                            spreadsheet.setColumnWidth(3,5000);
                            spreadsheet.setColumnWidth(4,3000);
                            spreadsheet.setColumnWidth(5,7000);


                            //Create first row on spreadsheet
                            XSSFRow row = spreadsheet.createRow(0);



                            //Styling for title
                            XSSFCellStyle titleStyle = workbook.createCellStyle();
                            XSSFFont titleFont = workbook.createFont();
                            titleFont.setFontHeightInPoints((short)16);
                            titleFont.setFontName("Segoe UI");
                            XSSFColor titleColor = new XSSFColor(Color.decode("#101b5c"));
                            titleFont.setColor(titleColor);
                            titleStyle.setFont(titleFont);

                            //Title should be on first cell of first row
                            XSSFCell cell = row.createCell(0);
                            cell.setCellStyle(titleStyle);
                            //Add the title
                            cell.setCellValue("Transactions Summary Report- The data in this report is fictional and is for demonstration purposes.");

                            //Set row position for writing next row
                            int rowPosition = 1;
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
                            cell = row.createCell(0);
                            cell.setCellValue("Barcode");
                            cell.setCellStyle(styleColumnHeaders);

                            //Second column header
                            cell = row.createCell(1);
                            cell.setCellValue("Item Name");
                            cell.setCellStyle(styleColumnHeaders);

                            //Third column header
                            cell = row.createCell(2);
                            cell.setCellValue("Employee Name");
                            cell.setCellStyle(styleColumnHeaders);

                            //Fourth column header
                            cell = row.createCell(3);
                            cell.setCellValue("Transaction");
                            cell.setCellStyle(styleColumnHeaders);

                            //Fifth column header
                            cell = row.createCell(4);
                            cell.setCellValue("Purpose");
                            cell.setCellStyle(styleColumnHeaders);

                            //Sixth column header
                            cell = row.createCell(5);
                            cell.setCellValue("Date");
                            cell.setCellStyle(styleColumnHeaders);

                            //Increment row position
                            rowPosition++;

                            //Count for progress bar
                            int count = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                count++;
                                progressBar.setProgress((count/task.getResult().size())*100);

                                //Easier if I capture data in a Transaction object
                                Transaction transactionObject = document.toObject(Transaction.class);

                                //Create a new row
                                row = spreadsheet.createRow(rowPosition);

                                //Styling
                                XSSFCellStyle cellStyle = workbook.createCellStyle();
                                cellStyle.setBorderLeft(BorderStyle.THIN);
                                cellStyle.setBorderRight(BorderStyle.THIN);
                                cellStyle.setBorderBottom(BorderStyle.THIN);
                                XSSFFont cellFont = workbook.createFont();
                                cellFont.setFontName("Segoe UI");
                                cellFont.setFontHeightInPoints((short)12);
                                cellStyle.setFont(cellFont);

                                //First column data
                                cell = row.createCell(0);
                                cell.setCellValue(transactionObject.getBarcode_number());
                                cell.setCellStyle(cellStyle);

                                //Second column data
                                cell = row.createCell(1);
                                cell.setCellValue(transactionObject.getDocument_title());
                                cell.setCellStyle(cellStyle);

                                //Third column data
                                cell = row.createCell(2);
                                cell.setCellValue(transactionObject.getEmployee_name());
                                cell.setCellStyle(cellStyle);

                                //Fourth column data
                                cell = row.createCell(3);
                                cell.setCellValue(transactionObject.getTransaction_type());
                                cell.setCellStyle(cellStyle);

                                //Fifth column data
                                cell = row.createCell(4);
                                cell.setCellValue(transactionObject.getPurpose());
                                cell.setCellStyle(cellStyle);

                                //Sixth column data
                                SimpleDateFormat sfd = new SimpleDateFormat("dd MMMM yyyy HH:mm");
                                String date = sfd.format(transactionObject.getDate().toDate());
                                cell = row.createCell(5);
                                cell.setCellValue(date); //The transaction.getDate returns a firebase timestamp
                                cell.setCellStyle(cellStyle);


                                //Increment row
                                rowPosition++;

                            }

                            //Create file system using specific name
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

                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private String getDateAndTime() {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd MMMM yyyy HH:mm");
        String formattedDate = df.format(c);
        return formattedDate;
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
                        new TransactionsSummary1.EmailTask().execute(attachment);
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

            String dateAndTime;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
            dateAndTime = dateFormat.format(calendar.getTime());

            //Get user data from shared preferences
            SharedPreferences employeeSharedPref = getActivity().getSharedPreferences("EmployeeData",0);

            //Build the body
            String body = "Please find attached the Transactions Summary Report.";

            //Name of attached file
            String fileName = "Transactions_Summary_Report.xlsx"; //TODO: Replace with string resource

            sender.sendMail("Transactions Summary Report",//TODO:Need to replace some of these with either string resources or something else not hardcoded.
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
