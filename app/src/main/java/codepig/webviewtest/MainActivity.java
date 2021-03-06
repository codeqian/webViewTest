package codepig.webviewtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    private EditText url_t,userAgent_t;
    private Button goBtn,jsTestBtn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        url_t=(EditText) findViewById(R.id.url_t);
        userAgent_t=(EditText) findViewById(R.id.userAgent_t);
        goBtn=(Button) findViewById(R.id.goBtn);
        jsTestBtn=(Button) findViewById(R.id.jsTestBtn);
        goBtn.setOnClickListener(clickBtn);
        jsTestBtn.setOnClickListener(clickBtn);
    }

    View.OnClickListener clickBtn = new Button.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent;
            switch (v.getId()){
                case R.id.goBtn:
                    intent=new Intent(getApplication(), webActivity.class);
                    intent.putExtra("webUrl", url_t.getText().toString());
                    intent.putExtra("userAgent", userAgent_t.getText().toString());
                    startActivity(intent);
                    break;
                case R.id.jsTestBtn:
                    intent=new Intent(getApplication(), webActivity.class);
                    intent.putExtra("webUrl", "file:///android_asset/test.html");
                    intent.putExtra("userAgent", "jsTest");
                    startActivity(intent);
                    break;
            }
        }
    };
}
