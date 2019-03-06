package angelzani.onlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
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

import angelzani.onlearn.UIClasses.CLCourse;
import angelzani.onlearn.UIClasses.OnSwipeTouchListener;

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

    }//end onCreate()

    private void initializeUI(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        final int _20px = height/40;

        // Loading Progress Anim
        findViewById(R.id.client_IV_LoadingBar).getLayoutParams().width = width/4;
        findViewById(R.id.client_IV_LoadingBar).getLayoutParams().height = findViewById(R.id.client_IV_LoadingBar).getLayoutParams().width;

        // Alert Message
        findViewById(R.id.client_CL_Alert).setPadding(height/80,height/80,height/80,height/80);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E9FFFFFF"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(_20px/2);
        findViewById(R.id.client_CL_AlertBox).setBackground(gradientDrawable);

        ((TextView)findViewById(R.id.client_TV_AlertTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        findViewById(R.id.client_TV_AlertTitle).setPadding(0,height/80,0,height/80);

        ((TextView)findViewById(R.id.client_TV_AlertMessage)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.client_TV_AlertMessage).setPadding(_20px,_20px,_20px,_20px);

        ((TextView)findViewById(R.id.client_TV_AlertClose)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.client_TV_AlertClose).getLayoutParams().width = height/16;
        findViewById(R.id.client_TV_AlertClose).getLayoutParams().height = findViewById(R.id.client_TV_AlertClose).getLayoutParams().width;
        setMargins(findViewById(R.id.client_TV_AlertClose), 0,0,0,height/80);
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E5EEFC"));
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setStroke(1, Color.parseColor("#82B1FF"));
        findViewById(R.id.client_TV_AlertClose).setBackground(gradientDrawable);

        // Alert dialog close
        findViewById(R.id.client_TV_AlertClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.client_CL_Alert).setVisibility(View.INVISIBLE);
            }
        });

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
                if(databaseError != null) showAlert("Error", databaseError.getMessage());
                if(!isInternetAvailable()){
                    //Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
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

        // SV Layouts margings:
        setMargins(findViewById(R.id.client_SV_All), _20px,0,_20px,0);
        setMargins(findViewById(R.id.client_SV_SearchResult), _20px,0,_20px,0);
        setMargins(findViewById(R.id.client_SV_Ongoing), _20px,0,_20px,0);
        setMargins(findViewById(R.id.client_SV_Ended), _20px,0,_20px,0);

        /* ----- Search Box ----- */
        setMargins(findViewById(R.id.client_CL_SearchWrap), _20px,_20px/4,_20px,0);
        findViewById(R.id.client_TV_Search).setPadding(_20px,_20px/2,_20px*2,_20px/2);
        ((TextView)findViewById(R.id.client_TV_Search)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        GradientDrawable gradientDrawableBackgroundSearch = new GradientDrawable();
        gradientDrawableBackgroundSearch.setColor(Color.parseColor("#ffffff"));
        gradientDrawableBackgroundSearch.setStroke(_20px/20, Color.parseColor("#e2e2e2"));
        gradientDrawableBackgroundSearch.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackgroundSearch.setCornerRadius(_20px);
        findViewById(R.id.client_TV_Search).setBackground(gradientDrawableBackgroundSearch);
        findViewById(R.id.client_TV_Search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ClientActivity.this);
                alert.setTitle("Search by Course Name : ");
                //lert.setMessage("Message :");

                // Set an EditText view to get user input
                final EditText input = new EditText(ClientActivity.this);
                alert.setView(input);
                input.setText(((TextView)findViewById(R.id.client_TV_Search)).getText());

                alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(input.getText().toString().trim().length() == 0){
                            //showAlert("Alert", "Name must contain at least 2 characters.");
                            return;
                        }

                        String searchString = input.getText().toString().trim();

                        findViewById(R.id.client_SV_All).setVisibility(View.INVISIBLE);
                        findViewById(R.id.client_TV_ClearSearch).setVisibility(View.VISIBLE);
                        findViewById(R.id.client_SV_SearchResult).setVisibility(View.VISIBLE);

                        ((TextView)findViewById(R.id.client_TV_Search)).setText(searchString);
                        final LinearLayout client_LL_SearchResult = findViewById(R.id.client_LL_SearchResult);
                        client_LL_SearchResult.removeAllViews();

                        dbRefCourse.orderByKey().startAt(searchString).endAt(searchString + "\uf8ff").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot DATA) {
                                for(DataSnapshot dataSnapshot : DATA.getChildren()) {
                                    final GradientDrawable gradientDrawableBackgroundCourses = new GradientDrawable();
                                    gradientDrawableBackgroundCourses.setColor(Color.parseColor("#ffffff"));
                                    //gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                                    gradientDrawableBackgroundCourses.setShape(GradientDrawable.RECTANGLE);
                                    gradientDrawableBackgroundCourses.setCornerRadius(_20px/2);

                                    final CLCourse courseLayout = new CLCourse(getApplicationContext(), dataSnapshot.getKey(), dataSnapshot.child("descr").getValue(String.class), dataSnapshot.child("lect").getValue(String.class));
                                    courseLayout.setId(View.generateViewId());
                                    courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                    client_LL_SearchResult.addView(courseLayout);

                                    courseLayout.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            //Теглене и проверка дали потребителя е в този курс: ако не е -> CourseActivity , ако е -> SignedCourseActivity
                                            showProgress();
                                            dbRefParticipation.child(courseLayout.getCourseId()).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    hideProgress();
                                                    if (!dataSnapshot.exists()) { // не е записан
                                                        Intent intent = new Intent(ClientActivity.this, CourseActivity.class);
                                                        intent.putExtra("courseId", courseLayout.getCourseId());
                                                        intent.putExtra("description", courseLayout.getDescription());
                                                        intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                                        startActivity(intent);
                                                    } else { // вече е записан в тази дисциплина
                                                        Intent intent = new Intent(ClientActivity.this, SignedCourseActivity.class);
                                                        intent.putExtra("courseId", courseLayout.getCourseId());
                                                        intent.putExtra("description", courseLayout.getDescription());
                                                        intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                                        intent.putExtra("groupId", dataSnapshot.getValue(String.class));
                                                        startActivity(intent);
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                                    hideProgress();
                                                    if (!isInternetAvailable()){
                                                    }
                                                    if (databaseError != null) showAlert("Error", databaseError.getMessage());
                                                }
                                            });
                                        }
                                    });

                                    setMargins(courseLayout, _20px, _20px, _20px, _20px / 8);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        courseLayout.setElevation(_20px / 10);
                                    } else {
                                        gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                                    }
                                    courseLayout.setBackground(gradientDrawableBackgroundCourses);

                                    TextView titleView = new TextView(getApplicationContext());
                                    titleView.setId(View.generateViewId());
                                    courseLayout.addView(titleView);

                                    ConstraintSet cs = new ConstraintSet();
                                    cs.clone(courseLayout);
                                    cs.connect(titleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, _20px);
                                    cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px);
                                    cs.connect(titleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, _20px);
                                    cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, _20px);
                                    cs.applyTo(courseLayout);

                                    titleView.setText(courseLayout.getCourseId());
                                    titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                                    titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                                    titleView.setMaxLines(2);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                if (!isInternetAvailable()){ }
                                if (databaseError != null) showAlert("Error", databaseError.getMessage());
                            }
                        });

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
        });
        findViewById(R.id.client_TV_ClearSearch).getLayoutParams().width = height/27;
        findViewById(R.id.client_TV_ClearSearch).getLayoutParams().height = height/27;
        ((TextView)findViewById(R.id.client_TV_ClearSearch)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px/2);
        findViewById(R.id.client_TV_ClearSearch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LinearLayout)findViewById(R.id.client_LL_SearchResult)).removeAllViews();
                findViewById(R.id.client_TV_ClearSearch).setVisibility(View.INVISIBLE);
                findViewById(R.id.client_SV_SearchResult).setVisibility(View.INVISIBLE);
                findViewById(R.id.client_SV_All).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.client_TV_Search)).setText("");
            }
        });

        // ---------- Elevations
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.client_LL_HeadMenu).setElevation(_20px/4);
            findViewById(R.id.client_TV_ClearSearch).setElevation(_20px/8);

            findViewById(R.id.client_CL_Loading).setElevation(_20px);
            findViewById(R.id.client_CL_Alert).setElevation(_20px*2);
            findViewById(R.id.client_CL_AlertBox).setElevation(_20px/4);
        }

        // ---------- Load Courses
        gradientDrawableBackgroundCourses = new GradientDrawable();
        gradientDrawableBackgroundCourses.setColor(Color.parseColor("#ffffff"));
        //gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
        gradientDrawableBackgroundCourses.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackgroundCourses.setCornerRadius(_20px/2);

        loadCourses();

        // ----- Зареждаме в ONGOING || ENDED
        //dbRefParticipation.orderByChild(user.getUid()) < - тегли абсолютно всички... трябва ми само тези, които съдържат child user.getUid...
        dbRefParticipation.orderByChild(user.getUid()).addChildEventListener(new ChildEventListener(){
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                final String courseId = dataSnapshot.getKey();
                final String groupId = dataSnapshot.child(user.getUid()).getValue(String.class);
                if(groupId == null) { // потребителят не е записан в тази дисциплина
                    return;
                }

                for(int i=0; i<courses.size(); i++) {
                    if(courses.get(i).getCourseId().equals(courseId)) { // вече го имаме зареден от dbRefCourse.orderByKey().startAt(last_courseId_loaded).limitToFirst(10).addChildEventListener

                        final CLCourse courseLayout = courses.get(i);
                        courseLayout.setOnClickListener(new View.OnClickListener() { // ще променим кликъра да не тегли излишно от датабазата (знаем че е записан)
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ClientActivity.this, SignedCourseActivity.class);
                                intent.putExtra("courseId", courseLayout.getCourseId());
                                intent.putExtra("description", courseLayout.getDescription());
                                intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                intent.putExtra("groupId", groupId);
                                startActivity(intent);
                            }
                        });

                        Log.e("tag", "dbRefGroups.child(\"" + courseLayout.getCourseId() + "\").child(\"" + groupId + "\").child(\"end\")");
                        dbRefGroups.child(courseLayout.getCourseId()).child(groupId).child("end").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ((ViewManager) courseLayout.getParent()).removeView(courseLayout);
                                if (dataSnapshot.getValue(Long.class) > System.currentTimeMillis()) { // отива в ONGOING
                                    ((LinearLayout) findViewById(R.id.client_LL_Ongoing)).addView(courseLayout);
                                } else { // отива в ENDED
                                    ((LinearLayout) findViewById(R.id.client_LL_Ended)).addView(courseLayout);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });

                        return; // излизаме от onChildAdded()
                    }
                }

                //щом не сме return-али -> трябва да добавим дисциплината в лейаутите
                dbRefCourse.child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final CLCourse courseLayout = new CLCourse(getApplicationContext(), courseId, dataSnapshot.child("descr").getValue(String.class), dataSnapshot.child("lect").getValue(String.class));
                        courseLayout.setId(View.generateViewId());
                        courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        courses.add(courseLayout);

                        dbRefGroups.child(courseLayout.getCourseId()).child(groupId).child("end").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ((ViewManager) courseLayout.getParent()).removeView(courseLayout);
                                if (dataSnapshot.getValue(Long.class) > System.currentTimeMillis()) { // отива в ONGOING
                                    ((LinearLayout) findViewById(R.id.client_LL_Ongoing)).addView(courseLayout);
                                } else { // отива в ENDED
                                    ((LinearLayout) findViewById(R.id.client_LL_Ended)).addView(courseLayout);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });

                        courseLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ClientActivity.this, SignedCourseActivity.class);
                                intent.putExtra("courseId", courseLayout.getCourseId());
                                intent.putExtra("description", courseLayout.getDescription());
                                intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                intent.putExtra("groupId", groupId);
                                startActivity(intent);
                            }
                        });

                        setMargins(courseLayout, _20px, _20px, _20px, _20px / 8);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            courseLayout.setElevation(_20px / 10);
                        } else {
                            gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                        }
                        courseLayout.setBackground(gradientDrawableBackgroundCourses);

                        TextView titleView = new TextView(getApplicationContext());
                        titleView.setId(View.generateViewId());
                        courseLayout.addView(titleView);

                        ConstraintSet cs = new ConstraintSet();
                        cs.clone(courseLayout);
                        cs.connect(titleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, _20px);
                        cs.applyTo(courseLayout);

                        titleView.setText(courseLayout.getCourseId());
                        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                        titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                        titleView.setMaxLines(2);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if(!isInternetAvailable()) {}
                        showAlert("Error", databaseError.getMessage());
                    }
                });

            }
            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) { }
            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) { }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (!isInternetAvailable()) { }
                if (databaseError != null) showAlert("Error", databaseError.getMessage());
            }
        });

        // -------------------- Scroll Logic (ако scrolla в courses е стигнал най-долу трябва да зареди следващите 10 специалности и т.н.)
        final ScrollView scrollAll = findViewById(R.id.client_SV_All);
        scrollAll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (findViewById(R.id.client_LL_All).getHeight() == (scrollAll.getHeight() + scrollAll.getScrollY())) {
                    //scroll view is at bottom
                    showProgress();
                    loadCourses();
                } else {
                    //scroll view is not at bottom
                }
            }
        });

        findViewById(R.id.client_LL_All).setMinimumHeight(height); // ако потребителят е записал първите 10 изттеглени или поне повечето от тях, лейаута ще е с малка височина и няма да можем да скролнем надолу -> логиката за теглене на по-нататъчни дисциплини умира -> това е fix

        // -------------------- Swipe Logic
        // ----- ALL/COURSES
        findViewById(R.id.client_SV_All).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в courses/all
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // отиваме в ongoing
                findViewById(R.id.client_TV_HM_Ongoing).callOnClick();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                // отиваме Никъде
            }
        });
        findViewById(R.id.client_SV_SearchResult).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в courses/all
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // отиваме в ongoing
                findViewById(R.id.client_TV_HM_Ongoing).callOnClick();
            }
        });
        findViewById(R.id.client_LL_All).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в courses/all
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // отиваме в ongoing
                findViewById(R.id.client_TV_HM_Ongoing).callOnClick();
            }
        });
        findViewById(R.id.client_LL_SearchResult).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в courses/all
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // отиваме в ongoing
                findViewById(R.id.client_TV_HM_Ongoing).callOnClick();
            }
        });
        // ----- ONGOING
        findViewById(R.id.client_SV_Ongoing).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в ongoing
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // отиваме в ended
                findViewById(R.id.client_TV_HM_Ended).callOnClick();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                // отиваме в courses/all
                findViewById(R.id.client_TV_HM_All).callOnClick();
            }
        });
        findViewById(R.id.client_LL_Ongoing).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в ongoing
            @Override
            public void onSwipeLeft() {
                super.onSwipeLeft();
                // отиваме в ended
                findViewById(R.id.client_TV_HM_Ended).callOnClick();
            }
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                // отиваме в courses/all
                findViewById(R.id.client_TV_HM_All).callOnClick();
            }
        });
        // ----- ENDED
        findViewById(R.id.client_SV_Ended).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в ended
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                // отиваме в ongoing
                findViewById(R.id.client_TV_HM_Ongoing).callOnClick();
            }
        });
        findViewById(R.id.client_LL_Ended).setOnTouchListener(new OnSwipeTouchListener(ClientActivity.this) { // в ended
            @Override
            public void onSwipeRight() {
                super.onSwipeRight();
                // отиваме в ongoing
                findViewById(R.id.client_TV_HM_Ongoing).callOnClick();
            }
        });

        //Animation Loading
        animationLoading = (AnimationDrawable) findViewById(R.id.client_IV_LoadingBar).getBackground();
        animationLoading.setOneShot(false);
        animationLoading.start();

    }// end of InitializeUI()
    private ArrayList<CLCourse> courses = new ArrayList<CLCourse>();
    private String last_courseId_loaded = " ";
    private GradientDrawable gradientDrawableBackgroundCourses;

    private void loadCourses() {
        dbRefCourse.orderByKey().startAt(last_courseId_loaded).limitToFirst(10).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot DATA) {
                hideProgress();
                for(DataSnapshot dataSnapshot : DATA.getChildren()) {

                    boolean isCurrentLoaded = false;

                    for(int i=0; i<courses.size(); i++) {
                        if(courses.get(i).getCourseId().equals(dataSnapshot.getKey())) {
                            isCurrentLoaded = true; // вече имаме тази група в лейаута -> избягваме създаване на еднакви обекти (може вече да е зареден в ongoing || ended LinearLayout)
                            break;
                        }
                    }

                    last_courseId_loaded = dataSnapshot.getKey();

                    if(!isCurrentLoaded) {
                        // ----- Зареждаме в ALL
                        final CLCourse courseLayout = new CLCourse(getApplicationContext(), dataSnapshot.getKey(), dataSnapshot.child("descr").getValue(String.class), dataSnapshot.child("lect").getValue(String.class));
                        courseLayout.setId(View.generateViewId());

                        courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                        courses.add(courseLayout);
                        ((LinearLayout) findViewById(R.id.client_LL_All)).addView(courseLayout);

                        courseLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ClientActivity.this, CourseActivity.class);
                                intent.putExtra("courseId", courseLayout.getCourseId());
                                intent.putExtra("description", courseLayout.getDescription());
                                intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                startActivityForResult(intent, 1);
                            }
                        });

                        final int _20px = height/40;

                        setMargins(courseLayout, _20px, _20px, _20px, _20px / 8);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            courseLayout.setElevation(_20px / 10);
                        } else {
                            gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                        }
                        courseLayout.setBackground(gradientDrawableBackgroundCourses);

                        TextView titleView = new TextView(getApplicationContext());
                        titleView.setId(View.generateViewId());
                        courseLayout.addView(titleView);

                        ConstraintSet cs = new ConstraintSet();
                        cs.clone(courseLayout);
                        cs.connect(titleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, _20px);
                        cs.applyTo(courseLayout);

                        titleView.setText(courseLayout.getCourseId());
                        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                        titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                        titleView.setMaxLines(2);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(databaseError!=null) showAlert("Error", databaseError.getMessage());
                if(!isInternetAvailable()){}
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) { // потребителя е записал група в дисциплината, в която сме го закарали (CourseActivity)
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                final String courseId = data.getStringExtra("courseId");
                final String groupId = data.getStringExtra("groupId");

                // --
                for(int i=0; i<courses.size(); i++) {
                    if(courses.get(i).getCourseId().equals(courseId)) { // вече го имаме зареден от dbRefCourse.orderByKey().startAt(last_courseId_loaded).limitToFirst(10).addChildEventListener

                        final CLCourse courseLayout = courses.get(i);
                        courseLayout.setOnClickListener(new View.OnClickListener() { // ще променим кликъра да не тегли излишно от датабазата (знаем че е записан)
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ClientActivity.this, SignedCourseActivity.class);
                                intent.putExtra("courseId", courseLayout.getCourseId());
                                intent.putExtra("description", courseLayout.getDescription());
                                intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                intent.putExtra("groupId", groupId);
                                startActivity(intent);
                            }
                        });

                        Log.e("tag", "dbRefGroups.child(\"" + courseLayout.getCourseId() + "\").child(\"" + groupId + "\").child(\"end\")");
                        dbRefGroups.child(courseLayout.getCourseId()).child(groupId).child("end").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                ((ViewManager) courseLayout.getParent()).removeView(courseLayout);
                                if (dataSnapshot.getValue(Long.class) > System.currentTimeMillis()) { // отива в ONGOING
                                    ((LinearLayout) findViewById(R.id.client_LL_Ongoing)).addView(courseLayout);
                                } else { // отива в ENDED
                                    ((LinearLayout) findViewById(R.id.client_LL_Ended)).addView(courseLayout);
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) { }
                        });

                        return; // излизаме от onActivityResult()
                    }
                }

                //щом не сме return-али -> трябва да добавим дисциплината в лейаутите
                dbRefCourse.child(courseId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final CLCourse courseLayout = new CLCourse(getApplicationContext(), courseId, dataSnapshot.child("descr").getValue(String.class), dataSnapshot.child("lect").getValue(String.class));
                        courseLayout.setId(View.generateViewId());
                        courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                        courses.add(courseLayout);

                        ((ViewManager) courseLayout.getParent()).removeView(courseLayout);
                        ((LinearLayout) findViewById(R.id.client_LL_Ongoing)).addView(courseLayout); // отива в ongoing задължително

                        courseLayout.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(ClientActivity.this, SignedCourseActivity.class);
                                intent.putExtra("courseId", courseLayout.getCourseId());
                                intent.putExtra("description", courseLayout.getDescription());
                                intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                intent.putExtra("groupId", groupId);
                                startActivity(intent);
                            }
                        });

                        final int _20px = height/40;
                        setMargins(courseLayout, _20px, _20px, _20px, _20px / 8);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            courseLayout.setElevation(_20px / 10);
                        } else {
                            gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                        }
                        courseLayout.setBackground(gradientDrawableBackgroundCourses);

                        TextView titleView = new TextView(getApplicationContext());
                        titleView.setId(View.generateViewId());
                        courseLayout.addView(titleView);

                        ConstraintSet cs = new ConstraintSet();
                        cs.clone(courseLayout);
                        cs.connect(titleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, _20px);
                        cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, _20px);
                        cs.applyTo(courseLayout);

                        titleView.setText(courseLayout.getCourseId());
                        titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                        titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                        titleView.setMaxLines(2);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if(!isInternetAvailable()) {}
                        showAlert("Error", databaseError.getMessage());
                    }
                });
                // --

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult

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

    private AnimationDrawable animationLoading;
    private boolean isAnimationLoadingOn = false;
    private void showProgress(){
        findViewById(R.id.client_CL_Loading).setVisibility(View.VISIBLE);
        //isAnimationLoadingOn = true;
    }
    private void hideProgress(){
        //if(isAnimationLoadingOn) {
        findViewById(R.id.client_CL_Loading).setVisibility(View.INVISIBLE);
        //isAnimationLoadingOn = false;
        //}
    }

    private void showAlert(final String title, final String message){
        ((TextView)findViewById(R.id.client_TV_AlertTitle)).setText(title);
        ((TextView)findViewById(R.id.client_TV_AlertMessage)).setText(message);

        findViewById(R.id.client_CL_Alert).setBackground(new BitmapDrawable(getResources(), createBlurBitmapFromScreen()));
        findViewById(R.id.client_CL_Alert).setVisibility(View.VISIBLE);
        Animation expandIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.expand_in);
        findViewById(R.id.client_CL_AlertBox).startAnimation(expandIn);
    }

    private Bitmap createBlurBitmapFromScreen() {
        Bitmap bitmap = null;
        findViewById(R.id.client_CL_Main).setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(findViewById(R.id.client_CL_Main).getDrawingCache());
        findViewById(R.id.client_CL_Main).setDrawingCacheEnabled(false);
        bitmap = Bitmap.createScaledBitmap(bitmap, 480, 800, false);

        Bitmap result = null;
        try {
            RenderScript rsScript = RenderScript.create(getApplicationContext());
            Allocation alloc = Allocation.createFromBitmap(rsScript, bitmap);

            ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
            blur.setRadius(21);
            blur.setInput(alloc);

            result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Allocation outAlloc = Allocation.createFromBitmap(rsScript, result);

            blur.forEach(outAlloc);
            outAlloc.copyTo(result);

            rsScript.destroy();
        } catch (Exception e) {
            return bitmap;
        }
        return result;
    }

    //Utility
    @Override
    public void onBackPressed() { // block finish()
        moveTaskToBack(true);
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected) showAlert("Alert", "No internet connection.");
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
