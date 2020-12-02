package com.example.bios;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bios.dex.Animal;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.Result;

public class Home extends AppCompatActivity implements ZXingScannerView.ResultHandler {

    private ZXingScannerView myScanneview;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.CAMERA}, 1);
    }


    public void btnScanear(View view) {
        myScanneview = new ZXingScannerView(this);
        setContentView(myScanneview);
        myScanneview.setResultHandler(this);
        myScanneview.startCamera();
    }

    @Override
    public void handleResult(Result result) {//Encargado de escanear el codigo y traducirlo

        Log.v("BundleResult",result.getText());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Animal escaneado");
        builder.setMessage(result.getText());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();

        myScanneview.resumeCameraPreview(this);
    }
}