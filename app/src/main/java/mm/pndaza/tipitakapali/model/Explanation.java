package mm.pndaza.tipitakapali.model;

public class Explanation {
    int baseParagraph;
    String bookid;
    int pageNumber;

    public Explanation(int baseParagraph, String bookid, int pageNumber) {
        this.baseParagraph = baseParagraph;
        this.bookid = bookid;
        this.pageNumber = pageNumber;
    }

    public int getBaseParagraph() {
        return baseParagraph;
    }

    public String getBookid() {
        return bookid;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
