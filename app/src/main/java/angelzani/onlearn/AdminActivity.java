package angelzani.onlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.EditText;
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

public class AdminActivity extends AppCompatActivity { // Ани

    //Display metrics
    private int width, height;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, dbRefUsers, dbRefCourse, dbRefGroups, dbRefParticipation, dbRefMaterials;


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
        dbRefMaterials= mRef.child("materials");


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
                final String courseId = dataSnapshot.getKey();
                final String descripton = dataSnapshot.child("descr").getValue(String.class);
                final String lecturerId = dataSnapshot.child("lect").getValue(String.class);
                CLCourse courseLayout = new CLCourse(getApplicationContext(),courseId,descripton,lecturerId);
                courseLayout.setId(View.generateViewId());

                findViewById(R.id.admin_TV_manageInfo).setVisibility(View.INVISIBLE);

                courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                ((LinearLayout)findViewById(R.id.admin_LL_manageWrap)).addView(courseLayout);

                courseLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(AdminActivity.this, ManageActivity.class);
                        intent.putExtra("courseId", courseId);
                        intent.putExtra("description", descripton);
                        intent.putExtra("lecturerId", lecturerId);
                        startActivity(intent);
                    }
                });

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
                cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px);
                cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,_20px);
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

        /*----------------------- ADD LAYOUT ----------------------*/

        GradientDrawable gradientDrawableBackground1 = new GradientDrawable();
        gradientDrawableBackground1.setColor(Color.parseColor("#ffffff"));
        //gradientDrawableBackground1.setStroke(1, Color.parseColor("#000000"));
        gradientDrawableBackground1.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackground1.setCornerRadius(_20px/2);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.admin_CL_addCourse).setElevation(_20px/8);
        } else {
            gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
        }
        findViewById(R.id.admin_CL_addCourse).setBackground(gradientDrawableBackground1);
        setMargins(findViewById(R.id.admin_CL_addCourse),_20px,_20px,_20px,_20px);
        setMargins(findViewById(R.id.admin_TV_addTitle),_20px,_20px,_20px,0);
        ((TextView)findViewById(R.id.admin_TV_addTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        setMargins(findViewById(R.id.admin_V_seperator1),0,_20px,0,0);
        GradientDrawable gradientDrawableBackground2 = new GradientDrawable();
        gradientDrawableBackground2.setColor(Color.parseColor("#efefef"));
        gradientDrawableBackground2.setStroke(1, Color.parseColor("#e0e0e0"));
        gradientDrawableBackground2.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackground2.setCornerRadius(_20px/4);
        findViewById(R.id.admin_TV_addDescr).setBackground(gradientDrawableBackground2);
        setMargins(findViewById(R.id.admin_TV_addDescr),0,_20px/2,0,0);
        ((TextView)findViewById(R.id.admin_TV_addDescr)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.admin_TV_addDescr).setPadding(height/160, height/160, height/160, height/160);

        //buttons save/clear
        ((TextView)findViewById(R.id.admin_TV_addSave)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        setMargins(findViewById(R.id.admin_TV_addSave), 0,_20px/2,0,_20px/2);
        findViewById(R.id.admin_TV_addSave).setPadding(_20px/2,_20px/4,_20px/2,_20px/4);
        GradientDrawable gradientDrawableBackground3 = new GradientDrawable();
        gradientDrawableBackground3.setColor(Color.parseColor("#00C853"));
        gradientDrawableBackground3.setStroke(1, Color.parseColor("#e0e0e0"));
        gradientDrawableBackground3.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackground3.setCornerRadius(_20px/4);
        findViewById(R.id.admin_TV_addSave).setBackground(gradientDrawableBackground3);
        ((TextView)findViewById(R.id.admin_TV_addClear)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        setMargins(findViewById(R.id.admin_TV_addClear), 0,_20px/2,0,_20px/2);
        findViewById(R.id.admin_TV_addClear).setPadding(_20px/2,_20px/4,_20px/2,_20px/4);
        GradientDrawable gradientDrawableBackground4 = new GradientDrawable();
        gradientDrawableBackground4.setColor(Color.parseColor("#D50000"));
        gradientDrawableBackground4.setStroke(1, Color.parseColor("#e0e0e0"));
        gradientDrawableBackground4.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackground4.setCornerRadius(_20px/4);
        findViewById(R.id.admin_TV_addClear).setBackground(gradientDrawableBackground4);

        //button group/material
        setMargins(findViewById(R.id.admin_CL_buttonAddGroup), 0,_20px/2,_20px,0);
        findViewById(R.id.admin_CL_buttonAddGroup).setPadding(_20px,_20px/4,_20px,_20px/4);
        findViewById(R.id.admin_IV_addGroupPlus).getLayoutParams().width = height/27;
        findViewById(R.id.admin_IV_addGroupPlus).getLayoutParams().height = height/27;
        ((TextView)findViewById(R.id.admin_TV_addGroup)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        findViewById(R.id.admin_TV_addGroup).setPadding(0,0,_20px,0);
        GradientDrawable gradientDrawableBackground5 = new GradientDrawable();
        gradientDrawableBackground5.setColor(Color.parseColor("#5892EF"));
        gradientDrawableBackground5.setStroke(_20px/10, Color.parseColor("#ffffff"));
        gradientDrawableBackground5.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackground5.setCornerRadius(_20px/4);
        findViewById(R.id.admin_CL_buttonAddGroup).setBackground(gradientDrawableBackground5);

        setMargins(findViewById(R.id.admin_CL_buttonAddMaterial), 0,_20px/2,_20px,0);
        findViewById(R.id.admin_CL_buttonAddMaterial).setPadding(_20px,_20px/4,_20px,_20px/4);
        findViewById(R.id.admin_IV_addMaterialPlus).getLayoutParams().width = height/27;
        findViewById(R.id.admin_IV_addMaterialPlus).getLayoutParams().height = height/27;
        ((TextView)findViewById(R.id.admin_TV_addMaterial)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        findViewById(R.id.admin_TV_addMaterial).setPadding(0,0,_20px,0);
        findViewById(R.id.admin_CL_buttonAddMaterial).setBackground(gradientDrawableBackground5);

        //OnClickListeners
        findViewById(R.id.admin_TV_addTitle).setOnClickListener(editCourseName);
        findViewById(R.id.admin_TV_addDescr).setOnClickListener(editDescription);
        findViewById(R.id.admin_TV_addSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String title = ((TextView)findViewById(R.id.admin_TV_addTitle)).getText().toString().trim();
                final String description = ((TextView)findViewById(R.id.admin_TV_addDescr)).getText().toString().trim();

                if(title.length() == 0){
                    Toast.makeText(getApplicationContext(), "Course name can not be empty.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(description.length() == 0){
                    Toast.makeText(getApplicationContext(), "Description must be filled.", Toast.LENGTH_LONG).show();
                    return;
                }
                if(description.length() > 10000){
                    Toast.makeText(getApplicationContext(), "Description be less than 10 000 characters.", Toast.LENGTH_LONG).show();
                    return;
                }

                hideSave();
                findViewById(R.id.admin_TV_addTitle).setOnClickListener(null);
                findViewById(R.id.admin_TV_addDescr).setOnClickListener(null);

                dbRefCourse.child(title).child("descr").setValue(description, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError != null){
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            showSave();
                            findViewById(R.id.admin_TV_addTitle).setOnClickListener(editCourseName);
                            findViewById(R.id.admin_TV_addDescr).setOnClickListener(editDescription);
                        } else {
                            showClear();
                            findViewById(R.id.admin_CL_buttonAddGroup).setVisibility(View.VISIBLE);
                            findViewById(R.id.admin_CL_buttonAddMaterial).setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
        findViewById(R.id.admin_TV_addClear).setOnClickListener(clearThisBS);

    }
                    /*-----END OF Initialize*/
                /* ------ OnClickListeners-------*/

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

    View.OnClickListener editCourseName = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(AdminActivity.this);
            alert.setTitle("Course name : ");
            //lert.setMessage("Message :");

            // Set an EditText view to get user input
            final EditText input = new EditText(AdminActivity.this);
            alert.setView(input);
            input.setText(((TextView)findViewById(R.id.admin_TV_addTitle)).getText());

            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(input.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Course name can not be empty.", Toast.LENGTH_LONG).show();
                    } else {
                        ((TextView)findViewById(R.id.admin_TV_addTitle)).setText(input.getText().toString().trim());
                    }
                    return;
                }
            });

            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            return;
                        }
                    });
            alert.show();
            input.requestFocus();
        }
    };

    View.OnClickListener editDescription = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder alert = new AlertDialog.Builder(AdminActivity.this);
            alert.setTitle("Course description : ");
            //lert.setMessage("Message :");

            // Set an EditText view to get user input
            final EditText input = new EditText(AdminActivity.this);
            alert.setView(input);
            input.setText(((TextView)findViewById(R.id.admin_TV_addDescr)).getText());

            alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    if(input.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Description must be filled.", Toast.LENGTH_LONG).show();
                        return;
                    } else if(input.getText().toString().trim().length() > 10000){
                        Toast.makeText(getApplicationContext(), "Description be less than 10 000 characters.", Toast.LENGTH_LONG).show();
                    } else {
                        ((TextView)findViewById(R.id.admin_TV_addDescr)).setText(input.getText().toString().trim());
                        return;
                    }
                }
            });

            alert.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // TODO Auto-generated method stub
                            return;
                        }
                    });
            alert.show();
            input.requestFocus();
        }
    };

    View.OnClickListener clearThisBS = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ((TextView)findViewById(R.id.admin_TV_addTitle)).setText("");
            ((TextView)findViewById(R.id.admin_TV_addDescr)).setText("");
            findViewById(R.id.admin_TV_addTitle).setOnClickListener(editCourseName);
            findViewById(R.id.admin_TV_addDescr).setOnClickListener(editDescription);
            findViewById(R.id.admin_CL_buttonAddGroup).setVisibility(View.INVISIBLE);
            findViewById(R.id.admin_CL_buttonAddMaterial).setVisibility(View.INVISIBLE);
            showSave();
            hideClear();
            ((LinearLayout)findViewById(R.id.admin_LL_groups)).removeAllViews();
            ((LinearLayout)findViewById(R.id.admin_LL_materials)).removeAllViews();
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

    private void showSave(){
        findViewById(R.id.admin_TV_addSave).setVisibility(View.VISIBLE);
    }
    private void hideSave(){
        findViewById(R.id.admin_TV_addClear).setVisibility(View.INVISIBLE);
    }
    private void showClear(){
        findViewById(R.id.admin_TV_addClear).setVisibility(View.VISIBLE);
    }
    private void hideClear(){
        findViewById(R.id.admin_TV_addClear).setVisibility(View.INVISIBLE);
    }
}
