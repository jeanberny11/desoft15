package com.desoft.desoft15.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.datecs.api.printer.ProtocolAdapter;
import com.datecs.api.printer.ProtocolAdapter.Channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public abstract class ConectionWithPrint extends Activity {
    boolean conecion = false;
    Context context;
    /* access modifiers changed from: private */
    public BluetoothSocket mBluetoothSocket;
    private Socket mPrinterSocket;
    private ProtocolAdapter mProtocolAdapter;
    /* access modifiers changed from: private */
    public boolean mRestart;
    public Printer printer;
    public String printerName;

    public abstract void closeConexionStatus(boolean z);

    public abstract void conexionStatus(boolean z);

    public ConectionWithPrint(Context context2, String printerName2) {
        this.context = context2;
        this.printerName = printerName2;
        System.out.println(printerName2);
    }

    private void doJob(final Runnable job, final String mensaje) {
        runOnUiThread(new Runnable() {
            public void run() {
                final ProgressDialog dialog = new ProgressDialog(ConectionWithPrint.this.context);
                dialog.setTitle("Espere porfavor...");
                dialog.setMessage(mensaje);
                dialog.setCancelable(false);
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            job.run();
                        } finally {
                            dialog.cancel();
                            ConectionWithPrint.this.runOnUiThread(new Runnable() {
                                public void run() {
                                    ConectionWithPrint.this.conexionStatus(ConectionWithPrint.this.conecion);
                                }
                            });
                        }
                    }
                }).start();
            }
        });
    }

    public void establishBluetoothConnection() {
        doJob(new Runnable() {
            public void run() {
                BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                                ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                            throw new Exception("La app no cuenta con los permisos para ejecutar esa accion");
                        }
                    }else {
                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                            throw new Exception("La app no cuenta con los permisos para ejecutar esa accion");
                        }
                    }

                    Set<BluetoothDevice> devices = adapter.getBondedDevices();
                    ArrayList<BluetoothDevice> deviceBonded = new ArrayList<>();
                    for (BluetoothDevice deviceFind : devices) {
                        if (deviceFind.getName().equalsIgnoreCase(ConectionWithPrint.this.printerName)) {
                            deviceBonded.add(deviceFind);
                        }
                    }
                    if (deviceBonded.size() <= 0) {
                        ConectionWithPrint.this.conecion = false;
                        return;
                    }
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    InputStream in = null;
                    OutputStream out = null;
                    adapter.cancelDiscovery();
                    Iterator it = deviceBonded.iterator();
                    while (it.hasNext()) {
                        in = null;
                        out = null;
                        try {
                            ConectionWithPrint.this.mBluetoothSocket = ((BluetoothDevice) it.next()).createRfcommSocketToServiceRecord(uuid);
                            ConectionWithPrint.this.mBluetoothSocket.connect();
                            in = ConectionWithPrint.this.mBluetoothSocket.getInputStream();
                            out = ConectionWithPrint.this.mBluetoothSocket.getOutputStream();
                            ConectionWithPrint.this.conecion = true;
                            break;
                        } catch (IOException e) {
                            e.printStackTrace();
                            ConectionWithPrint.this.conecion = false;
                        }
                    }
                    if (!ConectionWithPrint.this.conecion) {
                        ConectionWithPrint.this.error("No se pudo establecer la conexion", ConectionWithPrint.this.mRestart);
                        ConectionWithPrint.this.conecion = false;
                        return;
                    }
                    try {
                        ConectionWithPrint.this.initPrinter(in, out);
                        ConectionWithPrint.this.conecion = true;
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        ConectionWithPrint.this.error("Error al iniciar la impresora", ConectionWithPrint.this.mRestart);
                        ConectionWithPrint.this.conecion = false;
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                    ConectionWithPrint.this.conecion = false;
                }
            }
        }, "Conectando dispositivo");
    }

    /* access modifiers changed from: private */
    public void error(final String text, boolean resetConnection) {
        if (resetConnection) {
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(ConectionWithPrint.this.context, text, Toast.LENGTH_SHORT).show();
                }
            });
            establishBluetoothConnection();
        }
    }

    public void closeBlutoothConnection() {
        BluetoothSocket s = this.mBluetoothSocket;
        this.mBluetoothSocket = null;
        if (s != null) {
            try {
                if (this.printer != null) {
                    this.printer.release();
                }
                s.close();
                closeConexionStatus(true);
            } catch (IOException e) {
                closeConexionStatus(false);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void initPrinter(InputStream inputStream, OutputStream outputStream) throws IOException {
        this.mProtocolAdapter = new ProtocolAdapter(inputStream, outputStream);
        if (this.mProtocolAdapter.isProtocolEnabled()) {
            final Channel channel = this.mProtocolAdapter.getChannel(1);
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        try {
                            channel.pullEvent();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                            ConectionWithPrint.this.error(e2.getMessage(), ConectionWithPrint.this.mRestart);
                            return;
                        }
                    }
                }
            }).start();
            //Printer.isGeneralPrint = false;
            this.printer = new  Printer(channel.getInputStream(), channel.getOutputStream());
            return;
        }
        //Printer.isGeneralPrint = true;
        this.printer = new  Printer(inputStream, outputStream);
    }

    private void toast(final String text) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (!ConectionWithPrint.this.isFinishing()) {
                    Toast.makeText(ConectionWithPrint.this.context, text, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
