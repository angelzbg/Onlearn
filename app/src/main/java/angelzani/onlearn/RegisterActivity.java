package angelzani.onlearn;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

        initialiseUI();

        //Firebase
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mRef = mDatabase.getReference();

    } // End OnCreate

    private void initialiseUI(){
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        int _20px = height/40;

        findViewById(R.id.register_CL_header).getLayoutParams().height=height/16;
        findViewById(R.id.register_IB_back).getLayoutParams().width=height/20;
        findViewById(R.id.register_IB_back).getLayoutParams().height=height/20;
        ((TextView)findViewById(R.id.register_TV_head)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/33);
        ((Button)findViewById(R.id.register_B_signup)).setTextSize(TypedValue.COMPLEX_UNIT_PX, height/44);

        ((EditText)findViewById(R.id.register_ET_email)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText)findViewById(R.id.register_ET_password)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText)findViewById(R.id.register_ET_name)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText)findViewById(R.id.register_ET_address)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText)findViewById(R.id.register_ET_phone)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);
        ((EditText)findViewById(R.id.register_ET_date)).setTextSize(TypedValue.COMPLEX_UNIT_PX, _20px);

        findViewById(R.id.register_LL_regBox).setPadding(height/20,height/20,height/20,height/20);
        setMargins(findViewById(R.id.register_LL_regBox), _20px,_20px,_20px,_20px);

        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(Color.parseColor("#E5EEFC"));
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setCornerRadii(new float[] { _20px*2, _20px*2, _20px*4, _20px*4, _20px*2, _20px*2, _20px*4, _20px*4 });
        findViewById(R.id.register_LL_regBox).setBackground(gradientDrawable);

    }

    //Utility
    private void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

} // End Class
