package mm.pndaza.tipitakapali.model;

public class Word {
    int rowid;
    int location;

    public Word(int rowid, int location) {
        this.rowid = rowid;
        this.location = location;
    }

    public int getRowid() {
        return rowid;
    }

    public int getLocation() {
        return location;
    }
}
