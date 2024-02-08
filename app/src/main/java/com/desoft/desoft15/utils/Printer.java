package com.desoft.desoft15.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Printer {
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 2;
    public static final int BARCODE_CODABAR = 71;
    public static final int BARCODE_CODE128 = 73;
    public static final int BARCODE_CODE128AUTO = 75;
    public static final int BARCODE_CODE39 = 69;
    public static final int BARCODE_CODE93 = 72;
    public static final int BARCODE_EAN128 = 76;
    public static final int BARCODE_EAN13 = 67;
    public static final int BARCODE_EAN8 = 68;
    public static final int BARCODE_ITF = 70;
    public static final int BARCODE_PDF417 = 74;
    public static final int BARCODE_UPCA = 65;
    public static final int BARCODE_UPCE = 66;
    private static final int CARD_IO_TIME = 11000;
    public static final int FILL_BLACK = 1;
    public static final int FILL_INVERTED = 2;
    public static final int FILL_WHITE = 0;
    public static final int HRI_ABOVE = 1;
    public static final int HRI_BELOW = 2;
    public static final int HRI_BOTH = 3;
    public static final int HRI_NONE = 0;
    private static final int IO_INTERVAL = 5000;
    private static final int IO_TIME = 5000;
    private static final int MAX_BUFFER_SIZE = 2048;
    public static final int PAGE_BOTTOM = 1;
    public static final int PAGE_LEFT = 0;
    public static final int PAGE_RIGHT = 2;
    public static final int PAGE_TOP = 3;
    public static boolean isGeneralPrint = false;
    public int heightImage;
    /* access modifiers changed from: private */
    public InputStream mBaseInputStream;
    /* access modifiers changed from: private */
    public IOException mBaseInputStreamException;
    private OutputStream mBaseOutputStream;
    /* access modifiers changed from: private */
    public byte[] mBuffer;
    /* access modifiers changed from: private */
    public int mBufferDataSize;
    /* access modifiers changed from: private */
    public int mBufferPosition;
    private Settings mSettings;
    public int widthImage;

    private class Settings {
        public int barcodeAlign;
        public int barcodeHeight;
        public int barcodeHriCode;
        public int barcodeHriFont;
        public int barcodeScale;

        private Settings() {
        }
    }

    public Printer(OutputStream out) {
        if (out == null) {
            throw new NullPointerException("The out is null");
        }
        this.mBaseInputStream = null;
        this.mBaseOutputStream = new BufferedOutputStream(out, 2048);
        init();
    }

    public Printer(InputStream in, OutputStream out) {
        if (in == null) {
            throw new NullPointerException("The in is null");
        } else if (out == null) {
            throw new NullPointerException("The out is null");
        } else {
            this.mBaseInputStream = in;
            this.mBaseOutputStream = new BufferedOutputStream(out, 2048);
            this.mBuffer = new byte[2048];
            init();
            new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            int e = Printer.this.mBaseInputStream.read();
                            if (e == -1) {
                                throw new IOException("The end of the stream is reached");
                            }
                            synchronized (Printer.this.mBuffer) {
                                if (Printer.this.mBufferDataSize == 0) {
                                    Printer.this.mBufferPosition = 0;
                                }
                                if (Printer.this.mBufferPosition + Printer.this.mBufferDataSize == Printer.this.mBuffer.length) {
                                    if (Printer.this.mBufferPosition == 0) {
                                        Printer.this.mBufferPosition = Printer.this.mBufferPosition + 1;
                                        Printer.this.mBufferDataSize = Printer.this.mBufferDataSize - 1;
                                    }
                                    System.arraycopy(Printer.this.mBuffer, Printer.this.mBufferPosition, Printer.this.mBuffer, 0, Printer.this.mBufferDataSize);
                                    Printer.this.mBufferPosition = 0;
                                }
                                Printer.this.mBuffer[Printer.this.mBufferPosition + Printer.this.mBufferDataSize] = (byte) e;
                                Printer.this.mBufferDataSize = Printer.this.mBufferDataSize + 1;
                            }
                        } catch (IOException var4) {
                            Printer.this.mBaseInputStreamException = var4;
                            return;
                        }
                    }
                }
            }).start();
        }
    }

    public synchronized void release() {
        this.mBaseInputStreamException = new IOException("The object is released");
        try {
            this.mBaseInputStream.close();
        } catch (IOException e) {
        }
        try {
            this.mBaseOutputStream.close();
        } catch (IOException e2) {
        }
    }

    private void init() {
        this.mSettings = new Settings();
        this.mSettings.barcodeAlign = 0;
        this.mSettings.barcodeScale = 3;
        this.mSettings.barcodeHeight = 162;
        this.mSettings.barcodeHriFont = 0;
        this.mSettings.barcodeHriCode = 0;
    }

    private boolean isDataAvailable() {
        return this.mBufferDataSize > 0;
    }

    private void clearData() {
        while (isDataAvailable()) {
            byte[] bArr = this.mBuffer;
            synchronized (this.mBuffer) {
                this.mBufferPosition = 0;
                this.mBufferDataSize = 0;
            }
        }
    }

    public synchronized void write(int b) throws IOException {
        this.mBaseOutputStream.write(b);
    }

    public synchronized void write(byte[] b) throws IOException {
        this.mBaseOutputStream.write(b);
    }

    public synchronized void write(byte[] b, int offset, int length) throws IOException {
        this.mBaseOutputStream.write(b, offset, length);
    }

    /* JADX WARNING: CFG modification limit reached, blocks count: 135 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int readByte(int r11) throws IOException {
        /*
            r10 = this;
            long r6 = java.lang.System.currentTimeMillis()
            long r8 = (long) r11
            long r2 = r6 + r8
            byte[] r5 = r10.mBuffer
            if (r5 != 0) goto L_0x0018
            java.io.IOException r5 = new java.io.IOException
            java.lang.String r6 = "The input stream is null"
            r5.<init>(r6)
            throw r5
        L_0x0013:
            r6 = 10
            java.lang.Thread.sleep(r6)     // Catch:{ InterruptedException -> 0x002b }
        L_0x0018:
            int r5 = r10.mBufferDataSize
            if (r5 != 0) goto L_0x0030
            long r6 = java.lang.System.currentTimeMillis()
            int r5 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1))
            if (r5 <= 0) goto L_0x0030
            java.io.IOException r5 = r10.mBaseInputStreamException
            if (r5 == 0) goto L_0x0013
            java.io.IOException r5 = r10.mBaseInputStreamException
            throw r5
        L_0x002b:
            r4 = move-exception
            r4.printStackTrace()
            goto L_0x0018
        L_0x0030:
            byte[] r1 = r10.mBuffer
            byte[] r6 = r10.mBuffer
            monitor-enter(r6)
            int r5 = r10.mBufferDataSize     // Catch:{ all -> 0x0041 }
            if (r5 != 0) goto L_0x0044
            java.io.IOException r5 = new java.io.IOException     // Catch:{ all -> 0x0041 }
            java.lang.String r7 = "Receive Data Timeout"
            r5.<init>(r7)     // Catch:{ all -> 0x0041 }
            throw r5     // Catch:{ all -> 0x0041 }
        L_0x0041:
            r5 = move-exception
            monitor-exit(r6)     // Catch:{ all -> 0x0041 }
            throw r5
        L_0x0044:
            byte[] r5 = r10.mBuffer     // Catch:{ all -> 0x0041 }
            int r7 = r10.mBufferPosition     // Catch:{ all -> 0x0041 }
            byte r5 = r5[r7]     // Catch:{ all -> 0x0041 }
            r0 = r5 & 255(0xff, float:3.57E-43)
            int r5 = r10.mBufferPosition     // Catch:{ all -> 0x0041 }
            int r5 = r5 + 1
            r10.mBufferPosition = r5     // Catch:{ all -> 0x0041 }
            int r5 = r10.mBufferDataSize     // Catch:{ all -> 0x0041 }
            int r5 = r5 + -1
            r10.mBufferDataSize = r5     // Catch:{ all -> 0x0041 }
            monitor-exit(r6)     // Catch:{ all -> 0x0041 }
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.fastapp.webappsdk.Ayuda.Printer.readByte(int):int");
    }

    public int read() throws IOException {
        int readByte;
        if (this.mBuffer == null) {
            throw new IOException("The input stream is null");
        }
        byte[] bArr = this.mBuffer;
        synchronized (this.mBuffer) {
            readByte = !isDataAvailable() ? -1 : readByte(0);
        }
        return readByte;
    }

    private void flushStream() throws IOException {
        this.mBaseOutputStream.flush();
    }

    public synchronized void flush() throws IOException {
        this.mBaseOutputStream.write(new byte[1024]);
        this.mBaseOutputStream.flush();
    }

    public int getVoltage() throws IOException {
        byte[] buf;
        synchronized (this) {
            clearData();
            buf = new byte[]{27, 96};
            write(buf);
            flushStream();
            buf[0] = (byte) readByte(5000);
            buf[1] = (byte) readByte(5000);
        }
        return ((buf[0] & 255) - 32) * 100;
    }

    public int getTemperature() throws IOException {
        byte[] buf;
        synchronized (this) {
            clearData();
            buf = new byte[]{27, 96};
            write(buf);
            flushStream();
            buf[0] = (byte) readByte(5000);
            buf[1] = (byte) readByte(5000);
        }
        return (buf[1] & 255) - 32;
    }

    public int getStatus() throws IOException {
        byte[] buf;
        synchronized (this) {
            clearData();
            buf = new byte[]{27, 118};
            write(buf);
            flushStream();
            buf[0] = (byte) readByte(5000);
        }
        return buf[0] & 255;
    }

    public String[] readCard(boolean first, boolean second, boolean third, int wait) throws IOException {
        int var13;
        String[] sbuf;
        if (!first && !second && !third) {
            throw new IllegalArgumentException("No track selected");
        } else if (wait < 0) {
            throw new IllegalArgumentException("The wait is negative");
        } else {
            int tracks = 0;
            if (first) {
                tracks = 0 | 1;
            }
            if (second) {
                tracks |= 2;
            }
            if (third) {
                tracks |= 4;
            }
            byte[] buf = {27, 63, (byte) tracks};
            synchronized (this) {
                clearData();
                int repeat = wait / CARD_IO_TIME;
                do {
                    write(buf);
                    flushStream();
                    var13 = readByte(CARD_IO_TIME);
                    repeat--;
                    if (var13 != 0) {
                        break;
                    }
                } while (repeat > 0);
                byte index = -1;
                sbuf = new String[3];
                while (var13 != 0) {
                    switch (var13) {
                        case 241:
                            index = 0;
                            sbuf[0] = "";
                            break;
                        case 242:
                            index = 1;
                            sbuf[1] = "";
                            break;
                        case 243:
                            index = 2;
                            sbuf[2] = "";
                            break;
                        default:
                            if (index < 0) {
                                break;
                            } else {
                                sbuf[index] = sbuf[index] + ((char) (var13 & 255));
                                break;
                            }
                    }
                    var13 = readByte(5000);
                }
            }
            return sbuf;
        }
    }

    public String readBarcode(int wait) throws IOException {
        byte[] buf = {27, 66, (byte) (wait + 32), 27, 66, 32};
        StringBuffer sb = new StringBuffer();
        int status = 0;
        boolean ff = false;
        if (wait < 0) {
            throw new IllegalArgumentException("The wait is negative");
        }
        synchronized (this) {
            clearData();
            if (wait == 0) {
                write(buf, 0, 3);
            } else {
                write(buf);
            }
            flushStream();
            try {
                status = readByte((wait * 1000) + 500);
            } catch (IOException e) {
            }
            while (status != 0) {
                if (status != 255) {
                    if (ff) {
                        status -= 32;
                        ff = false;
                    }
                    sb.append((char) status);
                } else if (ff) {
                    sb.append(255);
                    sb.append(255);
                    ff = false;
                } else {
                    ff = true;
                }
                status = read();
                if (status < 0) {
                    break;
                }
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    public synchronized void reset() throws IOException {
        byte[] tbuf = new byte[6];
        int var32 = 0 + 1;
        tbuf[0] = 27;
        int var322 = var32 + 1;
        tbuf[var32] = 33;
        int var323 = var322 + 1;
        tbuf[var322] = 0;
        int var324 = var323 + 1;
        tbuf[var323] = 27;
        int i = var324 + 1;
        tbuf[var324] = 73;
        synchronized (this) {
            write(tbuf);
        }
    }

    public void turnOff() throws IOException {
        byte[] buf = {27, 43};
        synchronized (this) {
            write(buf);
        }
    }

    public void printSelfTest() throws IOException {
        byte[] buf = {27, 46};
        synchronized (this) {
            write(buf);
        }
    }

    public void selectCodetable(int codetable) throws IOException {
        byte[] buf = {27, 117, (byte) codetable};
        if (codetable < 0) {
            throw new IllegalArgumentException("The codetable is negative");
        }
        synchronized (this) {
            write(buf);
        }
    }

    public void setLineSpace(int lines) throws IOException {
        if (lines < 0 || lines > 255) {
            throw new IllegalArgumentException("The lines is out of range");
        }
        byte[] buf = {27, 51, (byte) (lines & 255)};
        synchronized (this) {
            write(buf);
        }
    }

    public void setAlign(int align) throws IOException {
        switch (align) {
            case 0:
            case 1:
            case 2:
                byte[] buf = {27, 97, (byte) align};
                synchronized (this) {
                    write(buf);
                }
                return;
            default:
                throw new IllegalArgumentException("The align is illegal");
        }
    }

    public synchronized void feedPaper(int lines) throws IOException {
        if (lines < 0 || lines > 255) {
            throw new IllegalArgumentException("The lines is out of range");
        }
        byte[] buf = {27, 74, (byte) lines};
        synchronized (this) {
            write(buf);
        }
    }

    public void printText(String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("The s is null");
        }
        synchronized (this) {
            write(s.getBytes());
        }
    }

    public void printText(String s, String encoding) throws IOException {
        if (s == null) {
            throw new NullPointerException("The s is null");
        }
        synchronized (this) {
            write(s.getBytes(encoding));
        }
    }

    private synchronized void printTaggedText(byte[] b) throws IOException {
        boolean set;
        int index;
        int var32 = 0;
        byte mode;
        byte mode2;
        byte mode3;
        byte mode4;
        int BREAK = "br".hashCode();
        int SMALL = "s".hashCode();
        int BOLD = "b".hashCode();
        int HIGH = "h".hashCode();
        int WIDE = "w".hashCode();
        int UNDERLINE = "u".hashCode();
        int ITALIC = "i".hashCode();
        int RESET = "reset".hashCode();
        int LEFT = "left".hashCode();
        int CENTER = "center".hashCode();
        int RIGHT = "right".hashCode();
        if (b == null) {
            throw new NullPointerException("The b is null");
        }
        int len = b.length;
        byte[] tbuf = new byte[(len + 6)];
        byte mode5 = 0;
        int pos = 0;
        int var322 = 0 + 1;
        if (!isGeneralPrint) {
            tbuf[0] = 27;
            int var323 = var322 + 1;
            tbuf[var322] = 33;
            int var324 = var323 + 1;
            tbuf[var323] = 0;
            int var325 = var324 + 1;
            tbuf[var324] = 27;
            int var326 = var325 + 1;
            tbuf[var325] = 73;
            int var327 = var326 + 1;
            tbuf[var326] = 0;
            var322 = var327;
        }
        int i = 0;
        int var328 = var322;
        while (i < len) {
            byte value = b[i];
            int var329 = var328 + 1;
            tbuf[var328] = value;
            if (value == 123) {
                pos = var329;
            } else if (value == 125 && pos >= 1 && (var329 - 1) - 6 <= pos) {
                if (tbuf[pos] == 47) {
                    set = false;
                    index = pos + 1;
                } else {
                    set = true;
                    index = pos;
                }
                int hash = 0;
                int hashlen = (var329 - 1) - index;
                for (int j = 0; j < hashlen; j++) {
                    int c = tbuf[index + j] & 255;
                    if (c >= 65 && c <= 90) {
                        c += 32;
                    }
                    hash = (hash * 31) + c;
                }
                if (hash == BREAK) {
                    int var3210 = pos - 1;
                    int var3211 = var3210 + 1;
                    tbuf[var3210] = 10;
                    var329 = var3211;
                } else if (hash == SMALL) {
                    if (set) {
                        mode5 = (byte) (mode5 | 1);
                    } else {
                        mode5 = (byte) (mode5 & -2);
                    }
                    int var3212 = pos - 1;
                    int var3213 = var3212 + 1;
                    tbuf[var3212] = 27;
                    int var3214 = var3213 + 1;
                    tbuf[var3213] = 33;
                    int var3215 = var3214 + 1;
                    tbuf[var3214] = mode5;
                    var329 = var3215;
                } else if (hash == BOLD) {
                    if (set) {
                        mode4 = (byte) (mode5 | 8);
                    } else {
                        mode4 = (byte) (mode5 & -9);
                    }
                    int var3216 = pos - 1;
                    int var3217 = var3216 + 1;
                    tbuf[var3216] = 27;
                    int var3218 = var3217 + 1;
                    tbuf[var3217] = 33;
                    int var3219 = var3218 + 1;
                    tbuf[var3218] = mode5;
                    var329 = var3219;
                } else if (hash == HIGH) {
                    if (set) {
                        mode3 = (byte) (mode5 | 16);
                    } else {
                        mode3 = (byte) (mode5 & -17);
                    }
                    int var3220 = pos - 1;
                    int var3221 = var3220 + 1;
                    tbuf[var3220] = 27;
                    int var3222 = var3221 + 1;
                    tbuf[var3221] = 33;
                    int var3223 = var3222 + 1;
                    tbuf[var3222] = mode5;
                    var329 = var3223;
                } else if (hash == WIDE) {
                    if (set) {
                        mode2 = (byte) (mode5 | 32);
                    } else {
                        mode2 = (byte) (mode5 & -33);
                    }
                    int var3224 = pos - 1;
                    int var3225 = var3224 + 1;
                    tbuf[var3224] = 27;
                    int var3226 = var3225 + 1;
                    tbuf[var3225] = 33;
                    int var3227 = var3226 + 1;
                    tbuf[var3226] = mode5;
                    var329 = var3227;
                } else if (hash == UNDERLINE) {
                    if (set) {
                        mode = (byte) (mode5 | 128);
                    } else {
                        mode = (byte) (mode5 & -129);
                    }
                    int var3228 = pos - 1;
                    int var3229 = var3228 + 1;
                    tbuf[var3228] = 27;
                    int var3230 = var3229 + 1;
                    tbuf[var3229] = 33;
                    int var3231 = var3230 + 1;
                    tbuf[var3230] = mode5;
                    var329 = var3231;
                } else if (hash == ITALIC) {
                    int var3232 = pos - 1;
                    int var3233 = var3232 + 1;
                    tbuf[var3232] = 27;
                    int var3234 = var3233 + 1;
                    tbuf[var3233] = 73;
                    int var3235 = var3234 + 1;
                    tbuf[var3234] = (byte) (set ? 1 : 0);
                    var329 = var3235;
                } else {
                    if (hash == RESET) {
                        mode5 = 0;
                        int var3236 = pos - 1;
                        int var3237 = var3236 + 1;
                        tbuf[var3236] = 27;
                        int var3238 = var3237 + 1;
                        tbuf[var3237] = 33;
                        int var3239 = var3238 + 1;
                        tbuf[var3238] = 0;
                        int var3240 = var3239 + 1;
                        tbuf[var3239] = 27;
                        var32 = var3240 + 1;
                        tbuf[var3240] = 73;
                        if (!isGeneralPrint) {
                            int var3241 = var32 + 1;
                            tbuf[var32] = 0;
                            var32 = var3241;
                        }
                        if (isGeneralPrint) {
                            int var3242 = var32 + 1;
                            tbuf[var32] = 27;
                            int var3243 = var3242 + 1;
                            tbuf[var3242] = 97;
                            var329 = var3243 + 1;
                            tbuf[var3243] = 0;
                        }
                    } else if (hash == LEFT) {
                        int var3244 = pos - 1;
                        int var3245 = var3244 + 1;
                        tbuf[var3244] = 27;
                        int var3246 = var3245 + 1;
                        tbuf[var3245] = 97;
                        int var3247 = var3246 + 1;
                        tbuf[var3246] = 0;
                        var329 = var3247;
                    } else if (hash == CENTER) {
                        int var3248 = pos - 1;
                        int var3249 = var3248 + 1;
                        tbuf[var3248] = 27;
                        int var3250 = var3249 + 1;
                        tbuf[var3249] = 97;
                        int var3251 = var3250 + 1;
                        tbuf[var3250] = 1;
                        var329 = var3251;
                    } else if (hash == RIGHT) {
                        int var3252 = pos - 1;
                        int var3253 = var3252 + 1;
                        tbuf[var3252] = 27;
                        int var3254 = var3253 + 1;
                        tbuf[var3253] = 97;
                        var32 = var3254 + 1;
                        tbuf[var3254] = 2;
                    }
                    var329 = var32;
                }
            }
            i++;
            var328 = var329;
        }
        synchronized (this) {
            write(tbuf, 0, var328);
        }
    }

    public void printTaggedText(String s) throws IOException {
        if (s == null) {
            throw new NullPointerException("The s is null");
        }
        printTaggedText(s.getBytes());
    }

    public synchronized void printTaggedText(String s, String encoding) throws IOException {
        if (s == null) {
            throw new NullPointerException("The s is null");
        }
        synchronized (this) {
            printTaggedText(s.getBytes(encoding));
        }
    }

    private static void convertARGBToGrayscale(int[] argb, int width, int height) {
        int pixels = width * height;
        for (int i = 0; i < pixels; i++) {
            argb[i] = ((((((argb[i] >> 16) & 255) * 19) + (((argb[i] >> 8) & 255) * 38)) + ((argb[i] & 255) * 7)) >> 6) & 255;
        }
    }

    private static void ditherImageByFloydSteinberg(int[] grayscale, int width, int height) {
        int error;
        int stopXM1 = width - 1;
        int stopYM1 = height - 1;
        int[] coef = {3, 5, 1};
        int offs = 0;
        for (int y = 0; y < height; y++) {
            int x = 0;
            while (x < width) {
                int v = grayscale[offs];
                if (v < 128) {
                    grayscale[offs] = 0;
                    error = v;
                } else {
                    grayscale[offs] = 255;
                    error = v - 255;
                }
                if (x != stopXM1) {
                    int ed = grayscale[offs + 1] + ((error * 7) / 16);
                    if (ed < 0) {
                        ed = 0;
                    } else if (ed > 255) {
                        ed = 255;
                    }
                    grayscale[offs + 1] = ed;
                }
                if (y != stopYM1) {
                    int i = -1;
                    int j = 0;
                    while (i <= 1) {
                        if (x + i >= 0 && x + i < width) {
                            int ed2 = grayscale[offs + width + i] + ((coef[j] * error) / 16);
                            if (ed2 < 0) {
                                ed2 = 0;
                            } else if (ed2 > 255) {
                                ed2 = 255;
                            }
                            grayscale[offs + width + i] = ed2;
                        }
                        i++;
                        j++;
                    }
                }
                x++;
                offs++;
            }
        }
    }

    public int[] convertImageToArgbArray(Resources res, int resourceid) {
        new Options().inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resourceid);
        this.widthImage = bitmap.getWidth();
        this.heightImage = bitmap.getHeight();
        int[] argb = new int[(this.widthImage * this.heightImage)];
        bitmap.getPixels(argb, 0, this.widthImage, 0, 0, this.widthImage, this.heightImage);
        bitmap.recycle();
        return argb;
    }

    public int[] convertImageToArgbArray(Resources res, String path) {
        new Options().inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        this.widthImage = bitmap.getWidth();
        this.heightImage = bitmap.getHeight();
        int[] argb = new int[(this.widthImage * this.heightImage)];
        bitmap.getPixels(argb, 0, this.widthImage, 0, 0, this.widthImage, this.heightImage);
        bitmap.recycle();
        return argb;
    }

    public int[] convertImageResizeToArgbArray(Resources res, String path, int scale) {
        new Options().inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth() / scale, bitmap.getHeight() / scale, true);
        this.widthImage = bitmap2.getWidth();
        this.heightImage = bitmap2.getHeight();
        int[] argb = new int[(this.widthImage * this.heightImage)];
        bitmap2.getPixels(argb, 0, this.widthImage, 0, 0, this.widthImage, this.heightImage);
        bitmap2.recycle();
        return argb;
    }

    public void printImage(int[] argb, int width, int height, int align, boolean dither) throws IOException {
        if (argb == null) {
            throw new NullPointerException("The argb is null");
        } else if (align < 0 || align > 2) {
            throw new IllegalArgumentException("The align is illegal");
        } else if (width < 1 || height < 1) {
            throw new IllegalArgumentException("The size of image is illegal");
        } else {
            convertARGBToGrayscale(argb, width, height);
            if (dither) {
                ditherImageByFloydSteinberg(argb, width, height);
            }
            byte[] var13 = new byte[((width * 3) + 9)];
            synchronized (this) {
                int var15 = 0 + 1;
                var13[0] = 27;
                int var152 = var15 + 1;
                try {
                    var13[var15] = 51;
                    int var153 = var152 + 1;
                    var13[var152] = 24;
                    write(var13, 0, var153);
                    int var154 = 0 + 1;
                    var13[0] = 27;
                    var152 = var154 + 1;
                    var13[var154] = 97;
                    int var155 = var152 + 1;
                    var13[var152] = (byte) align;
                    var152 = var155 + 1;
                    var13[var155] = 27;
                    int var156 = var152 + 1;
                    var13[var152] = 42;
                    var152 = var156 + 1;
                    var13[var156] = 33;
                    int var157 = var152 + 1;
                    var13[var152] = (byte) (width % 256);
                    var152 = var157 + 1;
                    var13[var157] = (byte) (width / 256);
                    var13[var13.length - 1] = 10;
                    int offs = 0;
                    for (int j = 0; j < height; j++) {
                        if (j > 0 && j % 24 == 0) {
                            write(var13);
                            for (int i = var152; i < var13.length - 1; i++) {
                                var13[i] = 0;
                            }
                        }
                        int i2 = 0;
                        while (i2 < width) {
                            int i3 = ((j % 24) / 8) + (i2 * 3) + 8;
                            var13[i3] = (byte) (((byte) ((argb[offs] < 128 ? 1 : 0) << (7 - (j % 8)))) | var13[i3]);
                            i2++;
                            offs++;
                        }
                    }
                    write(var13);
                } catch (Throwable th) {
                    th.printStackTrace();
                }
            }
        }
    }

    private static int compressRLE(byte[] src, int srcOffs, byte[] dst, int dstOffs, int length) {
        int offset = 0;
        int dstLen = 0;
        while (offset < length) {
            int count = 1;
            int currValue = src[srcOffs + offset] & 255;
            int i = offset + 1;
            while (i < length && count < 64 && currValue == (src[srcOffs + i] & 255)) {
                count++;
                i++;
            }
            if (count > 1) {
                dst[dstOffs + dstLen] = (byte) (count | 192);
                dstLen++;
                dst[dstOffs + dstLen] = (byte) currValue;
            } else if ((currValue & 192) == 192) {
                dst[dstOffs + dstLen] = -63;
                dstLen++;
                dst[dstOffs + dstLen] = (byte) currValue;
            } else {
                dst[dstOffs + dstLen] = (byte) currValue;
            }
            dstLen++;
            offset += count;
        }
        return dstLen;
    }

    public void printCompressedImage(int[] argb, int width, int height, int align, boolean dither) throws IOException {
        if (argb == null) {
            throw new NullPointerException("The argb is null");
        } else if (align < 0 || align > 2) {
            throw new IllegalArgumentException("The align is illegal");
        } else if (width < 1 || height < 1) {
            throw new IllegalArgumentException("The size of image is illegal");
        } else {
            convertARGBToGrayscale(argb, width, height);
            if (dither) {
                ditherImageByFloydSteinberg(argb, width, height);
            }
            synchronized (this) {
                int horzSizeInBytes = (width + 7) / 8;
                byte[] buffer = new byte[((horzSizeInBytes * 24) + 7)];
                byte[] packed = new byte[(horzSizeInBytes * 24 * 2)];
                int var17 = 0 + 1;
                buffer[0] = 27;
                int var172 = var17 + 1;
                buffer[var17] = 51;
                int var173 = var172 + 1;
                buffer[var172] = 24;
                write(buffer, 0, var173);
                int var174 = 0 + 1;
                buffer[0] = 27;
                int var175 = var174 + 1;
                buffer[var174] = 97;
                int var176 = var175 + 1;
                buffer[var175] = (byte) align;
                int var177 = var176 + 1;
                buffer[var176] = 27;
                int var178 = var177 + 1;
                buffer[var177] = 42;
                int var179 = var178 + 1;
                buffer[var178] = 17;
                int var1710 = var179 + 1;
                buffer[var179] = (byte) horzSizeInBytes;
                int offset = 0;
                for (int length = 0; length < height; length++) {
                    if (length > 0 && length % 24 == 0) {
                        int i = compressRLE(buffer, var1710, packed, 0, buffer.length - 7);
                        write(buffer, 0, var1710);
                        write(packed, 0, i);
                        write(10);
                        for (int i1 = var1710; i1 < buffer.length - 1; i1++) {
                            buffer[i1] = 0;
                        }
                    }
                    int i2 = 0;
                    while (i2 < width) {
                        int i3 = ((length % 24) * horzSizeInBytes) + (i2 / 8) + 7;
                        buffer[i3] = (byte) (((byte) ((argb[offset] < 128 ? 1 : 0) << (i2 % 8))) | buffer[i3]);
                        i2++;
                        offset++;
                    }
                }
                int length2 = compressRLE(buffer, var1710, packed, 0, buffer.length - 7);
                write(buffer, 0, var1710);
                write(packed, 0, length2);
                write(10);
            }
        }
    }

    public void printLogo(boolean wide, boolean high) throws IOException {
        byte mode = 0;
        if (wide) {
            mode = (byte) 1;
        }
        if (high) {
            mode = (byte) (mode + 2);
        }
        byte[] buf = {29, 47, mode};
        synchronized (this) {
            write(buf);
        }
    }

    public void printLogo() throws IOException {
        byte[] buf = {29, 47, 0};
        synchronized (this) {
            write(buf);
        }
    }

    public void setBarcode(int align, boolean small, int scale, int hri, int height) {
        int i = 1;
        if (align < 0) {
            throw new IllegalArgumentException("The align is illegal");
        } else if (scale < 2 || scale > 4) {
            throw new IllegalArgumentException("The scale is illegal");
        } else if (hri < 0) {
            throw new IllegalArgumentException("The hri is negative");
        } else if (height < 1 || height > 255) {
            throw new IllegalArgumentException("The height is illegal");
        } else {
            this.mSettings.barcodeAlign = align;
            Settings settings = this.mSettings;
            if (!small) {
                i = 0;
            }
            settings.barcodeHriFont = i;
            this.mSettings.barcodeScale = scale;
            this.mSettings.barcodeHriCode = hri;
            this.mSettings.barcodeHeight = height;
        }
    }


    public void printBarcode(int type, byte[] data) throws IOException {
        if (data == null) {
            throw new NullPointerException("The data is null.");
        } else {
            byte[] buf = new byte[21 + data.length];
            int offs = 0;
            offs = offs + 1;
            buf[offs] = 27;
            buf[offs++] = 97;
            buf[offs++] = (byte)this.mSettings.barcodeAlign;
            buf[offs++] = 29;
            buf[offs++] = 119;
            buf[offs++] = (byte)this.mSettings.barcodeScale;
            buf[offs++] = 29;
            buf[offs++] = 104;
            buf[offs++] = (byte)this.mSettings.barcodeHeight;
            buf[offs++] = 29;
            buf[offs++] = 72;
            buf[offs++] = (byte)this.mSettings.barcodeHriCode;
            buf[offs++] = 29;
            buf[offs++] = 102;
            buf[offs++] = (byte)this.mSettings.barcodeHriFont;
            switch(type) {
                case 65:
                    if (data.length != 11) {
                        throw new IllegalArgumentException("The length of UPCA barcode data must be 11 symbols");
                    }
                    break;
                case 66:
                    if (data.length != 11) {
                        throw new IllegalArgumentException("The length of UPCE barcode data must be 11 symbols");
                    }
                    break;
                case 67:
                    if (data.length != 12) {
                        throw new IllegalArgumentException("The length of EAN13 barcode data must be 12 symbols");
                    }
                    break;
                case 68:
                    if (data.length != 7) {
                        throw new IllegalArgumentException("The length of EAN8 barcode data must be 7 symbols");
                    }
                    break;
                case 69:
                case 70:
                case 71:
                case 72:
                case 73:
                case 75:
                case 76:
                    if (data.length < 1 || data.length > 255) {
                        throw new IllegalArgumentException("The length of barcode data must be between 1 and 255 symbols");
                    }
                    break;
                case 74:
                    if (data.length >= 1 && data.length <= 1000) {
                        break;
                    }

                    throw new IllegalArgumentException("The length of PDF417 barcode data must be between 1 and 1000 symbols");
                default:
                    throw new IllegalArgumentException("Invalid barcode type");
            }

            buf[offs++] = 29;
            buf[offs++] = 107;
            buf[offs++] = (byte)type;
            if (type == 73 && data[0] != 123) {
                buf[offs++] = (byte)(data.length + 2);
                buf[offs++] = 123;
                buf[offs++] = 66;
            } else if (type == 74) {
                buf[offs++] = 0;
                buf[offs++] = (byte)(data.length & 255);
                buf[offs++] = (byte)(data.length >> 8 & 255);
            } else {
                buf[offs++] = (byte)data.length;
            }

            for(int i = 0; i < data.length; ++i) {
                buf[offs++] = data[i];
            }

            synchronized(this) {
                this.write(buf, 0, offs);
            }
        }
    }

    public void printBarcode(int type, String data) throws IOException {
        if (data == null) {
            throw new NullPointerException("The data is null");
        } else {
            this.printBarcode(type, data.getBytes());
        }
    }

    public void printQRCode(int size, int eccLv, byte[] data) throws IOException {
        byte[] buf = new byte[(data.length + 10)];
        int var7 = 0 + 1;
        buf[0] = 27;
        int var72 = var7 + 1;
        buf[var7] = 97;
        int var73 = var72 + 1;
        buf[var72] = (byte) this.mSettings.barcodeAlign;
        int var74 = var73 + 1;
        buf[var73] = 29;
        int var75 = var74 + 1;
        buf[var74] = 81;
        int var76 = var75 + 1;
        buf[var75] = 6;
        int var77 = var76 + 1;
        buf[var76] = (byte) size;
        int var78 = var77 + 1;
        buf[var77] = (byte) eccLv;
        int var79 = var78 + 1;
        buf[var78] = (byte) data.length;
        int var710 = var79 + 1;
        buf[var79] = (byte) (data.length >> 8);
        int i = 0;
        while (true) {
            int var711 = var710;
            if (i < data.length) {
                var710 = var711 + 1;
                buf[var711] = data[i];
                i++;
            } else {
                write(buf);
                return;
            }
        }
    }

    public void printQRCode(int size, int eccLv, String data) throws IOException {
        if (data == null) {
            throw new NullPointerException("The data is null");
        }
        printQRCode(size, eccLv, data.getBytes());
    }

    public void calibrateBMMSensor() throws IOException {
        byte[] buf = {27, 67, 65, 76, 3};
        synchronized (this) {
            write(buf);
        }
    }

    public void feedLabel() throws IOException {
        byte[] buf = {12};
        synchronized (this) {
            write(buf);
        }
    }

    public void selectPageMode() throws IOException {
        byte[] buf = {27, 76, 24};
        synchronized (this) {
            write(buf);
        }
    }

    public void selectStandardMode() throws IOException {
        byte[] buf = {29, 85};
        synchronized (this) {
            write(buf);
        }
    }

    public void printPage() throws IOException {
        byte[] buf = {29, 90};
        synchronized (this) {
            write(buf);
        }
    }

    public void setPageRegion(int x, int y, int width, int height, int direction) throws IOException {
        if (x < 0 || y < 0 || width < 0 || height < 0 || direction < 0 || direction > 3) {
            throw new IllegalArgumentException("The illegal parameter");
        }
        byte[] buf = {27, 87, (byte) (x & 255), (byte) ((x >> 8) & 255), (byte) (y & 255), (byte) ((y >> 8) & 255), (byte) (width & 255), (byte) ((width >> 8) & 255), (byte) (height & 255), (byte) ((height >> 8) & 255), 29, 84, (byte) (direction & 255)};
        synchronized (this) {
            write(buf);
        }
    }

    public void setPageXY(int x, int y) throws IOException {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("The illegal parameter");
        }
        byte[] buf = {27, 36, (byte) (x & 255), (byte) ((x >> 8) & 255), 29, 36, (byte) (y & 255), (byte) ((y >> 8) & 255)};
        synchronized (this) {
            write(buf);
        }
    }

    public void drawPageRectangle(int x, int y, int width, int height, int fillMode) throws IOException {
        if (x < 0 || y < 0 || width < 0 || height < 0 || fillMode < 0 || fillMode > 2) {
            throw new IllegalArgumentException("The illegal parameter");
        }
        byte[] buf = {29, 82, (byte) (x & 255), (byte) ((x >> 8) & 255), (byte) (y & 255), (byte) ((y >> 8) & 255), (byte) (width & 255), (byte) ((width >> 8) & 255), (byte) (height & 255), (byte) ((height >> 8) & 255), (byte) (fillMode & 255)};
        synchronized (this) {
            write(buf);
        }
    }

    public void drawPageFrame(int x, int y, int width, int height, int fillMode, int thickness) throws IOException {
        if (x < 0 || y < 0 || width < 0 || height < 0 || fillMode < 0 || fillMode > 2 || thickness < 0) {
            throw new IllegalArgumentException("The illegal parameter");
        }
        byte[] buf = {29, 88, (byte) (x & 255), (byte) ((x >> 8) & 255), (byte) (y & 255), (byte) ((y >> 8) & 255), (byte) (width & 255), (byte) ((width >> 8) & 255), (byte) (height & 255), (byte) ((height >> 8) & 255), (byte) (fillMode & 255), (byte) (thickness & 255)};
        synchronized (this) {
            write(buf);
        }
    }
}
