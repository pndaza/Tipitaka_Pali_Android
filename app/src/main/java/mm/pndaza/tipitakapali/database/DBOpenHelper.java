package mm.pndaza.tipitakapali.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import mm.pndaza.tipitakapali.model.Book;
import mm.pndaza.tipitakapali.model.Bookmark;
import mm.pndaza.tipitakapali.model.Page;
import mm.pndaza.tipitakapali.model.Recent;
import mm.pndaza.tipitakapali.model.Sutta;
import mm.pndaza.tipitakapali.model.Tab;
import mm.pndaza.tipitakapali.model.Toc;
import mm.pndaza.tipitakapali.model.Word;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class DBOpenHelper extends SQLiteOpenHelper {

    private static DBOpenHelper sInstance;
    private static final String DATABASE_NAME = "tipitaka_pali.db";
    private static final int DATABASE_VERSION = 23;

    public static synchronized DBOpenHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.

        if (sInstance == null) {
            Log.d(TAG, "getInstance: DBOpenHelper is already initialized.");
            sInstance = new DBOpenHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DBOpenHelper(Context context) {
        super(context, context.getFilesDir() + "/databases/" + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void buildIndex() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS word_index ON words ( word )");
        database.execSQL("CREATE INDEX IF NOT EXISTS page_index ON pages ( bookid )");
        database.execSQL("CREATE INDEX IF NOT EXISTS dict_index ON dictionary ( word )");
    }

    public String getBookID(int rowidOfPage) {
        SQLiteDatabase database = getReadableDatabase();
        String bookid = null;
        Cursor cursor = database.rawQuery(
                "SELECT bookid FROM pages WHERE id = " + rowidOfPage, null);
        if (cursor != null && cursor.moveToFirst()) {
            bookid = cursor.getString(0);
            cursor.close();
        }
        
        return bookid;
    }

    public Book getBookInfo(String bookid) {
        SQLiteDatabase database = getReadableDatabase();
        String bookName = "";
        int firstPage = 1;
        int lastPage = 1;

        Cursor cursor = database.rawQuery(
                "SELECT name, firstpage, lastpage FROM books where id = '" + bookid + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            bookName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            firstPage = cursor.getInt(cursor.getColumnIndexOrThrow("firstpage"));
            lastPage = cursor.getInt(cursor.getColumnIndexOrThrow("lastpage"));
            cursor.close();
        }
        
        return new Book(bookid, bookName, firstPage, lastPage);
    }

    public String getBoookName(String bookid) {
        SQLiteDatabase database = getReadableDatabase();
        String bookName = null;
        Cursor cursor = database.rawQuery(
                "SELECT name FROM books where id = '" + bookid + "'", null);
        if (cursor != null && cursor.moveToFirst()) {
            bookName = cursor.getString(0);
            cursor.close();
        }
        
        return bookName;
    }

    public ArrayList<Page> getPages(String bookid) {

        ArrayList<Page> pages = new ArrayList<>();
        int id;
        int page;

        SQLiteDatabase database = getReadableDatabase();
        // will add only id and page number to arrayList.
        // page content will be load when viewpager adapter need it.
        String sql = String.format("SELECT id, page FROM pages WHERE bookid = '%s'", bookid);
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                id = cursor.getInt(0);
                page = cursor.getInt(1);
                pages.add(new Page(id, page));
            } while (cursor.moveToNext());
            cursor.close();
        }

        Log.d(TAG, "getPages: first page: id " + pages.get(0).get_id());

        
        return pages;
    }


    public ArrayList<Integer> getParagraphs(String bookid, int pageNumber) {
        ArrayList<Integer> paragraphs = new ArrayList<>();
        String strParas = null;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT paranum FROM pages WHERE bookid = ? AND page = ? AND paranum != ''",
                new String[]{bookid, String.valueOf(pageNumber)});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            strParas = cursor.getString(cursor.getColumnIndexOrThrow("paranum"));
            cursor.close();
        }
        if (strParas != null && strParas.length() > 0) {
            int lastIndex = strParas.length();
            strParas = strParas.substring(1, lastIndex - 1);
            String[] paras = strParas.split("-");
            for (String para : paras) {
                paragraphs.add(Integer.parseInt(para));
            }
        }
        
        return paragraphs;
    }

    public int getFirstParagraphNumber(String bookid) {
        int firstParagraph = 0;

        String sql = String.format(
                "SELECT paranum FROM pages WHERE bookid = '%s' AND paranum != ''", bookid);

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);
        // paragraph store format -1-2-  etc

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                String paras = cursor.getString(0).split("-")[1];
                firstParagraph = Integer.parseInt(paras);
            }
            cursor.close();
        }
        
        return firstParagraph;
    }

    public int getLastParagraphNumber(String bookid) {
        int lastParagraph = 0;

        String sql = String.format(
                "SELECT paranum FROM pages WHERE bookid = '%s' AND paranum != ''", bookid);

        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, null);

        // paragraph store format -1-2-  etc
        if (cursor != null) {
            if (cursor.moveToLast()) {
                String rawPara = cursor.getString(0);
                Log.d(TAG, "getLastParagraphNumber: rawstring " + rawPara);
                String[] paras = rawPara.split("-");
                Log.d(TAG, "getLastParagraphNumber: length " + paras.length);
                Log.d(TAG, "getLastParagraphNumber: index 0 " + paras[0]);
                Log.d(TAG, "getLastParagraphNumber: index 1 " + paras[1]);

                lastParagraph = Integer.parseInt(paras[paras.length - 1]);
            }
            cursor.close();
        }

        return lastParagraph;
    }

    public int getPageNumber(int rowidOfPage) {
        int pageNumber = 1;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT page FROM pages WHERE id = " + rowidOfPage, null);
        if (cursor != null && cursor.moveToFirst()) {
            pageNumber = cursor.getInt(0);
            cursor.close();
        }
        
        return pageNumber;
    }

    public int getPageNumber(String bookid, int paranum) {
        int page = 0;
        String lookupParagraph = "-" + paranum + "-";
        SQLiteDatabase database = getReadableDatabase();
        String sql = String.format(Locale.ENGLISH,
                "SELECT page FROM pages WHERE bookid = '%s' and paranum like '%%%s%%' LIMIT 1", bookid, lookupParagraph);
        Cursor cursor = database.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            page = cursor.getInt(0);
            cursor.close();
        }
        
        return page;
    }

    public String getPageContent(int id) {

        Log.d(TAG, "getPageContent: page id " + id);
        String content = null;
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT content FROM pages WHERE id = " + id, null);
        if (cursor != null && cursor.moveToFirst()) {
            content = cursor.getString(0);
            cursor.close();
        }
        
        return content;
    }

    public ArrayList<Toc> getToc(String bookid) {

        ArrayList<Toc> tocArrayList = new ArrayList<>();
        String rawToc = "";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                String.format("SELECT toc FROM books WHERE id = '%s'", bookid), null);
        if (cursor != null && cursor.moveToFirst()) {
            rawToc = cursor.getString(0);
            cursor.close();
        }
        

        String[] tocs = rawToc.split("\n");

        for (int i = 0; i < tocs.length; i++) {
            String[] toc = tocs[i].split("->");
            tocArrayList.add(new Toc(toc[0], toc[1], Integer.parseInt(toc[2])));
        }
        return tocArrayList;
    }

