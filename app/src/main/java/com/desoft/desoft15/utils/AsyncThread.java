package com.desoft.desoft15.utils;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

public abstract class AsyncThread<Params, Progress, Result> {
    /* access modifiers changed from: private */
    public Handler handler = new Handler(new Callback() {
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    if (isCancel) {
                        onCancel();
                    } else {
                        stop();
                        onPostExecute((Result) message.obj);
                    }
                    return true;
                case 2:
                    if (!AsyncThread.this.isCancel) {
                        AsyncThread.this.onProgressUpdate((Progress) message.obj);
                    }
                    return true;
                default:
                    return false;
            }
        }
    });
    /* access modifiers changed from: private */
    public boolean isCancel = false;
    private Thread thread = null;

    /* access modifiers changed from: protected */
    public abstract Result doInBackground(Params... paramsArr);

    /* access modifiers changed from: protected */
    public void onPreExecute() {
    }

    /* access modifiers changed from: protected */
    public void onProgressUpdate(Progress... progressArr) {
    }

    /* access modifiers changed from: protected */
    public void onPostExecute(Result result) {
    }

    /* access modifiers changed from: protected */
    public void onCancel() {
    }

    /* access modifiers changed from: protected */
    @SafeVarargs
    public final void publishProgress(Progress... values) {
        Message message = new Message();
        message.obj = values;
        message.what = 2;
        this.handler.sendMessage(message);
    }

    @SafeVarargs
    public final void execute(final Params... p) {
        onPreExecute();
        this.isCancel = false;
        this.thread = new Thread(new Runnable() {
            public void run() {
                Message message = new Message();
                message.obj = AsyncThread.this.doInBackground(p);
                message.what = 1;
                AsyncThread.this.handler.sendMessage(message);
            }
        }, "Procesando");
        this.thread.start();
    }

    public final void cancel() {
        if (this.thread != null && this.thread.isAlive()) {
            Message message = new Message();
            message.what = 1;
            this.isCancel = true;
            this.handler.sendMessage(message);
            stop();
        }
    }

    /* access modifiers changed from: private */
    public void stop() {
        if (this.thread != null) {
            Thread thre = this.thread;
            this.thread = null;
            thre.interrupt();
        }
    }

    public boolean isBusy() {
        return this.thread != null && this.thread.isAlive();
    }
}
