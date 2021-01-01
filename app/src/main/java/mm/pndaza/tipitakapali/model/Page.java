package mm.pndaza.tipitakapali.model;

public class Page {
    private int _id;
    private int pageNumber;
    private String content;
    private String paragraphNumber;

    public Page(int _id, int pageNumber ) {
        this._id = _id;
        this.pageNumber = pageNumber;
    }

    public int get_id() { return _id; }

    public int getPageNumber() {
        return pageNumber;
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getParagraphNumber() {
        return paragraphNumber;
    }

    public void setParagraphNumber(String paragraphNumber) {
        this.paragraphNumber = paragraphNumber;
    }

}
