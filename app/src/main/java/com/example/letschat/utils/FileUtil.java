package com.example.letschat.utils;


import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import com.devlomi.circularstatusview.BuildConfig;
import com.example.letschat.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtil {

    public static String getFileExtension(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    public static String getFileName(Context context, Uri uri) {
        String fileName = null;
        String scheme = uri.getScheme();
        if (scheme != null && scheme.equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index != -1) {
                    fileName = cursor.getString(index);
                }
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = uri.getPath();
            int cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }
        return fileName;
    }

    public static String getFileType(String filename) {
        if (filename != null && !filename.isEmpty()) {
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < filename.length() - 1) {
                String extension = filename.substring(dotIndex + 1).toLowerCase();
                switch (extension) {
                    case "pdf":
                        return "application/pdf";
                    case "jpg":
                    case "jpeg":
                        return "image/jpeg";
                    case "doc":
                    case "docx":
                        return "application/msword";
                    case "xls":
                    case "xlsx":
                        return "application/vnd.ms-excel";
                    case "gif":
                        return "image/gif";
                    case "js":
                        return "application/javascript";
                    case "py":
                        return "text/x-python";
                    default:
                        return null; // Unknown file type
                }
            }
        }
        return null; // Invalid filename or no extension found
    }


    public static void openFileInBrowser(Context context, String fileUrl) {
        Uri webpage = Uri.parse(fileUrl);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Handle case where no activity can handle the intent
            Toast.makeText(context, "No web browser available to open the URL", Toast.LENGTH_SHORT).show();
        }
    }


    public static void openFile(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        Uri uri = FileProvider.getUriForFile(context, "com.example.letschat.fileprovider", file);
        String mimeType = context.getContentResolver().getType(uri);

        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    public static void downloadFile(Context context, File file, ProgressBar progressBar) {
        progressBar.setVisibility(View.VISIBLE);

        Uri fileUri = FileProvider.getUriForFile(context, "com.example.letschat.fileprovider", file);
        String fileName = getFileName(context, fileUri);

        DownloadManager.Request request = new DownloadManager.Request(fileUri);
        request.setTitle("Downloading File: "+fileName);
        request.setDescription("Downloading...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context.getApplicationContext(), Environment.DIRECTORY_DOWNLOADS, fileName);

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = downloadManager.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadId == id) {
                    // Download complete
                    progressBar.setVisibility(View.GONE);
                    context.unregisterReceiver(this);

                    MediaScannerConnection.scanFile(context,
                            new String[] { file.getPath() },
                            null,
                            new MediaScannerConnection.OnScanCompletedListener() {
                                @Override
                                public void onScanCompleted(String path, Uri uri) {
                                    // Media scan completed
                                }
                            });

                }
            }
        };

        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public static void handleFile(Context context, String fileUrl, ProgressBar progressBar) {

        String fileName = getFileName(context, Uri.parse(fileUrl));

        // Check if the image file exists locally
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        if (file.exists()) {
            // If the file exists, open it using an implicit intent
            openFile(context, file);
        } else {
            // If the file doesn't exist, initiate the download
            downloadFile(context, file, progressBar);
        }
    }

    // Method to create a file for saving the captured image
    public static File createImageFile(Context context) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_"+timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return imageFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void scanFile(Context context, Uri imageUri){
        Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        scanIntent.setData(imageUri);
        context.sendBroadcast(scanIntent);
    }
}
