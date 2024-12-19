package mm.pndaza.tipitakapali.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Bookmark;
import mm.pndaza.tipitakapali.model.Recent;
import mm.pndaza.tipitakapali.utils.SharePref;


public class SplashScreenActivity extends AppCompatActivity {
    private static final String DATABASE_FILENAME = "tipitaka_pali.db";
    private static String OUTPUT_PATH;
    SharePref sharePref;
    private Context context;

    private static final String TAG = "splashScreen";
    private ArrayList<Bookmark> bookmarks = new ArrayList<>();
    private ArrayList<Recent> recents = new ArrayList<>();
    private static final String OLD_BOOK_ID_COLUMN_NAME = "bookid";
    private static final String OLD_PAGE_NUMBER_COLUMN_NAME = "pagenumber";
    private static final String NEW_BOOK_ID_COLUMN_NAME = "book_id";
    private static final String NEW_PAGE_NUMBER_COLUMN_NAME = "page_number";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (SharePref.getInstance(this).getPrefNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashscreen);

        context = this;
        OUTPUT_PATH = getOutputPath();

        sharePref = SharePref.getInstance(this);
        if (sharePref.isFirstTime()) {
            sharePref.setNotFirstTime();
            sharePref.saveDefault();
        }

        boolean dbCopyState = sharePref.isDatabaseCopied();
        boolean dbFileExit = new File(OUTPUT_PATH, DATABASE_FILENAME).exists();
        int savedDatabaseVersion = sharePref.getDatabaseVersion();
        int lastDatabaseVersion = DBOpenHelper.getInstance(this).getDatabaseVersion();

        if (dbCopyState && dbFileExit) {
            if (lastDatabaseVersion == savedDatabaseVersion) {
                startMainActivity();
            } else if (lastDatabaseVersion > savedDatabaseVersion) {
                Log.d(TAG, "onCreate: last db version " + lastDatabaseVersion);
                Log.d(TAG, "onCreate: saved db version " + savedDatabaseVersion);
                // update database
                recents = backupRecent();
                bookmarks = backupBookmarks();
                deleteDatabase();
                copyDatabase(lastDatabaseVersion);
            }
        } else {
            copyDatabase(lastDatabaseVersion);
        }

