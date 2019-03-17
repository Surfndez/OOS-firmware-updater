package fr.witchdoctors.c4ffein.oosfirmwareextractor;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AsyncCook extends AsyncTask<Void, Integer, Integer> {
    private WeakReference<MainActivity> callerActivityWR;
    private File sourceDirectory;
    private String zipFilePath;

    AsyncCook(Activity callerActivity, File sourceDirectory, String zipFilePath) {
        this.callerActivityWR = new WeakReference<>((MainActivity) callerActivity);
        this.sourceDirectory = sourceDirectory;
        this.zipFilePath = zipFilePath;
    }

    // Adapted from https://stackoverflow.com/a/14868161
    @Override
    protected Integer doInBackground(Void... voids) {
        try {
            FileOutputStream dest = new FileOutputStream(zipFilePath);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            if (sourceDirectory.isDirectory())
                zipSubFolder(out, sourceDirectory, sourceDirectory.getPath().length() + 1);
            else
                Log.d("Zipping", "Source File is not a directory");
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
        return 0;
    }

    // From https://stackoverflow.com/a/14868161
    private void zipSubFolder(ZipOutputStream out, File folder, int basePathLength) throws IOException {
        final int BUFFER = 2048;
        File[] fileList = folder.listFiles();
        BufferedInputStream origin;
        for (File file : fileList) {
            if (file.isDirectory()) {
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength);
                ZipEntry entry = new ZipEntry(relativePath + "/");
                entry.setTime(file.lastModified()); // To keep modification time after unzipping
                out.putNextEntry(entry);
                zipSubFolder(out, file, basePathLength);
            } else {
                byte data[] = new byte[BUFFER];
                String unmodifiedFilePath = file.getPath();
                String relativePath = unmodifiedFilePath.substring(basePathLength);
                FileInputStream fi = new FileInputStream(unmodifiedFilePath);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(relativePath);
                entry.setTime(file.lastModified()); // To keep modification time after unzipping
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
        }
    }

    @Override
    protected void onPreExecute() {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null)
            callerActivity.freeze();
    }

    @Override
    protected void onPostExecute(Integer result) {
        MainActivity callerActivity = callerActivityWR.get();
        if (callerActivity != null) {
            callerActivity.setTextCooked();
            callerActivity.setAllEnabled();
            callerActivity.stopWheel();
        }
    }
}