package angelzani.onlearn;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewManager;
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

import java.util.ArrayList;

import angelzani.onlearn.UIClasses.CLCourse;

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
        findViewById(R.id.client_CL_SearchWrap).setPadding(_20px,_20px,_20px,0);
        findViewById(R.id.client_TV_Search).setPadding(0,_20px/2,_20px*2,_20px/2);
        ((TextView)findViewById(R.id.client_TV_Search)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        GradientDrawable gradientDrawableBackgroundSearch = new GradientDrawable();
        gradientDrawableBackgroundSearch.setColor(Color.parseColor("#ffffff"));
        gradientDrawableBackgroundSearch.setStroke(1, Color.parseColor("#3f3f3f"));
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

                        final int currentCoursesSize = courses.size(); // понеже следим за нови дисцплини от датабазата и може да се получат разни...
                        for(int i=0; i<currentCoursesSize; i++) {
                            if(courses.get(i).getCourseId().toLowerCase().contains(searchString.toLowerCase())) {
                                final GradientDrawable gradientDrawableBackgroundCourses = new GradientDrawable();
                                gradientDrawableBackgroundCourses.setColor(Color.parseColor("#ffffff"));
                                //gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
                                gradientDrawableBackgroundCourses.setShape(GradientDrawable.RECTANGLE);
                                gradientDrawableBackgroundCourses.setCornerRadius(_20px/2);

                                final CLCourse courseLayout = new CLCourse(getApplicationContext(), courses.get(i).getCourseId(), courses.get(i).getDescription(), courses.get(i).getLecturerId());
                                courseLayout.setId(View.generateViewId());

                                courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                                client_LL_SearchResult.addView(courseLayout);

                                courseLayout.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //Теглене и проверка дали потребителя е в този курс: ако не е -> CourseActivity , ако е -> SignedCourseActivity
                                        dbRefParticipation.child(courseLayout.getCourseId()).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                                                if (!isInternetAvailable())
                                                    Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                                                if (databaseError != null)
                                                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
                                // KOE VIEW , КОЯ СТРАНА, ЗА КОЕ VIEW, ЗА КОЕ СТРАНА, КОЛКО ОТСТОЯНИЕ
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
        }

        // ---------- Load Courses
        final GradientDrawable gradientDrawableBackgroundCourses = new GradientDrawable();
        gradientDrawableBackgroundCourses.setColor(Color.parseColor("#ffffff"));
        //gradientDrawableBackgroundCourses.setStroke(1, Color.parseColor("#000000"));
        gradientDrawableBackgroundCourses.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackgroundCourses.setCornerRadius(_20px/2);

        dbRefCourse.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                // ----- Зареждаме в ALL
                final CLCourse courseLayout = new CLCourse(getApplicationContext(), dataSnapshot.getKey(), dataSnapshot.child("descr").getValue(String.class), dataSnapshot.child("lect").getValue(String.class));
                courseLayout.setId(View.generateViewId());

                courseLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                courses.add(courseLayout);
                ((LinearLayout) findViewById(R.id.client_LL_All)).addView(courseLayout);

                courseLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Теглене и проверка дали потребителя е в този курс: ако не е -> CourseActivity , ако е -> SignedCourseActivity
                        dbRefParticipation.child(courseLayout.getCourseId()).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
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
                                if (!isInternetAvailable())
                                    Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                                if (databaseError != null)
                                    Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
                // KOE VIEW , КОЯ СТРАНА, ЗА КОЕ VIEW, ЗА КОЕ СТРАНА, КОЛКО ОТСТОЯНИЕ
                cs.connect(titleView.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START, _20px);
                cs.connect(titleView.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, _20px);
                cs.connect(titleView.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, _20px);
                cs.connect(titleView.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, _20px);
                cs.applyTo(courseLayout);

                titleView.setText(courseLayout.getCourseId());
                titleView.setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
                titleView.setTextColor(getResources().getColor(R.color.light_blue3));
                titleView.setMaxLines(2);

                // ----- Зареждаме в ONGOING || ENDED
                dbRefParticipation.child(courseLayout.getCourseId()).child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final String groupIdCurrentBlock = dataSnapshot.getValue(String.class);
                        if (dataSnapshot.exists()) { // записан е в тази дисциплина

                            courseLayout.setOnClickListener(new View.OnClickListener() { // ще променим кликъра да не тегли излишно от датабазата (знаем че е записан)
                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(ClientActivity.this, SignedCourseActivity.class);
                                    intent.putExtra("courseId", courseLayout.getCourseId());
                                    intent.putExtra("description", courseLayout.getDescription());
                                    intent.putExtra("lecturerId", courseLayout.getLecturerId());
                                    intent.putExtra("groupId", groupIdCurrentBlock);
                                    startActivity(intent);
                                }
                            });

                            dbRefGroups.child(courseLayout.getCourseId()).child(dataSnapshot.getValue(String.class)).child("end").addListenerForSingleValueEvent(new ValueEventListener() {
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
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        if (!isInternetAvailable())
                            Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                        if (databaseError != null)
                            Toast.makeText(getApplicationContext(), "Load ongoing/ended: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
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
                if(databaseError!=null) Toast.makeText(getApplicationContext(), "Load Courses db error : " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                if(!isInternetAvailable()){
                    Toast.makeText(getApplicationContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                }
            }
        });

    }// end of InitializeUI()
    private ArrayList<CLCourse> courses = new ArrayList<CLCourse>();

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

    // ----- Scroll Logic
    private void hideHeader() {
        findViewById(R.id.client_CL_Head).setX(0);
        findViewById(R.id.client_CL_Head).setY(-(height/16));
    }
    private void showHeader() {
        findViewById(R.id.client_CL_Head).setX(0);
        findViewById(R.id.client_CL_Head).setY(height/16);
    }


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
