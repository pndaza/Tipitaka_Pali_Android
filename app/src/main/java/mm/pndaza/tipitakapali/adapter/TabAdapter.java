package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Tab;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;


public class TabAdapter extends RecyclerView.Adapter<TabAdapter.ViewHolder> {

    private ArrayList<Tab> tabs;
    private static ClickListener clickListener;
    private Context context;

    public TabAdapter(ArrayList<Tab> tabs) {
        this.tabs = tabs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View wordListItemView = inflater.inflate(R.layout.tab_item, parent, false);
        return new ViewHolder(wordListItemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Tab tab = tabs.get(position);
        holder.tvBookName.setText(Rabbit.uni2zg(tab.getBookName()));
        String pageNumber = "ႏွာ - " + NumberUtil.toMyanmar(tab.getCurrentPage());
        holder.tvPageNumber.setText(pageNumber);
    }

    @Override
    public int getItemCount() {
        return tabs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        TextView tvBookName;
        TextView tvPageNumber;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            tvBookName = itemView.findViewById(R.id.tv_book_name);
            tvPageNumber = itemView.findViewById(R.id.tv_page_number);
        }
        @Override
        public void onClick(View itemView) {
            clickListener.onItemClick(getAdapterPosition(), itemView);
        }

        @Override
        public boolean onLongClick(View itemView) {
            clickListener.onItemLongClick(getAdapterPosition(), itemView);
            return true;
        }

    }

    public void setOnItemClickListener(ClickListener clickListener) {
        TabAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);

        void onItemLongClick(int position, View v);
    }

    public void deleteItem(int position) {

        DBOpenHelper.getInstance(context).removeFromTab(
                tabs.get(position).getBookID(),tabs.get(position).getCurrentPage() );

        tabs.remove(position);

        notifyDataSetChanged();
    }

}
