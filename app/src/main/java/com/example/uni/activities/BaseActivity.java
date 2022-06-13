package com.example.uni.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.uni.utilities.Constants;
import com.example.uni.utilities.InitFirebase;
import com.example.uni.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;

public class BaseActivity extends AppCompatActivity {
    private DocumentReference documentReference;
    private PreferenceManager preferenceManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFields();
        initFunc();
    }
    private void initFields(){
        InitFirebase.init();
        preferenceManager = new PreferenceManager(this);
        documentReference = InitFirebase.firebaseFirestore.collection(Constants.USERS)
                .document(preferenceManager.getString(Constants.USER_ID));
    }
    private void initFunc(){}

    @Override
    protected void onPause() {
        super.onPause();
        if (preferenceManager.getBoolean(Constants.STATUS)) {
            documentReference.update(Constants.AVAILABLE, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (preferenceManager.getBoolean(Constants.STATUS)) {
            documentReference.update(Constants.AVAILABLE, 1);
        }
    }
}
