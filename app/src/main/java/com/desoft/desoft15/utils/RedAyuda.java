package com.desoft.desoft15.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build.VERSION;
import android.support.v4.media.session.PlaybackStateCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;


public class RedAyuda {

    public interface Valida {
        String OnConexionLocal();

        String OnConexionRemota();
    }

    public static void ValidarConexion(final String hostName, final Valida valida) {
        new AsyncThread<String, String, Boolean>() {
            /* access modifiers changed from: protected */
            public Boolean doInBackground(String... var) {
                String ipExternal = RedAyuda.getExternalIP();
                return Boolean.valueOf(ipExternal.equalsIgnoreCase("") || ipExternal.equalsIgnoreCase(RedAyuda.getExternalIPofHostName(hostName)));
            }

            /* access modifiers changed from: protected */
            public void onPostExecute(Boolean res) {
                super.onPostExecute(res);
                if (res.booleanValue()) {
                    valida.OnConexionLocal();
                } else {
                    valida.OnConexionRemota();
                }
            }
        }.execute(new String[0]);
    }

    public static String ValidarConexionSinThread(String hostName, Valida valida) {
        if (getExternalIP().equalsIgnoreCase("") || getExternalIP().equalsIgnoreCase(getExternalIPofHostName(hostName))) {
            return valida.OnConexionLocal();
        }
        return valida.OnConexionRemota();
    }

    public static String getExternalIPofHostName (String hostName) {
        try {
            return InetAddress.getByName(hostName).getHostAddress().trim();
        } catch (IOException e) {
            return "";
        }
    }

    public static String getExternalIP() {
        try {
            URLConnection con = new URL("http://checkip.amazonaws.com").openConnection();
            con.setConnectTimeout(20500);
            con.setReadTimeout(20500);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            for (String linea = in.readLine(); linea != null; linea = in.readLine()) {
                sb.append(linea);
            }
            String entity = sb.toString();
            long len = (long) entity.length();
            if (len == -1 || len >= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                return entity.trim();
            }
            return entity.trim();
        } catch (Exception e) {
            return "";
        }
    }

    public static double getLongitude() {
        try {
            URLConnection con = new URL("https://4.ifcfg.me/longitude").openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            for (String linea = in.readLine(); linea != null; linea = in.readLine()) {
                sb.append(linea);
            }
            String entity = sb.toString();
            long len = (long) entity.length();
            if (len == -1 || len >= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                return Double.parseDouble(entity.trim());
            }
            return Double.parseDouble(entity.trim());
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return 0.0d;
        }
    }

    public static double getLatitude() {
        try {
            URLConnection con = new URL("https://4.ifcfg.me/latitude").openConnection();
            con.setConnectTimeout(15000);
            con.setReadTimeout(15000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            for (String linea = in.readLine(); linea != null; linea = in.readLine()) {
                sb.append(linea);
            }
            String entity = sb.toString();
            long len = (long) entity.length();
            if (len == -1 || len >= PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID) {
                return Double.parseDouble(entity.trim());
            }
            return Double.parseDouble(entity.trim());
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return 0.0d;
        }
    }

    public static boolean getHaveNetworkConnection(Context context) {
        return getHaveNetworkMobile(context) || getHaveNetworkWifi(context);
    }

    public static boolean getHaveNetworkMobile(Context context) {
        NetworkInfo[] netInfo;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (VERSION.SDK_INT >= 21) {
            for (Network network : cm.getAllNetworks()) {
                NetworkInfo networkInfo = cm.getNetworkInfo(network);
                if (networkInfo.getType() == 0 && networkInfo.isConnected()) {
                    return true;
                }
            }
            return false;
        }
        for (NetworkInfo ni : cm.getAllNetworkInfo()) {
            if (ni.getType() == 0 && ni.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static boolean getHaveNetworkWifi(Context context) {
        NetworkInfo[] netInfo;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (VERSION.SDK_INT >= 21) {
            for (Network network : cm.getAllNetworks()) {
                NetworkInfo networkInfo = cm.getNetworkInfo(network);
                if (networkInfo.getType() == 1 && networkInfo.isConnected()) {
                    return true;
                }
            }
            return false;
        }
        for (NetworkInfo ni : cm.getAllNetworkInfo()) {
            if (ni.getType() == 1 && ni.isConnected()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOnline(Context context) {
        NetworkInfo netInfo = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public static boolean serverAvailableWithPort(String hostname, String port, int timeout) {
        boolean z;
        SocketAddress sockAddr = new InetSocketAddress(hostname, Integer.parseInt(port));
        Socket sock = new Socket();
        try {
            sock.connect(sockAddr, timeout);
            z = true;
            sock.close();
        } catch (Exception e) {
            z = false;
        }
        return z;
    }

    public static boolean serverAvailableWithoutPort(String hostname, int timeout) {
        try {
            return InetAddress.getByName(hostname).isReachable(timeout);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
