package mm.pndaza.tipitakapali.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.DictionaryAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class DictSearchFragment extends Fragment implements DictionaryAdapter.OnItemClickListener {

    private ArrayList<String> word_list = new ArrayList<>();
    private DictionaryAdapter adapter;
    private SearchView searchInput;
    private static Context context;
    private TabLayout tabLayout;
    private LinearLayout searchContainerView;

//    private static final String TAG = "BookSearchFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.search_title)));
        return inflater.inflate(R.layout.fragment_dict_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        MDetect.init(context);

        tabLayout = getActivity().findViewById(R.id.tabLayout);
        searchContainerView = view.findViewById(R.id.search_container);

        final RecyclerView recyclerView = view.findViewById(R.id.search_result_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        adapter = new DictionaryAdapter(word_list, this);
        recyclerView.setAdapter(adapter);

        searchInput = view.findViewById(R.id.search_input);
        searchInput.setQueryHint(MDetect.getDeviceEncodedText("အဘိဓာန်ကြည့်လိုသောပုဒ် ရိုက်ထည့်ပါ"));
        searchInput.setFocusable(true);
//        searchInput.requestFocusFromTouch();

        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String word) {
                showDictDialog(word);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryWord) {

                word_list.clear();

                // TODO improve performance for zawgyi
                queryWord = queryWord.trim();
                if (!MDetect.isUnicode()) {
                    // performance hack
                    //
                    /*if (queryWord.matches("[\u103a\u1033\u1034\u103a-\u103d\u1060-\u1097]")) {
                        queryWord = Rabbit.zg2uni(queryWord);
                    }*/

                    queryWord = Rabbit.zg2uni(queryWord);
                }

                if (queryWord.isEmpty()) {
                    word_list.clear();
                    adapter.notifyDataSetChanged();
                } else {


                    String sql = "SELECT DISTINCT word FROM dictionary WHERE word LIKE '" +
                            queryWord + "%'";

                    if (queryWord.length() < 2) {
                        sql = "SELECT DISTINCT word FROM dictionary WHERE word = '" +
                                queryWord + "'";
                    }

                    SQLiteDatabase sqLiteDatabase = DBOpenHelper.getInstance(context).getReadableDatabase();
                    Cursor cursor = sqLiteDatabase.rawQuery(sql, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            word_list.add(cursor.getString(cursor.getColumnIndexOrThrow("word")));

                        } while (cursor.moveToNext());
                    }
                }
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
                    searchContainerView.setVisibility(View.INVISIBLE);
//                    tabLayout.setVisibility(View.GONE);
                } else {
                    // Scrolling down
                    searchContainerView.setVisibility(View.VISIBLE);
//                    tabLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    @Override
    public void onResume() {
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.search_title)));
        super.onResume();
    }

    private void showDictDialog(String word) {
        Bundle args = new Bundle();
        args.putString("word", word);
        args.putBoolean("hide", true);

        DictionaryBottomSheetDialog dialog = new DictionaryBottomSheetDialog();
        dialog.setArguments(args);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "Dictionary");
    }


    @Override
    public void onItemClick(String word) {
        // hiding soft keyboard
        // Check if no view has focus:
        int waitingTime = 0;
        View view = getActivity().getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (view != null) {
            view.clearFocus();
            if(imm.isAcceptingText()){
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            waitingTime = 100;
            }
        }

        if (!MDetect.isUnicode()) {
            word = Rabbit.zg2uni(word);
        }

        Handler handler = new Handler();
        String finalWord = word;
        handler.postDelayed(() -> showDictDialog(finalWord), waitingTime);
    }

}
