package mm.pndaza.tipitakapali.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NissayaRepository extends BaseRepository {
    public NissayaRepository(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    public boolean haveNissaya(String bookId) {
        String sql = "SELECT * FROM nissaya WHERE book_id = ?";
        SQLiteDatabase database = getReadableDatabase();
        Cursor cursor = database.rawQuery(sql, new String[]{bookId});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.close();
            return true;
        }
        return false;
    }

    public String getBasket(String bookId) {
        String sql = "SELECT basket FROM nissaya WHERE book_id = ?";
        SQLiteDatabase database = getReadableDatabase();
        String basket = "";
        Cursor cursor = database.rawQuery(sql, new String[]{bookId});
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            basket = cursor.getString(0);
            cursor.close();
        }
        return basket;
    }
}
