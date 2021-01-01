package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.SearchResult;
import mm.pndaza.tipitakapali.model.Word;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SharePref;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {
    private ArrayList<Word> results;
    private String queryWord;

    private Context context;

    private OnItemClickListener onItemClickListener;

    private final static String TAG = "SearchResultAdapter";
    private static final int highlightColor = Color.parseColor("#E91E63");
    private static final int highlightColorNight = Color.parseColor("#FF5823");

    public SearchResultAdapter(ArrayList<Word> results, String query, OnItemClickListener onItemClickListener){
        this.results = results;
        this.queryWord = query;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View wordListItemView = inflater.inflate(R.layout.searchresult_row_item, parent, false);
        return new ViewHolder(wordListItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchResultAdapter.ViewHolder holder, int position) {
        SearchResult searchResult = loadData(position);
        String book_and_page = searchResult.getBookName() + " ။ နှာ - " +
                NumberUtil.toMyanmar(searchResult.getPage());


        holder.tv_book.setText(Rabbit.uni2zg(book_and_page));

        holder.tv_brief.setText(searchResult.getBrief());
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView tv_book;
        public TextView tv_brief;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_book = itemView.findViewById(R.id.tv_book);
            tv_brief = itemView.findViewById(R.id.tv_brief);

            itemView.setTag(this);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(results.get(getAdapterPosition()));
        }
    }


/*    @Override
    public View getView(int position, View view, ViewGroup parent) {

        SearchResult searchResult = loadData(position);
        ViewHolder viewHolder;

        if (view == null) {
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).
                    inflate(R.layout.searchresult_row_item, parent, false);
            viewHolder.book = view.findViewById(R.id.tv_book);
            viewHolder.brief = view.findViewById(R.id.tv_brief);
            view.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) view.getTag();
        }

        String book_and_page = searchResult.getBookName() + " ။ နှာ - " +
                NumberUtil.toMyanmar(searchResult.getPage());

        viewHolder.book.setText(Rabbit.uni2zg(book_and_page));
        viewHolder.brief.setText(searchResult.getBrief());

        return view;
    }*/

    private SearchResult loadData(int position){

        SearchResult searchResult = null;

        Word _word = results.get(position);
        int _rowidOfPage = _word.getRowid();
        int wordLocation = _word.getLocation();

        String sql = "SELECT bookid, page, content FROM pages WHERE id = " + _rowidOfPage;
        SQLiteDatabase sqLiteDatabase = DBOpenHelper.getInstance(context).getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);

        if( cursor != null && cursor.moveToFirst()){
            do{
                String bookid = cursor.getString(0);
                String bookname = DBOpenHelper.getInstance(context).getBoookName(bookid);
                int page = cursor.getInt(1);
                String content = cursor.getString(2);
                // use span for highlight
                SpannableString brief = getBrief(content, wordLocation);
                searchResult = new SearchResult(bookid, bookname, page, brief);

            } while (cursor.moveToNext());
        }
        cursor.close();
        sqLiteDatabase.close();
        return searchResult;
    }

    private SpannableString getBrief(String content, int wordLocation){

        // remmove HTML tag from source
        content = content.replaceAll("</span>(န္တိ|တိ)", "$1");
        content = content.replaceAll("<.*?>", " ");
        content = content.replaceAll(" +", " ");
        // split word
        List<String> wordList = Arrays.asList(content.trim().split(" "));
        int wordsCount = wordList.size();

        int wordIndex = wordLocation;
        Log.d(TAG, "total words are  " + wordsCount);
        Log.d(TAG, "saved word location is  " +wordIndex);
        if ( wordLocation >= wordsCount){
            Log.d(TAG, content);
        }


        //checking word or phrase
        String word = queryWord;
        int len = word.split(" ").length;

        String wordsBeforeQuery = "";
        String wordsAfterQuery = "";

        // get 17 words before query word if available
        int countTofindWords = 12;
        for ( int i = countTofindWords; i > 0; i--){
            if( wordIndex - i >= 0){
                int ii = 0 ;
                while ( ii < i){
                    wordsBeforeQuery += ( wordList.get((wordIndex - i) + ii) + " ");
                    ii++;
                }
                break;
            }
        }

        //fix some formatting
        wordsBeforeQuery = wordsBeforeQuery.replaceAll("([\u1040-\u1049]+။)\n","$1");


        // get 20 words after query word if available
        for ( int i = 20; i >=0; i--){
            if( wordIndex + i < wordsCount -1){
                int ii = 1;
                while ( ii < i){
                    wordsAfterQuery += ( " " + wordList.get(wordIndex + ii ));
                    ii++;
                }
                break;
            }
        }

        //fix some formatting
        wordsAfterQuery = wordsAfterQuery.replaceAll("([\u1040-\u1049]+။)\n","$1");

        // text will be displayed in zawgyi encoding.
        wordsBeforeQuery = Rabbit.uni2zg(wordsBeforeQuery);
        wordsAfterQuery = Rabbit.uni2zg(wordsAfterQuery);

        String brief = wordsBeforeQuery  +  Rabbit.uni2zg(wordList.get(wordIndex)) + wordsAfterQuery;

        // text will be displayed in zawgyi encoding.
//        brief = Rabbit.uni2zg(brief);
        queryWord = Rabbit.uni2zg(queryWord);

        Log.d(TAG, "length of brief is " + brief.length());

/*        int startIndexToHighlight = brief.indexOf(queryWord);
        int endIndexToHighlight = startIndexToHighlight + queryWord.length() ;
        Log.d(TAG, "query found at - " + startIndexToHighlight);
        Log.d(TAG, "and the end of match index is - " + endIndexToHighlight);
        SpannableString highlightedBrief = setHighLight(brief, startIndexToHighlight, endIndexToHighlight);*/

        int start_index = wordsBeforeQuery.length() - queryWord.length() + Rabbit.uni2zg(wordList.get(wordIndex)).length();
        int end_index = brief.length() - wordsAfterQuery.length();
        SpannableString highlightedBrief = setHighLight(brief, start_index,end_index);

        return highlightedBrief;

    }

    private SpannableString setHighLight(String brief, int start_index, int end_index){

        SpannableString highlightedText = new SpannableString(brief);
        int backgroundColor = SharePref.getInstance(context).getPrefNightModeState() ?
                highlightColorNight : highlightColor;
        // highlight query words
        // set foreground color for query words
        highlightedText.setSpan(
                new ForegroundColorSpan(Color.WHITE), start_index, end_index,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        // set background color for query words

        highlightedText.setSpan(
                new BackgroundColorSpan(backgroundColor), start_index, end_index,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        return highlightedText;
    }

    public interface OnItemClickListener {
        void onItemClick(Word word);
    }

}
