package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.model.Explanation;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.NumberUtil;

public class ExplanationListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Explanation> expList;

    public ExplanationListAdapter(Context context, ArrayList<Explanation> list) {

        this.context = context;
        this.expList = list;
    }


    @Override
    public int getCount() {
        return expList.size(); //returns total of items in the booksList
    }

    @Override
    public Explanation getItem(int position) {
        return expList.get(position); //returns booksList item at the specified position
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        TextView textView = convertView.findViewById(android.R.id.text1);
        int baseParagraph = expList.get(position).getBaseParagraph();
        textView.setText(MDetect.getDeviceEncodedText( "စာပိုဒ် အမှတ် - " + NumberUtil.toMyanmar(baseParagraph)));

        return convertView;
    }

}
