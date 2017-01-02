package cn.edu.gdmec.s07150638.myweather;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

public class MainActivity extends Activity implements Runnable{

    HttpURLConnection httpConn = null;
    InputStream din = null;
    Vector<String> cityname = new Vector<String>();
    Vector<String> low = new Vector<String>();
    Vector<String> high = new Vector<String>();
    Vector<String> icon = new Vector<String>();
    Vector<String> summary = new Vector<String>();
    Vector<Bitmap> bitmap = new Vector<Bitmap>();

    int weatherIndex[] = new int[20];
    String city = "guangzhou";
    boolean bPress = false;
    boolean bHasData = false;
    LinearLayout body;
    Button find;
    EditText value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("天气查询");

        body = (LinearLayout) findViewById(R.id.my_body);
        find = (Button) findViewById(R.id.find);
        value = (EditText) findViewById(R.id.value);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                body.removeAllViews();
                city = value.getText().toString();
                Toast.makeText(MainActivity.this,
                        "正在查询天气...",Toast.LENGTH_LONG).show();
                Thread th = new Thread(MainActivity.this);
                th.start();
            }
        });
    }

    @Override
    public void run() {

        cityname.removeAllElements();
        low.removeAllElements();
        high.removeAllElements();
        icon.removeAllElements();
        bitmap.removeAllElements();
        summary.removeAllElements();

        parseData();
        downImage();

        Message message = new Message();
        message.what = 1;
        handler.sendMessage(message);


    }

    public void parseData(){
        int i = 0;
        String sValue;
        String weatherUrl = "http://flash.weather.com.cn/wmaps/xml/"+city+".xml";
        String weatherIcon = "http://m.weather.com.cn/img/c";

        try {
            URL url = new URL(weatherUrl);
            httpConn = (HttpURLConnection) url.openConnection();
            httpConn.setRequestMethod("GET");
            din = httpConn.getInputStream();
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(din, "UTF-8");

            int evtType = xmlParser.getEventType();
            while (evtType != XmlPullParser.END_DOCUMENT){

                switch (evtType){
                    case XmlPullParser.START_TAG:
                        String tag = xmlParser.getName();
                        if (tag.equalsIgnoreCase("city")){
                            cityname.addElement(xmlParser.getAttributeValue(null,"cityname")+"天气：");
                            summary.addElement(xmlParser.getAttributeValue(null,"stateDetailed"));
                            low.addElement("最低："+xmlParser.getAttributeValue(null,"tem2"));
                            high.addElement("最高："+xmlParser.getAttributeValue(null,"tem1"));
                            icon.addElement(weatherIcon+xmlParser.getAttributeValue(null,"state1")+".gif");
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        default:break;
                }
                evtType = xmlParser.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                din.close();
                httpConn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void downImage(){

        int i = 0;
        for (i = 0; i < icon.size(); i++){
            try {
                URL url = new URL(icon.elementAt(i));
                System.out.println(icon.elementAt(i));
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setRequestMethod("GET");
                din = httpConn.getInputStream();
                bitmap.addElement(BitmapFactory.decodeStream(httpConn.getInputStream()));
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    din.close();
                    httpConn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private final Handler handler = new Handler(){
      public void handleMessage(Message msg){
          switch (msg.what){
              case 1:
                  showData();
                  break;
          }
          super.handleMessage(msg);
      }
    };
    public void showData(){
        body.removeAllViews();
        body.setOrientation(LinearLayout.VERTICAL);
        LayoutParams params = new LayoutParams(
                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        params.weight = 80;
        params.height = 50;
        for (int i = 0;i<cityname.size();i++){
            LinearLayout linerlayout = new LinearLayout(this);
            linerlayout.setOrientation(LinearLayout.HORIZONTAL);

            TextView dayView = new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(cityname.elementAt(i));
            linerlayout.addView(dayView);

            TextView summaryView = new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(summary.elementAt(i));
            linerlayout.addView(summaryView);

            ImageView icon = new ImageView(this);
            icon.setLayoutParams(params);
            icon.setImageBitmap(bitmap.elementAt(i));
            linerlayout.addView(icon);

            TextView lowView = new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(low.elementAt(i));
            linerlayout.addView(lowView);

            TextView highView = new TextView(this);
            dayView.setLayoutParams(params);
            dayView.setText(high.elementAt(i));
            linerlayout.addView(highView);

            body.addView(linerlayout);



        }
    }
}
