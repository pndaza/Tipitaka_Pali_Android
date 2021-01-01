package mm.pndaza.tipitakapali.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    String id;
    String name;
    int firstPage;
    int lastPage;

    public Book(String id, String name, int firstPage, int lastPage) {
        this.id = id;
        this.name = name;
        this.firstPage = firstPage;
        this.lastPage = lastPage;
    }

    public Book(String name){
        this.name = name;
    }

    protected Book(Parcel in) {
        id = in.readString();
        name = in.readString();
        firstPage = in.readInt();
        lastPage = in.readInt();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getFirstPage() {
        return firstPage;
    }

    public int getLastPage() {
        return lastPage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeInt(firstPage);
        dest.writeInt(lastPage);
    }
}
