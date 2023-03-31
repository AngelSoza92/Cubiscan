package app.ejemplo.qbiscan;

import androidx.appcompat.app.AppCompatActivity;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.content.ContentValues.TAG;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private String deviceName = null;
    private String deviceAddress;
    public static Handler handler;
    //public static BluetoothSocket mmSocket;
    //public static ConnectedThread connectedThread;
    // public static CreateConnectThread createConnectThread;
    TextView textViewInfo, textViewConex, textLargo, textAncho, textAlto, textPeso;
    ToggleButton tbtnValidar;
    Button btnCaptura, btnEnviar;
    EditText textSKU;
    String skuActual;
    Bundle losExtras;
    String usuario, cuba;

    String soloLargo = "";
    String soloAncho = "";
    String soloAlto = "";
    String soloPeso = "";
    String mensajePantalla = "";
    ImageView imgView;

    private final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int MESSAGE_LOST_CONNECT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        losExtras =getIntent().getExtras();
        try {
            usuario = losExtras.getString("loggedUser");
            cuba = losExtras.getString("qubiCode");
        }catch (NullPointerException ex){
            usuario = " ";
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        //this.registerReceiver(mReceiver, filter);

        // UI Initialization


        tbtnValidar = findViewById(R.id.tbtValidar);
        textViewInfo = findViewById(R.id.textViewInfo);
        textLargo = findViewById(R.id.txtLargo);
        textAncho = findViewById(R.id.txtAncho);
        textAlto = findViewById(R.id.txtAlto);
        textPeso = findViewById(R.id.txtPeso);
        btnCaptura = findViewById(R.id.btCaptura);
        btnEnviar = findViewById(R.id.btEnviar);
        textSKU = findViewById(R.id.txtSKU);
        imgView = findViewById(R.id.imageView);


        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            //deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            textViewConex.setText("Connecting to " + deviceName + "...");
         ;


            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            //  createConnectThread.start();
        }
        permiteBotones(false);
        permiteBotonDeEnviar(false);
        tbtnValidar.setChecked(false);
        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                textViewConex.setText("Connected to " + deviceName);


                                break;
                            case -1:
                                textViewConex.setText("Device fails to connect");


                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String mensajeHotDog = msg.obj.toString(); // Read message from Arduino
                        try {
                            String[] parts = mensajeHotDog.split(" ");
                            soloLargo = parts[1].replace("-", "");
                            soloAncho = parts[3].replace("-", "");
                            soloAlto = parts[5].replace("-", "");
                            soloPeso = parts[7].replace("-", "");
                            mensajePantalla = "Eje X: " + soloLargo + " cm\nEje Y: " + soloAncho + " cm" +
                                    "\nEje Z: " + soloAlto + " cm\nPeso: " + soloPeso + " kg";
                            System.out.println(mensajeHotDog);
                            textViewInfo.setText(mensajePantalla);
                        } catch (ArrayIndexOutOfBoundsException ex) {
                            System.out.println("Exception mensaje muy corto: " + mensajeHotDog);
                            textViewInfo.setText(mensajeHotDog);
                        }
                        break;
                    case MESSAGE_LOST_CONNECT:
                        textViewConex.setText("Se desconectó");
                        textViewInfo.setText("");
                        ;
                        break;
                    default:
                        textViewConex.setText("Se desconectó");
                        textViewInfo.setText("");
                        break;
                }
            }
        };

        // Select Bluetooth Device


        btnCaptura.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                System.out.println("queremos entrar");
                //textLargo.setText(soloLargo);
                //textAncho.setText(soloAncho);
                //textAlto.setText(soloAlto);
                //textPeso.setText(soloPeso);
                mitre2();
            }
        });

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skuActual = textSKU.getText().toString().trim();
                String peso = textPeso.getText().toString().trim();
                String largo = textLargo.getText().toString().trim();
                String ancho = textAncho.getText().toString().trim();
                String alto = textAlto.getText().toString().trim();
                String zku = skuActual;
                noMeToquenLosHuevos(zku, peso, largo, ancho, alto);
            }
        });

        tbtnValidar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("Entra al boton por lo menos wn");
                boolean presiona = tbtnValidar.isChecked();
                skuActual = textSKU.getText().toString().replace(" ", "");
                tbtnValidar.setChecked(false);
                permiteBotones(false);

                if (presiona) {
                    if (skuActual.length() > 0) {
                        String url = "http://10.107.226.241/verificar_sku?sku=" + skuActual;
                        System.out.println("URL VALIDAR: " + url);
                        validarSKU(url);
                    } else {
                        alertaDeError();
                        Toast.makeText(getApplicationContext(), "Ingresa SKU", Toast.LENGTH_LONG).show();
                        tbtnValidar.setChecked(false);
                    }
                } else {
                    textSKU.requestFocus();
                }
            }
        });

    }


    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app

        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    public void permiteBotones(boolean x) {
        btnCaptura.setEnabled(x);

        textSKU.setEnabled(!x);
        if (!x) {
            imgView.setImageBitmap(null);
        }
    }

    public void permiteBotonDeEnviar(boolean x) {
        btnEnviar.setEnabled(x);
    }




    public void validarSKU(String url) {
        StringRequest stringRequest = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String valor = "0";
                System.out.println("RESPUESTA :" + response);
                try {
                    JSONArray arregloJSON = new JSONArray(response);
                    JSONObject objetoJSON = arregloJSON.getJSONObject(0);
                    valor = objetoJSON.getString("resultado").trim();
                    hacerAlgoConElValor(valor);
                } catch (JSONException e) {
                    alertaDeError();
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alertaDeError();
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                System.out.println(error);
            }
        });
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 500000000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 500000000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                alertaDeError();
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                System.out.println(error);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }

    public void hacerAlgoConElValor(String valor) {
        if (valor.equals("0")) {
            alertaDeError();
            Toast.makeText(getApplicationContext(), "SKU Inválido", Toast.LENGTH_LONG).show();
            tbtnValidar.setChecked(false);
            permiteBotones(false);
            permiteBotonDeEnviar(false);
        } else if (valor.equals("500")) {
            alertaDeError();
            Toast.makeText(getApplicationContext(), "ERROR 500", Toast.LENGTH_LONG).show();
            tbtnValidar.setChecked(false);
            permiteBotones(false);
            permiteBotonDeEnviar(false);
        } else {
            skuActual = valor;
            textSKU.setText(valor);
            tbtnValidar.setChecked(true);
            permiteBotones(true);
        }
    }

    public void conclusionEnvio(String valor) {
        if (valor.equals("OK")) {
            Toast.makeText(getApplicationContext(), "Información enviada a Active", Toast.LENGTH_LONG).show();
            textLargo.setText("");
            textAncho.setText("");
            textAlto.setText("");
            textPeso.setText("");
            textSKU.setText("");
            tbtnValidar.setChecked(false);
            permiteBotones(false);
            permiteBotonDeEnviar(false);
            textSKU.requestFocus();
        } else {
            Toast.makeText(getApplicationContext(), "Hay un problema", Toast.LENGTH_LONG).show();
            tbtnValidar.setChecked(false);
            permiteBotones(false);
            permiteBotonDeEnviar(false);
        }
    }

    public void noMeToquenLosHuevos(String sku, String peso, String largo, String ancho, String alto) {
        try {
            int zku = Integer.parseInt(sku);
            if (!(textPeso.getText().toString().replace(" ", "").equals(""))) {
                enviarJson("http://10.107.226.241/actualizar_sku_", sku, peso, largo, ancho, alto);
            } else {
                textSKU.requestFocus();
            }
        } catch (NumberFormatException e) {
            textSKU.requestFocus();
        }
    }

    private void enviarJson(String URL, String sku0, String peso0, String largo0, String ancho0, String alto0) {
        StringRequest sr = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.isEmpty()) {
                    System.out.println("RESPUESTA :" + response);
                    try {
                        JSONArray arregloJSON = new JSONArray(response);
                        JSONObject objetoJSON = arregloJSON.getJSONObject(0);
                        String valor = objetoJSON.getString("resultado").trim();
                        conclusionEnvio(valor);
                    } catch (JSONException e) {
                        alertaDeError();
                        Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                } else {
                    alertaDeError();
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    System.out.println("Hay errores inexplicables y este. Me cago en la hostia");
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                System.out.println(error);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> parametros = new HashMap<String, String>();
                float largonum, altonum, anchonum =0;
                largonum = Float.valueOf(largo0);
                altonum = Float.valueOf(alto0);
                anchonum = Float.valueOf(ancho0);
                float vol = 0;
                vol = largonum*altonum*anchonum;
                String volumen0 = vol+"";

                parametros.put("sku", sku0);
                parametros.put("volumen", volumen0);
                parametros.put("ancho", ancho0);
                parametros.put("largo", largo0);
                parametros.put("alto", alto0);
                parametros.put("peso", peso0);
                return parametros;
            }
        };
        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(sr);
    }

    public void alertaDeError(){
        MediaPlayer mp = MediaPlayer.create(this,R.raw.error);
        mp.start();
    }

    public void hacerAlgoConElValor2(String largo, String ancho, String alto, String peso, String factor) {
        if (largo.equals("0")||ancho.equals("0")||alto.equals("0")||peso.equals("0")) {
            alertaDeError();
            Toast.makeText(getApplicationContext(), "Problemas en cubiscan", Toast.LENGTH_LONG).show();
            tbtnValidar.setChecked(false);
            permiteBotones(false);
            permiteBotonDeEnviar(false);
        }
        else {
            float largonum=0, altonum=0, anchonum=0, pesonum=0;
            largonum = Float.valueOf(largo);
            altonum = Float.valueOf(alto);
            anchonum = Float.valueOf(ancho);
            pesonum = Float.valueOf(peso);

            if(factor.equals("2720")||factor.equals("2278")){
                float lb_to_kg_factor = (float)0.45359237;
                pesonum = pesonum*lb_to_kg_factor;
            }

            if(factor.equals("166")||factor.equals("139")){
                float lb_to_kg_factor = (float)0.45359237;
                float inch_to_cm_factor = (float)2.54;

                largonum = largonum*inch_to_cm_factor;
                altonum = altonum*inch_to_cm_factor;
                anchonum = anchonum*inch_to_cm_factor;
                pesonum = pesonum*lb_to_kg_factor;

            }

            if(factor.equals("366")||factor.equals("306")){
                float inch_to_cm_factor = (float)2.54;
                largonum = largonum*inch_to_cm_factor;
                altonum = altonum*inch_to_cm_factor;
                anchonum = anchonum*inch_to_cm_factor;
            }

            DecimalFormat formato1 = new DecimalFormat("0.0");
            DecimalFormat formato2 = new DecimalFormat("0.00");

            textLargo.setText(formato1.format(largonum));
            textAncho.setText(formato1.format(anchonum));
            textAlto.setText(formato1.format(altonum));
            textPeso.setText(formato2.format(pesonum) );

            tbtnValidar.setChecked(true);
            permiteBotones(true);
            permiteBotonDeEnviar(true);
        }
    }

    public void mitre2(){
        System.out.println("AAAPI: "+"http://10.107.226.241:4050/"+cuba);
        StringRequest stringRequest = new StringRequest("http://10.107.226.241:4050/"+cuba, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                String valor = "0";
                String largo,ancho,alto,peso,factor="";
                System.out.println("RESPUESTA :" + response);
                String otroresponse="";
                otroresponse = "["+response+"]";
                System.out.println("otro resp:"+otroresponse);
                try {
                    JSONArray arregloJSON = new JSONArray(otroresponse);
                   JSONObject objetoJSON = arregloJSON.getJSONObject(0);

                    valor = objetoJSON.getString("dim_weight").trim();
                    largo = objetoJSON.getString("length").trim();
                    ancho = objetoJSON.getString("width").trim();
                    alto = objetoJSON.getString("height").trim();
                    peso = objetoJSON.getString("weight").trim();
                    factor = objetoJSON.getString("factor").trim();

                    largo = largo.replace("[","").replace(",null]","");
                    ancho = ancho.replace("[","").replace(",null]","");
                    alto = alto.replace("[","").replace(",null]","");
                    peso = peso.replace("[","").replace(",null]","");

                    System.out.println("Factor "+factor);
                    //System.out.println(valor);
                   hacerAlgoConElValor2(largo, ancho, alto, peso, factor);

                } catch (JSONException e) {
                    alertaDeError();
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                alertaDeError();
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                System.out.println(error);
            }
        });
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 500000000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 500000000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {
                alertaDeError();
                Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                System.out.println(error);
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(stringRequest);
    }


}


