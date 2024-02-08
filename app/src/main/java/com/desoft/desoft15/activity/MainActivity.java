package com.desoft.desoft15.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.datecs.api.printer.Printer;
import com.desoft.desoft15.R;
import com.desoft.desoft15.activity.BluetoothPickerActivity;
import com.desoft.desoft15.printer.PrintData;
import com.desoft.desoft15.printer.PrintDataSunmi;
import com.desoft.desoft15.printer.PrintType;
import com.desoft.desoft15.printer.PrinterAlignment;
import com.desoft.desoft15.printer.PrinterManager;
import com.desoft.desoft15.printer.PrinterTextStyle;
import com.desoft.desoft15.utils.ConectionWithPrint;
import com.desoft.desoft15.utils.ESCUtil;
import com.desoft.desoft15.utils.RedAyuda;
import com.desoft.desoft15.utils.SunmiPrintHelper;
import com.desoft.desoft15.utils.UtilAyuda;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {
    public ArrayList<ColaImpresion> colaImpresions = new ArrayList<>();
    private final ArrayList<PrintData> printDataList = new ArrayList<>();
    private final ArrayList<PrintDataSunmi> printDataListSunmi = new ArrayList<>();
    public int countDown = 0;
    public int countUp = 0;
    ProgressBar progressBar;
    SwipeRefreshLayout swipeLayout;
    WebView webView;
    private DownloadManager downloadManager;
    private FusedLocationProviderClient fusedLocationClient;
    private final int BARCODESCANNERREQUEST = 147852;
    private String BARCODESCANNERFIELD = "";
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 500;
    private final static int FILECHOOSER_RESULTCODE = 400;

    private class AndroidInterfaces {
        static final int PRINT_FEEDPAPER = 2;
        static final int PRINT_TAGGEDTEXT = 1;

        AndroidInterfaces() {
        }

        @JavascriptInterface
        public void OpenPrinter(String printername) {
            if (printername == null || printername.isEmpty()) {
                printername = MainActivity.this.getSharedPreferences("Config", 0).getString("printer", "");
            }
            try {
                MainActivity.this.Imprimir(printername);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void OpenPrinter2(String printername) {
            if (printername == null || printername.isEmpty()) {
                printername = MainActivity.this.getSharedPreferences("Config", 0).getString("printer", "");
            }
            try {
                MainActivity.this.Imprimirnew(printername);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void OpenPrinterSunmi() {
            try {
                MainActivity.this.ImprimirSunmi();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @JavascriptInterface
        public void PrintTaggedText(String taggedtext) {
            PrintTaggedText(taggedtext, "CP858");
        }

        @JavascriptInterface
        public void PrintTextNew(String texto, int align, int size, boolean bold) {
            PrinterAlignment alignment;
            PrinterTextStyle textsize;
            switch (align) {
                case 2:
                    alignment = PrinterAlignment.right;
                    break;
                case 1:
                    alignment = PrinterAlignment.center;
                    break;
                default:
                    alignment = PrinterAlignment.left;
                    break;
            }
            switch (size) {
                case 1:
                    textsize = PrinterTextStyle.bigtext;
                    break;
                case 2:
                    textsize = PrinterTextStyle.extrabig;
                    break;
                default:
                    textsize = PrinterTextStyle.normaltext;
                    break;
            }
            printDataList.add(new PrintData(PrintType.text, texto, alignment, textsize, 0, bold));
        }

        @JavascriptInterface
        public void PrintSunmiText(String texto, int align, int size, boolean bold, boolean underline) {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.text, texto, align, size, 0, bold, underline, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void PrintSunmi2ColumnText(String col1, String col2, int al1, int al2, int size, boolean bold, boolean underline) {
            ArrayList<String> texts = new ArrayList<>();
            texts.add(col1);
            texts.add(col2);
            ArrayList<Integer> aligns = new ArrayList<>();
            aligns.add(al1);
            aligns.add(al2);
            printDataListSunmi.add(new PrintDataSunmi(PrintType.column, "", 0, size, 0, bold, underline, texts, aligns));
        }

        @JavascriptInterface
        public void PrintSunmi3ColumnText(String col1, String col2, String col3, int al1, int al2, int al3, int size, boolean bold, boolean underline) {
            ArrayList<String> texts = new ArrayList<>();
            texts.add(col1);
            texts.add(col2);
            texts.add(col3);
            ArrayList<Integer> aligns = new ArrayList<>();
            aligns.add(al1);
            aligns.add(al2);
            aligns.add(al3);
            printDataListSunmi.add(new PrintDataSunmi(PrintType.column, "", 0, size, 0, bold, underline, texts, aligns));
        }

        @JavascriptInterface
        public void PrintSunmiBarCode(String barcode, int encode) {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.text, barcode, 0, 0, encode, false, false, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void PrintSunmiBarCode(String barcode, int encode, int height, int width) {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.barcode, barcode, height, width, encode, false, false, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void FeedSunmiLine(int lines) {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.feed, "", 0, 0, lines, false, false, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void PrintSunmiSeparator() {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.separaor, "", 0, 0, 0, false, false, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void SunmiCutPaper() {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.cuter, "", 0, 0, 0, false, false, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void PrintSunmiLogo() {
            printDataListSunmi.add(new PrintDataSunmi(PrintType.image, "", 0, 0, 0, false, false, new ArrayList<String>(), new ArrayList<>()));
        }

        @JavascriptInterface
        public void FeedLine(int lines) {
            printDataList.add(new PrintData(PrintType.feed, "", null, null, lines, false));
        }

        @JavascriptInterface
        public void Printbarcodenew(int type, String barcode) {
            printDataList.add(new PrintData(PrintType.barcode, barcode, null, null, type, false));
        }

        @JavascriptInterface
        public void PrintTaggedText(String taggedtext, String charset) {
            colaImpresions.add(new ColaImpresion(taggedtext, charset, 0, 1));
        }

        @JavascriptInterface
        public void FeedPaper(int lines) {
            colaImpresions.add(new ColaImpresion("", "", lines, 2));
        }

        @JavascriptInterface
        public void PrintSunmiTestPage() {
            SunmiPrintHelper.getInstance().printExample(MainActivity.this);
        }

        @JavascriptInterface
        public void resetPrint() {
            PrintTaggedText("{reset}", "CP858");
        }

        @JavascriptInterface
        public void ClearHistorial() {
            ClearHistorialWebView();
        }

        @JavascriptInterface
        public void ChangeColorStatusBar(final String colorHex) {
            runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        Window window = getWindow();
                        if (Build.VERSION.SDK_INT >= 19) {
                            window.clearFlags(67108864);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            window.addFlags(Integer.MIN_VALUE);
                        }
                        if (Build.VERSION.SDK_INT >= 21) {
                            window.setStatusBarColor(Color.parseColor(colorHex));
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            });
        }

        @JavascriptInterface
        public void printbarcode(int barcodetype, String text) {
            colaImpresions.add(new ColaImpresion(text, "", barcodetype, 3));
        }

        @JavascriptInterface
        public void PrintLogoNew() {
            printDataList.add(new PrintData(PrintType.image, "", PrinterAlignment.center, PrinterTextStyle.normaltext, 0, false));
        }

        @JavascriptInterface
        public void printlogo() {
            colaImpresions.add(new ColaImpresion("", "CP858", 0, 4));
        }

        @JavascriptInterface
        public void OcultarTeclado() {
            runOnUiThread(new Runnable() {
                public void run() {
                    UtilAyuda.OcultarTeclado(MainActivity.this);
                }
            });
        }

        @JavascriptInterface
        public String getIdDispositivo() {
            return UtilAyuda.getIdDispositivo(MainActivity.this);
        }

        @JavascriptInterface
        public void AplicarUrlsDefault(final String UrlLocal, final String UrlRemota, final boolean NavWeb) {
            runOnUiThread(new Runnable() {
                public void run() {
                    SharedPreferences.Editor editor = getSharedPreferences("Config", 0).edit();
                    editor.putBoolean("cache", false);
                    editor.putBoolean("zoom", false);
                    editor.putBoolean("navweb", NavWeb);
                    editor.putString("UrlLocal", UrlLocal.trim());
                    editor.putString("UrlRemoto", UrlRemota.trim());
                    editor.putString("printer", "");
                    editor.apply();
                    ClearHistorialWebView();
                    RestarConfigWebView();
                    DetectaConexion();
                }
            });
        }

        @JavascriptInterface
        public String getLastLocation() {
            JSONObject result = new JSONObject();
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                try {
                    result.put("altitude", 0);
                    result.put("latitude", 0);
                } catch (JSONException e) {
                    System.out.println(e.getMessage());
                }
                return result.toString();
            }
            Location location = MainActivity.this.fusedLocationClient.getLastLocation().getResult();
            assert location != null;
            try {
                result.put("altitude", location.getAltitude());
                result.put("latitude", location.getLatitude());
            } catch (JSONException e) {
                System.out.println(e.getMessage());
            }
            return result.toString();
        }

        @JavascriptInterface
        public void openinbrowser(String url) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        }

        @JavascriptInterface
        public void leerbarcode(String idcampo) {
            /*Intent barcode = new Intent(MainActivity.this, BarcodeScanerActivity.class);
            barcode.putExtra("idcampo", idcampo);
            startActivityForResult(barcode, 1123);*/
            IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
            integrator.addExtra("idcampo", idcampo);
            BARCODESCANNERFIELD = idcampo;
            integrator.initiateScan();
        }

        @JavascriptInterface
        public void sendWhatsAppImage(String imagename) {
            sendWhatsAppImage(imagename, "");
        }

        @JavascriptInterface
        public void sendWhatsAppImage(String imagename, String message) {
            try {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.setPackage("com.whatsapp");
                shareIntent.putExtra(Intent.EXTRA_TEXT, message);
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                    String[] projection = {
                            MediaStore.MediaColumns._ID,
                            MediaStore.MediaColumns.DISPLAY_NAME,
                    };
                    String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                    String[] selectionArgs = new String[]{Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/"};
                    Cursor cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
                    Uri uri = null;
                    if (Objects.requireNonNull(cursor).getCount() == 0) {
                        Toast.makeText(MainActivity.this, "No file found in \"" + Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/\"", Toast.LENGTH_LONG).show();
                        return;
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
                            if (fileName.equals(imagename)) {
                                long id = cursor.getLong(idx2);
                                uri = ContentUris.withAppendedId(contentUri, id);
                                break;
                            }
                        }
                        if (uri == null) {
                            Toast.makeText(MainActivity.this, imagename + " not found", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        }
                    }
                    cursor.close();
                } else {
                    File file = new File(new File(Environment.getExternalStorageDirectory() + "/Desoftinf/"), imagename);
                    Uri imageUri = Uri.parse(file.getAbsolutePath());
                    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                }
                //startActivity(shareIntent);
                startActivity(Intent.createChooser(shareIntent, null));
            } catch (ActivityNotFoundException ex) {
                System.out.println(ex.getMessage());
                Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @JavascriptInterface
        public boolean checkImageExist(String imagename) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                String[] projection = {
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                };
                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                String[] selectionArgs = new String[]{Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/"};
                Cursor cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
                Uri uri = null;
                if (Objects.requireNonNull(cursor).getCount() == 0) {
                    System.out.println("No file found in \"" + Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/\"");
                    cursor.close();
                    return false;
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
                        if (fileName.equals(imagename)) {
                            long id = cursor.getLong(idx2);
                            uri = ContentUris.withAppendedId(contentUri, id);
                            break;
                        }
                    }
                    if (uri == null) {
                        System.out.println("Imagen no existe");
                        cursor.close();
                        return false;
                    } else {
                        cursor.close();
                        return true;
                    }
                }
            }else {
                File file = new File(new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Desoftinf/"), imagename);
                return file.exists();
            }
        }

        @JavascriptInterface
        public String getConnectedDevices() {
            BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        "android.permission.BLUETOOTH_CONNECT"
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        "android.permission.BLUETOOTH_SCAN"
                ) == PackageManager.PERMISSION_GRANTED
                ) {
                    if (bluetoothManager.getAdapter().isDiscovering()) {
                        bluetoothManager.getAdapter().cancelDiscovery();
                    }
                    JSONArray deviceslist = new JSONArray();
                    Set<BluetoothDevice> bondeddevices = bluetoothManager.getAdapter().getBondedDevices();
                    for (BluetoothDevice d : bondeddevices) {
                        JSONObject device = new JSONObject();
                        try {
                            device.put("name", d.getName());
                            device.put("address", d.getAddress());
                        } catch (JSONException e) {
                            return "ERROR: " + e.getMessage();
                        }
                        deviceslist.put(device);
                    }
                    return deviceslist.toString();
                } else {
                    JSONArray deviceslist = new JSONArray();
                    return deviceslist.toString();
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(),
                        "android.permission.BLUETOOTH_ADMIN"
                ) == PackageManager.PERMISSION_GRANTED) {
                    if (bluetoothManager.getAdapter().isDiscovering()) {
                        bluetoothManager.getAdapter().cancelDiscovery();
                    }
                    JSONArray deviceslist = new JSONArray();
                    Set<BluetoothDevice> bondeddevices = bluetoothManager.getAdapter().getBondedDevices();
                    for (BluetoothDevice d : bondeddevices) {
                        JSONObject device = new JSONObject();
                        try {
                            device.put("name", d.getName());
                            device.put("address", d.getAddress());
                        } catch (JSONException e) {
                            return "ERROR: " + e.getMessage();
                        }
                        deviceslist.put(device);
                    }
                    return deviceslist.toString();
                } else {
                    JSONArray deviceslist = new JSONArray();
                    return deviceslist.toString();
                }
            }
        }

        @JavascriptInterface
        public void selectPrinterDevice(String idcampo) {
            BluetoothManager bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (!bluetoothManager.getAdapter().isEnabled()) {
                Toast.makeText(getApplicationContext(), "El bluetooth esta apagado!", Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(MainActivity.this, BluetoothPickerActivity.class);
            intent.putExtra("idcampo", idcampo);
            startActivityForResult(intent, 1111);
        }
    }

    private static class ColaImpresion {
        String CharSet;
        String Text;
        int TipoImpresion;
        int linesfeed;

        ColaImpresion(String Text2, String CharSet2, int linesfeed2, int TipoImpresion2) {
            this.Text = Text2;
            this.CharSet = CharSet2;
            this.linesfeed = linesfeed2;
            this.TipoImpresion = TipoImpresion2;
        }
    }

    /* access modifiers changed from: protected */
    @SuppressLint({"SetJavaScriptEnabled"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        CreateDir();
        CreatelocalurlDir();
        this.webView = (WebView) findViewById(R.id.webview);
        this.swipeLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        this.swipeLayout.setColorSchemeResources(R.color.blue, R.color.purple, R.color.green, R.color.orange);
        this.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                webView.reload();
            }
        });
        this.swipeLayout.setEnabled(false);
        this.webView.getSettings().setLoadsImagesAutomatically(true);
        this.webView.getSettings().setJavaScriptEnabled(true);
        if (getSharedPreferences("Config", 0).getBoolean("zoom", false)) {
            this.webView.getSettings().setBuiltInZoomControls(true);
            this.webView.getSettings().setSupportZoom(true);
            this.webView.getSettings().setDisplayZoomControls(false);
        }
        this.webView.addJavascriptInterface(new AndroidInterfaces(), "AndroidInterfaces");
        this.webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        this.webView.getSettings().setSaveFormData(true);
        this.webView.clearSslPreferences();
        this.webView.setCertificate(this.webView.getCertificate());
        this.webView.getSettings().setDomStorageEnabled(true);
        this.webView.getSettings().setAllowFileAccess(true);
        this.webView.getSettings().setAllowContentAccess(true);
        this.webView.getSettings().setGeolocationEnabled(true);
        this.webView.getSettings().setGeolocationDatabasePath(getFilesDir().getPath());
        this.webView.getSettings().setUseWideViewPort(true);
        this.webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }

            public void onPageFinished(WebView view, String url) {
                if (swipeLayout.isRefreshing()) {
                    swipeLayout.setRefreshing(false);
                }
                super.onPageFinished(view, url);
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (errorCode == -2) {
                    findViewById(R.id.error).setVisibility(View.VISIBLE);
                    webView.loadData("<h2 id=\"hola\">ERROR DE CONEXION</h2> <script>hola.style.textAlign=\"center\"</script>", "text/html", "utf-8");
                    return;
                }
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                super.onReceivedSslError(view, handler, error);
                handler.proceed();
            }
        });
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(0);
                progressBar.setMax(100);
                progressBar.setVisibility(View.VISIBLE);
                progressBar.setProgress(progress);
                progressBar.incrementProgressBy(progress);
                if (progress == 100) {
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // this should never happen, it means user revoked permissions
                    // need to warn and quit?
                    callback.invoke(origin, false, false);
                } else {
                    callback.invoke(origin, true, true);
                }
            }

            // For 3.0+ Devices (Start)
            // onActivityResult attached before constructor
            protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
            }


            // For Lollipop 5.0+ Devices
            public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (uploadMessage != null) {
                    uploadMessage.onReceiveValue(null);
                    uploadMessage = null;
                }

                uploadMessage = filePathCallback;

                Intent intent = fileChooserParams.createIntent();
                try {
                    startActivityForResult(intent, REQUEST_SELECT_FILE);
                } catch (ActivityNotFoundException e) {
                    uploadMessage = null;
                    Toast.makeText(MainActivity.this, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                    return false;
                }
                return true;
            }

            //For Android 4.1 only
            protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "File Browser"), FILECHOOSER_RESULTCODE);
            }

            protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
            }
        });

        this.webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (URLUtil.isNetworkUrl(url)) {
                try {
                    if (url.startsWith("https")) {
                        SSLContext ctx = SSLContext.getInstance("TLS");
                        ctx.init(null, new TrustManager[]{
                                new X509TrustManager() {
                                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                                    }

                                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                                    }

                                    public X509Certificate[] getAcceptedIssuers() {
                                        return new X509Certificate[]{};
                                    }
                                }
                        }, null);
                        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
                        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
                    }
                    final URL urlx = new URL(url);
                    Thread thread = new Thread(() -> {
                        try {
                            URLConnection urlConnection = urlx.openConnection();
                            //urlConnection.setRequestMethod("GET");
                            urlConnection.setDoOutput(true);
                            urlConnection.connect();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                ContentValues values = new ContentValues();
                                values.put(MediaStore.MediaColumns.DISPLAY_NAME, URLUtil.guessFileName(url, contentDisposition, mimetype));       //file name
                                values.put(MediaStore.MediaColumns.MIME_TYPE, mimetype);        //file extension, will automatically add to file
                                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/");     //end "/" is not mandatory
                                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);      //important!
                                OutputStream outputStream = getContentResolver().openOutputStream(Objects.requireNonNull(uri));
                                InputStream inputStream = urlConnection.getInputStream();

                                byte[] buffer = new byte[1024];
                                int bufferLength = 0;

                                while ((bufferLength = inputStream.read(buffer)) > 0) {
                                    Objects.requireNonNull(outputStream).write(buffer, 0, bufferLength);
                                }
                                Objects.requireNonNull(outputStream).close();
                            } else {
                                File file = new File(new File(Environment.getExternalStorageDirectory() + "/Desoftinf/"), URLUtil.guessFileName(url, contentDisposition, mimetype));
                                FileOutputStream fileOutput = new FileOutputStream(file);
                                InputStream inputStream = urlConnection.getInputStream();

                                byte[] buffer = new byte[1024];
                                int bufferLength = 0;

                                while ((bufferLength = inputStream.read(buffer)) > 0) {
                                    fileOutput.write(buffer, 0, bufferLength);
                                }
                                fileOutput.close();
                            }
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Descarga Finalizada!", Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (IOException e) {
                            final Exception ex = new Exception(e.getMessage());
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), ex.getMessage() + "Por favor intentalo nuevamente!", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    });
                    thread.start();
                    Toast.makeText(getApplicationContext(), "Descarga iniciada!", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Por favor intentalo nuevamente!", Toast.LENGTH_LONG).show();
                }
            } else {
                try {
                    String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
                    String filename = System.currentTimeMillis() + "." + filetype;
                    String[] sourcepart = url.split(",");
                    String imagestring = sourcepart[1];
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
                        values.put(MediaStore.MediaColumns.MIME_TYPE, mimetype);
                        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/");     //end "/" is not mandatory
                        Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);      //important!
                        OutputStream outputStream = getContentResolver().openOutputStream(Objects.requireNonNull(uri));
                        byte[] decodedBytes = Base64.decode(imagestring, Base64.DEFAULT);
                        Objects.requireNonNull(outputStream).write(decodedBytes);
                        Objects.requireNonNull(outputStream).close();
                    } else {
                        File file = new File(new File(Environment.getExternalStorageDirectory() + "/Desoftinf/"), filename);
                        byte[] decodedBytes = Base64.decode(imagestring, Base64.DEFAULT);
                        OutputStream os = new FileOutputStream(file);
                        os.write(decodedBytes);
                        os.close();
                    }
                    webView.loadUrl("javascript:codeb('imagenrecibo','" + filename + "')");
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        DetectaConexion();
    }

    public void RestarConfigWebView() {
        if (getSharedPreferences("Config", 0).getBoolean("zoom", false)) {
            this.webView.getSettings().setBuiltInZoomControls(true);
            this.webView.getSettings().setSupportZoom(true);
            this.webView.getSettings().setDisplayZoomControls(false);
        } else {
            this.webView.getSettings().setBuiltInZoomControls(false);
            this.webView.getSettings().setSupportZoom(false);
            this.webView.getSettings().setDisplayZoomControls(false);
        }
    }

    @SuppressLint({"RtlHardcoded"})
    public void DetectaConexion() {
        boolean contieneDosPunto;
        boolean contieneSlash;
        SharedPreferences sharedPreferences = getSharedPreferences("Config", 0);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        if (RedAyuda.getHaveNetworkMobile(this)) {
            editor.putBoolean("conexionRemoto", true);
            editor.apply();
            CargarUrl();
            return;
        }
        String url = Objects.requireNonNull(sharedPreferences.getString("UrlRemoto", "")).trim().replaceAll("(?i)http://", "").replaceAll("(?i)https://", "");
        contieneDosPunto = !(url.indexOf(":") <= 0 || url.indexOf(":") >= url.length());
        contieneSlash = !(url.indexOf("/") <= 0 || url.indexOf("/") >= url.length());
        int length = contieneDosPunto ? url.indexOf(":") : contieneSlash ? url.indexOf("/") : url.length();
        String url2 = url.substring(0, length);
        if (url2.length() <= 0 || Objects.requireNonNull(sharedPreferences.getString("UrlRemoto", "")).equalsIgnoreCase(sharedPreferences.getString("UrlLocal", ""))) {
            editor.putBoolean("conexionRemoto", false);
            editor.apply();
            CargarUrl();
            return;
        }
        RedAyuda.ValidarConexion(url2, new RedAyuda.Valida() {
            public String OnConexionLocal() {
                editor.putBoolean("conexionRemoto", false);
                editor.apply();
                CargarUrl();
                return "";
            }

            public String OnConexionRemota() {
                editor.putBoolean("conexionRemoto", true);
                editor.apply();
                CargarUrl();
                return "";
            }
        });
    }

    public void CargarUrl() {
        findViewById(R.id.error).setVisibility(View.GONE);
        SharedPreferences preferences = getSharedPreferences("Config", 0);
        final boolean isurllocal = preferences.getBoolean("localurl", false);
        if (isurllocal) {
            this.webView.loadUrl("file:///android_asset/prestamosoff/index.html");
        } else {
            String url = preferences.getBoolean("conexionRemoto", false) ? preferences.getString("UrlRemoto", "") : preferences.getString("UrlLocal", "");
            if (url == null || url.isEmpty()) {
                this.webView.loadUrl("http://desoftinf.com/printersdk.php");
            } else {
                this.webView.loadUrl(url);
            }
        }
    }

    public void Error(View view) {
        if (!RedAyuda.getHaveNetworkConnection(this)) {
            findViewById(R.id.error).setVisibility(View.VISIBLE);
            this.webView.loadData("<h2 id=\"hola\">ERROR DE CONEXION</h2> <script>hola.style.textAlign=\"center\"</script>", "text/html", "utf-8");
            Toast.makeText(getApplicationContext(), "Por favor intentalo nuevamente!", Toast.LENGTH_LONG).show();
            return;
        }
        findViewById(R.id.error).setVisibility(View.GONE);
        DetectaConexion();
    }

    public void CreateDir() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Desoftinf/");
        if (!file.exists()) {
            file.mkdir();
        }
    }

    private void CreatelocalurlDir() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Desoftinf/htdocs/");
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void onPause() {
        super.onPause();
        try {
            Class.forName("android.webkit.WebView").getMethod("onPause", null).invoke(this.webView, null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        try {
            Class.forName("android.webkit.WebView").getMethod("onResume", null).invoke(this.webView, null);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        this.webView.destroy();
        this.webView = null;
        SunmiPrintHelper.getInstance().deInitSunmiPrinterService(this);
        super.onDestroy();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean z = true;
        if (keyCode == 82) {
            this.webView.loadUrl("javascript:(function () { try { openMenu(); }catch(erro){/*alert(erro.message);*/}})();");
        }
        if (event.getAction() == 0 && keyCode == 24) {
            if (this.countUp == 2) {
                new CountDownTimer(3000, 500) {
                    public void onTick(long l) {
                    }

                    public void onFinish() {
                        countUp = 0;
                        countDown = 0;
                    }
                }.start();
            } else {
                this.countUp++;
            }
            if (this.countUp < 2) {
                return true;
            }
            return false;
        } else if (event.getAction() != 0 || keyCode != 25) {
            return super.onKeyDown(keyCode, event);
        } else {
            if (this.countDown == 3) {
                Config_Dialog();
                this.countDown = 0;
                this.countUp = 0;
            } else if (this.countUp >= 2) {
                this.countDown++;
            }
            if (this.countUp < 2 || this.countDown >= 3) {
                z = false;
            }
            return z;
        }
    }

    private void Config_Dialog() {
        final SharedPreferences preferences = getSharedPreferences("Config", 0);
        View view = getLayoutInflater().inflate(R.layout.prompt_config, null);
        final EditText etUrlLocal = (EditText) view.findViewById(R.id.etUrlLocal);
        final EditText etUrlRemoto = (EditText) view.findViewById(R.id.etUrlRemoto);
        final EditText etPrinter = (EditText) view.findViewById(R.id.etPrinter);
        final CheckBox cbCache = (CheckBox) view.findViewById(R.id.cbCache);
        final CheckBox cbZoom = (CheckBox) view.findViewById(R.id.cbZoom);
        final CheckBox cbBack = (CheckBox) view.findViewById(R.id.cbBack);
        final CheckBox cblocal = (CheckBox) view.findViewById(R.id.cblocal);
        etUrlLocal.setText(preferences.getString("UrlLocal", ""));
        etUrlRemoto.setText(preferences.getString("UrlRemoto", ""));
        etPrinter.setText(preferences.getString("printer", ""));
        cbCache.setChecked(preferences.getBoolean("cache", false));
        cbZoom.setChecked(preferences.getBoolean("zoom", false));
        cbBack.setChecked(preferences.getBoolean("navweb", false));
        cblocal.setChecked(preferences.getBoolean("localurl", false));
        final AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Configuración")
                .setIcon(R.mipmap.configuracion_small)
                .setView(view)
                .setNegativeButton("CANCELAR", null)
                .setNeutralButton("DEFAULT", null)
                .setPositiveButton("APLICAR", null).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(-2).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });
                dialog.getButton(-3).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("cache", false);
                        editor.putBoolean("zoom", false);
                        editor.putBoolean("navweb", false);
                        editor.putString("UrlLocal", "");
                        editor.putString("UrlRemoto", "");
                        editor.putString("printer", "");
                        editor.putBoolean("conexionRemoto", false);
                        editor.putBoolean("localurl", false);
                        editor.apply();
                        dialog.cancel();
                        ClearHistorialWebView();
                        RestarConfigWebView();
                        DetectaConexion();
                    }
                });
                dialog.getButton(-1).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        if (!cblocal.isChecked()) {
                            if (etUrlLocal.getText().toString().trim().isEmpty() || (!etUrlLocal.getText().toString().toLowerCase().contains("http://") && !etUrlLocal.getText().toString().toLowerCase().contains("https://")) && !cblocal.isChecked()) {
                                etUrlLocal.setError("Datos invalidos");
                                etUrlLocal.requestFocus();
                                return;
                            }
                            if (etUrlRemoto.getText().toString().trim().isEmpty() || (!etUrlRemoto.getText().toString().toLowerCase().contains("http://") && !etUrlRemoto.getText().toString().toLowerCase().contains("https://")) && !cblocal.isChecked()) {
                                etUrlRemoto.setError("Datos invalidos");
                                etUrlRemoto.requestFocus();
                                return;
                            }
                        }
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putBoolean("cache", cbCache.isChecked());
                        editor.putBoolean("zoom", cbZoom.isChecked());
                        editor.putBoolean("navweb", cbBack.isChecked());
                        editor.putString("UrlLocal", etUrlLocal.getText().toString().trim());
                        editor.putString("UrlRemoto", etUrlRemoto.getText().toString().trim());
                        editor.putString("printer", etPrinter.getText().toString().trim());
                        editor.putBoolean("localurl", cblocal.isChecked());
                        editor.apply();
                        dialog.cancel();
                        ClearHistorialWebView();
                        RestarConfigWebView();
                        DetectaConexion();
                    }
                });
            }
        });
        dialog.show();
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (getSharedPreferences("Config", 0).getBoolean("navweb", false) || !this.webView.canGoBack()) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setIcon(R.mipmap.applogo)
                    .setTitle(R.string.app_name)
                    .setMessage("Desea salir de la aplicacion?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();
            dialog.show();
            return;
        }
        this.webView.goBack();
    }

    public void ClearHistorialWebView() {
        this.webView.clearFormData();
        this.webView.clearHistory();
        this.webView.clearView();
    }

    public void Imprimir(String printername) {
        new ConectionWithPrint(MainActivity.this, printername) {
            public void conexionStatus(boolean status) {
                if (status) {
                    Toast.makeText(MainActivity.this, "Conexión establecida.", Toast.LENGTH_SHORT).show();
                    for (ColaImpresion colaImpresion : colaImpresions) {
                        try {
                            switch (colaImpresion.TipoImpresion) {
                                case 1:
                                    this.printer.printTaggedText(colaImpresion.Text, colaImpresion.CharSet);
                                    this.printer.reset();
                                    break;
                                case 2:
                                    this.printer.feedPaper(colaImpresion.linesfeed);
                                    break;
                                case 3: {
                                    this.printer.setAlign(Printer.ALIGN_CENTER);
                                    this.printer.printBarcode(colaImpresion.linesfeed, colaImpresion.Text);
                                    this.printer.feedPaper(10);
                                    this.printer.reset();
                                    break;
                                }
                                case 4: {
                                    try {
                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                            Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                                            String[] projection = {
                                                    MediaStore.MediaColumns._ID,
                                                    MediaStore.MediaColumns.DISPLAY_NAME,
                                            };
                                            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                                            String[] selectionArgs = new String[]{Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/"};
                                            Cursor cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
                                            Uri uri = null;
                                            if (Objects.requireNonNull(cursor).getCount() == 0) {
                                                this.printer.printTaggedText("No file found in \"" + Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/\"", colaImpresion.CharSet);
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
                                                    if (fileName.equals("logo.jpeg")) {
                                                        long id = cursor.getLong(idx2);
                                                        uri = ContentUris.withAppendedId(contentUri, id);
                                                        break;
                                                    }
                                                }
                                                if (uri == null) {
                                                    this.printer.printTaggedText("El archivo de la imagen no existe", colaImpresion.CharSet);
                                                } else {
                                                    InputStream inputStream = getContentResolver().openInputStream(uri);
                                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                                    cursor.close();
                                                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                    int[] intarray = new int[bitmap.getWidth() * bitmap.getHeight()];
                                                    bitmap.getPixels(intarray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                                                    this.printer.printImage(intarray, bitmap.getWidth(), bitmap.getHeight(), Printer.ALIGN_CENTER, true);
                                                    this.printer.reset();
                                                    this.printer.feedPaper(10);
                                                }
                                            }
                                        }else {
                                            File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Desoftinf//logo.jpeg");
                                            if (!file.exists()) {
                                                this.printer.printTaggedText("El archivo de la imagen no existe", colaImpresion.CharSet);
                                                break;
                                            }
                                            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                            int[] intarray = new int[bitmap.getWidth() * bitmap.getHeight()];
                                            bitmap.getPixels(intarray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                                            this.printer.printImage(intarray, bitmap.getWidth(), bitmap.getHeight(), Printer.ALIGN_CENTER, true);
                                            this.printer.reset();
                                            this.printer.feedPaper(10);
                                        }
                                        break;
                                    } catch (Exception ex) {
                                        this.printer.printTaggedText("Error al imprimir la imagen", colaImpresion.CharSet);
                                        break;
                                    }
                                }
                            }
                            Thread.sleep(200);
                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this, "Error al imprimir el recibo en la impresora: " + this.printerName, Toast.LENGTH_SHORT).show();
                        }
                    }
                    try {
                        this.printer.flush();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    try {
                        closeBlutoothConnection();
                        Toast.makeText(MainActivity.this, "Conecion con el printer cerrado", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {

                    try {
                        closeBlutoothConnection();
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                colaImpresions.clear();
            }

            public void closeConexionStatus(boolean status) {
                if (status) {
                    Toast.makeText(MainActivity.this, "Conexión cerrada.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "No se pudo, cerrar la conexión.", Toast.LENGTH_SHORT).show();
                }
            }
        }.establishBluetoothConnection();
    }

    public void Imprimirnew(String printername) {
        final PrinterManager printerManager = new PrinterManager();
        try {
            Toast.makeText(MainActivity.this, "Conectando impresora", Toast.LENGTH_SHORT).show();
            printerManager.loadPrinterbyName(this, printername);
            printerManager.openConnection(this);
            Toast.makeText(MainActivity.this, "Conectado al printer " + printername, Toast.LENGTH_SHORT).show();
            printerManager.resetPrint();
            for (PrintData printData : printDataList) {
                switch (printData.getType()) {
                    case text:
                        printerManager.print(printData);
                        break;
                    case feed:
                        printerManager.feed(printData.getFeedline());
                        break;
                    case barcode:
                        printerManager.printBarcode(printData.getFeedline(), printData.getTexto());
                        break;
                    case image:
                        printerManager.printLogo(MainActivity.this);
                        break;
                    default:
                        throw new Exception("Tipo de impresion no configurado");
                }
            }
            printerManager.feedandcut();
            //Thread.sleep(1500);
            printerManager.closeConnection();
            printDataList.clear();
            Toast.makeText(MainActivity.this, "Impresion finalizada", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            if (printerManager.isConnected()) {
                try {
                    printerManager.closeConnection();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    public void ImprimirSunmi() {
        final SunmiPrintHelper sunmiPrintHelper = SunmiPrintHelper.getInstance();
        Toast.makeText(MainActivity.this, "Conectando impresora", Toast.LENGTH_SHORT).show();
        if (sunmiPrintHelper.sunmiPrinter != SunmiPrintHelper.FoundSunmiPrinter) {
            colaImpresions.clear();
            Toast.makeText(MainActivity.this, "No se encontro la impesora sunmi", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            sunmiPrintHelper.initPrinter();
            for (PrintDataSunmi printData : printDataListSunmi) {
                switch (printData.getType()) {
                    case text: {
                        sunmiPrintHelper.setAlign(printData.getAlignment());
                        sunmiPrintHelper.printText(printData.getTexto(), (float) printData.getSize(), printData.isBold(), printData.isUnderline(), null);
                        break;
                    }
                    case feed:
                        sunmiPrintHelper.feedPaper(printData.getFeedline());
                        break;
                    case barcode:
                        sunmiPrintHelper.setAlign(1);
                        sunmiPrintHelper.sendRawData(ESCUtil.alignCenter());
                        sunmiPrintHelper.printBarCode(printData.getTexto(), printData.getFeedline(), printData.getAlignment() <= 0 ? 40 : printData.getAlignment(), printData.getSize() <= 0 ? 2 : (int) printData.getSize(), 2);
                        break;
                    case image: {
                        try {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                                Uri contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                                String[] projection = {
                                        MediaStore.MediaColumns._ID,
                                        MediaStore.MediaColumns.DISPLAY_NAME,
                                };
                                String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=?";
                                String[] selectionArgs = new String[]{Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/"};
                                Cursor cursor = getContentResolver().query(contentUri, projection, selection, selectionArgs, null);
                                Uri uri = null;
                                if (Objects.requireNonNull(cursor).getCount() == 0) {
                                    sunmiPrintHelper.setAlign(1);
                                    sunmiPrintHelper.printText("No file found in \"" + Environment.DIRECTORY_DOWNLOADS + "/Desoftinf/\"", 15, true, true, null);
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
                                        if (fileName.equals("logo.jpeg")) {
                                            long id = cursor.getLong(idx2);
                                            uri = ContentUris.withAppendedId(contentUri, id);
                                            break;
                                        }
                                    }
                                    if (uri == null) {
                                        sunmiPrintHelper.setAlign(1);
                                        sunmiPrintHelper.printText("El archivo de la imagen no existe", 15, true, true, null);
                                    } else {
                                        InputStream inputStream = getContentResolver().openInputStream(uri);
                                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                        cursor.close();
                                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                        int[] intarray = new int[bitmap.getWidth() * bitmap.getHeight()];
                                        bitmap.getPixels(intarray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                                        sunmiPrintHelper.setAlign(1);
                                        sunmiPrintHelper.sendRawData(ESCUtil.alignCenter());
                                        sunmiPrintHelper.printBitmap(bitmap, 1);
                                    }
                                }
                            }else {
                                File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/Desoftinf//logo.jpeg");
                                if (!file.exists()) {
                                    sunmiPrintHelper.setAlign(1);
                                    sunmiPrintHelper.printText("El archivo de la imagen no existe", 15, true, true, null);
                                    break;
                                }
                                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                int[] intarray = new int[bitmap.getWidth() * bitmap.getHeight()];
                                bitmap.getPixels(intarray, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                                sunmiPrintHelper.setAlign(1);
                                sunmiPrintHelper.sendRawData(ESCUtil.alignCenter());
                                sunmiPrintHelper.printBitmap(bitmap, 1);
                            }
                            break;
                        } catch (Exception ex) {
                            sunmiPrintHelper.setAlign(1);
                            sunmiPrintHelper.printText("Error al imprimir la imagen", 15, true, true, null);
                            break;
                        }
                    }
                    case column: {
                        int[] width = new int[]{1, 1};
                        int[] aligns = new int[printData.getColumnAlign().size()];
                        for (int i = 0; i < printData.getColumnAlign().size(); i++) {
                            aligns[i] = printData.getColumnAlign().get(i);
                        }
                        sunmiPrintHelper.printColumnsString(printData.getColumnText().toArray(new String[0]), width, aligns, printData.isBold());
                    }
                    case separaor: {
                        if (Objects.equals(sunmiPrintHelper.getPrinterPaper(), "58mm")) {
                            sunmiPrintHelper.printText("--------------------------------\n");
                        } else {
                            sunmiPrintHelper.printText("------------------------------------------------\n");
                        }
                        break;
                    }
                    case cuter: {
                        sunmiPrintHelper.cutpaper();
                        break;
                    }
                    default:
                        throw new Exception("Tipo de impresion no configurado");
                }
            }
            sunmiPrintHelper.feedPaper();
            sunmiPrintHelper.cutpaper();
            printDataListSunmi.clear();
            Toast.makeText(MainActivity.this, "Impresion finalizada", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            sunmiPrintHelper.deInitSunmiPrinterService(this);
            Toast.makeText(MainActivity.this, ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*private void buttonGenerate_onClick(View view) {
        try {
            String productId = editTextProductId.getText().toString();
            Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<EncodeHintType, ErrorCorrectionLevel>();
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            Writer codeWriter;
            codeWriter = new Code128Writer();
            BitMatrix byteMatrix = codeWriter.encode(productId, BarcodeFormat.CODE_128,400, 200, hintMap);
            int width = byteMatrix.getWidth();
            int height = byteMatrix.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    bitmap.setPixel(i, j, byteMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
                }
            }
            imageViewResult.setImageBitmap(bitmap);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 1123) {
            final String idcampo = Objects.requireNonNull(data).getStringExtra("idcampo");
            final String barcode = data.getStringExtra("codigo");
            webView.loadUrl("javascript:codeb('" + idcampo + "','" + barcode + "')");
        }

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            webView.loadUrl("javascript:codeb('" + BARCODESCANNERFIELD + "','" + scanResult.getContents() + "')");
        } else {
            webView.loadUrl("javascript:codeb('" + BARCODESCANNERFIELD + "','')");
        }

        if (requestCode == 1111) {
            final String idcampo = Objects.requireNonNull(data).getStringExtra("idcampo");
            String result = "";
            if (resultCode == Activity.RESULT_OK) {
                result = data.getStringExtra("name") + "," + data.getStringExtra("address");
            }
            //webView.loadUrl("javascript:codeb('" + idcampo + "','" + result + "')");
            webView.loadUrl("javascript: (function(){document.getElementById('" + idcampo + "').value ='" + result + "';})();");
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = data == null || resultCode != MainActivity.RESULT_OK ? null : data.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }
}
