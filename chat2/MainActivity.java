package com.example.chat2;
import android.app.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.sql.Time;


public class MainActivity extends Activity {
    private TextView viewText;
    String SERVER_IP;
    int SERVER_PORT;
    private ClientThread clientThread;
    private Thread thread;
    private Handler handler;
    private Runnable updateButtonRunnable;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Request camera permission
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewText = findViewById(R.id.viewText);

        Button button1 = findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click1();
            }
        });

        Button button2 = findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click2();
            }
        });
    }

    private void click1() {
        EditText sendText = findViewById(R.id.sendText);
        Editable editableText = sendText.getText();
        String text = editableText.toString();
        clientThread.sendMessage(text);
        viewText.append(text + "\n");
    }

    private void click2() {
        EditText ip = findViewById(R.id.ip);
        Editable editableText = ip.getText();
        SERVER_IP = editableText.toString();

        EditText port = findViewById(R.id.port);
        editableText = port.getText();
        SERVER_PORT = Integer.parseInt(editableText.toString());
        clientThread = new ClientThread(SERVER_IP, SERVER_PORT);
        Thread thread = new Thread(clientThread);
        thread.start();


        handler = new Handler(Looper.getMainLooper());
        updateButtonRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (clientThread) {
                    if (clientThread.newMessage) {
                        String serverMessage = clientThread.serverMessage;
                        viewText.append("[Server] " + serverMessage + "\n");
                        clientThread.newMessage = false;
                    }
                }
                handler.postDelayed(updateButtonRunnable, 1000);
            }
        };

        handler.post(updateButtonRunnable);
    }
}