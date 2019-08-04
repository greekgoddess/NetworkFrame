package com.ding.netframework;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ding.networkframe.ImageRequest;
import com.ding.networkframe.NetRequestManager;
import com.ding.networkframe.NetRequetsError;
import com.ding.networkframe.Request;
import com.ding.networkframe.Response;

public class MainActivity extends AppCompatActivity {
    private Button textView;
    private Button close;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetRequestManager.init(null);
        setContentView(R.layout.activity_main);

        close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetRequestManager.getInstance().stop();
            }
        });
        textView = findViewById(R.id.text);
        tv = findViewById(R.id.dae);

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String url = "http://apis.juhe.cn/simpleWeather/query?key=c9d90fd76995a9f06acba84e9f9469b1&";
//                String url = "http://ask.csdn.net/questions/701988";
                String url = "http://imgsrc.baidu.com/forum/pic/item/78310a55b319ebc41b5e01e88c26cffc1e171698.jpg";

//                com.ding.networkframe.Request request = new com.ding.networkframe.StringRequest(
//                        Request.Method.POST,
//                        url,
//                        new com.ding.networkframe.Response.Listener<String>() {
//                            @Override
//                            public void onResponse(String data) {
//                                tv.setText(data);
//                            }
//                        },
//                        new com.ding.networkframe.Response.ErrorListener() {
//                            @Override
//                            public void onError(NetRequetsError error) {
//                                Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
//                            }
//                        }
//                );
//                request.setShouldCache(false);
//                request.addHttpParam("city", "南京");
//                NetRequestManager.getInstance().request(request);

                ImageRequest request = new ImageRequest(Request.Method.GET, url,
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap data) {
                                textView.setBackgroundDrawable(new BitmapDrawable(data));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onError(NetRequetsError error) {

                    }
                });
                NetRequestManager.getInstance().request(request);
            }
        });
    }
}
