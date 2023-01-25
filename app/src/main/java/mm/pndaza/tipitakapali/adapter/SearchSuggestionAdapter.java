package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class SearchSuggestionAdapter  extends RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder> {

    private final ArrayList<String> suggestionList;

    private final OnItemClickListener onItemClickListener;

    public SearchSuggestionAdapter( ArrayList<String> suggestionList, OnItemClickListener onItemClickListener) {
        this.suggestionList = suggestionList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View wordListItemView = inflater.inflate(R.layout.simple_list_item, parent, false);
        return new ViewHolder(wordListItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(Rabbit.uni2zg(suggestionList.get(position)));
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView textView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_list_item);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(suggestionList.get(getAdapterPosition()));
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String word);
    }
}
