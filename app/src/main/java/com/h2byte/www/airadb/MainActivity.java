package com.h2byte.www.airadb;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.BatchUpdateException;

public class MainActivity extends AppCompatActivity {

    private TextView mTextViewStatus;
    private LinearLayout mHiddenLayout;
    private TextView mTextViewIP;
    private TextView mTextViewPort;
    private Button  mADBButton;
    private String mPort = "5555";
    private static String TAG = "Air ADB";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adb("start");
        Log.d(TAG, "ADB listening port: " + getPort());
        setText("start");

        mADBButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String port = getPort();
                if (port.equals("-1")) {
                    adb("start");
                    setText("start");
                }
                else {
                    adb("stop");
                    setText("stop");
                }
                Log.d(TAG, "ADB listening port: " + getPort());
            }
        });
    }
    private String getIP() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int IP = wifiInfo.getIpAddress();
        int mask = 0xff;
        return String.format("%d.%d.%d.%d",
                IP & mask,
                IP >> 8 & mask,
                IP >> 16 & mask,
                IP >> 24 & mask);
    }

    private String getPort() {
        String cmd = "getprop service.adb.tcp.port";
        String port = "-1";
        try {
            Runtime.getRuntime().exec("su");
            Process proc = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            port = in.readLine();
            return port;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return port;
    }

    private void adb(String action) {
        String cmd = "setprop service.adb.tcp.port ";
        if (action.equals("start")) {
            cmd = cmd + mPort;
        } else {
            cmd = cmd + "-1";
        }
        String[] cmds = {"su", cmd, "stop adbd", "start adbd"};
        try {
//            Runtime.getRuntime().exec(cmds);
            //can not exec array. use loop instead.
            for (String c : cmds) {
                Runtime.getRuntime().exec(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void setText(String action) {
        mTextViewStatus = (TextView) findViewById(R.id.adb_status);
        mHiddenLayout = (LinearLayout) findViewById(R.id.hidden_layout);
        mTextViewIP = (TextView) findViewById(R.id.IPAdress);
        mTextViewPort = (TextView) findViewById(R.id.port);
        mADBButton = (Button) findViewById(R.id.adb_button);

        if (action.equals("stop")) {
            mTextViewStatus.setText(R.string.adb_stopped);
            mHiddenLayout.setVisibility(View.INVISIBLE);
            mADBButton.setText(R.string.adb_start_button);
        }
        else {
            mTextViewStatus.setText(R.string.adb_started);
            mHiddenLayout.setVisibility(View.VISIBLE);
            mTextViewIP.setText(getIP());
//            mTextViewPort.setText(getPort());
            mTextViewPort.setText("5555");
            mADBButton.setText(R.string.adb_stop_button);
        }
    }
}
