package angelzani.onlearn;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SignedCourseActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener { // Калофер

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, dbRefUsers, dbRefCourse, dbRefGroups, dbRefParticipation, dbRefMaterials;
    private Spinner spinner;
    private static final String[] paths = {"item 1", "item 2", "item 3", "item4", "item5"};


    }

@Override
protected void onCreate(Bundle savedInstanceState){
super.onCreate(savedInstanceState);
setContentView(R.layout.activity_signed_course);

Spinner spinner = findViewById(R.id.sp);
ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.materials, android.R.layout.simple_spinner_item);
adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
spinner.setAdapter(adapter);
spinner.setOnItemSelectedListener(this);



}

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text=parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }




















    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_course);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(); //root
        dbRefUsers = mRef.child("users");
        dbRefCourse = mRef.child("course");
        dbRefGroups = mRef.child("groups");
        dbRefParticipation = mRef.child("participation");
        dbRefMaterials = mRef.child("materials");

        // Взимане на подадените стойности от ClientActivity
        Intent intent = getIntent();
    final String courseIdExtra = intent.getStringExtra("courseId");
    final String descriptionExtra = intent.getStringExtra("description");
    final String lecturerIdExtra = intent.getStringExtra("lecturerId");
    final String groupIdExtra = intent.getStringExtra("groupId");


Toast.makeText(getApplicationContext(), "Course name: " + courseIdExtra, Toast.LENGTH_LONG).show();


        // Изтегляне на позволената информация за потребителят, който е създал дисциплината


        dbRefUsers.child(lecturerIdExtra).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Displaying data
                Toast.makeText(getApplicationContext(), "Lecturer name: " + dataSnapshot.getValue(String.class), Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        dbRefUsers.child(lecturerIdExtra).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Displaying data
                Toast.makeText(getApplicationContext(), "Lecturer email: " + dataSnapshot.getValue(String.class), Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Информация за групата
        dbRefGroups.child(courseIdExtra).child(groupIdExtra).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String groupName = dataSnapshot.getKey(); // groupIdExtra
                Long startLongDate = dataSnapshot.child("start").getValue(Long.class);
                Long endLongDate = dataSnapshot.child("end").getValue(Long.class);
                int signedPeople = dataSnapshot.child("current").getValue(Integer.class);
                int maxPeople = dataSnapshot.child("max").getValue(Integer.class);

                Date startDate = new Date(startLongDate), endDate = new Date(endLongDate);

                //Displaying data
                Toast.makeText(getApplicationContext(), "Group name: " + groupName + "\nStart date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(startDate)
                        + "\nEnd date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(endDate)
                        + "\nSigned people: " + signedPeople + "/" + maxPeople, Toast.LENGTH_LONG).show();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Всички айдита на хора, които са записани в тази група в тази дисциплина
        dbRefParticipation.child(courseIdExtra).orderByValue().equalTo(groupIdExtra).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                for(DataSnapshot dataSnapshot : DATA.getChildren()) {
                    final String userID = dataSnapshot.getKey();
                    //Toast.makeText(getApplicationContext(), "ID: " + userID, Toast.LENGTH_SHORT).show();

                    // -----
                    dbRefUsers.child(userID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataName) {

                            final String userName = dataName.getValue(String.class);

                            dbRefUsers.child(userID).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    String userEmail = dataSnapshot.getValue(String.class);

                                    //Displaying data
                                    Toast.makeText(getApplicationContext(), userName + " - " + userEmail, Toast.LENGTH_LONG).show();

                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) { }
                            });

                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) { }
                    });
                    // -----
                }//for loop
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        //Всички материали от дисциплината
        dbRefMaterials.child(courseIdExtra).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                for(DataSnapshot dataSnapshot : DATA.getChildren()) {
                    String materialName = dataSnapshot.getKey();
                    String materialURL = dataSnapshot.getValue(String.class);

                    //Displaying data
                    Toast.makeText(getApplicationContext(), materialName + " - " + materialURL, Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }

        });

    }// края на onCreate()

    //Utility
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        //if(!isConnected) showAlert("Alert", "No internet connection.");
        return isConnected;
    }

    private void setMargins (View v, int left, int top, int right, int bottom) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(left, top, right, bottom);
            v.requestLayout();
        }
    }
}