/*    public ArrayList<String> getAllBook() {
        ArrayList<String> bookList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT id FROM books", null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookList.add(cursor.getString(cursor.getColumnIndex("id")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return bookList;
    }*/

/*
    public ArrayList<Integer> getPageIdListOfBook(String bookid) {

        ArrayList<Integer> pageIdList = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = getReadableDatabase();
        String sql = String.format("SELECT _id FROM pages WHERE bookid = '%s'", bookid);
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                pageIdList.add(cursor.getInt(0));
            } while (cursor.moveToNext());
        }
        return pageIdList;
    }
*/

    public ArrayList<Word> getPageIdListOfWord(String word) {

        ArrayList<Word> pageIdList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT rowids FROM words WHERE word = ?";
        Cursor cursor = database.rawQuery(sql, new String[]{word});
        if (cursor != null && cursor.moveToFirst()) {
            // word column is unique. will get only one row.
            String rowids = cursor.getString(cursor.getColumnIndexOrThrow("rowids"));
            pageIdList = parseRowId(rowids);
            cursor.close();
        }
        
        return pageIdList;

    }

    private ArrayList<Word> parseRowId(String rowids) {
        ArrayList<Word> ids = new ArrayList<>();
        String[] rows = rowids.split(",");

        for (String row : rows) {
            String[] info = row.split("_");
            int rowid = Integer.parseInt(info[0]);
            int wordLocation = Integer.parseInt(info[1]);
            ids.add(new Word(rowid, wordLocation));
        }
        return ids;
    }

    public ArrayList<String> getWordList(String query) {
        ArrayList<String> word_list = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT word FROM words where word like '" + query + "%' LIMIT 500";

//        Log.d("wordlist query ", sql);
        Cursor cursor = database.rawQuery(sql, null);
//        Log.d("cursor count is ", "" + cursor.getCount());
        if (cursor != null && cursor.moveToFirst()) {
            do {
                word_list.add(cursor.getString(0));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return word_list;
    }

/*    public String getCategory(int id) {
        String category = "";
        Cursor cursor = getReadableDatabase().rawQuery("SELECT bookid FROM pages WHERE id = " + id, null);
        if (cursor.moveToFirst() && cursor != null) {
            String bookid = cursor.getString(cursor.getColumnIndex("bookid"));
            category = bookid.split("_")[0];
        }
        cursor.close();
        return category;
    }*/

    public ArrayList<String> getExplanationBooks(String bookid) {

        ArrayList<String> expBooks = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT exp from pali_attha_tika_match WHERE base = ?", new String[]{bookid});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                expBooks.add(cursor.getString(cursor.getColumnIndexOrThrow("exp")));
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        return expBooks;
    }

    public boolean isExistInTipitakaAbidan(String word) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT word FROM dictionary WHERE word = ? AND book = 1", new String[]{word});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        
        return false;
    }

    public boolean isExistTranslationBook(String bookid) {
        SQLiteDatabase database = getReadableDatabase();
        Log.d(TAG, "isExistTranslationBook: " + bookid);
        Cursor cursor = database.rawQuery(
                "SELECT bookid FROM tran_books WHERE bookid = ?", new String[]{bookid});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    public String getTranslationBookID(String bookid) {
        Log.d(TAG, "getTranslationBookID: pali book id  " + bookid);
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT tran_bookid FROM tran_books WHERE bookid = ?", new String[]{bookid});
        Log.d(TAG, "getTranslationBookID: cursor count " + cursor.getCount());
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            String tran_bookid = cursor.getString(0);
            Log.d(TAG, "getTranslationBookID: " + tran_bookid);
            cursor.close();
            return tran_bookid;
        }
        return null;
    }

    public ArrayList<Bookmark> getBookmarks() {
        SQLiteDatabase database = getReadableDatabase();
        ArrayList<Bookmark> bookmarkList = new ArrayList<>();
        Cursor cursor = database.rawQuery(" SELECT note, bookid, pagenumber FROM bookmark", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String note = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                    String bookid = cursor.getString(cursor.getColumnIndexOrThrow("bookid"));
                    String bookName = getBoookName(bookid);
                    int pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("pagenumber"));
                    bookmarkList.add(new Bookmark(note, bookid, bookName, pageNumber));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        
        return bookmarkList;
    }

    public void addToBookmark(String note, String bookid, int pageNumber) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("INSERT INTO bookmark (note, bookid, pagenumber) VALUES (?,?,?)",
                new Object[]{note, bookid, pageNumber});
    }

    public void removeFromBookmark(int rowid) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM bookmark WHERE rowid = " + rowid);
        
    }

    public void removeAllBookmarks() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM bookmark");
        
    }


    public ArrayList<Recent> getAllRecent() {
        ArrayList<Recent> recentList = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(" SELECT rowid, bookid, pagenumber FROM recent ORDER BY rowid DESC", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String bookid = cursor.getString(cursor.getColumnIndexOrThrow("bookid"));
                    String bookName = getBoookName(bookid);
                    int pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("pagenumber"));
                    recentList.add(new Recent(bookid, bookName, pageNumber));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        
        return recentList;
    }

    private boolean isBookExistInRecent(String bookid) {
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(
                "SELECT bookid FROM recent Where bookid = '" + bookid + "'", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return true;
            }
            cursor.close();
        }
        
        return false;
    }

    public void addToRecent(String bookid, int pageNumber) {
        SQLiteDatabase database = getWritableDatabase();
        if (isBookExistInRecent(bookid)) {
            database.execSQL("UPDATE recent SET bookid = '" + bookid + "', pagenumber = '" + pageNumber
                    + "' WHERE bookid = '" + bookid + "'");
        } else {
            database.execSQL("INSERT INTO recent (bookid, pagenumber) VALUES (?,?)", new Object[]{bookid, pageNumber});
        }
        
    }

    public void removeAllRecent() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM recent");
        
    }


    public void addAllTab(ArrayList<Tab> tabs) {
        SQLiteDatabase database = getWritableDatabase();
        for (Tab tab : tabs) {
            database.execSQL("INSERT INTO tab(bookid, bookname, pagenumber) VALUES (?, ?, ?)",
                    new Object[]{tab.getBookID(), tab.getBookName(), tab.getCurrentPage()});
        }
        
    }

