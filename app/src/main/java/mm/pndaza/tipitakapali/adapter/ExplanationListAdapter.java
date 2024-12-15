package mm.pndaza.tipitakapali.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.model.ParagraphMapping;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.NumberUtil;

public class ExplanationListAdapter extends BaseAdapter {

    //    private Context context;
//    private ArrayList<Explanation> expList;
    private final ArrayList<ParagraphMapping> mappings;
    private final GlanceButtonListener listener;

    public ExplanationListAdapter(ArrayList<ParagraphMapping> mappings, GlanceButtonListener glanceButtonListener) {

//        this.context = context;
        this.mappings = mappings;
        this.listener = glanceButtonListener;
    }

    public interface GlanceButtonListener {
        void onClick(ParagraphMapping mapping);
    }

    @Override
    public int getCount() {
        return mappings.size(); //returns total of items in the booksList
    }

    @Override
    public ParagraphMapping getItem(int position) {
        return mappings.get(position); //returns booksList item at the specified position
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.paragraph_list_item, parent, false);
        }
        ParagraphMapping mapping = mappings.get(position);

        TextView title = convertView.findViewById(R.id.title);
        TextView subTitle = convertView.findViewById(R.id.subtitle);
        ImageView glanceButton = convertView.findViewById(R.id.eye_button);
        glanceButton.setFocusable(false);
        glanceButton.setOnClickListener(view -> listener.onClick(mapping));

        title.setText(MDetect.getDeviceEncodedText("စာပိုဒ် :" + NumberUtil.toMyanmar(mapping.paragraphNumber)));
        subTitle.setText(MDetect.getDeviceEncodedText(mapping.toBookName + "၊ နှာ-" + NumberUtil.toMyanmar(mapping.toPageNumber)));

        return convertView;
    }

}
