package angelzani.onlearn;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    //Display metrics
    private int width, height;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT); // Екрана в нормално състояние без да се завърта повече
        setContentView(R.layout.activity_login);

        initializeUI();

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(); //root

    }//end onCreate()

    @Override
    public void onStart() { // този метод следва веднага след onCreate()
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null){ // логнати сме
            //теглим атрибут role и роверяваме дали е client или admin -> ClientActivity || AdminActivity
            showProgress();
            mRef.child("users").child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.getValue(String.class).equals("admin")) startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                    else startActivity(new Intent(LoginActivity.this, ClientActivity.class));
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    if(!isInternetAvailable()) {
                        hideProgress();
                        Toast.makeText(getApplicationContext(), "No Internet Connection.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else hideProgress();
    }//end onStart()

    private void initializeUI(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        int _20px = height/40, _18px = height/44;
        ((EditText) findViewById(R.id.login_ET_Email)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText) findViewById(R.id.login_ET_Password)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((TextView) findViewById(R.id.login_TV_Login)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _18px);
        ((TextView) findViewById(R.id.login_TV_Register)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _18px);
        findViewById(R.id.login_CL_RegisterBox).setPadding(_20px*2,_20px*2,_20px*2,_20px*2);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E5EEFC"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[] { _20px*2, _20px*2, _20px*4, _20px*4, _20px*2, _20px*2, _20px*4, _20px*4 });
        findViewById(R.id.login_CL_RegisterWrap).setBackground(gradientDrawable);

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#80ccfc"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[] { 0, 0, _20px, _20px, _20px, _20px, 0, 0 });
        findViewById(R.id.login_TV_Login).setBackground(gradientDrawable);
        findViewById(R.id.login_TV_Login).setPadding(0,height/53,0,height/53);

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#00000000"));
        gradientDrawable.setStroke(height/400, Color.parseColor("#80ccfc"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[] { _20px, _20px, 0, 0, 0, 0, _20px, _20px });
        findViewById(R.id.login_TV_Register).setBackground(gradientDrawable);
        findViewById(R.id.login_TV_Register).setPadding(0,height/53,0,height/53);

        ((TextView) findViewById(R.id.login_TV_Logo)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/10);

        ConstraintSet cs = new ConstraintSet();
        cs.clone((ConstraintLayout)findViewById(R.id.login_CL_RegisterBox));
        cs.connect(R.id.login_TI_Password, ConstraintSet.TOP, R.id.login_ET_Email, ConstraintSet.BOTTOM, _20px*2);
        cs.connect(R.id.login_TV_Register, ConstraintSet.TOP, R.id.login_TI_Password, ConstraintSet.BOTTOM, _20px*2);
        cs.applyTo((ConstraintLayout)findViewById(R.id.login_CL_RegisterBox));

        findViewById(R.id.login_IV_LoadingBar).getLayoutParams().width = width/5;
        findViewById(R.id.login_IV_LoadingBar).getLayoutParams().height = findViewById(R.id.login_IV_LoadingBar).getLayoutParams().width;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.login_CL_RegisterWrap).setElevation(_20px/2);
            findViewById(R.id.login_TV_Logo).setElevation(_20px/2);
        }

        animationLoading = (AnimationDrawable) findViewById(R.id.login_IV_LoadingBar).getBackground();
        animationLoading.setOneShot(false);
        animationLoading.start();

        //On Click Listeners
        findViewById(R.id.login_TV_Register).setOnClickListener(goToRegister);
        findViewById(R.id.login_TV_Login).setOnClickListener(loginListener);
    }

    /*----- OnClickListeners [ START ] -----*/

    //Go To Register Activity
    View.OnClickListener goToRegister = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent =  new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
    };

    //Login
    View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String email = ((EditText)findViewById(R.id.login_ET_Email)).getText().toString().trim();
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(getApplicationContext(), "Please enter valid email address.", Toast.LENGTH_LONG).show();
                return;
            }

            final String password = ((EditText)findViewById(R.id.login_ET_Password)).getText().toString().trim();
            if(password.length() < 6) {
                Toast.makeText(getApplicationContext(), "Password must contain at least 6 characters.", Toast.LENGTH_LONG).show();
                return;
            }

            if(!isInternetAvailable()) {
                Toast.makeText(getApplicationContext(), "No Internet Connection.", Toast.LENGTH_LONG).show();
                return;
            }

            login(email, password);
        }
    };

    /*----- OnClickListeners [  END  ] -----*/

    private void login(final String email, final String password) {
        showProgress();
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    //Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    //updateUI(user);

                    mRef.child("users").child(user.getUid()).child("role").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.getValue(String.class).equals("admin")) startActivity(new Intent(LoginActivity.this, AdminActivity.class));
                            else {
                                startActivity(new Intent(LoginActivity.this, ClientActivity.class));
                                finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            if(!isInternetAvailable()) {
                                hideProgress();
                                Toast.makeText(getApplicationContext(), "No Internet Connection.", Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "signInWithEmail:failure", task.getException());
                    hideProgress();
                    Toast.makeText(LoginActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    //updateUI(null);
                }
            }
        });
    }//end login()

    private AnimationDrawable animationLoading;
    private boolean isAnimationLoadingOn = false;
    private void showProgress(){
        findViewById(R.id.login_CL_Loading).setVisibility(View.VISIBLE);
        //isAnimationLoadingOn = true;
    }
    private void hideProgress(){
        //if(isAnimationLoadingOn) {
            findViewById(R.id.login_CL_Loading).setVisibility(View.INVISIBLE);
            //isAnimationLoadingOn = false;
        //}
    }

    //Utility
    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }

}//end LoginActivity{}
