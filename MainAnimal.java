package com.example.bios;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.VoiceInteractor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.bios.dex.Animal;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

//main animal?

public class MainAnimal extends AppCompatActivity {

    private List<Animal> listAnimal = new ArrayList<Animal>();
    ArrayAdapter<Animal> arrayAdapterAnimal;

    EditText nomA, estC, evolA, loc, longiA, pesoA, anat, dif, element, diet;

    ImageView imageView;
    Button crearQr;
    Button guardarImagen;
    ListView listV_animales;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Animal animalSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_animal);

        nomA = findViewById(R.id.txt_nombreAnimal);
        estC = findViewById(R.id.txt_estadoConservacion);
        evolA = findViewById(R.id.txt_evolucionAnterior);
        loc = findViewById(R.id.txt_locacion);
        longiA = findViewById(R.id.txt_longitudAproximada);
        pesoA = findViewById(R.id.txt_pesoAproximado);
        anat = findViewById(R.id.txt_anatomia);
        dif = findViewById(R.id.txt_diformismo);
        element = findViewById(R.id.txt_elementoBios);
        diet = findViewById(R.id.txt_dieta);
        imageView = findViewById(R.id.image_view);
        crearQr =findViewById(R.id.generate_qr_btn);
        guardarImagen =findViewById(R.id.download_image);
        listV_animales = findViewById(R.id.lv_datosAnimales);

        ActivityCompat.requestPermissions(MainAnimal.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        ActivityCompat.requestPermissions(MainAnimal.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        inicializarFirebase();//siempre de primero

        //continua despues de aqui
        crearQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initQRCode();
            }
        });

        guardarImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarenGaleria();
            }
        });
        //continua despues de aqui
        listarDatos();

        listV_animales.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                animalSelected = (Animal) parent.getItemAtPosition(position);//casting...
                nomA.setText(animalSelected.getNombreAnimal());
                estC.setText(animalSelected.getEstConservacion());
                evolA.setText(animalSelected.getEvolucionAnterior());
                loc.setText(animalSelected.getLocacion());
                longiA.setText(animalSelected.getLongitudAprox());
                pesoA.setText(animalSelected.getPesoAprox());
                anat.setText(animalSelected.getAnatomia());
                dif.setText(animalSelected.getDiformismo());
                element.setText(animalSelected.getElementoBios());
                diet.setText(animalSelected.getDieta());
            }
        });

    }

    private void initQRCode() {
        String nombreAnimal = "Nombre Animal : "+nomA.getText().toString();
        String estadoConservacion = "Estado de conservación: "+estC.getText().toString();
        String evolucionAnterior = "Evolucion de: "+evolA.getText().toString();
        String locacion = "Locación: "+loc.getText().toString();
        String longitud = "Longitud Aproximada: "+longiA.getText().toString();
        String peso = "Peso Aproximado: "+pesoA.getText().toString();
        String anatomia = "Anatomia: "+anat.getText().toString();
        String diformismo = "Diformismo: "+dif.getText().toString();
        String dieta = "Dieta: "+diet.getText().toString();

        StringBuilder textToSend = new StringBuilder();
        textToSend.append(nombreAnimal+"\n"+estadoConservacion+"\n"+evolucionAnterior+"\n"+locacion+"\n"+longitud+"\n"+peso+"\n"+anatomia+"\n"+diformismo+"\n"+dieta);
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(textToSend.toString(), BarcodeFormat.QR_CODE, 600, 600);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(View.VISIBLE);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private void guardarenGaleria(){
        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        FileOutputStream outputStream = null;
        File file = Environment.getExternalStorageDirectory();
        File dir = new File(file.getAbsolutePath() + "/CodigosQr");

        dir.mkdir();
        String nombreArchivo = String .format("%d.png",System.currentTimeMillis());
        File outFile = new File(dir,nombreArchivo);
        Toast.makeText(this,"Imagen guardada", Toast.LENGTH_LONG).show();

        try{
            outputStream = new FileOutputStream(outFile);
        }catch(Exception e){
            e.printStackTrace();//para diagnosticar una excepción. Te dice qué sucedió y en qué parte del código sucedió esto
        }
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        try{
            outputStream.flush();
        }catch(Exception e){
            e.printStackTrace();
        }
        try{
            outputStream.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void listarDatos() {
        databaseReference.child("Animal").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listAnimal.clear();
                for (DataSnapshot objSnaptshot : dataSnapshot.getChildren()){
                    Animal animal = objSnaptshot.getValue(Animal.class);//traer todo el objeto instanciado
                    listAnimal.add(animal);

                    arrayAdapterAnimal = new ArrayAdapter<Animal>(MainAnimal.this, android.R.layout.simple_list_item_1, listAnimal);
                    listV_animales.setAdapter(arrayAdapterAnimal);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        String nombreAnimal = nomA.getText().toString();
        String estadoConservacion = estC.getText().toString();
        String evolucion = evolA.getText().toString();
        String locacion = loc.getText().toString();
        String longitudAprox = longiA.getText().toString();
        String pesoAprox = pesoA.getText().toString();
        String anatomia = anat.getText().toString();
        String diformismo = dif.getText().toString();
        String elementBios = element.getText().toString();
        String dieta = diet.getText().toString();

        switch (item.getItemId()){
            case R.id.icon_add:{
                Toast.makeText(this,"Agregar", Toast.LENGTH_LONG).show();
                if (nombreAnimal.equals("")||estadoConservacion.equals("")||evolucion.equals("")||locacion.equals("")||longitudAprox.equals("")||pesoAprox.equals("")||anatomia.equals("")||diformismo.equals("")||elementBios.equals("")||dieta.equals("")){
                    validacion();
                }
                else {
                    //Instancia
                    Animal animal = new Animal(nombreAnimal,estadoConservacion,evolucion,locacion,longitudAprox,pesoAprox,anatomia,diformismo,elementBios,dieta);
                    animal.setNombreAnimal(nombreAnimal);
                    animal.setEstConservacion(estadoConservacion);
                    animal.setEvolucionAnterior(evolucion);
                    animal.setLocacion(locacion);
                    animal.setLongitudAprox(longitudAprox);
                    animal.setPesoAprox(pesoAprox);
                    animal.setAnatomia(anatomia);
                    animal.setDiformismo(diformismo);
                    animal.setElementoBios(elementBios);
                    animal.setDieta(dieta);
                    //nombre como identificador en base de datos
                    databaseReference.child("Animal").child(animal.getNombreAnimal()).setValue(animal);     //se crea en firebase un nodo llamado animal, dentro de ese nodo, se va a tener como principal el nombreAnimal, algo el id
                    Toast.makeText(this, "Agregado", Toast.LENGTH_LONG).show();
                    limpiarCajas();
                }
                break;
            }
            case R.id.icon_save:{
                Animal animal = new Animal(nombreAnimal,estadoConservacion,evolucion,locacion,longitudAprox,pesoAprox,anatomia,diformismo,elementBios,dieta);
                animal.setNombreAnimal(animalSelected.getNombreAnimal());
                animal.setEstConservacion(nomA.getText().toString().trim());
                animal.setEvolucionAnterior(evolA.getText().toString().trim());
                animal.setLocacion(loc.getText().toString().trim());
                animal.setLongitudAprox(longiA.getText().toString().trim());
                animal.setPesoAprox(pesoA.getText().toString().trim());
                animal.setAnatomia(anat.getText().toString().trim());
                animal.setDiformismo(dif.getText().toString().trim());
                animal.setElementoBios(element.getText().toString().trim());
                animal.setDieta(diet.getText().toString().trim());
                databaseReference.child("Animal").child(animal.getNombreAnimal()).setValue(animal);
                Toast.makeText(this,"Actualizado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                break;
            }
            case R.id.icon_delete:{
                Animal animal = new Animal(nombreAnimal,estadoConservacion,evolucion,locacion,longitudAprox,pesoAprox,anatomia,diformismo,elementBios,dieta);
                animal.setNombreAnimal(animalSelected.getNombreAnimal());
                databaseReference.child("Animal").child(animal.getNombreAnimal()).removeValue();
                Toast.makeText(this,"Eliminado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                break;
            }
            default:break;
        }
        return true;
    }

    private void limpiarCajas() {

        nomA.setText("");
        estC.setText("");
        evolA.setText("");
        loc.setText("");
        longiA.setText("");
        pesoA.setText("");
        anat.setText("");
        dif.setText("");
        element.setText("");
        diet.setText("");
    }

    private void validacion() {

        String nombreAnimal = nomA.getText().toString();
        String estadoConservacion = estC.getText().toString();
        String evolucion = evolA.getText().toString();
        String locacion = loc.getText().toString();
        String longitudAprox = longiA.getText().toString();
        String pesoAprox = pesoA.getText().toString();
        String anatomia = anat.getText().toString();
        String diformismo = dif.getText().toString();
        String elementBios = element.getText().toString();
        String dieta = diet.getText().toString();
        if (nombreAnimal.equals("")){
            nomA.setError("Required");
        }
        else if (estadoConservacion.equals("")){
            estC.setError("Required");
        }
        else if (evolucion.equals("")){
            evolA.setError("Required");
        }
        else if (locacion.equals("")){
            loc.setError("Required");
        }
        else if (longitudAprox.equals("")){
            longiA.setError("Required");
        }
        else if (pesoAprox.equals("")){
            pesoA.setError("Required");
        }
        else if (anatomia.equals("")){
            anat.setError("Required");
        }
        else if (diformismo.equals("")){
            dif.setError("Required");
        }
        else if (elementBios.equals("")){
            element.setError("Required");
        }
        else if (dieta.equals("")){
            diet.setError("Required");
        }

    }

}