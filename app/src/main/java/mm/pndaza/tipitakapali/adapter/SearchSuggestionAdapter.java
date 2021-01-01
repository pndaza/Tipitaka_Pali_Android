package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class SearchSuggestionAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<String> suggestionList;

    private static class ViewHolder {
        TextView textView;
    }

    public SearchSuggestionAdapter(Context context, ArrayList<String> suggestionList) {
        this.context = context;
        this.suggestionList = suggestionList;
    }

    @Override
    public int getCount() {
        return suggestionList.size();
    }

    @Override
    public Object getItem(int i) {
        return suggestionList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).
                    inflate(R.layout.simple_list_item, parent, false);
            viewHolder.textView = convertView.findViewById(R.id.tv_list_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.textView.setText(Rabbit.uni2zg(suggestionList.get(position)));

        // returns the view for the current row
        return convertView;
    }

}
