package com.solojet.onefoldtv;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
;
import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.SetOptions;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Map;

import config.FirebaseUtils;
import de.hdodenhof.circleimageview.CircleImageView;
import models.Profile;

import static models.ConstantVariables.IS_ADMIN;
import static models.ConstantVariables.PROFILE_PICTURES;
import static models.ConstantVariables.USERS_PATH;
import static models.ConstantVariables.USER_PROFILE;

public class FormFillActivity extends AppCompatActivity {
    private TextView txtWarning;
    private EditText edtPhoneNumber, edtDistrict, edtAssembly, edtBio;
    private CircleImageView imgDp;
    private Button btnSave;
    private CountryCodePicker ccp;
    private RadioGroup rdGroupGender, rdGroupMember;
    private RadioButton rdbYes;
    private Spinner spnField;
    private boolean numberValid;
    private boolean isMember;
    private String userId;
    private SharedPreferences.Editor editor;
    private final Gson gson = new Gson();
    private Uri filePath;
    private ProgressBar prgBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_fill);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Edit profile");
        }

        TextView txtFirstName = findViewById(R.id.txtFirstName);
        TextView txtLastName = findViewById(R.id.txtLastName);
        txtWarning = findViewById(R.id.txtWarning);
        edtBio = findViewById(R.id.edtBio);
        edtPhoneNumber = findViewById(R.id.editText_carrierNumber);
        edtDistrict = findViewById(R.id.edtDistrict);
        edtAssembly = findViewById(R.id.edtAssembly);
        prgBar = findViewById(R.id.prgLogin);

        imgDp = findViewById(R.id.imgDp);
        btnSave = findViewById(R.id.btnSave);
        spnField = findViewById(R.id.spnFieldType);

        ccp = findViewById(R.id.ccp);
        ccp.registerCarrierNumberEditText(edtPhoneNumber);
        rdGroupGender = findViewById(R.id.rdbGroupGender);
        rdGroupMember = findViewById(R.id.rdbGroupMember);
        rdbYes = findViewById(R.id.rdbYes);
        RadioButton rdbNo = findViewById(R.id.rdbNo);
        RadioButton rdbMale = findViewById(R.id.rdbMale);
        RadioButton rdbFemale = findViewById(R.id.rdbFemale);

        CardView crdField = findViewById(R.id.crdField);
        userId = FirebaseUtils.getAuth().getCurrentUser().getUid();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = prefs.edit();
        String json = prefs.getString(USER_PROFILE, "");
        Profile profile = (json.equals("")) ? null : gson.fromJson(json, Profile.class);
        if(profile !=null) {
            txtFirstName.setText(profile.getFirstName());
            txtLastName.setText(profile.getLastName());

            if(profile.getImageUrl().isEmpty())
                Glide.with(this).load(getResources().getDrawable(R.drawable.blank_profile_pic)).into(imgDp);
            else
                Glide.with(this).load(profile.getImageUrl()).into(imgDp);
        }

        String callerActivity = getIntent().getStringExtra("caller");
        if(callerActivity!=null && callerActivity.equals("profileActivity") && profile != null){
            edtBio.setText(profile.getBio());
            ccp.setFullNumber(profile.getPhone());

            if(profile.getGender().equals("male"))
                rdbMale.toggle();
            else
                rdbFemale.toggle();

            isMember = profile.isMember();
            if(isMember){
                rdbYes.toggle();
                edtDistrict.setText(profile.getDistrict());
                edtAssembly.setText(profile.getAssembly());

                switch (profile.getField()){
                    case "Uyo Field":
                        spnField.setSelection(1);
                        break;
                    case "Ikot Ekpene Field":
                        spnField.setSelection(2);
                        break;
                    case "Eket Field":
                        spnField.setSelection(3);
                        break;
                    case "Oron Field":
                        spnField.setSelection(4);
                        break;
                    case "Ikot Abasi Field":
                        spnField.setSelection(5);
                        break;
                    case "Etinan Field":
                        spnField.setSelection(6);
                        break;
                    case "Ikono Field":
                        spnField.setSelection(7);
                        break;
                    default:
                        spnField.setSelection(0);
                }
            }
            else{
                rdbNo.toggle();
                crdField.setVisibility(View.GONE);
            }
        }

        rdGroupMember.setOnCheckedChangeListener((radioGroup, i) -> {
            if(rdbYes.isChecked()){
                isMember = true;
                crdField.setVisibility(View.VISIBLE);
            }
            else{
                isMember = false;
                crdField.setVisibility(View.GONE);
            }
        });

        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> numberValid =isValidNumber);

        spnField.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void openCropper(View view) {
        CropImage.activity().setAspectRatio(1,1).start(this);
    }

    public void updateProfile(View v){
        String phone = ccp.getFullNumber();
        String country = ccp.getSelectedCountryName();
        String gender="";
        String field, district, assembly;
        String bio = edtBio.getText().toString();

        switch (rdGroupGender.getCheckedRadioButtonId()) {
            case R.id.rdbMale:
                gender = "male";
                break;
            case R.id.rdbFemale:
                gender = "female";
                break;
        }

        //verify fields meet requirement
        if(TextUtils.isEmpty(phone)){
            edtPhoneNumber.setError("Enter Phone number");
            txtWarning.setText("Enter Phone number");
            txtWarning.setVisibility(View.VISIBLE);
            return;
        }
        if(!numberValid){
            edtPhoneNumber.setError("Phone number incorrect");
            txtWarning.setText("Phone number incorrect");
            txtWarning.setVisibility(View.VISIBLE);
            return;
        }
        if(TextUtils.isEmpty(gender)){
            Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show();
            txtWarning.setText("Select gender");
            txtWarning.setVisibility(View.VISIBLE);
            return;
        }

        int i = rdGroupMember.getCheckedRadioButtonId();
        if (i==-1){
            Toast.makeText(this, "Select membership", Toast.LENGTH_SHORT).show();
            txtWarning.setText("Select membership");
            txtWarning.setVisibility(View.VISIBLE);
            return;
        }
        if(isMember){
            field = spnField.getSelectedItem().toString();
            district = edtDistrict.getText().toString();
            assembly = edtAssembly.getText().toString();

            if(field.equals("-- Select Field --")){
                txtWarning.setText("Select your field");
                txtWarning.setVisibility(View.VISIBLE);
                return;
            }
            if(TextUtils.isEmpty(district)){
                edtDistrict.setError("Enter your district");
                txtWarning.setText("Enter your district");
                txtWarning.setVisibility(View.VISIBLE);
                return;
            }
            if(district.length() < 3){
                edtDistrict.setError("District too short");
                txtWarning.setText("District too short");
                txtWarning.setVisibility(View.VISIBLE);
                return;
            }

            if(TextUtils.isEmpty(assembly)){
                edtAssembly.setError("Enter your assembly");
                txtWarning.setText("Enter your assembly");
                txtWarning.setVisibility(View.VISIBLE);
                return;
            }
            if(assembly.length() < 3){
                edtAssembly.setError("Assembly too short");
                txtWarning.setText("Assembly too short");
                txtWarning.setVisibility(View.VISIBLE);
                return;
            }
        }
        else{
            field = "";
            district = "";
            assembly = "";
        }

        //Map new user datails, and ready to save to db
        Map<String, Object> update = new HashMap<>();
        update.put("phone", phone);
        update.put("country", country);
        update.put("gender", gender);
        update.put("field", field);
        update.put("district", district);
        update.put("assembly", assembly);
        update.put("member", isMember);
        update.put("bio", bio);

        btnSave.setEnabled(false);
        prgBar.setVisibility(View.VISIBLE);

        if(filePath!=null)
            FirebaseUtils.getStorage().getReference().child(PROFILE_PICTURES).child(userId).putFile(filePath)
                .addOnSuccessListener(taskSnapshot -> taskSnapshot.getMetadata().getReference().getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            String url = uri.toString();
                            update.put("imageUrl", url);
                            FirebaseUtils.getDatabase().collection(USERS_PATH).document(userId).set(update, SetOptions.merge())
                                    .addOnSuccessListener(FormFillActivity.this, aVoid -> FirebaseUtils.getDatabase()
                                            .collection(USERS_PATH).document(userId).get().addOnCompleteListener(task -> {
                                                if(task.isSuccessful()&&task.getResult()!=null){
                                                    Profile profile = task.getResult().toObject(Profile.class);
                                                    String profileJson = gson.toJson(profile);
                                                    editor.putString(USER_PROFILE, profileJson);
                                                    editor.putBoolean(IS_ADMIN, profile.isAdmin());
                                                    editor.apply();
                                                    btnSave.setEnabled(true);
                                                    prgBar.setVisibility(View.GONE);
                                                    openDialog();
                                                }
                                            }))
                                    .addOnFailureListener(FormFillActivity.this, e -> {
                                txtWarning.setText(e.getMessage());
                                btnSave.setEnabled(true);
                                prgBar.setVisibility(View.GONE);
                            });
                        }))
                .addOnFailureListener(e -> {
                    txtWarning.setText(e.getMessage());
                    btnSave.setEnabled(true);
                    prgBar.setVisibility(View.GONE);
                })
                .addOnProgressListener(taskSnapshot -> {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                            .getTotalByteCount());
                });
        else
            FirebaseUtils.getDatabase().collection(USERS_PATH).document(userId).set(update, SetOptions.merge())
                .addOnSuccessListener(FormFillActivity.this, aVoid -> FirebaseUtils.getDatabase()
                        .collection(USERS_PATH).document(userId).get().addOnCompleteListener(task -> {
                    if(task.isSuccessful()&&task.getResult()!=null){
                        Profile profile = task.getResult().toObject(Profile.class);
                        String profileJson = gson.toJson(profile);
                        editor.putString(USER_PROFILE, profileJson);
                        editor.putBoolean(IS_ADMIN, profile.isAdmin());
                        editor.apply();
                        btnSave.setEnabled(true);
                        prgBar.setVisibility(View.GONE);
                        openDialog();
                    }
                })).addOnFailureListener(FormFillActivity.this, e -> {
                    txtWarning.setText(e.getMessage());
                    btnSave.setEnabled(true);
                    prgBar.setVisibility(View.GONE);
                });

    }

    private void openDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(FormFillActivity.this);
        LayoutInflater inflater = LayoutInflater.from(FormFillActivity.this);
        View dialogView = inflater.inflate(R.layout.dialog_profile_update, null);
        builder.setView(dialogView);
        final AlertDialog dialog= builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false);
        dialog.show();

        Button btnContinue = dialog.findViewById(R.id.btnContinue);
        btnContinue.setOnClickListener(view -> {
            finish();
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                filePath = result.getUri();
                Glide.with(this).load(filePath).into(imgDp);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Failed. "+ error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
