package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AsyncMD5 extends AsyncTask<Void, Integer, String> {
    private WeakReference<MainActivity> callerActivityWR;
    private Uri fileUri;

    AsyncMD5(Activity callerActivity, Uri fileUri) {
        this.callerActivityWR = new WeakReference<>((MainActivity) callerActivity);
        this.fileUri = fileUri;
    }

    @Override
    // From https://stackoverflow.com/questions/13152736/how-to-generate-an-md5-checksum-for-a-file-in-android/14922433#14922433
    protected String doInBackground(Void... voids) {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null) {
            try (InputStream inputStream = callerActivity.getApplicationContext().getContentResolver().openInputStream(fileUri)) {
                if (inputStream == null)
                    return null;
                MessageDigest digest;
                try {
                    digest = MessageDigest.getInstance("MD5");
                } catch (NoSuchAlgorithmException e) {
                    return "No MD5 MessageDigest instance available";
                }

                byte[] buffer = new byte[8192];
                int read;
                try {
                    while ((read = inputStream.read(buffer)) > 0) {
                        digest.update(buffer, 0, read);
                    }
                    byte[] md5sum = digest.digest();
                    BigInteger bigInt = new BigInteger(1, md5sum);
                    String output = bigInt.toString(16);
                    output = String.format("%32s", output).replace(' ', '0');
                    return output;
                } catch (IOException e) {
                    throw new RuntimeException("Unable to process file for MD5", e);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null)
            callerActivity.freeze();
    }

    @Override
    protected void onPostExecute(String s) {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null) {
            if (s != null) {
                callerActivity.setMD5(s);
                callerActivity.setExtractAndFileSearchEnabled();
            } else
                callerActivity.setFileSearchEnabled();
            callerActivity.stopWheel();
        }
    }
}