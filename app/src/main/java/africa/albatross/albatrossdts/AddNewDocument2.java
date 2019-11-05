package africa.albatross.albatrossdts;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import africa.albatross.albatrossdts.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import id.zelory.compressor.Compressor;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddNewDocument2.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddNewDocument2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddNewDocument2 extends Fragment {
    private static final String TAG = "Camera";
    private static final int MY_CAMERA_REQUEST_CODE = 100;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    //private String mParam1;
    //private String mParam2;

    private OnFragmentInteractionListener mListener;

    //Back button
    private Button btnBack;

    //Next button
    private Button btnNext;

    //Take Photo button
    private Button btnTakePhoto;

    //Image viewd
    private ImageView imgDocumentPhoto;

    //Progress bar
    private ProgressBar progressBar;

    //TextView
    private TextView txtPhotoUrl;

    private static final int REQUEST_TAKE_PHOTO = 1;

    private String currentPhotoPath;

    private String uploadedPhotoURL;

    FirebaseStorage storage;

    public AddNewDocument2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddNewDocument2.
     */
    // TODO: Rename and change types and number of parameters
    public static AddNewDocument2 newInstance(String param1, String param2) {
        AddNewDocument2 fragment = new AddNewDocument2();
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

        //Initialize firebase storage
        storage = FirebaseStorage.getInstance();

        //Placeholder for uploaded photo url
        //If the shared preference holder has a value, use that because the user might have already
        //uploaded but came back here. Otherwise use a default photo.
        SharedPreferences sharedPref = getContext().getSharedPreferences("AddNewDocumentData",0);
        String link = sharedPref.getString("photo_url","");
        if(link.equals("")){
            uploadedPhotoURL="https://firebasestorage.googleapis.com/v0/b/albatrossdts.appspot.com/o/images%2Ffolder-303891_640.png?alt=media&token=34966164-09db-42f9-b0d3-4356d852ed73"; //TODO: This is a temporary placeholder. Replace.
        }else{
            uploadedPhotoURL= link;
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_new_document2, container, false);
        btnBack = view.findViewById(R.id.btnBack2AddNew);
        btnNext = view.findViewById(R.id.btnNext2AddNew);
        btnTakePhoto = view.findViewById(R.id.btnTakePhoto);
        imgDocumentPhoto = view.findViewById(R.id.imgDocumentPhoto);
        progressBar = view.findViewById(R.id.progressBarDocumentPhoto);
        txtPhotoUrl = view.findViewById(R.id.txtPhotoUrl);


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

        //Set OnClick for photo button
        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Check for camera permissions before launching intent (https://stackoverflow.com/questions/43042725/revoked-permission-android-permission-camera/43070198)

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Permission is not granted
                    requestPermissions(new String[] {Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
                }
                else{
                    dispatchTakePictureIntent();
                }

            }
        });

        Glide.with(getContext())
                .asBitmap()
                .load(uploadedPhotoURL)
                .into(imgDocumentPhoto);

        //Add onclick listener to imageview to allow user to
        //open the image url in a browser.
        imgDocumentPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(uploadedPhotoURL));
                startActivity(intent);
            }
        });
        //Make it invisible for now. It'll be made visible after upload and glide is complete below.
        //imgDocumentPhoto.setVisibility(View.INVISIBLE);


        // Inflate the layout for this fragment
        return view;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_REQUEST_CODE) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "Camera permission denied", Toast.LENGTH_LONG).show();
            }

        }
    }

    private void goNext() {
        //Add the entered values into shared preferences, then load the AddNewDocument3 fragment.

        SharedPreferences sharedPref = getContext().getSharedPreferences("AddNewDocumentData",0);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("photo_url",uploadedPhotoURL);
        editor.apply();

        launchNextFragment();

    }

    private void launchNextFragment() {
        //Replaces the fragment in the frame_layout in app_bar_main.xml

        //The next fragment to launch
        //AddNewDocument3 fragment = AddNewDocument3.newInstance(null,null);

        //See solution at https://stackoverflow.com/questions/13216916/how-to-replace-the-activitys-fragment-from-the-fragment-itself/13217087
        ((MainActivity)getActivity()).replaceFragment("AddNewDocument3",true);
    }

    private void goBack() {
        //launch the previous fragment, AddNewDocument1
        AddNewDocument1 fragment = AddNewDocument1.newInstance(null,null);
        ((MainActivity)getActivity()).replaceFragment("AddNewDocument1",true);

    }

    private void dispatchTakePictureIntent() {
        //see https://developer.android.com/training/camera/photobasics
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "africa.albatross.albatrossdts.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                Log.i(TAG,"About to start camera intent.");
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        //See https://developer.android.com/training/camera/photobasics
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == getActivity().RESULT_OK) { //See https://stackoverflow.com/questions/23085100/onactivityresult-result-ok-can-not-be-resolved-to-a-variable-in-android
            //Start an asynctask that compresses the photo and then calls the uploadPhoto function. Using asynctask because I understand compression is intensive and shouldn't be done in main thread.
            //Disable btnNext to prevent the user clicking while photo is being uploaded
            btnNext.setEnabled(false);
            new ImageCompressionTask().execute(new File(currentPhotoPath));
        }
    }

    private void uploadPhoto(File theFile) {
        //Uploads the photo to firebase storage
        //https://firebase.google.com/docs/storage/android/upload-files

        //Display the progress bar
        progressBar.setVisibility(View.VISIBLE);

        StorageReference storageRef = storage.getReference();

        Uri file = Uri.fromFile(theFile);
        final StorageReference photoRef = storageRef.child("images/"+file.getLastPathSegment());
        UploadTask uploadTask = photoRef.putFile(file);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                //Get the url of the photo
                photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(final Uri uri) { //On success, load the image into the image view using glide.
                        //Load the image into the imageview
                        Glide.with(getContext())
                                .asBitmap()
                                .load(uri.toString())
                                .listener(new RequestListener<Bitmap>() {//This is an oncomplete listener that allows me to make the progress bar invisible once glide completes
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                        //Set the uploadedPhotoURL. This is used in the onClickListener for the imageview
                                        uploadedPhotoURL = uri.toString();

                                        imgDocumentPhoto.setVisibility(View.VISIBLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                        //Re-enable btnNext
                                        btnNext.setEnabled(true);

                                        return false;
                                    }
                                })
                                .into(imgDocumentPhoto);



                    }
                });
            }
        });

    }

    private class ImageCompressionTask extends AsyncTask<File,Void,File> {
        //Good explanation of AsyncTask here: https://stackoverflow.com/questions/9671546/asynctask-android-example
        //See also the following for an error I encountered when changing to File return type: https://stackoverflow.com/questions/33616123/asynctask-doinbackgroundstring-clashes-with-doinbackgroundparams
        @Override
        protected File doInBackground(File... files) {
            File compressedImage = compressImage(files[0]);
            return compressedImage;
        }

        @Override
        protected void onPostExecute(File file) {
            //When it's done compressing, it will run the below in the main UI thread
            super.onPostExecute(file);
            uploadPhoto(file);
        }
    }

    private File compressImage(File file) {
        File compressedImageFile = null;
        try {
            compressedImageFile = new Compressor(getContext()).compressToFile(file);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return compressedImageFile;

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