        /*if (dbCopyState && dbFileExit && lastDatabaseVersion == savedDatabaseVersion) {
            startMainActivity();
        }else if (dbCopyState && dbFileExit){
            Log.d(TAG, "onCreate: last db version " + lastDatabaseVersion);
            Log.d(TAG, "onCreate: saved db version " + savedDatabaseVersion);
            // update database
            if( lastDatabaseVersion > savedDatabaseVersion) {
                bookmarks = backupBookmarks();
            }
            deleteDatabase();
            copyDatabase(lastDatabaseVersion);
        } else {
            copyDatabase(lastDatabaseVersion);
        }*/

    }

    private String getOutputPath() {
        return getFilesDir() + "/databases/";
    }

    private void copyDatabase(int dbVersion) {

//        Toast.makeText(context, "Copying", Toast.LENGTH_SHORT).show();
        new CopyFromAssets().execute(dbVersion);
    }

    @SuppressLint("StaticFieldLeak")
    public class CopyFromAssets extends AsyncTask<Integer, Double, Integer> {

        protected Integer doInBackground(Integer... integers) {

            Log.d(TAG, "called doInBackground ");

            int dbVersion = integers[0];

            File path = new File(getOutputPath());
            // check database folder is exist and if not, make folder.
            if (!path.exists()) {
                path.mkdirs();
            }

            // extract from Assets folder to app-data folder
            byte[] buffer = new byte[102400];
            long alreadyCopy = 0;

            try {
                InputStream inputStream = getAssets().open("databases/tipitaka_pali.zip");
                ZipInputStream zin = new ZipInputStream(inputStream);
                ZipEntry ze;

                while ((ze = zin.getNextEntry()) != null) {
                    Log.d(TAG, "Unzipping " + ze.getName());

                    if (ze.isDirectory()) {
                        dirChecker(OUTPUT_PATH, ze.getName());
                    } else {
                        File f = new File(OUTPUT_PATH, ze.getName());
                        if (!f.exists()) {
                            boolean success = f.createNewFile();
                            if (!success) {
                                Log.w(TAG, "Failed to create file " + f.getName());
                                continue;
                            }
                            FileOutputStream fout = new FileOutputStream(f);
                            int count;
                            Long size = ze.getSize();
                            Log.v(TAG, String.valueOf(size));
                            while ((count = zin.read(buffer)) != -1) {
                                alreadyCopy += count;
                                fout.write(buffer, 0, count);
                                publishProgress(1.0d * alreadyCopy / size);
                            }
                            zin.closeEntry();
                            fout.close();
                        }
                    }
                }
                zin.close();
            } catch (Exception e) {
                Log.e(TAG, "unzip", e);
            }

            publishProgress(1.1);

            if (!recents.isEmpty()) {
                restoreRecent(recents);
            }

            if (!bookmarks.isEmpty()) {
                Log.d(TAG, "doInBackground: restoring backup bookmark");
                Log.d(TAG, "doInBackground: Bookmark count - " + bookmarks.size());
                restoreBookmark(bookmarks);
            }

            Log.d(TAG, "building index");
            DBOpenHelper.getInstance(context).buildIndex();

            return dbVersion;
        }

        @Override
        protected void onProgressUpdate(final Double... values) {
            super.onProgressUpdate(values);

            TextView progress = findViewById(R.id.tv_progress);
            int percentage = (int) (values[0] * 100);
            String report = percentage + "%";
            progress.setText(report);
            if (values[0] == 1.1) {
//                Toast.makeText(getApplicationContext(), "copied!", Toast.LENGTH_SHORT).show();
                progress.setText("Creating index...\n  Please wait");
            }

        }

        @Override
        protected void onPostExecute(Integer value) {
            sharePref.setDatabaseVersion(value);
            sharePref.setDbCopyState(true);
            startMainActivity();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }

    private void startMainActivity() {

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }, 500);
    }

    private static void dirChecker(String destination, String dir) {
        File f = new File(destination, dir);

        if (!f.isDirectory()) {
            boolean success = f.mkdirs();
            if (!success) {
                Log.w(TAG, "Failed to create folder " + f.getName());
            }
        }
    }

    private void deleteDatabase() {
        // deleting  temporary files created by sqlite
        File temp1 = new File(OUTPUT_PATH, DATABASE_FILENAME + "-shm");
        if (temp1.exists()) {
            temp1.delete();
        }
        File temp2 = new File(OUTPUT_PATH, DATABASE_FILENAME + "-wal");
        if (temp2.exists()) {
            temp2.delete();
        }

        new File(OUTPUT_PATH, DATABASE_FILENAME).delete();
    }

    private ArrayList<Recent> backupRecent() {
        ArrayList<Recent> allRecent = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DBOpenHelper.getInstance(this).getReadableDatabase();
            String recentTable = "recent";
            String bookIdColumnName = getBookIdColumnName(database, recentTable);
            String pageNumberColumnName = getPageNumberColumnName(database, recentTable);
            String sql = "SELECT " + bookIdColumnName + ", " + pageNumberColumnName
                    + " FROM " + recentTable;

            cursor = database.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String bookId = cursor.getString(cursor.getColumnIndexOrThrow(bookIdColumnName));
                    int pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow(pageNumberColumnName));
                    String bookName = ""; // Do not need to get book name here
                    allRecent.add(new Recent(bookId, bookName, pageNumber));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log the exception for debugging
            Log.e("BackupRecent", "Error backing up recent data", e);
        } finally {
            // Ensure the cursor and database are closed to avoid resource leaks
            if (cursor != null) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }

        return allRecent;
    }


    private void restoreRecent(ArrayList<Recent> recents) {

        for (Recent recent : recents) {
            DBOpenHelper.getInstance(this).addToRecent(recent.getBookid(), recent.getPageNumber());
        }
        if (recents == null || recents.isEmpty()) {
            Log.w("RestoreRecent", "The recents list is null or empty. Nothing to restore.");
            return;
        }

        SQLiteDatabase database = null;
        try {
            database = DBOpenHelper.getInstance(this).getWritableDatabase();
            database.beginTransaction(); // Start a transaction for better performance and consistency

            for (Recent recent : recents) {
                try {
                    DBOpenHelper.getInstance(this).addToRecent(recent.getBookid(), recent.getPageNumber());
                } catch (Exception e) {
                    // Log and continue with the next item
                    Log.e("RestoreRecent", "Error restoring recent: " + recent, e);
                }
            }

            database.setTransactionSuccessful(); // Mark the transaction as successful
        } catch (Exception e) {
            // Log the exception
            Log.e("RestoreRecent", "Error during restore operation", e);
        } finally {
            if (database != null) {
                if (database.inTransaction()) {
                    database.endTransaction(); // End the transaction
                }
                database.close(); // Close the database
            }
        }
    }


    private ArrayList<Bookmark> backupBookmarks() {
        ArrayList<Bookmark> bookmarkList = new ArrayList<>();
        SQLiteDatabase database = null;
        Cursor cursor = null;

        try {
            database = DBOpenHelper.getInstance(this).getReadableDatabase();
            String bookmarkTable = "bookmark";
            String bookIdColumnName = getBookIdColumnName(database, bookmarkTable);
            String pageNumberColumnName = getPageNumberColumnName(database, bookmarkTable);

            String sql = "SELECT note, " + bookIdColumnName + ", " + pageNumberColumnName
                    + " FROM " + bookmarkTable;

            cursor = database.rawQuery(sql, null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    try {
                        String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                        String bookId = cursor.getString(cursor.getColumnIndexOrThrow(bookIdColumnName));
                        int pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow(pageNumberColumnName));
                        String bookName = ""; // Do not need to get book name here
                        bookmarkList.add(new Bookmark(note, bookId, bookName, pageNumber));
                    } catch (Exception e) {
                        // Log error for the specific bookmark and continue
                        Log.e("BackupBookmarks", "Error processing a bookmark entry", e);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            // Log the exception for debugging
            Log.e("BackupBookmarks", "Error backing up bookmarks", e);
        } finally {
            // Ensure the cursor and database are closed to avoid resource leaks
            if (cursor != null) {
                cursor.close();
            }
            if (database != null && database.isOpen()) {
                database.close();
            }
        }

        return bookmarkList;
    }


    private void restoreBookmark(ArrayList<Bookmark> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            Log.w("RestoreBookmark", "The bookmarks list is null or empty. Nothing to restore.");
            return;
        }

        SQLiteDatabase database = null;
        try {
            database = DBOpenHelper.getInstance(this).getWritableDatabase();
            database.beginTransaction(); // Start a transaction for consistency and performance

            for (Bookmark bookmark : bookmarks) {
                try {
                    DBOpenHelper.getInstance(this).addToBookmark(
                            bookmark.getNote(),
                            bookmark.getBookID(),
                            bookmark.getPageNumber()
                    );
                } catch (Exception e) {
                    // Log error for the specific bookmark and continue
                    Log.e("RestoreBookmark", "Error restoring bookmark: " + bookmark, e);
                }
            }

            database.setTransactionSuccessful(); // Commit the transaction if all is well
        } catch (Exception e) {
            // Log any overarching error
            Log.e("RestoreBookmark", "Error during restore operation", e);
        } finally {
            if (database != null) {
                if (database.inTransaction()) {
                    database.endTransaction(); // End the transaction (rollback if not successful)
                }
                database.close(); // Close the database
            }
        }
    }

    public String getBookIdColumnName(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        String bookId = NEW_BOOK_ID_COLUMN_NAME;
        if (cursor.moveToFirst()) {
            do {
                String currentColumnName = cursor.getString(1); // Index 1 for the "name" column
                if (currentColumnName.equals(OLD_BOOK_ID_COLUMN_NAME)) {
                    bookId = OLD_BOOK_ID_COLUMN_NAME;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return bookId;
    }

    private String getPageNumberColumnName(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        String pageNumber = NEW_PAGE_NUMBER_COLUMN_NAME;
        if (cursor.moveToFirst()) {
            do {
                String currentColumnName = cursor.getString(1); // Index 1 for the "name" column
                if (currentColumnName.equals(OLD_PAGE_NUMBER_COLUMN_NAME)) {
                    pageNumber = OLD_PAGE_NUMBER_COLUMN_NAME;
                    break;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return pageNumber;
    }
}
