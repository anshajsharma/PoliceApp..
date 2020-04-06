package com.sih.policeapp;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import de.hdodenhof.circleimageview.CircleImageView;


public class AddNewCriminalDetails extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "AddNewCriminalDetails";
    private EditText DOB,DOC;
    private int c=0;

    String imageUri;
    CircleImageView circleImageView;
    // array lists
    // for the spinner in the format : City_no : City , State. Eg : 144 : New Delhi , India
    ArrayList<String> listSpinner=new ArrayList<String>();
    // to store the city and state in the format : City , State. Eg: New Delhi , India
    ArrayList<String> listAll=new ArrayList<String>();
    // for listing all states
    ArrayList<String> listState=new ArrayList<String>();
    // for listing all cities
    ArrayList<String> listCity=new ArrayList<String>();
    // access all auto complete text views

    ArrayList<String> MainCrimesType = new ArrayList<>();

    Map<String , ArrayList<String>> stateDistrict;
    Map<String , ArrayList<String>> districtLocality = new HashMap<>();
    Map<String , Long > localityPincode = new HashMap<>();
    private ImageView img_DOB,img_DOC;

    ArrayAdapter<CharSequence> adapter;

    AutoCompleteTextView act,state,dist;
    Spinner spinner;
    private Button addCriminal;
    private TextView criminalAddress,crimeAddress,criminalName,bodyMark,crimeRating,crimeType;
    private ProgressDialog mProgressDialog;
    private Bitmap bitmap;
    private int error=0;
    private RatingBar ratingBar;
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_criminal_details);
        circleImageView = findViewById(R.id.new_criminal_pic);
        stateDistrict = new HashMap<>();
        state = findViewById(R.id.state);
        DOB= findViewById(R.id.DOB);
        DOC = findViewById(R.id.DOC);
        img_DOB = findViewById(R.id.img_DOB);
        img_DOC=findViewById(R.id.img_DOC);
        dist = findViewById(R.id.district);
        mProgressDialog = new ProgressDialog(this);
        criminalAddress = findViewById(R.id.address_of_criminal);
        crimeAddress = findViewById(R.id.crime_address);
        criminalName =findViewById(R.id.criminal_name);
        bodyMark = findViewById(R.id.body_mark);
        crimeType = findViewById(R.id.crime_type);
        addCriminal =findViewById(R.id.add_criminal);
        spinner = findViewById(R.id.main_crime_type);
        ratingBar = findViewById(R.id.rating_of_crime2);

        ratingBar.setRating(2.0f);



        adapter = ArrayAdapter.createFromResource(this,R.array.MainCrimeTypes,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);



        imageUri = getIntent().getStringExtra("image");
        Picasso.with(getApplicationContext())
                .load(imageUri)
                .placeholder(R.drawable.avtar)
                .into(circleImageView);
       // spinner=findViewById(R.id.spCity);
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(imageUri));
        } catch (IOException e) {
            error = 1;
            e.printStackTrace();
        }
        callAll();
        setOnClicks();


        }
    public void callAll()
    {
        obj_list();
        addState();
    }
    private void setOnClicks()
    {
        DOC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c=1;
                showDatePickerDialog();
            }
        });
        DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c=0;
                showDatePickerDialog();
            }
        });
        img_DOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c=0;
                showDatePickerDialog();
            }
        });
        img_DOC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                c=1;
                showDatePickerDialog();
            }
        });
        addCriminal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String criminal_id,criminal_name,criminal_address,criminals_DOB,criminal_BodyMark ,criminal_rating;
                final String crime_id,crime_adder_authority_Id,district_of_crime,
                        state_of_crime,date_of_crime,case_status,crime_type,address_of_crime ,rating_of_crime;
                final String time_when_crime_added , main_crime_type;

                final DatabaseReference mRootRef , mCrime_Ref , mCriminalRef ;
                mRootRef = FirebaseDatabase.getInstance().getReference();
                mCrime_Ref = mRootRef.child("crime_ref");
                mCriminalRef = mRootRef.child("criminal_ref");

                criminal_id = mRootRef.child("criminal_id_creation").push().getKey();
                criminal_address = criminalAddress.getText().toString().trim();
                criminals_DOB = DOB.getText().toString().trim();
                criminal_BodyMark = bodyMark.getText().toString().trim();
                criminal_name = criminalName.getText().toString().trim();
                main_crime_type = spinner.getSelectedItem().toString().trim();
                int flag = 0;
                int temp=0;


                if(main_crime_type.equals("Select Crime Type"))
                {
                    ((TextView)spinner.getSelectedView()).setError("Please Select Crime Type");
                    flag=1;
                }else{
                    ((TextView)spinner.getSelectedView()).setError(null);
                }

                if(criminal_address.equals("") || criminal_address.length()<10)
                {
                    criminalAddress.setError("Plz enter full address of criminal");
                    flag = 1;
                }else{
                    criminalAddress.setError(null);

                }
                if(criminals_DOB.equals(""))
                {
                    DOB.setError("Plz enter full address of criminal");
                    flag = 1;
                }else{
                    DOB.setError(null);

                }
                if(criminal_BodyMark.equals(""))
                {
                    bodyMark.setError("Plz enter criminal's body mark");
                    flag = 1;

                }else{
                    bodyMark.setError(null);

                }
                if(criminal_name.equals(""))
                {
                    criminalName.setError("Plz enter criminal's name");
                    flag = 1;
                }else{
                    criminalName.setError(null);

                }


                final StorageReference storageRef;


                crime_id = mRootRef.child("crime_id_creation").push().getKey();
                date_of_crime = DOC.getText().toString().trim();
                state_of_crime = state.getText().toString().trim();
                district_of_crime = dist.getText().toString().trim();
                address_of_crime = crimeAddress.getText().toString().trim();
                time_when_crime_added = String.valueOf(System.currentTimeMillis());
                float rat = ratingBar.getRating();
                rating_of_crime = String.valueOf(rat).trim();

               // Toast.makeText(AddNewCriminalDetails.this,  rating_of_crime, Toast.LENGTH_SHORT).show();

                criminal_rating = rating_of_crime;
                if(FirebaseAuth.getInstance().getCurrentUser() != null)
                {
                    crime_adder_authority_Id = FirebaseAuth.getInstance().getUid();
                }
                else{
                    crime_adder_authority_Id = "NULL";
                }
                case_status = "Pending";
                crime_type = crimeType.getText().toString().trim();




                if(crime_type.equals(""))
                {
                    crimeType.setError("Plz enter type of crime or name of crime");
                    flag = 1;
                }else{
                    crimeType.setError(null);

                }
                if(address_of_crime.equals("") || address_of_crime.length()<10)
                {
                    crimeAddress.setError("Plz enter full address of crime");
                    flag = 1;
                }else{
                    crimeAddress.setError(null);

                }
                if(date_of_crime.equals(""))
                {
                    DOC.setError("Plz enter date of crime"); flag = 1;
                }else{
                    DOC.setError(null);

                }
                if(rating_of_crime.equals(String.valueOf(1.0f)) ||rating_of_crime.equals(String.valueOf(2.0f)) ||rating_of_crime.equals(String.valueOf(3.0f))
                        ||rating_of_crime.equals(String.valueOf(4.0f)) ||rating_of_crime.equals(String.valueOf(5.0f)))
                {
                    // ratingBar.setError(null);

                }else{
                    flag = 1;
                    Toast.makeText(AddNewCriminalDetails.this, "Enter rating between 1 to 5" + rating_of_crime, Toast.LENGTH_SHORT).show();
                }
                if(!listState.contains(state_of_crime))
                {
                    state.setError("Plz enter valid State");
                    temp=1; flag = 1;
                }else{
                    state.setError(null);

                }
                dist_list(state_of_crime);
                if(!listCity.contains(district_of_crime))
                {
                   if(temp==1) dist.setError("Plz enter valid District");
                   else dist.setError("District entered is Invalid or does not lie in " + state_of_crime);
                    flag = 1;
                }else{
                    dist.setError(null);

                }



                if(flag==0)
                {

                    mProgressDialog.setTitle("Uploading");
                    mProgressDialog.setMessage("Please wait while we upload your data.");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();

                    assert criminal_id != null;
                    storageRef = FirebaseStorage.getInstance().getReference().child(criminal_id).child("profile_pic.jpg");
                    storageRef.putFile(Uri.parse(imageUri)).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String profile_pic_url;
                                        profile_pic_url = uri.toString();
                                        String res = main_crime_type + "( " + crime_type + "in" + district_of_crime + "," + state_of_crime + " )" ;
                                        HashMap<String, String> CriminalMap = new HashMap<>();
                                        CriminalMap.put("criminal_id",criminal_id);
                                        CriminalMap.put("criminal_name",criminal_name);
                                        CriminalMap.put("criminal_address",criminal_address);
                                        CriminalMap.put("criminals_DOB",criminals_DOB);
                                        CriminalMap.put("criminal_BodyMark",criminal_BodyMark);
                                        CriminalMap.put("criminal_rating",criminal_rating);
                                        CriminalMap.put("profile_pic_url",profile_pic_url);
                                        CriminalMap.put("last_crime",crime_id);
                                        mCriminalRef.child(criminal_id).setValue(CriminalMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                                HashMap<String, String> CrimeMap = new HashMap<>();
                                                CrimeMap.put("crime_id",crime_id);
                                                CrimeMap.put("criminal_id",criminal_id);
                                                CrimeMap.put("crime_adder_authority_Id",crime_adder_authority_Id);
                                                CrimeMap.put("district_of_crime",district_of_crime);
                                                CrimeMap.put("state_of_crime",state_of_crime);
                                                CrimeMap.put("date_of_crime",date_of_crime);
                                                CrimeMap.put("case_status",case_status);
                                                CrimeMap.put("crime_type",crime_type);
                                                CrimeMap.put("address_of_crime",address_of_crime);
                                                CrimeMap.put("rating_of_crime",rating_of_crime);
                                                CrimeMap.put("time_when_crime_added",time_when_crime_added);
                                                CrimeMap.put("main_crime_type",main_crime_type);
                                                assert crime_id != null;
                                                mCrime_Ref.child(crime_id).setValue(CrimeMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        mRootRef.child("criminal_crimes_relation_ref").child(criminal_id).child(crime_id).setValue(time_when_crime_added).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(AddNewCriminalDetails.this, "Successfully Added!", Toast.LENGTH_SHORT).show();
                                                                mProgressDialog.hide();
                                                                Intent intent = new Intent(AddNewCriminalDetails.this , AddCriminalActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });


                                                    }
                                                });
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });




                }




            }
        });
    }


    // Get the content of cities.json from assets directory and store it as string
    public String getJson()
    {
        String json=null;
        try
        {
            // Opening cities.json file
            InputStream is = getAssets().open("cities.json");
            // is there any content in the file
            int size = is.available();
            byte[] buffer = new byte[size];
            // read values in the byte array
            is.read(buffer);
            // close the stream --- very important
            is.close();
            // convert byte to string
            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
            return json;
        }
        return json;
    }

    // This add all JSON object's data to the respective lists
    void obj_list()
    {
        // Exceptions are returned by JSONObject when the object cannot be created
        try
        {
            // Convert the string returned to a JSON object
            JSONObject jsonObject=new JSONObject(getJson());
            // Get Json array
            JSONArray array=jsonObject.getJSONArray("array");
            // Navigate through an array item one by one
            for(int i=0;i<array.length();i++)
            {
                // select the particular JSON data
                JSONObject object=array.getJSONObject(i);
                String city=object.getString("name");
                String state=object.getString("state");
                // add to the lists in the specified format
                listSpinner.add(String.valueOf(i+1)+" : "+city+" , "+state);
                listAll.add(city+" , "+state);
                listCity.add(city);
                listState.add(state);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    void dist_list(String s)
    {
        // Exceptions are returned by JSONObject when the object cannot be created
        try
        {
            // Convert the string returned to a JSON object
            JSONObject jsonObject=new JSONObject(getJson());
            // Get Json array
            listCity.clear();
            JSONArray array=jsonObject.getJSONArray("array");
            // Navigate through an array item one by one
            for(int i=0;i<array.length();i++)
            {
                JSONObject object=array.getJSONObject(i);
                String city=object.getString("name");
                String state=object.getString("state");
                if(state.equals(s))
                listCity.add(city);
            }
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
    // The third auto complete text view
    void addState()
    {
        Set<String> set = new HashSet<String>(listState);
        act=(AutoCompleteTextView)findViewById(R.id.state);
        adapterSetting(new ArrayList(set));
    }

    // setting adapter for auto complete text views
    void adapterSetting(ArrayList arrayList)
    {
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,arrayList);
        act.setAdapter(adapter);
        hideKeyBoard();
    }

    // hide keyboard on selecting a suggestion
    public void hideKeyBoard()
    {
            act.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    dist_list(state.getText().toString());
                    Log.i(TAG, "onItemClick: "+ act.getText().toString());
                    act=findViewById(R.id.district);
                    adapterSetting(listCity);
                }
            });


    }

    private void showDatePickerDialog()
    {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        );
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }


    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int date_of_month) {
        String day,mon,date;
        month = month + 1;
        day = String.valueOf(date_of_month);
        mon = String.valueOf(month);
       int mYear = Calendar.getInstance().get(Calendar.YEAR);
        int mMonth     =   Calendar.getInstance().get(Calendar.MONTH);
        int m     =  Calendar.getInstance().get(Calendar.DAY_OF_MONTH);


        if(day.length()==1) day = "0"+day;
        if(mon.length()==1) mon = "0"+mon;
        date = day + "/" + mon + "/" + year ;
      if(c==0)
      {
          DOB.setText(date);
      }
        if(c==1)
        {
            DOC.setText(date);
        }
    }
}
