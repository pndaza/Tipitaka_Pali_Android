package mm.pndaza.tipitakapali.model;

import android.text.SpannableString;

public class SearchResult {
    private String bookid;
    private String bookname;
    private int page;
    private SpannableString brief;

    public SearchResult(String bookid, String bookname, int page, SpannableString brief){
        this.bookid = bookid;
        this.bookname = bookname;
        this.page = page;
        this.brief = brief;
    }

    public String getBookid() { return bookid; }

    public String getBookName() {
        return bookname;
    }

    public int getPage() {
        return page;
    }

    public SpannableString getBrief() {
        return brief;
    }
}
