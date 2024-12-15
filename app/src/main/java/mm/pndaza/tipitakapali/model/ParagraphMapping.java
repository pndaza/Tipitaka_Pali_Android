package mm.pndaza.tipitakapali.model;

public class ParagraphMapping {
    public int paragraphNumber;
    public String fromBookId;
    public int fromPageNumber;
    public String toBookId;
    public int toPageNumber;
    public String toBookName;

    public ParagraphMapping(int paragraphNumber, String fromBookId, int fromPageNumber, String toBookId, int toPageNumber, String toBookName) {
        this.paragraphNumber = paragraphNumber;
        this.fromBookId = fromBookId;
        this.fromPageNumber = fromPageNumber;
        this.toBookId = toBookId;
        this.toPageNumber = toPageNumber;
        this.toBookName = toBookName;
    }
}
