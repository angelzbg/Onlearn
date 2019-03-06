package angelzani.onlearn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
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
import android.text.Html;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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


import java.util.Calendar;
import java.util.Date;




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
    private ImageView backBtn;

    private int width, height;


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
        backBtn=findViewById(R.id.course_IV_back);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        final GradientDrawable gradientDrawableBackgroundCourses = new GradientDrawable();
        gradientDrawableBackgroundCourses.setColor(Color.parseColor("#ffffff"));
        gradientDrawableBackgroundCourses.setShape(GradientDrawable.RECTANGLE);
        gradientDrawableBackgroundCourses.setCornerRadii(new float[] { height/20, height/20, height/20, height/20, height/20, height/20, height/20, height/20});

        backBtn.getLayoutParams().height=height/11;
        backBtn.getLayoutParams().width=width/11;
        backBtn.setColorFilter(Color.parseColor("#ffffff"));

        courseName.getLayoutParams().height=height/15;
        courseName.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/30);
        courseName.setTextColor(getResources().getColor(R.color.white));

        desc.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/36);
        desc.setTextColor(getResources().getColor(R.color.light_blue3));
        desc.setPadding(height/40,height/100,height/70,0);
        desc.setBackground(gradientDrawableBackgroundCourses);

        lectureName.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/36);
        lectureName.setTextColor(getResources().getColor(R.color.light_blue3));
        lectureName.setPadding(height/40,height/150,height/40,0);

        lectureEmail.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/36);
        lectureEmail.setTextColor(getResources().getColor(R.color.light_blue3));
        lectureEmail.setPadding(height/40,height/150,height/40,height/80);



        final Intent intent=getIntent();

        final String courseId =intent.getStringExtra("courseId");
        String description=intent.getStringExtra("description");
        String lecturerId=intent.getStringExtra("lecturerId");


        final String d="<i><font color='#2676FF'>DESCRIPTION:  </font></i>";
        final String l="<i><font color='#2676FF'>LECTURE:  </font></i>";
        final String e="<i><font color='#2676FF'> EMAIL: </font>";
        final String k="<b><i><font color='#2676FF'>@</font></b>";








        courseName.setText(courseId);
        desc.setText(Html.fromHtml(d+description));


        dbRefUsers.child(lecturerId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                lectureName.setText(Html.fromHtml(l+name));
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
                lectureEmail.setText(Html.fromHtml(k+e+email));
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
                final String groupName = dataSnapshot.getKey();
                final int currentNumber= dataSnapshot.child("current").getValue(Integer.class);
                final int max= dataSnapshot.child("max").getValue(Integer.class);
                Long start=dataSnapshot.child("start").getValue(Long.class);
                Long end=dataSnapshot.child("end").getValue(Long.class);

                final ConstraintLayout groupLayout =  new ConstraintLayout(getApplicationContext());
                groupLayout.setId(View.generateViewId());
                groupLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                groups.addView(groupLayout);

                groupLayout.setBackground(gradientDrawableBackgroundCourses);
                groupLayout.setPadding(height/35,height/80,height/40,height/80);

                TextView groupNameTV = new TextView(getApplicationContext());
                groupNameTV.setId(View.generateViewId());
                groupLayout.addView(groupNameTV);
                groupNameTV.setText(groupName);

                groupNameTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/37);
                groupNameTV.setTextColor(Color.parseColor("#2676FF"));
                groupNameTV.setMaxLines(2);

                final TextView members=new TextView(getApplicationContext());
                members.setId(View.generateViewId());
                groupLayout.addView(members);


                members.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/36);
                members.setTextColor(Color.parseColor("#2676FF"));






                if(currentNumber<10) {
                    members.setText("        "+currentNumber + "/" + max);
                }
                else{
                    members.setText("      "+currentNumber + "/" + max);
                }
                if(currentNumber==max){
                    members.setTextColor(Color.parseColor("#D42121"));
                }
                Date startDateTime = new Date(start);
                Date endDateTime=new Date(end);

                Calendar cStart = Calendar.getInstance();
                Calendar cEnd = Calendar.getInstance();
                cStart.setTime(startDateTime);
                cEnd.setTime(endDateTime);
                int startDayOfYear = cStart.get(Calendar.DAY_OF_MONTH);
                int startMouth=cStart.get(Calendar.MONTH)+1; // +1 защото връща предишния месец
                int startYear=cStart.get(Calendar.YEAR);

                int endDayOfYear=cEnd.get(Calendar.DAY_OF_MONTH);
                int endMouth=cEnd.get(Calendar.MONTH)+1; // +1 защото връща предишния месец
                int endYear=cEnd.get(Calendar.YEAR);

                TextView startDate=new TextView(getApplicationContext());
                startDate.setId(View.generateViewId());
                groupLayout.addView(startDate);
                startDate.setText("Start date:  "+startDayOfYear+"/"+startMouth+"/"+startYear);
                startDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/38);
                startDate.setTextColor(getResources().getColor(R.color.light_blue3));

                TextView endDate=new TextView(getApplicationContext());
                endDate.setId(View.generateViewId());
                groupLayout.addView(endDate);
                endDate.setText("End date:    "+endDayOfYear+"/"+endMouth+"/"+endYear);
                endDate.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/38);
                endDate.setTextColor(getResources().getColor(R.color.light_blue3));



                final Button joinBtn = new Button(getApplicationContext());
                    joinBtn.setId(View.generateViewId());
                    joinBtn.setText("JOIN");
                    joinBtn.setTextColor(Color.parseColor("#ffffff"));
                    joinBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/28);



                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    groupLayout.setElevation(height/80);
                }
                joinBtn.setBackgroundResource(R.drawable.btnjoin);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    joinBtn.setOutlineSpotShadowColor(Color.parseColor("#A359A8"));
                }
                joinBtn.setShadowLayer(height/100,8,8,Color.parseColor("#738AFF"));



                final Button fullBtn =new Button(getApplicationContext());
                    fullBtn.setId(View.generateViewId());
                    fullBtn.setText("FULL");
                    fullBtn.setTextColor(Color.parseColor("#ffffff"));
                    fullBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, height/28);

                    fullBtn.setBackgroundResource(R.drawable.btnfull);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    joinBtn.setOutlineSpotShadowColor(Color.parseColor("#A359A8"));
                }
                joinBtn.setShadowLayer(height/100,8,8,Color.parseColor("#738AFF"));

                if(currentNumber<max){
                    groupLayout.addView(joinBtn);
                }
                else {
                    groupLayout.addView(fullBtn);
                }

               joinBtn.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View v) {
                       dbRefParticipation.child(courseId).child(user.getUid()).setValue(groupName, new DatabaseReference.CompletionListener() {
                           @Override
                           public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                               if(databaseError == null){ // няма грешки -> успешен запис
                                   // Да върнем в ClientActivity отговор, че сме записали дисциплина
                                   Intent returnIntent = new Intent();
                                   returnIntent.putExtra("courseId",courseId);
                                   returnIntent.putExtra("groupId",groupName);
                                   setResult(Activity.RESULT_OK, returnIntent);
                                   finish();
                               }
                               else{
                                   Toast.makeText(getApplicationContext(), "It seems that this group is already full.", Toast.LENGTH_SHORT).show();
                                   // Да направим обновление:
                                   groupLayout.removeView(joinBtn);
                                   groupLayout.addView(fullBtn);
                                   members.setText("      "+max + "/" + max);
                               }
                           }
                       });
                   }
               });



                ConstraintSet cs = new ConstraintSet();
                cs.clone(groupLayout);
                cs.connect(groupNameTV.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
                cs.connect(groupNameTV.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);

                cs.connect(startDate.getId(), ConstraintSet.TOP, groupNameTV.getId(), ConstraintSet.BOTTOM);
                cs.connect(startDate.getId(), ConstraintSet.START, groupNameTV.getId(), ConstraintSet.START);

                cs.connect(endDate.getId(), ConstraintSet.TOP, startDate.getId(), ConstraintSet.BOTTOM);
                cs.connect(endDate.getId(), ConstraintSet.START, startDate.getId(), ConstraintSet.START);

                cs.connect(members.getId(), ConstraintSet.TOP, groupNameTV.getId(), ConstraintSet.TOP);
                cs.connect(members.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END, width/9);

                if(currentNumber<max){
                    cs.connect(joinBtn.getId(), ConstraintSet.TOP, members.getId(), ConstraintSet.BOTTOM);
                    cs.connect(joinBtn.getId(), ConstraintSet.START, members.getId(), ConstraintSet.START );
                }
                else{
                    cs.connect(fullBtn.getId(), ConstraintSet.TOP, members.getId(), ConstraintSet.BOTTOM);
                    cs.connect(fullBtn.getId(), ConstraintSet.START, members.getId(), ConstraintSet.START);
                }


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


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
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
