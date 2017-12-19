package com.example.saglik.redcarpetapp.Activity;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.saglik.redcarpetapp.Classes.Party;
import com.example.saglik.redcarpetapp.Classes.User;
import com.example.saglik.redcarpetapp.Database.DatabaseWriter;
import com.example.saglik.redcarpetapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class PartyCheckinActivity extends AppCompatActivity {

    TextView locationView;
    TextView dateView;
    TextView infoView;
    TextView organizerView;
    ImageView imageView;
    Button registerButton;
    Party currentParty = new Party();
    User user = new User();
    DatabaseReference mDatabase;
    String partyName;
    boolean isParticipant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_party_checkin);

        Intent intent = getIntent();
        partyName = intent.getStringExtra("partyName");
        setTitle(partyName);

        organizerView = findViewById(R.id.organizerView);
        locationView = findViewById(R.id.locationView);
        dateView = findViewById(R.id.dateView);
        infoView = findViewById(R.id.infoView);
        imageView = findViewById(R.id.imageView);
        registerButton = findViewById(R.id.registerButton);



        mDatabase = FirebaseDatabase.getInstance().getReference("parties/"+partyName+"/");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                FirebaseUser userA = mAuth.getCurrentUser();
                String userID = userA.getUid();
                currentParty = dataSnapshot.getValue(Party.class);
                currentParty.setKey(dataSnapshot.getKey());
                isParticipant = dataSnapshot.child("participants").child(userID).exists();
                populateViews();
                setRegisterButton();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        //Open googlemaps on click
        locationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String map = "http://maps.google.co.in/maps?q=" + currentParty.getLocation();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
                startActivity(intent);
            }
        });
        //Register user to this party
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String partyID = currentParty.getKey();
                DatabaseWriter dbWriter = new DatabaseWriter();
                if(registerButton.getText().equals("REGISTER")){
                    dbWriter.addParticipantToParty(partyID);
                    registerButton.setText("UNREGISTER");
                }
                else if(registerButton.getText().equals("UNREGISTER")){
                    dbWriter.removeParticipanFromParty(partyID);
                    registerButton.setText("REGISTER");
                }

            }
        });
    }

    public void setRegisterButton(){
        if(isParticipant)
            registerButton.setText("UNREGISTER");
        else
            registerButton.setText("REGISTER");
    }

    private void populateViews() {
        organizerView.setText("Event Organizer: "+currentParty.getOrganizer());
        locationView.setText("Location(Click to navigate): "+currentParty.getLocation());
        dateView.setText("Event Date: "+currentParty.getDate());
        infoView.setText("Event Info: "+currentParty.getInfo());
//        Picasso.with(PartyCheckinActivity.this).load(currentParty.getImageLink()).resize(200,200).centerCrop().into(imageView);
        Picasso.with(PartyCheckinActivity.this).load(currentParty.getImageLink()).fit().centerCrop()
                .placeholder(R.drawable.downloading) //image that you show during the download
                .error(R.drawable.downloaderror) //image error if doesn't download it correctly
                .into(imageView); //your imageView
    }

}