/*    public void addToTab(String bookid, String bookName, String pageNumber) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("UPDATE tab SET bookid = ?, bookname = ?, pagenumber = ?",
                new Object[]{bookid, bookName, pageNumber});
        

    }*/

    public void removeAllTab() {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM tab");
        
    }

    public void removeFromTab(String bookid, int pageNumber) {
        SQLiteDatabase database = getWritableDatabase();
        database.execSQL("DELETE FROM tab WHERE bookid = ? AND pagenumber = ?",
                new Object[]{bookid, pageNumber});
        
    }

    public ArrayList<Tab> getAllTab() {
        ArrayList<Tab> tabs = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery("SELECT bookid, bookname, pagenumber FROM tab", null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String bookid = cursor.getString(cursor.getColumnIndexOrThrow("bookid"));
                    String bookName = cursor.getString(cursor.getColumnIndexOrThrow("bookname"));
                    int pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("pagenumber"));
                    tabs.add(new Tab(bookid, bookName, pageNumber));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        
        return tabs;
    }

    public ArrayList<Sutta> getAllSutta() {
        ArrayList<Sutta> suttas = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT suttas.name, book_id, books.name as book_name, page_number from suttas " +
                "INNER JOIN books on books.id = suttas.book_id";

        Cursor cursor = database.rawQuery(sql, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                    String bookid = cursor.getString(cursor.getColumnIndexOrThrow("book_id"));
                    String bookName = cursor.getString(cursor.getColumnIndexOrThrow("book_name"));
                    int pageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("page_number"));
                    suttas.add(new Sutta(name, bookid, bookName, pageNumber));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return suttas;
    }
    public int getDatabaseVersion() {
        return DATABASE_VERSION;
    }
}
