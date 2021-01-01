package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.SearchResultAdapter;
import mm.pndaza.tipitakapali.adapter.SearchSuggestionAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Word;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SearchFactory;

public class BookSearchFragment extends Fragment
        implements CompoundButton.OnCheckedChangeListener, SearchResultAdapter.OnItemClickListener {


    public interface OnSearchResultItemClickListener {
        void onSearchResultItemClick(String bookid, int pageNumber, String queryWord);
    }

    private SearchResultAdapter.OnItemClickListener onItemClickListener;
    private OnSearchResultItemClickListener listener;


    private ArrayList<Word> searchResult = new ArrayList<>();
    private ArrayList<String> word_list = new ArrayList<>();
    private SearchResultAdapter adapter;
    private static Context context;
    private String queryWord;

    private SearchView searchInput;
    private LinearLayout searchOptionsView;

    private TabLayout tabLayout;

//    private static final String TAG = "BookSearchFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        onItemClickListener = this;
        context = getContext();
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.search_title)));
        return inflater.inflate(R.layout.fragment_book_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        word_list = new ArrayList<>();

//        tabLayout = getActivity().findViewById(R.id.tabLayout);

        searchOptionsView = view.findViewById(R.id.search_options);
        searchInput = view.findViewById(R.id.search_input);

        final CheckBox checkBoxPali = view.findViewById(R.id.checkBox_pali);
        final CheckBox checkBoxAttha = view.findViewById(R.id.checkBox_attha);
        final CheckBox checkBoxTika = view.findViewById(R.id.checkBox_tika);
        final CheckBox checkBoxAnnya = view.findViewById(R.id.checkBox_annya);

        checkBoxPali.setOnCheckedChangeListener(this);
        checkBoxAttha.setOnCheckedChangeListener(this);
        checkBoxTika.setOnCheckedChangeListener(this);
        checkBoxAnnya.setOnCheckedChangeListener(this);

        checkBoxAttha.setText(MDetect.getDeviceEncodedText("အဋ္ဌကထာ"));

        final ListView search_suggestion_view = view.findViewById(R.id.search_suggestion_list);
        final RecyclerView search_result_view = view.findViewById(R.id.search_result_list);
        search_result_view.setLayoutManager(new LinearLayoutManager(context));
//        search_result_view.setFastScrollEnabled(true);
//        search_result_view.setFastScrollAlwaysVisible(true);

//        final SearchView searchInput = view.findViewById(R.id.search_input);
        searchInput.setQueryHint(MDetect.getDeviceEncodedText("ရှာလိုသော ပုဒ်/ပုဒ်များ ရိုက်ထည့်ရန်"));
        searchInput.setFocusable(true);
//        searchInput.requestFocusFromTouch();

        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String rawQuery) {

                // hide suggestion search_result
                search_suggestion_view.setVisibility(View.GONE);
                if (search_result_view.getVisibility() == View.INVISIBLE) {
                    search_result_view.setVisibility(View.VISIBLE);
                }

                rawQuery = rawQuery.trim();
                if (!MDetect.isUnicode()) {
                    rawQuery = Rabbit.zg2uni(rawQuery);
                }

                queryWord = rawQuery;
                final String query = rawQuery;

                Boolean[] searchFilter = new Boolean[]{
                        checkBoxPali.isChecked(),
                        checkBoxAttha.isChecked(),
                        checkBoxTika.isChecked(),
                        checkBoxAnnya.isChecked()};

                //long start_time = System.currentTimeMillis();

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        searchResult = SearchFactory.Search(context, query, searchFilter);
                    }
                }).run();


                if (searchResult == null) {
                    search_result_view.setAdapter(null);
                } else {
                    adapter = new SearchResultAdapter(searchResult, query, onItemClickListener);
                    search_result_view.setAdapter(adapter);
                }

                String info = getString(R.string.not_found);
                // တွေ့ရှိမှု အကြိမ်
                if (!searchResult.isEmpty()) {
                    info = getResources().getString(R.string.founds) + " " + NumberUtil.toMyanmar(searchResult.size()) + " " + getResources().getString(R.string.founded_time);
                }
                getActivity().setTitle(MDetect.getDeviceEncodedText(info));

                return false;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {

                search_suggestion_view.setVisibility(View.VISIBLE);
                search_result_view.setVisibility(View.INVISIBLE);


                if (!MDetect.isUnicode()) {
                    queryText = Rabbit.zg2uni(queryText);
                }

//                searchInput.setQuery(queryText, false);
                // trim whitespace in start of query
                final String query = queryText.replaceAll("^\\s+", "");
//                Log.d("search text is ", queryText);

                if (query.length() < 2) {
                    word_list.clear();
//                    search_suggestion_view.setVisibility(View.GONE);
                } else {
                    // suggestion will be shown only for one word
                    if (!query.contains(" ")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                word_list = DBOpenHelper.getInstance(context).getWordList(query);
                            }
                        }).run();
                    }
                }

                // suggestion list view
                SearchSuggestionAdapter adapter = new SearchSuggestionAdapter(context, word_list);
                search_suggestion_view.setAdapter(adapter);

                return false;
            }
        });

        //
        search_suggestion_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String word = (String) adapterView.getItemAtPosition(i);
                Log.d("click word is ", word);
                searchInput.setQuery(MDetect.getDeviceEncodedText(word), true);
//                searchInput.setQuery(word, false);
            }
        });

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof OnSearchResultItemClickListener) {
            listener = (OnSearchResultItemClickListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement BookSearchFragment.OnSearchResultItemClickListener");
        }

    }


    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        searchResult.clear();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            searchInput.setQuery(searchInput.getQuery(), true);
        }
    }


    @Override
    public void onItemClick(Word word) {
        int rowid = word.getRowid();
        String bookid = DBOpenHelper.getInstance(context).getBookID(rowid);
        int currentPage = DBOpenHelper.getInstance(context).getPageNumber(rowid);
        listener.onSearchResultItemClick(bookid, currentPage, queryWord);
    }
}
