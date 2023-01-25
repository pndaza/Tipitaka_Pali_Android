package mm.pndaza.tipitakapali.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.activity.SearchResultActivity;
import mm.pndaza.tipitakapali.adapter.SearchSuggestionAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class BookSearchFragment extends Fragment implements SearchSuggestionAdapter.OnItemClickListener {
    private ArrayList<String> word_list = new ArrayList<>();
    private SearchSuggestionAdapter adapter;
    private SearchView searchInput;
    private Context context;
    private String queryWord;
    private TabLayout tabLayout;
    private LinearLayout searchContainerView;
    private String TAG = "BookSearchFragment";


//    private static final String TAG = "BookSearchFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        context = getContext();
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.search_title)));
        return inflater.inflate(R.layout.fragment_book_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        MDetect.init(context);

        tabLayout = getActivity().findViewById(R.id.tabLayout);
        searchContainerView = view.findViewById(R.id.search_container);

        final RecyclerView search_suggestion_view = view.findViewById(R.id.search_suggestion_list);
        search_suggestion_view.setLayoutManager(new LinearLayoutManager(context));
        search_suggestion_view.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        adapter = new SearchSuggestionAdapter(word_list, this);
        search_suggestion_view.setAdapter(adapter);

        searchInput = view.findViewById(R.id.search_input);
        searchInput.setQueryHint(MDetect.getDeviceEncodedText("ရှာလိုသော ပုဒ်/ပုဒ်များ ရိုက်ထည့်ရန်"));
        searchInput.setFocusable(true);
//        searchInput.requestFocusFromTouch();

        searchInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryWord) {
                View view = getActivity().getCurrentFocus();
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                if (view != null) {
                    view.clearFocus();
                    if(imm.isAcceptingText()){
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }

                queryWord = MDetect.getDeviceEncodedText(queryWord);
                String queryWordinUni = queryWord;

//                if (!MDetect.isUnicode()) {
//                    queryWordinUni = Rabbit.zg2uni(queryWordinUni);
//                }

                queryWordinUni = queryWordinUni.trim();
                Log.d("click word is ", queryWordinUni);
                Bundle args = new Bundle();
                args.putString("query_word", queryWordinUni);

                Intent intent = new Intent(getActivity(), SearchResultActivity.class);
                intent.putExtras(args);
                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String queryText) {

                if (!MDetect.isUnicode()) {
                    queryText = Rabbit.zg2uni(queryText);
                }

//                searchInput.setQuery(queryText, false);
                // trim whitespace in start of query
                final String query = queryText.replaceAll("^\\s+", "");
//                Log.d("search text is ", queryText);

                if (query.length() < 2) {
                    word_list.clear();
                } else {
                    // suggestion will be shown only for one word
                    if (!query.contains(" ")) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                word_list.clear();
                                word_list.addAll(DBOpenHelper.getInstance(context).getWordList(query));
                                adapter.notifyDataSetChanged();
//                                Log.d(TAG, "count: " + word_list.size());
                            }
                        }).run();
                    }
                }

                // suggestion list view
                adapter.notifyDataSetChanged();
                Log.d(TAG, "onQueryTextChange: " + adapter.getItemCount());

                return false;
            }
        });

        search_suggestion_view.addOnScrollListener(new RecyclerView.OnScrollListener() {

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
    public void onAttach(Context context) {

        super.onAttach(context);

    }

    @Override
    public void onItemClick(String word) {
        searchInput.setQuery(MDetect.getDeviceEncodedText(word), true);
    }
}
