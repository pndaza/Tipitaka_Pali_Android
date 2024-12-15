package mm.pndaza.tipitakapali.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

public class PageMapRepository  extends BaseRepository{
    public PageMapRepository(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    public int getMyanmarPageNumber(String bookId, int pageNumber){
        int mmPageNumber = 0;

        String sql = "SELECT mm_page_number from pali_mm_page_map " +
                "where pali_book_id = ? AND pali_page_number = ?";
        try (Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{bookId, String.valueOf(pageNumber)})) {
            if (cursor != null && cursor.moveToFirst()) {
                mmPageNumber = cursor.getInt(0);
            }
        }
        return mmPageNumber;
    }
}