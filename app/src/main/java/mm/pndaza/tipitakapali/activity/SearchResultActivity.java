package mm.pndaza.tipitakapali.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.SearchResultAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Word;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SearchFactory;
import mm.pndaza.tipitakapali.utils.SharePref;

public class SearchResultActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, SearchResultAdapter.OnItemClickListener {

    private String queryWord;
    private ArrayList<Word> searchResult = new ArrayList<>();
    private SearchResultAdapter adapter;
    private SearchResultAdapter.OnItemClickListener onItemClickListener;
    private RecyclerView search_result_view;
    private CheckBox checkBoxPali;
    private CheckBox checkBoxAttha;
    private CheckBox checkBoxTika;
    private CheckBox checkBoxAnnya;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (SharePref.getInstance(this).getPrefNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        MDetect.init(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);
        onItemClickListener = this;

        setSupportActionBar(findViewById(R.id.toolbar_in_search_result));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Bundle args = getIntent().getExtras();
        if (args != null) {
            queryWord = args.getString("query_word");
        }

        search_result_view = findViewById(R.id.search_result_list);

        checkBoxPali = findViewById(R.id.checkBox_pali);
        checkBoxAttha = findViewById(R.id.checkBox_attha);
        checkBoxTika = findViewById(R.id.checkBox_tika);
        checkBoxAnnya = findViewById(R.id.checkBox_annya);

        checkBoxPali.setOnCheckedChangeListener(this);
        checkBoxAttha.setOnCheckedChangeListener(this);
        checkBoxTika.setOnCheckedChangeListener(this);
        checkBoxAnnya.setOnCheckedChangeListener(this);
        checkBoxAttha.setText(MDetect.getDeviceEncodedText("အဋ္ဌကထာ"));

        final RecyclerView search_result_view = findViewById(R.id.search_result_list);
        search_result_view.setLayoutManager(new LinearLayoutManager(this));

        loadSearchResult();


    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        searchResult.clear();
        loadSearchResult();
    }

    @Override
    public void onItemClick(Word word) {
        int rowid = word.getRowid();
        String bookid = DBOpenHelper.getInstance(this).getBookID(rowid);
        int currentPage = DBOpenHelper.getInstance(this).getPageNumber(rowid);
        startReadBookActivity(bookid, currentPage, queryWord);
    }

    private void loadSearchResult() {
        //
        if (!MDetect.isUnicode()) {
            queryWord = Rabbit.zg2uni(queryWord);
        }

        final String query = queryWord;

        Boolean[] searchFilter = new Boolean[]{
                checkBoxPali.isChecked(),
                checkBoxAttha.isChecked(),
                checkBoxTika.isChecked(),
                checkBoxAnnya.isChecked()};

        //long start_time = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                searchResult = SearchFactory.Search(getApplicationContext(), query, searchFilter);
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
        setTitle(MDetect.getDeviceEncodedText(info));

    }

    private void startReadBookActivity(String bookid, int pageNumber, String queryWord) {

        Bundle args = new Bundle();
        args.putString("book_id", bookid);
        args.putInt("current_page", pageNumber);
        args.putString("search_text", queryWord);

        Intent intent = new Intent(this, BookReaderActivity.class);
        intent.putExtras(args);
        startActivity(intent);

    }
}
