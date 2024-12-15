package mm.pndaza.tipitakapali.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.model.Paragraph;

public class ParagraphRepository extends  BaseRepository{
    public ParagraphRepository(SQLiteOpenHelper dbHelper) {
        super(dbHelper);
    }

    public ArrayList<Paragraph> getParagraphs(String bookid, int pageNumber) {
        ArrayList<Paragraph> paragraphs = new ArrayList<>();
        SQLiteDatabase database = getReadableDatabase();
        String sql = "SELECT paragraph_number, paragraph_index FROM paragraphs " +
        " WHERE book_id = ? AND page_number = ?";
        Cursor cursor = database.rawQuery(sql,new String[]{bookid, String.valueOf(pageNumber)});
        if (cursor != null && cursor.moveToFirst()) {
           do{
               int paragraphNumber = cursor.getInt(cursor.getColumnIndexOrThrow("paragraph_number"));
               int paragraphIndex = cursor.getInt(cursor.getColumnIndexOrThrow("paragraph_index"));
               paragraphs.add(new Paragraph(paragraphNumber, paragraphIndex));
           }while (cursor.moveToNext());

           cursor.close();
        }

        return paragraphs;
    }
}
