package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.BookListAdapter;
import mm.pndaza.tipitakapali.model.Book;

public class BookListFragment extends Fragment {

    public interface BookListFragmentListener {
        void onBookItemClick(String bookid);
    }

    private ArrayList<Book> books;
    private Context context;
    BookListFragmentListener listener;

    public BookListFragment() {
    }

    public static final BookListFragment newInstance(ArrayList<Book> books) {
        Bundle args = new Bundle();
        args.putParcelableArrayList("book", books);
        BookListFragment f = new BookListFragment();
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        books = getArguments().getParcelableArrayList("book");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_book_list, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        BookListAdapter bookListAdapter = new BookListAdapter(context, books);
        ListView listView = view.findViewById(R.id.lv_book_list);
        listView.setAdapter(bookListAdapter);
        handleOnClickEvent(listView);
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof BookListFragmentListener) {
            listener = (BookListFragmentListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement BookListFragment.BookListFragmentListener");
        }

    }

    private void handleOnClickEvent(ListView listView) {

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listview, View view, int position, long arg3) {

                Book book = (Book) listview.getItemAtPosition(position);
                // header item object is string
                if (!book.getId().isEmpty()) {
                    listener.onBookItemClick(book.getId());
                }
            }
        });
    }
}
