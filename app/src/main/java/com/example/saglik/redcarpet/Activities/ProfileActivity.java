package com.example.saglik.redcarpet.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;
import com.example.saglik.redcarpet.Classes.User;
import com.example.saglik.redcarpet.Database.DatabaseWriter;
import com.example.saglik.redcarpet.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileActivity extends AppCompatActivity {
    private static final String REQUIRED = "REQUIRED";
    private EditText nameText;
    private EditText locationText;
    private Switch adminSwitch;
    private Switch privacySwitch;
    private RatingBar ratingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setTitle("Profile Settings");

        nameText = findViewById(R.id.editText1);
        locationText = findViewById(R.id.editText2);
        adminSwitch = findViewById(R.id.switch1);
        privacySwitch = findViewById(R.id.switch2);
        ratingBar = findViewById(R.id.ratingBar);

        setEditTextViews();
    }

    public void saveAndProceed(View view){
        String nickname = nameText.getText().toString();
        String location = locationText.getText().toString();
        boolean isAdmin = adminSwitch.isChecked();
        boolean isPrivate = privacySwitch.isChecked();
        if (TextUtils.isEmpty(nickname)) {
            nameText.setError(REQUIRED);
            return;
        }
        // Body is required
        if (TextUtils.isEmpty(location)) {
            locationText.setError(REQUIRED);
            return;
        }
        User userDB = new User(nickname,location, isPrivate, isAdmin);
        DatabaseWriter dbWriter = new DatabaseWriter();
        dbWriter.registerUser(userDB);
        Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
        startActivity(intent);
    }

    private void setEditTextViews(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userID = currentUser.getUid();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("users/" + userID + "/");
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user==null)
                    return;
                String nickname = user.getNickname();
                String location = user.getLocation();
                String hidden = dataSnapshot.child("isPrivate").getValue().toString();
                boolean isAdmin = user.isAdmin();
                nameText.setText(nickname);
                locationText.setText(location);
                if (hidden.equals("true"))
                    privacySwitch.setChecked(true);
                else
                    privacySwitch.setChecked(false);
                adminSwitch.setChecked(isAdmin);
                if (isAdmin) {
                    ratingBar.setVisibility(View.VISIBLE);
                    ratingBar.setRating(user.getRating());
                } else {
                    ratingBar.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
