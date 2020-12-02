package com.example.bios;

import com.google.firebase.database.FirebaseDatabase;

public class Bios extends android.app.Application{

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);//clase encargada de la persistencia de datos
    }
}
