package angelzani.onlearn;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity { // Ани

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
        setContentView(R.layout.activity_register);

        initializeUI();

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

    } // End OnCreate

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private void initializeUI(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        int _20px = height/40;

        findViewById(R.id.register_CL_header).getLayoutParams().height=height/16;
        findViewById(R.id.register_IB_back).getLayoutParams().width=height/20;
        findViewById(R.id.register_IB_back).getLayoutParams().height=height/20;
        setMargins(findViewById(R.id.register_IB_back), height/80, 0, 0, 0);
        ((TextView)findViewById(R.id.register_TV_head)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        ((Button)findViewById(R.id.register_B_signup)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);

        ((EditText)findViewById(R.id.register_ET_email)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText)findViewById(R.id.register_ET_password)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px); setMargins(findViewById(R.id.register_TI_Password), 0,_20px,0,0);
        ((EditText)findViewById(R.id.register_ET_name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px); setMargins(findViewById(R.id.register_ET_name), 0,_20px,0,0);
        ((EditText)findViewById(R.id.register_ET_address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px); setMargins(findViewById(R.id.register_ET_address), 0,_20px,0,0);
        ((EditText)findViewById(R.id.register_ET_phone)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px); setMargins(findViewById(R.id.register_ET_phone), 0,_20px,0,0);
        ((TextView)findViewById(R.id.register_TV_dob)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px); setMargins(findViewById(R.id.register_TV_dob), 0,_20px,0,0);

        findViewById(R.id.register_LL_regBox).setPadding(height/20,height/20,height/20,height/20);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E5EEFC"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[] { _20px*2, _20px*2, _20px*4, _20px*4, _20px*2, _20px*2, _20px*4, _20px*4 });
        findViewById(R.id.register_LL_regBox).setBackground(gradientDrawable);

        // Loading Progress Anim
        findViewById(R.id.register_IV_LoadingBar).getLayoutParams().width = width/4;
        findViewById(R.id.register_IV_LoadingBar).getLayoutParams().height = findViewById(R.id.register_IV_LoadingBar).getLayoutParams().width;

        // Alert Message
        findViewById(R.id.register_CL_Alert).setPadding(height/80,height/80,height/80,height/80);

        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E9FFFFFF"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(_20px/2);
        findViewById(R.id.register_CL_AlertBox).setBackground(gradientDrawable);

        ((TextView)findViewById(R.id.register_TV_AlertTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        findViewById(R.id.register_TV_AlertTitle).setPadding(0,height/80,0,height/80);

        ((TextView)findViewById(R.id.register_TV_AlertMessage)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.register_TV_AlertMessage).setPadding(_20px,_20px,_20px,_20px);

        ((TextView)findViewById(R.id.register_TV_AlertClose)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.register_TV_AlertClose).getLayoutParams().width = height/16;
        findViewById(R.id.register_TV_AlertClose).getLayoutParams().height = findViewById(R.id.register_TV_AlertClose).getLayoutParams().width;
        setMargins(findViewById(R.id.register_TV_AlertClose), 0,0,0,height/80);
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E5EEFC"));
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setStroke(1, Color.parseColor("#82B1FF"));
        findViewById(R.id.register_TV_AlertClose).setBackground(gradientDrawable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.register_CL_header).setElevation(_20px/4);
            findViewById(R.id.register_LL_regBox).setElevation(_20px/2);

            findViewById(R.id.register_CL_Loading).setElevation(_20px);
            findViewById(R.id.register_CL_Alert).setElevation(_20px*2);

            findViewById(R.id.register_CL_AlertBox).setElevation(_20px/4);
        }

        animationLoading = (AnimationDrawable) findViewById(R.id.register_IV_LoadingBar).getBackground();
        animationLoading.setOneShot(false);
        animationLoading.start();

        //OnClickListeners
        findViewById(R.id.register_IB_back).setOnClickListener(goBack);
        findViewById(R.id.register_B_signup).setOnClickListener(registerListener);
        findViewById(R.id.register_TV_dob).setOnClickListener(new View.OnClickListener() { // youtube.com/watch?v=hwe1abDO2Ag <- tutorial + github.com/mitchtabian/DatePickerDialog
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(RegisterActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                //Log.d(TAG, "onDateSet: mm/dd/yyy: " + month + "/" + day + "/" + year);

                String date = year + "-" + month + "-" + day;
                ((TextView)findViewById(R.id.register_TV_dob)).setText(date);
            }
        };
        // Alert dialog close
        findViewById(R.id.register_TV_AlertClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.register_CL_Alert).setVisibility(View.INVISIBLE);
            }
        });

    } //end initializeUI()

    private void registerNewUser(final String email, final String password, final String name, final String address, final String phone, final String dob) {
        showProgress();
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    finalizeRegistration(email, password, name, address, phone, dob);

                } else {
                    hideProgress();
                    showAlert("Error",task.getException().getMessage());
                    //Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }//end registerNewUser()

    private boolean registered = false;
    private void finalizeRegistration(final String email, final String password, final String name, final String address, final String phone, final String dob){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    final FirebaseUser user = mAuth.getCurrentUser();
                    final DatabaseReference dbRefUser = mRef.child("users").child(user.getUid());

                    mRef.child("users").child(user.getUid()).child("role").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && !registered)
                            {
                                dbRefUser.child("name").setValue(name);
                                dbRefUser.child("adr").setValue(address);
                                dbRefUser.child("dob").setValue(dob);
                                dbRefUser.child("phone").setValue(phone);

                                registered=true;

                                finish();
                                startActivity(new Intent(RegisterActivity.this, ClientActivity.class));
                                //finish();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            hideProgress();
                            showAlert("Error", databaseError.getMessage());
                            //Toast.makeText(RegisterActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });

                    /*mRef.child("users").child(user.getUid()).child("role").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists())
                            {
                                final DatabaseReference dbRefUser = mRef.child("users").child(user.getUid());

                                dbRefUser.child("name").setValue(name, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                        if(databaseError != null){
                                            Toast.makeText(RegisterActivity.this, "name: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                        } else {
                                            dbRefUser.child("adr").setValue(address, new DatabaseReference.CompletionListener() {
                                                @Override
                                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                    if(databaseError != null){
                                                        Toast.makeText(RegisterActivity.this, "adr: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                    } else {
                                                        dbRefUser.child("phone").setValue(phone, new DatabaseReference.CompletionListener() {
                                                            @Override
                                                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                if(databaseError != null){
                                                                    Toast.makeText(RegisterActivity.this, "phone: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                                } else {
                                                                    dbRefUser.child("dob").setValue(dob, new DatabaseReference.CompletionListener() {
                                                                        @Override
                                                                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                                                            if(databaseError != null){
                                                                                Toast.makeText(RegisterActivity.this, "dob: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                                                            } else {
                                                                                startActivity(new Intent(RegisterActivity.this, ClientActivity.class));
                                                                                finish();
                                                                            }
                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });

                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toast.makeText(RegisterActivity.this, databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });*/
                }
                else hideProgress();
            }
        });
    }

    /*----- On Click Listeners [ START ] -----*/

    View.OnClickListener registerListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String email = ((EditText)findViewById(R.id.register_ET_email)).getText().toString().trim();
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                showAlert("Alert","Email must be valid.");
             //   Toast.makeText(getApplicationContext(), "Please enter valid email address.", Toast.LENGTH_LONG).show();
                return;
            }

            String password = ((EditText)findViewById(R.id.register_ET_password)).getText().toString().trim();
            if(password.length() < 6) {
                showAlert("Alert","Password must contain at least 6 characters.");
                //Toast.makeText(getApplicationContext(), "Password must contain at least 6 characters.", Toast.LENGTH_LONG).show();
                return;
            }

            String name=((EditText)findViewById(R.id.register_ET_name)).getText().toString().trim();
            if(name.length()<2){
                showAlert("Alert","Name must contain at least 2 characters.");
                Toast.makeText(getApplicationContext(), "Name must contain at least 2 characters.", Toast.LENGTH_LONG).show();
                return;
            }

            String dob = null, address=null, phone = null;
            if(!((EditText)findViewById(R.id.register_ET_address)).getText().toString().trim().isEmpty()) address=((EditText)findViewById(R.id.register_ET_address)).getText().toString().trim();

            if(!((EditText)findViewById(R.id.register_ET_phone)).getText().toString().trim().isEmpty()) phone=((EditText)findViewById(R.id.register_ET_phone)).getText().toString().trim();

            if(!((TextView)findViewById(R.id.register_TV_dob)).getText().toString().trim().isEmpty()) dob=((TextView)findViewById(R.id.register_TV_dob)).getText().toString();

            if(!isInternetAvailable()) {
                //Toast.makeText(getApplicationContext(), "No Internet Connection.", Toast.LENGTH_LONG).show();
                return;
            }

            registerNewUser(email,password,name,address,phone, dob);

        }
    };


    View.OnClickListener goBack = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /*----- On Click Listeners [  END  ] -----*/

    private AnimationDrawable animationLoading;
    private boolean isAnimationLoadingOn = false;
    private void showProgress(){
        findViewById(R.id.register_CL_Loading).setVisibility(View.VISIBLE);
        //isAnimationLoadingOn = true;
    }
    private void hideProgress(){
        //if(isAnimationLoadingOn) {
        findViewById(R.id.register_CL_Loading).setVisibility(View.INVISIBLE);
        //isAnimationLoadingOn = false;
        //}
    }

    private void showAlert(final String title, final String message){
        ((TextView)findViewById(R.id.register_TV_AlertTitle)).setText(title);
        ((TextView)findViewById(R.id.register_TV_AlertMessage)).setText(message);

        findViewById(R.id.register_CL_Alert).setBackground(new BitmapDrawable(getResources(), createBlurBitmapFromScreen()));
        findViewById(R.id.register_CL_Alert).setVisibility(View.VISIBLE);
        Animation expandIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.expand_in);
        findViewById(R.id.register_CL_AlertBox).startAnimation(expandIn);
    }

    private Bitmap createBlurBitmapFromScreen() {
        Bitmap bitmap = null;
        findViewById(R.id.register_CL_main).setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(findViewById(R.id.register_CL_main).getDrawingCache());
        findViewById(R.id.register_CL_main).setDrawingCacheEnabled(false);
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
    private void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected) showAlert("Alert", "No internet connection.");
        return isConnected;
    }


} // End Class
