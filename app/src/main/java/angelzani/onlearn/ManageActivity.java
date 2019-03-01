package angelzani.onlearn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

public class ManageActivity extends AppCompatActivity { // Марин
    /* Трябва да може да се променя описанието на дисциплината,
     * да може да се редактират началните+крайните дати на групите(), както и да се променя максималната бройка за записване,
     * трябва да може да се добавят групи, както и да се добявят и редактират материали
     */

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, dbRefUsers, dbRefCourse, dbRefGroups, dbRefParticipation, dbRefMaterials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

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

        // Взимане на подадените стойности от AdminActivity
        Intent intent = getIntent();
        final String courseIdExtra = intent.getStringExtra("courseId");
        final String descriptionExtra = intent.getStringExtra("description");
        //final String lecturerIdExtra = intent.getStringExtra("lecturerId"); // всъщност ням ада трябва това инфо

        // ---------- TEST [ START ]
        Toast.makeText(getApplicationContext(), "Group name: " + courseIdExtra, Toast.LENGTH_SHORT).show();

        /*//Прмер за обновяване на описание на дисциплина
        String description = "Някакво описание";
        dbRefCourse.child(courseIdExtra).child("descr").setValue(description, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError!=null) Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                else Toast.makeText(getApplicationContext(), "Упешно обновено описание на дисциплина", Toast.LENGTH_LONG).show();
            }
        });
        */

        // Изтегляне на всички групи на тази дисциплина
        dbRefGroups.child(courseIdExtra).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                for(DataSnapshot dataSnapshot : DATA.getChildren()) {

                    String groupName = dataSnapshot.getKey();
                    Long startLongDate = dataSnapshot.child("start").getValue(Long.class);
                    Long endLongDate = dataSnapshot.child("end").getValue(Long.class);
                    int signedPeople = dataSnapshot.child("current").getValue(Integer.class);
                    int maxPeople = dataSnapshot.child("max").getValue(Integer.class);

                    Date startDate = new Date(startLongDate), endDate = new Date(endLongDate);

                    //Displaying data
                    Toast.makeText(getApplicationContext(), "Group name: " + groupName + "\nStart date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(startDate)
                            + "\nEnd date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(endDate)
                            + "\nSigned people: " + signedPeople + "/" + maxPeople, Toast.LENGTH_LONG).show();

                    /*//Пример за обновяване на максималната бройка за записване (махнеш ли коментарите ще запише максимална стойност 60 на всички групи за дисциплината в активитито, просто пример)
                    dbRefGroups.child(courseIdExtra).child(groupName).child("max").setValue(60, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError!=null) Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            else Toast.makeText(getApplicationContext(), "Упешно обновена максимална бройка за записване", Toast.LENGTH_LONG).show();
                        }
                    });
                    */

                }//end for loop
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

        /*//Пример за добавяне на материал
        dbRefMaterials.child(courseIdExtra).child("Име на нов материал").setValue("URL към новия материал", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError!=null) Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                else Toast.makeText(getApplicationContext(), "Упешно добавен материал", Toast.LENGTH_LONG).show();
            }
        });
        */

        //Пример за обновяване на материал
        /*
        dbRefMaterials.child(courseIdExtra).child("Име на съществуващ материал").setValue("нов URL", new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError!=null) Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                else Toast.makeText(getApplicationContext(), "Упешно обновен материал", Toast.LENGTH_LONG).show();
            }
        });
        */


        // ---------- TEST [  END  ]

    }// end onCreate()

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
