package com.desoft.desoft15.activity;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

//implements ZXingScannerView.ResultHandler
public class BarcodeScanerActivity extends AppCompatActivity {
    //private ZXingScannerView escanerZXing;
    private String idcampo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //escanerZXing = new ZXingScannerView(this);
        //setContentView(escanerZXing);
        idcampo=getIntent().getStringExtra("idcampo");
    }

    @Override
    public void onResume() {
        super.onResume();
        // El "manejador" del resultado es esta misma clase, por eso implementamos ZXingScannerView.ResultHandler
        /*escanerZXing.setResultHandler(this);
        escanerZXing.startCamera(); // Comenzar la cámara en onResume*/
    }

    @Override
    public void onPause() {
        super.onPause();
        //escanerZXing.stopCamera(); // Pausar en onPause
    }

   /**public void handleResult(Result rawResult) {
        Intent intentRegreso = new Intent();
        intentRegreso.putExtra("codigo", rawResult.getText());
        intentRegreso.putExtra("idcampo",idcampo);
        setResult(Activity.RESULT_OK, intentRegreso);
        finish();
    }*/
}
