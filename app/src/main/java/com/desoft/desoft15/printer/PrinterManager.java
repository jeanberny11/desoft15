package com.desoft.desoft15.printer;

import static com.desoft.desoft15.printer.PrinterCommands.ESC;
import static com.desoft.desoft15.printer.PrinterCommands.GS;
import static com.desoft.desoft15.printer.PrinterCommands.LF;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;

import com.desoft.desoft15.utils.ESCUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Objects;

public class PrinterManager {
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;

    public void loadPrinter(Context context, String printeraddress) throws Exception {
        if (bluetoothAdapter == null) {
            throw new Exception("No se encontro preiferico Bluetooth en el dispositivo!");
        }
        if (!bluetoothAdapter.isEnabled()) {
            throw new Exception("El dispositivo bluetooh esat desactivado!");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("La app no cuenta con los permisos para esa accion!");
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("La app no cuenta con los permisos para esa accion!");
            }
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothDevice = bluetoothAdapter.getRemoteDevice(printeraddress);
        if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            throw new Exception("El dispositivo seleccionado no esta vinculado");
        }
    }

    public void loadPrinterbyName(Context context, String printeraname) throws Exception {
        if (bluetoothAdapter == null) {
            throw new Exception("No se encontro preiferico Bluetooth en el dispositivo!");
        }
        if (!bluetoothAdapter.isEnabled()) {
            throw new Exception("El dispositivo bluetooh esat desactivado!");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("La app no cuenta con los permisos para esa accion!");
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("La app no cuenta con los permisos para esa accion!");
            }
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        bluetoothDevice = null;
        for (BluetoothDevice dev : bluetoothAdapter.getBondedDevices()
        ) {
            if (dev.getName().equals(printeraname)) {
                bluetoothDevice = dev;
                break;
            }
        }
        if (bluetoothDevice == null) {
            throw new Exception("No se encontro una impresora con el nombre " + printeraname);
        }
        if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            throw new Exception("El dispositivo seleccionado no esta vinculado");
        }
    }

    public void openConnection(Context context) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("La app no cuenta con los permisos para esa accion!");
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                throw new Exception("La app no cuenta con los permisos para esa accion!");
            }
        }
        if (Build.VERSION.SDK_INT >= 10) {
            Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", int.class);
            bluetoothSocket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
        } else {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(bluetoothDevice.getUuids()[0].getUuid());
        }
        if (bluetoothSocket == null) {
            throw new Exception("No se ha podido establecer la conexion con el dispositivo!");
        }
        bluetoothSocket.connect();
        this.outputStream = bluetoothSocket.getOutputStream();
    }

    public boolean isConnected() {
        return (bluetoothSocket == null || outputStream == null);
    }

    public void closeConnection() throws IOException {
        this.outputStream.flush();
        this.outputStream.close();
        bluetoothSocket.close();
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    private String ajustartexto(String text, PrinterTextStyle style) {
        StringBuilder result = new StringBuilder();
        switch (style) {
            case extrabig: {//-----small text size
                if (text.length() <= PrinterCommands.MAXEXTRABIGCHAR) {
                    result.append(text);
                } else {
                    String ln = text;
                    while (ln.length() > 0) {
                        if (ln.length() > PrinterCommands.MAXEXTRABIGCHAR) {
                            boolean blk = false;
                            for (int i = PrinterCommands.MAXEXTRABIGCHAR; i > 0; i--) {
                                if (ln.charAt(i) == ' ') {
                                    blk = true;
                                    result.append(ln, 0, i).append("\n");
                                    ln = ln.substring(i + 1);
                                    break;
                                }
                            }
                            if (!blk) {
                                result.append(ln, 0, PrinterCommands.MAXEXTRABIGCHAR);
                                ln = ln.substring(PrinterCommands.MAXEXTRABIGCHAR + 1);
                            }
                        } else {
                            result.append(ln);
                            ln = "";
                        }
                    }
                }
                break;
            }
            case normaltext: {//-----Normal text size
                if (text.length() <= PrinterCommands.MAXNORMALCHAR) {
                    result.append(text);
                } else {
                    String ln = text;
                    while (ln.length() > 0) {
                        if (ln.length() > PrinterCommands.MAXNORMALCHAR) {
                            boolean blk = false;
                            for (int i = PrinterCommands.MAXNORMALCHAR; i > 0; i--) {
                                if (ln.charAt(i) == ' ') {
                                    blk = true;
                                    result.append(ln, 0, i).append("\n");
                                    ln = ln.substring(i + 1);
                                    break;
                                }
                            }
                            if (!blk) {
                                result.append(ln, 0, PrinterCommands.MAXNORMALCHAR);
                                ln = ln.substring(PrinterCommands.MAXNORMALCHAR + 1);
                            }
                        } else {
                            result.append(ln);
                            ln = "";
                        }
                    }
                }
                break;
            }
            case bigtext: {//-----Normal text size
                if (text.length() <= PrinterCommands.MAXBIGCHAR) {
                    result.append(text);
                } else {
                    String ln = text;
                    while (ln.length() > 0) {
                        if (ln.length() > PrinterCommands.MAXBIGCHAR) {
                            boolean blk = false;
                            for (int i = PrinterCommands.MAXBIGCHAR; i > 0; i--) {
                                if (ln.charAt(i) == ' ') {
                                    blk = true;
                                    result.append(ln, 0, i).append("\n");
                                    ln = ln.substring(i + 1);
                                    break;
                                }
                            }
                            if (!blk) {
                                result.append(ln, 0, PrinterCommands.MAXBIGCHAR);
                                ln = ln.substring(PrinterCommands.MAXBIGCHAR + 1);
                            }
                        } else {
                            result.append(ln);
                            ln = "";
                        }
                    }
                }
                break;
            }
        }

        return result.toString();
    }

    public void printtest() throws Exception {
        // ============================================================================
        // Issuing receipts
        // ============================================================================

        // Initialize printer
        this.outputStream.write(new byte[]{ESC, '@'});

        // --- Print stamp --->>>
        // Set line spacing: For the TM-T20, 1.13 mm (18/406 inches)
        this.outputStream.write(new byte[]{ESC, 3, 18});
        // Set unidirectional print mode: Cancel unidirectional print mode (Unnecessary
        // in the TM-T20, so it is comment out)
        //   For models equipped with the ESC U command, implementation is recommended.
        //   Designate unidirectional printing with the ESC U command to get a print
        //   result with equal upper and lower parts for the stamp frame symbol
        //    ESC "U" 1
        // Select justification: Centering

        // Select character size: (horizontal (times 2) x vertical (times 2))
        this.outputStream.write(new byte[]{GS, '!', 0x11});
        // Print stamp data and line feed: quadruple-size character section, 1st line
        this.outputStream.write(0xC9);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xBB);
        this.outputStream.write(LF);
        // Print stamp data and line feed: quadruple-size character section, 2nd line
        this.outputStream.write(0xBA);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0x45);
        this.outputStream.write(0x50);
        this.outputStream.write(0x53);
        this.outputStream.write(0x4F);
        this.outputStream.write(0x4E);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0xBA);
        this.outputStream.write(LF);
        // Print stamp data and line feed: quadruple-size character section, 3rd line
        //   Left frame and empty space data
        this.outputStream.write(0xBA);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        //   Select character size: Normal size
        this.outputStream.write(new byte[]{GS, '!', 0x00});
        //   Character string data in the frame
        this.outputStream.write("Thank you ".getBytes());
        //   Select character size: horizontal (times 2) x vertical (times 1)
        this.outputStream.write(new byte[]{GS, '!', 0x11});
        //   Empty space and right frame data, and print and line feed
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0x20);
        this.outputStream.write(0xBA);
        this.outputStream.write(LF);
        // Print stamp data and line feed: quadruple-size character section, 4th line
        this.outputStream.write(0xC8);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xCD);
        this.outputStream.write(0xBC);
        this.outputStream.write(LF);
        // Initializing line spacing
        this.outputStream.write(new byte[]{ESC, '2'});
        // Set unidirectional print mode: Cancel unidirectional print mode
        // (Unnecessary in the TM-T20, so it is comment out)
        //    ESC "U" 0
        // Select character size: Normal size
        this.outputStream.write(new byte[]{GS, '!', 0x00});
        // --- Print stamp ---<<<

        // --- Print the date and time --->>>
        // Print and feed paper: In case TM-T20, feeding amount = 0.250 mm (4/406 inches)
        this.outputStream.write(new byte[]{ESC, 'J', 4});
        this.outputStream.write("NOVEMBER 1, 2012  10:30".getBytes());
        // Print and feed n lines: Feed the paper three lines
        this.outputStream.write(new byte[]{ESC, 'd', 3});
        // --- Print the date and time ---<<<

        // --- Print details A --->>>
        // Select justification: Left justification
        this.outputStream.write(new byte[]{ESC, 'a', 0});
        // Details text data and print and line feed
        this.outputStream.write("TM-Uxxx                            6.75".getBytes());
        this.outputStream.write(LF);
        this.outputStream.write("TM-Hxxx                            6.00".getBytes());
        this.outputStream.write(LF);
        this.outputStream.write("PS-xxx                             1.70".getBytes());
        this.outputStream.write(LF);
        // --- Print details A ---<<<

        // --- Print details B --->>>
        // Set unidirectional print mode: Set unidirectional print mode
        // (Unnecessary in the TM-T20, so it is comment out)
        //   For models equipped with the ESC U command, implementation is recommended.
        //   Designate unidirectional printing with the ESC U command to get a print
        //   result with equal upper and lower parts for double-height characters
        //    ESC "U" 1
        // Select character size: horizontal (times 1) x vertical (times 2)
        this.outputStream.write(new byte[]{GS, '!', 0x01});
        // Details text data and print and line feed
        this.outputStream.write("TOTAL                             14.45".getBytes());
        this.outputStream.write(LF);
        // Set unidirectional print mode: Cancel unidirectional print mode
        // (Unnecessary in the TM-T20, so it is comment out)
        //    ESC "U" 0
        // Select character size: Normal size
        this.outputStream.write(new byte[]{GS, '!', 0x00});
        // Details characters data and print and line feed
        this.outputStream.write("---------------------------------------".getBytes());
        this.outputStream.write(LF);
        this.outputStream.write("PAID                              50.00".getBytes());
        this.outputStream.write(LF);
        this.outputStream.write("CHANGE                            35.55".getBytes());
        this.outputStream.write(LF);
        // --- Print details B ---<<<

        // --- Issue receipt --->>>
        // Operating the drawer
        // Generate pulse: Drawer kick-out connector pin 2, 2 x 2 ms on, 20 x 2 ms off
        this.outputStream.write(new byte[]{ESC, 'p', 0, 2, 20});

        // Select cut mode and cut paper: [Function B] Feed paper to (cutting position
        // + 0 mm) and executes a partial cut (one point left uncut).
        this.outputStream.write(new byte[]{GS, 'V', 66, 0});
        // --- Issue receipt ---<<<
        // ============================================================================
        // Issuing receipts
        // ============================================================================

        // Initialize printer
        resetPrint();
    }

    public void resetPrint() throws IOException {
        this.outputStream.write(PrinterCommands.ESC_RESET);
    }

    /*public void print(String text, PrinterAlignment alignment, PrinterTextStyle style, boolean bold) throws IOException {
        String tiprint=ajustartexto(text,style);
        final byte[] cc = {0x1B, 0x21, 0x03};  // 0- small size text
        final byte[] nn = new byte[]{0x1B, 0x21, 0x00}; //1-mormal size text
        final byte[] bb2 = new byte[]{0x1B, 0x21, 0x20}; // 2- BIG size text
        switch (alignment){
            case left:{
                //left align
                this.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                break;
            }
            case center:{
                //center align
                this.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                break;
            }
            case right:{
                //right align
                this.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                break;
            }
        }
        switch (style){
            case smalltext: this.outputStream.write(cc);
                break;
            case normaltext: this.outputStream.write(nn);
                break;
            case bigtext: this.outputStream.write(bb2);
                break;
        }
        if (bold){
            byte[] bd = new byte[]{0x1B, 0x21, 0x08};  // 2- only bold text
            this.outputStream.write(bd);
        }
        this.outputStream.write(tiprint.getBytes("CP858"));
        this.outputStream.write(LF);
    }*/

    public void print(PrintData data) throws IOException {
        String tiprint = ajustartexto(data.getTexto(), data.getSize());
        final byte[] nt = {0x1D, 0x21, 0x00};  // 1- normal size text
        final byte[] bt = new byte[]{0x1D, 0x21, 0x11}; //2-doble ancho size text
        final byte[] ebt = new byte[]{0x1D, 0x21, 0x55}; // 3- BIG size text
        switch (data.getAlignment()) {
            case left: {
                //left align
                this.outputStream.write(PrinterCommands.ESC_ALIGN_LEFT);
                break;
            }
            case center: {
                //center align
                this.outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
                break;
            }
            case right: {
                //right align
                this.outputStream.write(PrinterCommands.ESC_ALIGN_RIGHT);
                break;
            }
        }
        switch (data.getSize()) {
            case normaltext:
                this.outputStream.write(nt);
                break;
            case bigtext:
                this.outputStream.write(bt);
                break;
            case extrabig:
                this.outputStream.write(ebt);
                break;
        }
        if (data.isBold()) {
            byte[] bd = new byte[]{0x1D, 0x21, 0x01};  // 2- only bold text
            this.outputStream.write(bd);
        }
        this.outputStream.write(tiprint.getBytes("ISO8859-2"));
        this.outputStream.write(LF);
        resetPrint();
    }

    public void feed() throws IOException {
        this.outputStream.write(PrinterCommands.FEED_LINE);
    }

    public void feed(int lines) throws IOException {
        for (int i = 0; i < lines; i++) {
            this.outputStream.write(PrinterCommands.FEED_LINE);
        }
    }

    public void feedandcut() throws IOException {
        this.outputStream.write(PrinterCommands.FEED_PAPER_AND_CUT);
    }

    public void printBarcode(int type, String data) throws IOException {
        if (data == null) {
            throw new NullPointerException("The data is null");
        }
        byte[] bytedata = data.getBytes();
        byte[] buf = new byte[4 + bytedata.length];
        this.outputStream.write(new byte[]{0x1B, 'a', 1});
        this.outputStream.write(new byte[]{0x1D, 0x68, 25});
        //this.outputStream.write(new byte[]{0x1D,0x48,2});
        //this.outputStream.write(new byte[]{0x1D,0x66,1});

        // Print barcode: (A) format, barcode system = CODE39
        buf[0] = 0x1D;
        buf[1] = 0x6B;
        buf[2] = (byte) (type & 0xFF);
        ;
        System.arraycopy(bytedata, 0, buf, 3, bytedata.length);
        buf[(bytedata.length + 4) - 1] = 0;
        this.outputStream.write(buf);
        //this.outputStream.write(new byte[]{0x1B,'J',35});
        resetPrint();
    }

    public void printLogo(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                };
                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                String[] selectionArgs = new String[]{Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/"};
                Cursor cursor = context.getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
                Uri uri = null;
                if (Objects.requireNonNull(cursor).getCount() == 0) {
                    String ms = "No file found in \"" + Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/\"";
                    this.outputStream.write(ms.getBytes());
                } else {
                    while (cursor.moveToNext()) {
                        int idx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                        if (idx <= 0) {
                            idx = 1;
                        }
                        String fileName = cursor.getString(idx);
                        int idx2 = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                        if (idx2 <= 0) {
                            idx2 = 0;
                        }
                        if (Objects.equals(fileName, "logo.jpeg")) {
                            long id = cursor.getLong(idx2);
                            uri = ContentUris.withAppendedId(contentUri, id);
                            break;
                        }
                    }
                    if (uri == null) {
                        this.outputStream.write("El archivo de la imagen no existe".getBytes());
                    } else {
                        InputStream inputStream = context.getContentResolver().openInputStream(uri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        cursor.close();
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        int[] intarray = new int[bitmap.getWidth() * bitmap.getHeight()];
                        bitmap.getPixels(intarray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                        this.outputStream.write(ESCUtil.alignCenter());
                        this.outputStream.write(ESCUtil.printBitmap(bitmap, 2));
                        Thread.sleep(2000);
                        resetPrint();
                        feed(1);
                    }
                }
            } else {
                File file = new File(Environment.getExternalStorageDirectory() + "/Desoftinf//logo.jpeg");
                if (!file.exists()) {
                    this.outputStream.write("El archivo de la imagen no existe".getBytes());
                }
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                this.outputStream.write(ESCUtil.alignCenter());
                this.outputStream.write(ESCUtil.printBitmap(bitmap, 2));
                Thread.sleep(2000);
                resetPrint();
                feed(1);
            }
        } catch (Exception ex) {
            try {
                this.outputStream.write("Error al imprimir la imagen".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

