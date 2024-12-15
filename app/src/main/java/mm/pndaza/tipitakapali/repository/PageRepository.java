package mm.pndaza.tipitakapali.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.model.Page;

public class PageRepository extends BaseRepository{
    public PageRepository(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    public ArrayList<Page> getPages(String bookId, int pageNumber, int limit){
        ArrayList<Page> pages= new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT page, content FROM pages WHERE book_id = ? AND page >= ? LIMIT ?";
        Cursor cursor = db.rawQuery(sql, new String[]{bookId, String.valueOf(pageNumber), String.valueOf(limit)});
        if (cursor != null && cursor.moveToFirst()){
            do{
              int page = cursor.getInt(cursor.getColumnIndexOrThrow("page"));
              String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));
              pages.add(new Page(page, content));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return pages;
    }
}
