package angelzani.onlearn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
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

import angelzani.onlearn.UIClasses.CLCourse;

public class AdminActivity extends AppCompatActivity {

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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_admin);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(); //root
        dbRefUsers=mRef.child("users");
        dbRefCourse=mRef.child("course");
        dbRefGroups=mRef.child("groups");
        dbRefParticipation=mRef.child("participation");


        initializeUI();

    }//end onCreate()

    private void initializeUI()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        final int _20px=height/40;

        //Header Layout
        findViewById(R.id.admin_CL_header).getLayoutParams().height=height/16;


        //Profile Icon
        findViewById(R.id.admin_IV_profile).getLayoutParams().height=height/18;
        findViewById(R.id.admin_IV_profile).getLayoutParams().width=height/18;
        setMargins(findViewById(R.id.admin_IV_profile),height/80,0,height/80,0);
        findViewById(R.id.admin_LL_toProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(AdminActivity.this, ProfileActivity.class),1);
            }
        });

        //Name TextView
        ((TextView)findViewById(R.id.admin_TV_name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        dbRefUsers.child(user.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    ((TextView)findViewById(R.id.admin_TV_name)).setText(dataSnapshot.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isInternetAvailable())
                {
                    Toast.makeText(getApplicationContext(),"No internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

                 /*------ HeadMenu TAB ------*/
        findViewById(R.id.admin_LL_headMenu).getLayoutParams().height=(int)(height/14.5);
        ((TextView)findViewById(R.id.admin_TV_HM_manage)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((TextView)findViewById(R.id.admin_TV_HM_add)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.admin_TV_HM_manage).setOnClickListener(switchMenu);
        findViewById(R.id.admin_TV_HM_add).setOnClickListener(switchMenu);

            // ----- Elevation
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.admin_LL_headMenu).setElevation(_20px/4);
        }

            //------Теглене на всички курсове на Админ
        final GradientDrawable gradientDrawableBackgroundCourses = new GradientDrawable();
        gradientDrawableBackgroundCourses.setColor(Color.parseColor("#ffffff"));
        //gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
        gradientDrawableBackgroundCourses.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackgroundCourses.setCornerRadius(_20px/2);

        Query query = dbRefCourse.orderByChild("lect").equalTo(user.getUid()); // всички курсове с lecturer current user

        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String courseId = dataSnapshot.getKey();
                String descripton = dataSnapshot.child("descr").getValue(String.class);
                String lecturerId = dataSnapshot.child("lect").getValue(String.class);
                CLCourse courseLayout = new CLCourse(getApplicationContext(),courseId,descripton,lecturerId);
                courseLayout.setId(View.generateViewId());

                findViewById(R.id.admin_TV_manageInfo).setVisibility(View.INVISIBLE);

                courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                ((LinearLayout)findViewById(R.id.admin_LL_manageWrap)).addView(courseLayout);

                setMargins(courseLayout, _20px,_20px,_20px,_20px/8);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    courseLayout.setElevation(_20px/8);
                } else {
                    gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                }
                courseLayout.setBackground(gradientDrawableBackgroundCourses);

                TextView titleView = new TextView(getApplicationContext());
                titleView.setId(View.generateViewId());
                courseLayout.addView(titleView);

                ConstraintSet cs = new ConstraintSet();
                cs.clone(courseLayout);
                // KOE VIEW , КОЯ СТРАНА, ЗА КОЕ VIEW, ЗА КОЕ СТРАНА, КОЛКО ОТСТОЯНИЕ
                cs.connect(titleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, _20px);
                cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px*2);
                cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,_20px*2);
                cs.applyTo(courseLayout);

                titleView.setText(courseId);
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                titleView.setMaxLines(2);
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
                if(databaseError!=null) Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
                    /*-----END OF Initialize*/
                /* ------ OnClickListener-------*/

    View.OnClickListener switchMenu = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            findViewById(R.id.admin_TV_HM_manage).setBackgroundResource(R.drawable.background_tab_menu_unselected);
            findViewById(R.id.admin_TV_HM_add).setBackgroundResource(R.drawable.background_tab_menu_unselected);
            v.setBackgroundResource(R.drawable.background_tab_menu_selected);

            switch (v.getId()){
                case R.id.admin_TV_HM_manage:

                    findViewById(R.id.admin_CL_manageLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.admin_CL_addLayout).setVisibility(View.INVISIBLE);
                    break;

                case R.id.admin_TV_HM_add:
                    findViewById(R.id.admin_CL_addLayout).setVisibility(View.VISIBLE);
                    findViewById(R.id.admin_CL_manageLayout).setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };

    //Result logout
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

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
