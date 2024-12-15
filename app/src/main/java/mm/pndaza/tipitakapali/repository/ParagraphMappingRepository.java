package mm.pndaza.tipitakapali.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import mm.pndaza.tipitakapali.model.ParagraphMapping;

public class ParagraphMappingRepository  extends BaseRepository {

    public ParagraphMappingRepository(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

 // function to get all paragraph mapping details
   public ArrayList<ParagraphMapping> getParagraphMappings(String bookID, int pageNumber) {
       ArrayList<ParagraphMapping> paragraphMappingList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT paragraph, to_book_id, to_page_number, name from paragraph_mapping " +
                "INNER JOIN books on paragraph_mapping.to_book_id = books.id " +
                "WHERE paragraph_mapping.from_book_id = ? and from_page_number = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{bookID, String.valueOf(pageNumber)});
        if(cursor == null){
            return paragraphMappingList;
        }
        while (cursor.moveToNext()){
            int paragraph = cursor.getInt(cursor.getColumnIndexOrThrow("paragraph"));
            String toBookId = cursor.getString(cursor.getColumnIndexOrThrow("to_book_id"));
            int toPageNumber = cursor.getInt(cursor.getColumnIndexOrThrow("to_page_number"));
            String bookName = cursor.getString(cursor.getColumnIndexOrThrow("name"));
            ParagraphMapping mapping = new ParagraphMapping(
                    paragraph,  bookID, pageNumber, toBookId, toPageNumber, bookName

            );
            paragraphMappingList.add(mapping);

        }
cursor.close();
        return paragraphMappingList;

    }
}
