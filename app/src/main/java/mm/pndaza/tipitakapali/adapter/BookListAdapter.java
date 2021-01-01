package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.model.Book;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class BookListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Book> booksList;

    private static final int BOOK = 0;
    private static final int HEADER = 1;

    public BookListAdapter(Context context, ArrayList<Book> list) {

        this.context = context;
        this.booksList = list;
    }

    @Override
    public int getItemViewType(int position) {
        // id of header is added as empty string
        if (booksList.get(position).getId().isEmpty()){
            return HEADER;
        } else {
            return BOOK;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return booksList.size(); //returns total of items in the booksList
    }

    @Override
    public Object getItem(int position) {
        return booksList.get(position); //returns booksList item at the specified position
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null) {
            switch (getItemViewType(position)){
                case HEADER:
                    convertView = LayoutInflater.from(context).inflate(R.layout.book_list_header, parent, false);
                    break;
                case BOOK:
                    convertView = LayoutInflater.from(context).inflate(R.layout.simple_list_item, parent, false);
                    break;
            }
        }


        switch (getItemViewType(position)){
            case HEADER:
                TextView tvHeader = convertView.findViewById(R.id.tv_list_header);
                // display as zawgyi
                tvHeader.setText(Rabbit.uni2zg(booksList.get(position).getName()));
                break;
            case BOOK:
                TextView tvBook = convertView.findViewById(R.id.tv_list_item);
                // display as zawgyi
                tvBook.setText(Rabbit.uni2zg(booksList.get(position).getName()));
                break;
        }

        return convertView;
    }

}
