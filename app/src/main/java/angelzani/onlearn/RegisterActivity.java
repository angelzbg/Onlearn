package angelzani.onlearn;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
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
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.register_CL_header).setElevation(_20px/4);
            findViewById(R.id.register_LL_regBox).setElevation(_20px/2);
        }

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

    } //end initializeUI()

    private void registerNewUser(final String email, final String password, final String name, final String address, final String phone, final String dob) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    finalizeRegistration(email, password, name, address, phone, dob);

                } else {
                    // If sign in fails, display a message to the user.
                    //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    //updateUI(null);
                }
            }
        });
    }//end registerNewUser()

    private void finalizeRegistration(final String email, final String password, final String name, final String address, final String phone, final String dob){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {

                    final FirebaseUser user = mAuth.getCurrentUser();
                    final DatabaseReference dbRefUser = mRef.child("users").child(user.getUid());
                    dbRefUser.child("name").setValue(name);
                    dbRefUser.child("adr").setValue(address);
                    dbRefUser.child("dob").setValue(dob);
                    dbRefUser.child("phone").setValue(phone);

                    startActivity(new Intent(RegisterActivity.this, ClientActivity.class));
                    finish();

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
            }
        });
    }

    /*----- On Click Listeners [ START ] -----*/

    View.OnClickListener registerListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String email = ((EditText)findViewById(R.id.register_ET_email)).getText().toString().trim();
            if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                Toast.makeText(getApplicationContext(), "Please enter valid email address.", Toast.LENGTH_LONG).show();
                return;
            }

            String password = ((EditText)findViewById(R.id.register_ET_password)).getText().toString().trim();
            if(password.length() < 6) {
                Toast.makeText(getApplicationContext(), "Password must contain at least 6 characters.", Toast.LENGTH_LONG).show();
                return;
            }

            String name=((EditText)findViewById(R.id.register_ET_name)).getText().toString().trim();
            if(name.length()<2){
                Toast.makeText(getApplicationContext(), "Name must contain at least 2 characters.", Toast.LENGTH_LONG).show();
                return;
            }

            String dob = null, address=null, phone = null;
            if(!((EditText)findViewById(R.id.register_ET_address)).getText().toString().trim().isEmpty()) address=((EditText)findViewById(R.id.register_ET_address)).getText().toString().trim();

            if(!((EditText)findViewById(R.id.register_ET_phone)).getText().toString().trim().isEmpty()) phone=((EditText)findViewById(R.id.register_ET_phone)).getText().toString().trim();

            if(!((TextView)findViewById(R.id.register_TV_dob)).getText().toString().trim().isEmpty()) dob=((TextView)findViewById(R.id.register_TV_dob)).getText().toString();

            if(!isInternetAvailable()) {
                Toast.makeText(getApplicationContext(), "No Internet Connection.", Toast.LENGTH_LONG).show();
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
        return isConnected;
    }


} // End Class
