package angelzani.onlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.URLUtil;
import android.widget.DatePicker;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import angelzani.onlearn.DBClasses.FBGroup;
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
                startActivity(new Intent(AdminActivity.this, ProfileActivity.class));
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

            /*----- Elevation ------ */
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.admin_LL_headMenu).setElevation(_20px/4);
        }

            /*------Теглене на всички курсове на Админ-------*/
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
                    courseLayout.setElevation(_20px/10);
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
                cs.connect(titleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END,_20px);
                cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM,_20px);
                cs.applyTo(courseLayout);

                titleView.setText(courseId);
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                titleView.setMaxLines(2);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
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
            gradientDrawableBackground1.setStroke(1, Color.parseColor("#000000"));
        }
        findViewById(R.id.admin_CL_addCourse).setBackground(gradientDrawableBackground1);
        setMargins(findViewById(R.id.admin_CL_addCourse),_20px,_20px,_20px,_20px);
        setMargins(findViewById(R.id.admin_TV_addTitle),_20px,_20px,_20px,0);
        ((TextView)findViewById(R.id.admin_TV_addTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        setMargins(findViewById(R.id.admin_V_seperator1),0,_20px/2,0,0);
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
                if(title.matches(".*[.#$\\[\\]].*")){
                    Toast.makeText(getApplicationContext(), "Course name can not contain symbols: '.', '#', '$', '[', ']'", Toast.LENGTH_LONG).show();
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

        //clicker AddGroupButton
        findViewById(R.id.admin_CL_buttonAddGroup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ConstraintLayout groupLayout = new ConstraintLayout(getApplicationContext());
                groupLayout.setId(View.generateViewId());
                ((LinearLayout)findViewById(R.id.admin_LL_groups)).addView(groupLayout);
                setMargins(groupLayout, _20px,_20px,_20px,_20px/4);
                GradientDrawable gdBackground1 = new GradientDrawable();
                gdBackground1.setColor(Color.parseColor("#ffffff"));
                gdBackground1.setShape(GradientDrawable.RECTANGLE);
                gdBackground1.setCornerRadius(_20px/2);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    groupLayout.setElevation(_20px/8);
                } else {
                    gdBackground1.setStroke(1, Color.parseColor("#000000"));
                }
                groupLayout.setBackground(gdBackground1);
                groupLayout.setPadding(_20px/2,_20px/2,_20px/2,_20px/2);

                final EditText groupNameET = new EditText(getApplicationContext());
                groupNameET.setId(View.generateViewId());
                groupNameET.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                groupLayout.addView(groupNameET);
                groupNameET.setHint("Group Name");
                groupNameET.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                groupNameET.setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
                groupNameET.setTextColor(Color.BLACK);
                groupNameET.setHintTextColor(Color.LTGRAY);

                final TextView groupNameTV = new TextView(getApplicationContext());
                groupNameTV.setId(View.generateViewId());
                groupNameTV.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                groupLayout.addView(groupNameTV);
                groupNameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                groupNameTV.setTextColor(Color.BLACK);

                final View seperator = new View(getApplicationContext());
                seperator.setId(View.generateViewId());
                groupLayout.addView(seperator);
                seperator.getLayoutParams().height=1;
                seperator.setBackgroundColor(getResources().getColor(R.color.light_blue2));

                final TextView startDate = new TextView(getApplicationContext());
                startDate.setId(View.generateViewId());
                startDate.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                groupLayout.addView(startDate);
                startDate.setHint("Select start date");
                startDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                startDate.setTextColor(Color.BLACK);
                startDate.setHintTextColor(Color.LTGRAY);
                final TextView endDate = new TextView(getApplicationContext());
                endDate.setId(View.generateViewId());
                endDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                endDate.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                groupLayout.addView(endDate);
                endDate.setHint("Select end date");
                endDate.setTextColor(Color.BLACK);
                endDate.setHintTextColor(Color.LTGRAY);

                TextView maxMemb = new TextView(getApplicationContext());
                maxMemb.setId(View.generateViewId());
                groupLayout.addView(maxMemb);
                maxMemb.setText("Max members:");
                maxMemb.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                maxMemb.setTextColor(Color.BLACK);

                final EditText maxET = new EditText(getApplicationContext());
                maxET.setId(View.generateViewId());
                maxET.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                groupLayout.addView(maxET);
                maxET.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                maxET.setHint("MAX");
                maxET.setRawInputType(InputType.TYPE_CLASS_NUMBER);
                maxET.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                maxET.setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
                maxET.setTextColor(Color.BLACK);
                maxET.setHintTextColor(Color.LTGRAY);
                final TextView maxTV = new TextView(getApplicationContext());
                maxTV.setId(View.generateViewId());
                maxTV.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                groupLayout.addView(maxTV);
                maxTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                maxTV.setTextColor(Color.BLACK);

                final TextView buttonSave = new TextView(getApplicationContext());
                buttonSave.setId(View.generateViewId());
                groupLayout.addView(buttonSave);
                buttonSave.setText("SAVE");
                buttonSave.setTextColor(Color.WHITE);
                buttonSave.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                buttonSave.setPadding(_20px/2,_20px/4,_20px/2,_20px/4);
                GradientDrawable gdBackground2 = new GradientDrawable();
                gdBackground2.setColor(Color.parseColor("#00C853"));
                gdBackground2.setStroke(1, Color.parseColor("#e0e0e0"));
                gdBackground2.setShape(GradientDrawable.RECTANGLE);
                gdBackground2.setCornerRadius(_20px/4);
                buttonSave.setBackground(gdBackground2);

                ConstraintSet cs = new ConstraintSet();
                cs.clone(groupLayout);
                cs.connect(groupNameET.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,0);
                cs.connect(groupNameET.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                cs.connect(groupNameET.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);

                cs.connect(groupNameTV.getId(),ConstraintSet.TOP,ConstraintSet.PARENT_ID,ConstraintSet.TOP,0);
                cs.connect(groupNameTV.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                cs.connect(groupNameTV.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);

                cs.connect(seperator.getId(),ConstraintSet.TOP,groupNameET.getId(),ConstraintSet.BOTTOM,0);
                cs.connect(seperator.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                cs.connect(seperator.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);

                cs.connect(startDate.getId(),ConstraintSet.TOP,seperator.getId(),ConstraintSet.BOTTOM,_20px/2);
                cs.connect(startDate.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                cs.connect(startDate.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);

                cs.connect(endDate.getId(),ConstraintSet.TOP,startDate.getId(),ConstraintSet.BOTTOM,_20px/2);
                cs.connect(endDate.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                cs.connect(endDate.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);

                cs.connect(maxMemb.getId(),ConstraintSet.TOP,endDate.getId(),ConstraintSet.BOTTOM,_20px/2);
                cs.connect(maxMemb.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);

                cs.connect(maxET.getId(),ConstraintSet.TOP,maxMemb.getId(),ConstraintSet.TOP,0);
                cs.connect(maxET.getId(),ConstraintSet.START,maxMemb.getId(),ConstraintSet.END,_20px/2);
                cs.connect(maxET.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);
                cs.connect(maxET.getId(),ConstraintSet.BOTTOM,maxMemb.getId(),ConstraintSet.BOTTOM,0);

                cs.connect(maxTV.getId(),ConstraintSet.TOP,maxMemb.getId(),ConstraintSet.TOP,0);
                cs.connect(maxTV.getId(),ConstraintSet.START,maxMemb.getId(),ConstraintSet.END,_20px/2);
                cs.connect(maxTV.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);
                cs.connect(maxTV.getId(),ConstraintSet.BOTTOM,maxMemb.getId(),ConstraintSet.BOTTOM,0);

                cs.connect(buttonSave.getId(),ConstraintSet.TOP,maxMemb.getId(),ConstraintSet.BOTTOM,_20px/2);
                cs.connect(buttonSave.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                cs.connect(buttonSave.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);

                cs.applyTo(groupLayout);

                //Кликъри
                startDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month + 1;
                                //String string_date = "12-12-2012";
                                String string_date = day + "-" + month + "-" + year;
                                startDate.setText(string_date);
                            }
                        };
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(AdminActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year,month,day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }
                });

                endDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                month = month + 1;
                                //String string_date = "12-12-2012";
                                String string_date = day + "-" + month + "-" + year;
                                endDate.setText(string_date);
                            }
                        };
                        Calendar cal = Calendar.getInstance();
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog dialog = new DatePickerDialog(AdminActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year,month,day);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog.show();
                    }
                });

                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String currentGroupName = groupNameET.getText().toString().trim();
                        String stringStartDate = startDate.getText().toString();
                        String stringEndDate = endDate.getText().toString();
                        String stringMax = maxET.getText().toString().trim();

                        if(currentGroupName.isEmpty() || stringStartDate.isEmpty() || stringEndDate.isEmpty() || stringMax.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "All fields must be filled.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(currentGroupName.matches(".*[.#$\\[\\]].*")) {
                            Toast.makeText(getApplicationContext(), "Group name can not contain symbols: '.', '#', '$', '[', ']'", Toast.LENGTH_LONG).show();
                        }

                        long currentStartDate = 0, currentEndDate = 0;
                        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");
                        try {
                            Date d = f.parse(stringStartDate);
                            currentStartDate = d.getTime();
                            d = f.parse(stringEndDate);
                            currentEndDate = d.getTime();

                        } catch (ParseException e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            return;
                        }

                        if(currentStartDate >= currentEndDate) {
                            Toast.makeText(getApplicationContext(), "End date has to be later than start date!", Toast.LENGTH_LONG).show();
                            return;
                        }

                        int max = Integer.parseInt(maxET.getText().toString().trim());

                        if(max <= 0) { // ==
                            Toast.makeText(getApplicationContext(), "Max members of each group have to be positive number!", Toast.LENGTH_LONG).show();
                        }

                        FBGroup group = new FBGroup(currentStartDate, currentEndDate, max);

                        String courseNAME = ((TextView)findViewById(R.id.admin_TV_addTitle)).getText().toString();
                        buttonSave.setVisibility(View.INVISIBLE);
                        dbRefGroups.child(courseNAME).child(currentGroupName).setValue(group, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null) {
                                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    buttonSave.setVisibility(View.VISIBLE);
                                } else {
                                    ConstraintSet cs = new ConstraintSet();
                                    cs.clone(groupLayout);
                                    cs.connect(seperator.getId(),ConstraintSet.TOP,groupNameTV.getId(),ConstraintSet.BOTTOM,_20px/2);
                                    cs.connect(seperator.getId(),ConstraintSet.START,ConstraintSet.PARENT_ID,ConstraintSet.START,0);
                                    cs.connect(seperator.getId(),ConstraintSet.END,ConstraintSet.PARENT_ID,ConstraintSet.END,0);
                                    cs.applyTo(groupLayout);

                                    maxTV.setText(maxET.getText().toString().trim());
                                    maxET.setVisibility(View.GONE);

                                    groupNameTV.setText(groupNameET.getText().toString().trim());
                                    groupNameET.setVisibility(View.GONE);

                                    buttonSave.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
            }
        });

        //clicker addMaterial
        findViewById(R.id.admin_CL_buttonAddMaterial).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ConstraintLayout materialLayout = new ConstraintLayout(getApplicationContext());
                materialLayout.setId(View.generateViewId());
                ((LinearLayout)findViewById(R.id.admin_LL_materials)).addView(materialLayout);

                setMargins(materialLayout, _20px,_20px,_20px,_20px/4);
                GradientDrawable gdBackground1 = new GradientDrawable();
                gdBackground1.setColor(Color.parseColor("#ffffff"));
                gdBackground1.setShape(GradientDrawable.RECTANGLE);
                gdBackground1.setCornerRadius(_20px/2);
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    materialLayout.setElevation(_20px/8);
                } else {
                    gdBackground1.setStroke(1, Color.parseColor("#000000"));
                }
                materialLayout.setBackground(gdBackground1);
                materialLayout.setPadding(_20px/2,_20px/2,_20px/2,_20px/2);

                final EditText nameET = new EditText(getApplicationContext());
                nameET.setId(View.generateViewId());
                nameET.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                materialLayout.addView(nameET);
                nameET.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                nameET.setHint("Material name");
                nameET.setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
                nameET.setTextColor(Color.BLACK);
                nameET.setHintTextColor(Color.LTGRAY);

                final EditText urlET = new EditText(getApplicationContext());
                urlET.setId(View.generateViewId());
                urlET.setLayoutParams(new ViewGroup.LayoutParams(ConstraintLayout.LayoutParams.MATCH_CONSTRAINT, ViewGroup.LayoutParams.WRAP_CONTENT));
                materialLayout.addView(urlET);
                urlET.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                urlET.setHint("Material URL");
                urlET.setBackground(new ColorDrawable(getResources().getColor(R.color.transparent)));
                urlET.setTextColor(Color.BLACK);
                urlET.setHintTextColor(Color.LTGRAY);

                final TextView buttonSave = new TextView(getApplicationContext());
                buttonSave.setId(View.generateViewId());
                materialLayout.addView(buttonSave);
                buttonSave.setText("SAVE");
                buttonSave.setTextColor(Color.WHITE);
                buttonSave.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                buttonSave.setPadding(_20px/2,_20px/4,_20px/2,_20px/4);
                GradientDrawable gdBackground2 = new GradientDrawable();
                gdBackground2.setColor(Color.parseColor("#00C853"));
                gdBackground2.setStroke(1, Color.parseColor("#e0e0e0"));
                gdBackground2.setShape(GradientDrawable.RECTANGLE);
                gdBackground2.setCornerRadius(_20px/4);
                buttonSave.setBackground(gdBackground2);

                final ConstraintSet cs = new ConstraintSet();
                cs.clone(materialLayout);
                cs.connect(nameET.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                cs.connect(nameET.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                cs.connect(nameET.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

                cs.connect(urlET.getId(), ConstraintSet.TOP, nameET.getId(), ConstraintSet.BOTTOM, _20px/2);
                cs.connect(urlET.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                cs.connect(urlET.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);

                cs.connect(buttonSave.getId(), ConstraintSet.TOP, urlET.getId(), ConstraintSet.BOTTOM, _20px/2);
                cs.connect(buttonSave.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                cs.connect(buttonSave.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                cs.applyTo(materialLayout);

                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String materialName = nameET.getText().toString().trim();
                        String materialURL = urlET.getText().toString().toLowerCase().trim();

                        if(materialName.isEmpty() || materialURL.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "All fields have to be filled.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if(!URLUtil.isValidUrl(materialURL)) {
                            Toast.makeText(getApplicationContext(), "Invalid material URL.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String courseNAME = ((TextView)findViewById(R.id.admin_TV_addTitle)).getText().toString();
                        buttonSave.setVisibility(View.INVISIBLE);
                        dbRefMaterials.child(courseNAME).child(materialName).setValue(materialURL, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null) {
                                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                    buttonSave.setVisibility(View.VISIBLE);
                                } else {

                                    TextView name = new TextView(getApplicationContext());
                                    name.setId(View.generateViewId());
                                    materialLayout.addView(name);
                                    name.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);;
                                    name.setTextColor(Color.parseColor("#ff89e5"));
                                    name.setShadowLayer(1.5f, -1, 1, Color.LTGRAY);
                                    name.setText(materialName);

                                    cs.clone(materialLayout);
                                    cs.connect(name.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                                    cs.connect(name.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                                    cs.connect(name.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                                    cs.connect(name.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
                                    cs.applyTo(materialLayout);

                                    GradientDrawable gdBackground = new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[] {0xFFFFD76B,0xFFFFBC6A});
                                    gdBackground.setShape(GradientDrawable.RECTANGLE);
                                    gdBackground.setCornerRadius(_20px);
                                    gdBackground.setStroke(1, Color.parseColor("#72A8FF"));
                                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                                        materialLayout.setElevation(_20px/8);
                                    }
                                    materialLayout.setBackground(gdBackground);

                                    buttonSave.setVisibility(View.GONE);
                                    nameET.setVisibility(View.GONE);
                                    urlET.setVisibility(View.GONE);
                                }
                            }
                        });
                    }
                });
            }
        });


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

    //Utility
    @Override
    public void onBackPressed() { // block finish()
        moveTaskToBack(true);
    }

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
