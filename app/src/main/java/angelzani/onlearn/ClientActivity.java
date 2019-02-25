package angelzani.onlearn;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.annotation.NonNull;
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

public class ClientActivity extends AppCompatActivity {

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
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // Екрана в нормално състояние без да се завърта повече
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

        //za trirnr
        findViewById(R.id.client_CL_OngoingLayout).setOnClickListener(new View.OnClickListener() {
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
                startActivityForResult(new Intent(ClientActivity.this, ProfileActivity.class),1);
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

        // ---------- Elevations
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            findViewById(R.id.client_LL_HeadMenu).setElevation(_20px/4);
        }

    }// end of InitializeUI()

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
