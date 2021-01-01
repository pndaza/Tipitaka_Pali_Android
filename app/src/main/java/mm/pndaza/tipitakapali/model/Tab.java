package mm.pndaza.tipitakapali.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Tab implements Parcelable{

    private String bookID;
    private String bookName;
    private int currentPage;

    public Tab(String bookID, String bookName, int currentPage) {
        this.bookID = bookID;
        this.bookName = bookName;
        this.currentPage = currentPage;
    }

    protected Tab(Parcel in) {
        bookID = in.readString();
        bookName = in.readString();
        currentPage = in.readInt();
    }

    public static final Creator<Tab> CREATOR = new Creator<Tab>() {
        @Override
        public Tab createFromParcel(Parcel in) {
            return new Tab(in);
        }

        @Override
        public Tab[] newArray(int size) {
            return new Tab[size];
        }
    };


    public String getBookID() {
        return bookID;
    }

    public String getBookName() {
        return bookName;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(bookID);
        dest.writeString(bookName);
        dest.writeInt(currentPage);
    }

    @NonNull
    @Override
    public String toString() {
        return bookID + "/" + bookName + "/" + currentPage;
    }
}
