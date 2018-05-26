package com.example.arife.mobileprogrammingproject;

import android.widget.Button;


public interface MyClickListener {
    void onEdit(int p, Button helpButton);
    void onAccept(int p);
    void onReject(int p);
}