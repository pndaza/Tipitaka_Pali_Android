package mm.pndaza.tipitakapali.repository;

import static androidx.constraintlayout.widget.Constraints.TAG;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MyanmarBookRepository extends BaseRepository{
    public MyanmarBookRepository(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    public boolean haveTranslation(String paliBookId) {
        String sql = "SELECT * FROM burmese_translated_books WHERE pali_book_id = ?";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[]{paliBookId});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    public String getLinkType(String paliBookId) {
        String sql = "SELECT link_type FROM burmese_translated_books WHERE pali_book_id = ?";
        SQLiteDatabase database = getReadableDatabase();
        String link_type = "paragraph";
        Cursor cursor = database.rawQuery(sql, new String[]{paliBookId});
        if (cursor != null && cursor.moveToFirst()) {
            link_type = cursor.getString(0);
            cursor.close();
            return link_type;
        }
        return link_type;
    }

    public String getMyanmarBookID(String paliBookId, int pageNumber) {
        Log.d(TAG, "getTranslationBookID: pali book id  " + paliBookId);
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT mm_book_id FROM burmese_translated_books WHERE pali_book_id = ?" +
                " AND ? BETWEEN pali_first_page AND pali_last_page";
        Cursor cursor = database.rawQuery( sql
                , new String[]{paliBookId, String.valueOf(pageNumber)});
        if (cursor != null &&  cursor.getCount() > 0) {
            cursor.moveToFirst();
            String myanmarBookId = cursor.getString(0);
            Log.d(TAG, "getTranslationBookID: " + myanmarBookId);
            cursor.close();
            return myanmarBookId;
        }
        return null;
    }
}
