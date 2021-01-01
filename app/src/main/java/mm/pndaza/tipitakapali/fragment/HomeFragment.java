package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.CategoryTabAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Book;
import mm.pndaza.tipitakapali.utils.MDetect;

public class HomeFragment extends Fragment{

    private Context context;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.app_name_mm)));
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        initListView(view);

    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);

    }

    private void initListView(View view) {

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);

        CategoryTabAdapter adapter = new CategoryTabAdapter(getActivity().getSupportFragmentManager(), 1);

        addFragement(view, adapter);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

    private void addFragement(View view, CategoryTabAdapter adapter) {

        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("mula", "ပါဠိ");
        categories.put("attha", "အဋ္ဌကထာ");
        categories.put("tika", "ဋီကာ");
        categories.put("annya", "အည");

        String[] subCategoriesOfPaliAtthaTika = getResources().getStringArray(R.array.subcategories_pali_attha_tika);
        String[] idOfSubCategoriesOfPaliAtthaTika = getResources().getStringArray(R.array.sub_categories_pali_attha_tika_id);
        String[] subCategoriesOfAnnya = getResources().getStringArray(R.array.subcategories_annya);
        String[] idOfSubCategoriesOfAnnya = getResources().getStringArray(R.array.subcategories_annya_id);

        for (Map.Entry mapEntry : categories.entrySet()) {
            ArrayList<Book> books = new ArrayList<>();
            if (mapEntry.getKey().equals("annya")) {
                for (int i = 0; i < subCategoriesOfAnnya.length; i++) {
                    // add header using empty id
                    books.add(new Book("", subCategoriesOfAnnya[i], 0, 0));
                    //add books
                    String bookId = mapEntry.getKey() + idOfSubCategoriesOfAnnya[i];
                    addBooks(books, bookId);
                }
            } else {
                for (int i = 0; i < subCategoriesOfPaliAtthaTika.length; i++) {
                    // add header using empty id
                    books.add(new Book("", subCategoriesOfPaliAtthaTika[i], 0, 0));
                    String bookId = mapEntry.getKey() + idOfSubCategoriesOfPaliAtthaTika[i];
                    addBooks(books, bookId);
                }
            }
            adapter.addFragment(
                    BookListFragment.newInstance(
                            books), MDetect.getDeviceEncodedText((String) mapEntry.getValue()));
        }
    }

    private void addBooks(ArrayList<Book> books, String bookId) {

        String sql = "select id, name, firstpage, lastpage from books where id like '" + bookId + "%'";
        Cursor cursor = DBOpenHelper.getInstance(context).getReadableDatabase().rawQuery(sql, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                books.add(
                        new Book(cursor.getString(cursor.getColumnIndex("id")),
                                cursor.getString(cursor.getColumnIndex("name")),
                                cursor.getInt(cursor.getColumnIndex("firstpage")),
                                cursor.getInt(cursor.getColumnIndex("lastpage"))));
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}
