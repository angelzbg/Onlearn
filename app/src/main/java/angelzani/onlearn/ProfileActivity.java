package angelzani.onlearn;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ProfileActivity extends AppCompatActivity {

    //Display metrics
    private int width, height;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mRef, dbRefUsers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        setContentView(R.layout.activity_profile);

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference(); //root
        dbRefUsers=mRef.child("users");

        initializeUI();
    }// End onCreate()

    private void initializeUI()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        int _20px = height/40, _10px=height/80, _40px=height/20, _50px=height/16;

        // Header
        findViewById(R.id.profile_CL_header).getLayoutParams().height=height/16;
        findViewById(R.id.profile_IB_back).getLayoutParams().width=height/20;
        findViewById(R.id.profile_IB_back).getLayoutParams().height=height/20;
        findViewById(R.id.profile_IB_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setMargins(findViewById(R.id.profile_IB_back), height/80, 0, 0, 0);
        ((TextView)findViewById(R.id.profile_TV_head)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);

        // Info
        ((TextView)findViewById(R.id.profile_TV_email)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        findViewById(R.id.profile_TV_email).setPadding(_10px,0,0,0);
        ((TextView)findViewById(R.id.profile_TV_email)).setText(user.getEmail());
        ((TextView)findViewById(R.id.profile_TV_member)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/53);
        findViewById(R.id.profile_TV_member).setPadding(_10px,0,0,0);
        Date date=new Date(user.getMetadata().getCreationTimestamp());
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
        ((TextView)findViewById(R.id.profile_TV_member)).setText("Member since: "+df2.format(date));

        //Name
        findViewById(R.id.profile_IV_name).getLayoutParams().height=height/8;
        findViewById(R.id.profile_IV_name).getLayoutParams().width=height/8;
        setMargins(findViewById(R.id.profile_IV_name), _10px,0,0,0);
        ((TextView)findViewById(R.id.profile_TV_name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.profile_TV_name).setPadding(_10px,0,_10px,0);
        findViewById(R.id.profile_IV_editName).getLayoutParams().height=_40px;
        findViewById(R.id.profile_IV_editName).getLayoutParams().width=_40px;
        setMargins(findViewById(R.id.profile_IV_editName), 0,_10px,_10px,0);

        // Address
        findViewById(R.id.profile_IV_address).getLayoutParams().height=_50px;
        findViewById(R.id.profile_IV_address).getLayoutParams().width=_50px;
        setMargins(findViewById(R.id.profile_IV_address), 0,_20px,0,0);
        ((TextView)findViewById(R.id.profile_TV_address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.profile_TV_address).setPadding(_10px,0,_10px,0);
        findViewById(R.id.profile_IV_editAddress).getLayoutParams().height=_40px;
        findViewById(R.id.profile_IV_editAddress).getLayoutParams().width=_40px;
        setMargins(findViewById(R.id.profile_IV_editAddress), 0,0,_10px,0);

        //Phone
        findViewById(R.id.profile_IV_phone).getLayoutParams().height=_50px;
        findViewById(R.id.profile_IV_phone).getLayoutParams().width=_50px;
        setMargins(findViewById(R.id.profile_IV_phone), 0,_20px,0,0);
        ((TextView)findViewById(R.id.profile_TV_phone)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.profile_TV_phone).setPadding(_10px,0,_10px,0);
        findViewById(R.id.profile_IV_editPhone).getLayoutParams().height=_40px;
        findViewById(R.id.profile_IV_editPhone).getLayoutParams().width=_40px;
        setMargins(findViewById(R.id.profile_IV_editPhone), 0,0,_10px,0);

        //Date of birth
        findViewById(R.id.profile_IV_dob).getLayoutParams().height=_50px;
        findViewById(R.id.profile_IV_dob).getLayoutParams().width=_50px;
        setMargins(findViewById(R.id.profile_IV_dob), 0,_20px,0,0);
        ((TextView)findViewById(R.id.profile_TV_dob)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.profile_TV_dob).setPadding(_10px,0,_10px,0);
        findViewById(R.id.profile_IV_editDob).getLayoutParams().height=_40px;
        findViewById(R.id.profile_IV_editDob).getLayoutParams().width=_40px;
        setMargins(findViewById(R.id.profile_IV_editDob), 0,0,_10px,0);

        // Elevations
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.profile_CL_header).setElevation(_20px/4);
        }

        //logout
        ((TextView)findViewById(R.id.profile_TV_Logout)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        findViewById(R.id.profile_TV_Logout).setPadding(_10px,_20px,0,0);
        findViewById(R.id.profile_TV_Logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });


        //Firebase
        dbRefUsers.child(user.getUid()).child("name").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ((TextView)findViewById(R.id.profile_TV_name)).setText(dataSnapshot.getValue(String.class));
                } else {
                    ((TextView)findViewById(R.id.profile_TV_name)).setText("Unknown");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isInternetAvailable()){
                    //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_LONG).show();
                }
            }
        });
        dbRefUsers.child(user.getUid()).child("adr").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ((TextView)findViewById(R.id.profile_TV_address)).setText(dataSnapshot.getValue(String.class));
                } else {
                    ((TextView)findViewById(R.id.profile_TV_address)).setText("Unknown");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isInternetAvailable()){
                    //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_LONG).show();
                }
            }
        });
        dbRefUsers.child(user.getUid()).child("phone").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ((TextView)findViewById(R.id.profile_TV_phone)).setText(dataSnapshot.getValue(String.class));
                } else {
                    ((TextView)findViewById(R.id.profile_TV_phone)).setText("Unknown");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isInternetAvailable()){
                    //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_LONG).show();
                }
            }
        });
        dbRefUsers.child(user.getUid()).child("dob").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    ((TextView)findViewById(R.id.profile_TV_dob)).setText(dataSnapshot.getValue(String.class));
                } else {
                    ((TextView)findViewById(R.id.profile_TV_dob)).setText("Unknown");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if(!isInternetAvailable()){
                    //Toast.makeText(getApplicationContext(),"No internet",Toast.LENGTH_LONG).show();
                }
            }
        });

        // Edit functionality
        findViewById(R.id.profile_IV_editName).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
                alert.setTitle("Edit name : ");
                //lert.setMessage("Message :");

                // Set an EditText view to get user input
                final EditText input = new EditText(ProfileActivity.this);
                alert.setView(input);
                input.setText(((TextView)findViewById(R.id.profile_TV_name)).getText());

                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(input.getText().toString().trim().length()<2){
                            showAlert("Alert", "Name must contain at least 2 characters.");
                            //Toast.makeText(getApplicationContext(),"Name must contain at least 2 characters.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(input.getText().toString().trim().length()>100){
                            showAlert("Alert", "Name must contain no more than 100 characters.");
                            //Toast.makeText(getApplicationContext(),"Name must contain no more than 100 characters.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        dbRefUsers.child(user.getUid()).child("name").setValue(input.getText().toString().trim(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    showAlert("Error", databaseError.getMessage());
                                    //Toast.makeText(getApplicationContext(),databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
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
            //Address
        findViewById(R.id.profile_IV_editAddress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
                alert.setTitle("Edit address : ");
                //lert.setMessage("Message :");

                // Set an EditText view to get user input
                final EditText input = new EditText(ProfileActivity.this);
                alert.setView(input);
                input.setText(((TextView)findViewById(R.id.profile_TV_address)).getText());

                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if(input.getText().toString().trim().length()<6){
                            showAlert("Alert", "Address must contain at least 6 characters.");
                            //Toast.makeText(getApplicationContext(),"Address must contain at least 6 characters.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        if(input.getText().toString().trim().length()>100){
                            showAlert("Alert", "Address must contain no more than 100 characters.");
                            //Toast.makeText(getApplicationContext(),"Address must contain no more than 100 characters.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        dbRefUsers.child(user.getUid()).child("adr").setValue(input.getText().toString().trim(), new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    showAlert("Error", databaseError.getMessage());
                                    //Toast.makeText(getApplicationContext(),databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
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

        //Phone
        findViewById(R.id.profile_IV_editPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(ProfileActivity.this);
                alert.setTitle("Edit phone : ");
                //lert.setMessage("Message :");

                // Set an EditText view to get user input
                final EditText input = new EditText(ProfileActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
                alert.setView(input);
                input.setText(((TextView)findViewById(R.id.profile_TV_phone)).getText());

                alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        if(input.getText().toString().trim().length()>50){
                            showAlert("Alert", "Phone must contain no more than 50 characters.");
                            // Toast.makeText(getApplicationContext(),"Phone must contain no more than 50 characters.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        String phone = input.getText().toString().trim();
                        if(phone.isEmpty()) phone = null;
                        dbRefUsers.child(user.getUid()).child("phone").setValue(phone, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                if(databaseError!=null){
                                    showAlert("Error", databaseError.getMessage());
                                    //Toast.makeText(getApplicationContext(),databaseError.getMessage(), Toast.LENGTH_LONG).show();
                                }
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
        //Date
        findViewById(R.id.profile_IV_editDob).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(ProfileActivity.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth, mDateSetListener, year,month,day);
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
                dbRefUsers.child(user.getUid()).child("dob").setValue(date, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if(databaseError!=null){
                            showAlert("Error", databaseError.getMessage());
                            //oast.makeText(getApplicationContext(),databaseError.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        };

        // Alert Message
        findViewById(R.id.profile_CL_Alert).setPadding(height/80,height/80,height/80,height/80);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E9FFFFFF"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadius(_20px/2);
        findViewById(R.id.profile_CL_AlertBox).setBackground(gradientDrawable);

        ((TextView)findViewById(R.id.profile_TV_AlertTitle)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        findViewById(R.id.profile_TV_AlertTitle).setPadding(0,height/80,0,height/80);

        ((TextView)findViewById(R.id.profile_TV_AlertMessage)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.profile_TV_AlertMessage).setPadding(_20px,_20px,_20px,_20px);

        ((TextView)findViewById(R.id.profile_TV_AlertClose)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        findViewById(R.id.profile_TV_AlertClose).getLayoutParams().width = height/16;
        findViewById(R.id.profile_TV_AlertClose).getLayoutParams().height = findViewById(R.id.profile_TV_AlertClose).getLayoutParams().width;
        setMargins(findViewById(R.id.profile_TV_AlertClose), 0,0,0,height/80);
        gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E5EEFC"));
        gradientDrawable.setShape(GradientDrawable.OVAL);
        gradientDrawable.setStroke(1, Color.parseColor("#82B1FF"));
        findViewById(R.id.profile_TV_AlertClose).setBackground(gradientDrawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.profile_CL_Alert).setElevation(_20px*2);
            findViewById(R.id.profile_CL_AlertBox).setElevation(_20px/4);
        }
        // Alert dialog close
        findViewById(R.id.profile_TV_AlertClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findViewById(R.id.profile_CL_Alert).setVisibility(View.INVISIBLE);
            }
        });


    }// end of initializeUI()
    private DatePickerDialog.OnDateSetListener mDateSetListener;

    //Alert
    private void showAlert(final String title, final String message){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.profile_TV_AlertTitle)).setText(title);
                ((TextView)findViewById(R.id.profile_TV_AlertMessage)).setText(message);

                findViewById(R.id.profile_CL_Alert).setBackground(new BitmapDrawable(getResources(), createBlurBitmapFromScreen()));
                findViewById(R.id.profile_CL_Alert).setVisibility(View.VISIBLE);
                Animation expandIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.expand_in);
                findViewById(R.id.profile_CL_AlertBox).startAnimation(expandIn);
            }
        }, 250);
    }

    private Bitmap createBlurBitmapFromScreen() {
        Bitmap bitmap = null;
        findViewById(R.id.profile_CL_main).setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(findViewById(R.id.profile_CL_main).getDrawingCache());
        findViewById(R.id.profile_CL_main).setDrawingCacheEnabled(false);
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
