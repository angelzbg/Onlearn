package angelzani.onlearn;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CourseActivity extends AppCompatActivity { // Даниел


    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, dbRefUsers, dbRefCourse, dbRefGroups, dbRefParticipation;

    private TextView courseName;
    private TextView desc;
    private TextView lectureName;
    private TextView lectureEmail;
    private LinearLayout groups;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(); //root
        dbRefUsers=mRef.child("users");
        dbRefCourse=mRef.child("course");
        dbRefGroups=mRef.child("groups");
        dbRefParticipation=mRef.child("participation");

        courseName=findViewById(R.id.course_TV_courseName);
        desc=findViewById(R.id.course_TV_desc);
        lectureName=findViewById(R.id.course_TV_lectureName);
        lectureEmail=findViewById(R.id.course_TV_lectureEmail);
        groups=findViewById(R.id.course_LL_groups);

        Intent intent=getIntent();

        String courseId =intent.getStringExtra("courseId");
        String description=intent.getStringExtra("description");
        String lecturerId=intent.getStringExtra("lecturerId");

        courseName.setText(courseId);
        desc.setText(description);

        dbRefUsers.child(lecturerId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                lectureName.setText(name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        dbRefUsers.child(lecturerId).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.getValue(String.class);
                lectureEmail.setText(email);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        long millis = System.currentTimeMillis();
        Query query1 = dbRefGroups.child(courseId).orderByChild("start").startAt(millis);
        query1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String groupName = dataSnapshot.getKey();
                int currentNumber= dataSnapshot.child("current").getValue(Integer.class);
                int max= dataSnapshot.child("max").getValue(Integer.class);
                Long start=dataSnapshot.child("start").getValue(Long.class);
                Long end=dataSnapshot.child("end").getValue(Long.class);

                ConstraintLayout groupLayout =  new ConstraintLayout(getApplicationContext());
                groupLayout.setId(View.generateViewId());
                groupLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                groups.addView(groupLayout);

                TextView groupNameTV = new TextView(getApplicationContext());
                groupNameTV.setId(View.generateViewId());
                groupLayout.addView(groupNameTV);
                groupNameTV.setText(groupName);



                ConstraintSet cs = new ConstraintSet();
                cs.clone(groupLayout);
                cs.connect(groupNameTV.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                cs.connect(groupNameTV.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

                cs.applyTo(groupLayout);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }//end onCreate()








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
