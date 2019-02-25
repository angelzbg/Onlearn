package angelzani.onlearn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import angelzani.onlearn.UIClasses.Course;

public class ClientActivity extends AppCompatActivity { // Ангел

    //Display metrics
    private int width, height;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, dbRefUsers, dbRefCourse, dbRefGroups, dbRefParticipation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // Екрана в нормално състояние без да се завърта в landscape
        setContentView(R.layout.activity_client);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(); //root
        dbRefUsers = mRef.child("users");
        dbRefCourse = mRef.child("course");
        dbRefGroups = mRef.child("groups");
        dbRefParticipation = mRef.child("participation");

        initializeUI();

        //za triene
        findViewById(R.id.client_TV_Search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ClientActivity.this, CourseActivity.class);
                intent.putExtra("courseId", "Mobile apps");
                intent.putExtra("description","test");
                intent.putExtra("lecturerId","hDBh2zyKc1WZYQBL9YQogRZEiCo1");
                startActivity(intent);
            }
        });

    }//end onCreate()

    private void initializeUI(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        int _20px = height/40;

        /* ----- Header Layout ----- */
        findViewById(R.id.client_CL_Head).getLayoutParams().height = height/16;
        //Profile Icon
        findViewById(R.id.client_IV_Profile).getLayoutParams().width = height/18;
        findViewById(R.id.client_IV_Profile).getLayoutParams().height = findViewById(R.id.client_IV_Profile).getLayoutParams().width;
        setMargins(findViewById(R.id.client_IV_Profile), height/80,0,height/80,0);
        findViewById(R.id.client_LL_ToProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ClientActivity.this, ProfileActivity.class));
            }
        });
        //Name text view
        ((TextView)findViewById(R.id.client_TV_Name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        dbRefUsers.child(user.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ((TextView)findViewById(R.id.client_TV_Name)).setText(dataSnapshot.getValue(String.class));
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isInternetAvailable()){
                    Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

        /* ----- Head Menu Tabs ----- */
        findViewById(R.id.client_LL_HeadMenu).getLayoutParams().height = (int)(height/14.5);
        ((TextView)findViewById(R.id.client_TV_HM_All)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((TextView)findViewById(R.id.client_TV_HM_Ongoing)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((TextView)findViewById(R.id.client_TV_HM_Ended)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.client_TV_HM_All).setOnClickListener(switchMenu);
        findViewById(R.id.client_TV_HM_Ongoing).setOnClickListener(switchMenu);
        findViewById(R.id.client_TV_HM_Ended).setOnClickListener(switchMenu);

        /* ----- Search Box ----- */
        findViewById(R.id.client_CL_SearchWrap).setPadding(_20px*2,_20px,_20px*2,0);
        findViewById(R.id.client_TV_Search).setPadding(0,_20px/2,_20px*2,_20px/2);
        ((TextView)findViewById(R.id.client_TV_Search)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        GradientDrawable gradientDrawableBackgroundSearch = new GradientDrawable();
        gradientDrawableBackgroundSearch.setColor(Color.parseColor("#ffffff"));
        gradientDrawableBackgroundSearch.setStroke(1, Color.parseColor("#FDFDFD"));
        gradientDrawableBackgroundSearch.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackgroundSearch.setCornerRadius(_20px/2);
        findViewById(R.id.client_TV_Search).setBackground(gradientDrawableBackgroundSearch);
        findViewById(R.id.client_TV_ClearSearch).getLayoutParams().width = height/27;
        findViewById(R.id.client_TV_ClearSearch).getLayoutParams().height = height/27;
        findViewById(R.id.client_TV_ClearSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // CLEAR SEARCH LOGIC
            }
        });

        /* ----- Padding на лейаутите*/

        // ---------- Elevations
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.client_LL_HeadMenu).setElevation(_20px/4);
            findViewById(R.id.client_TV_Search).setElevation(_20px/8);
            findViewById(R.id.client_TV_ClearSearch).setElevation(_20px/6);
        }

        // ---------- Load Courses
        dbRefCourse.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

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
                if(databaseError!=null) Toast.makeText(getApplicationContext(), "Load Courses db error : " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                if(!isInternetAvailable()){
                    Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }// end of InitializeUI()
    private ArrayList<Course> courses = new ArrayList<Course>();

    /* ----- OnClickListeners [ START ] ----- */

    View.OnClickListener switchMenu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            findViewById(R.id.client_TV_HM_All).setBackgroundResource(R.drawable.background_tab_menu_unselected);
            findViewById(R.id.client_TV_HM_Ongoing).setBackgroundResource(R.drawable.background_tab_menu_unselected);
            findViewById(R.id.client_TV_HM_Ended).setBackgroundResource(R.drawable.background_tab_menu_unselected);
            v.setBackgroundResource(R.drawable.background_tab_menu_selected);

            switch (v.getId()){
                case R.id.client_TV_HM_All:
                    findViewById(R.id.client_CL_OngoingLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.client_CL_EndedLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.client_CL_AllLayout).setVisibility(View.VISIBLE); // само all layout се вижда
                    break;
                case R.id.client_TV_HM_Ongoing:
                    findViewById(R.id.client_CL_AllLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.client_CL_EndedLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.client_CL_OngoingLayout).setVisibility(View.VISIBLE); // само ongoing layout се вижда
                    break;
                case R.id.client_TV_HM_Ended:
                    findViewById(R.id.client_CL_OngoingLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.client_CL_AllLayout).setVisibility(View.INVISIBLE);
                    findViewById(R.id.client_CL_EndedLayout).setVisibility(View.VISIBLE); // само ended layout се вижда
                    break;
            }

        }
    };

    /* ----- OnClickListeners [  END  ] ----- */

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
