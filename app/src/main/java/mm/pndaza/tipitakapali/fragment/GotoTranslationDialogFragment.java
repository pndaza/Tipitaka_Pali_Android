package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.ExplanationListAdapter;
import mm.pndaza.tipitakapali.adapter.ParagraphListAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Explanation;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;


public class GotoTranslationDialogFragment extends DialogFragment {

    private Context context;
    private String bookid;
    private int pageNumber;
    private TextView tv_empty;

    private ArrayList<Integer> para_list = null;
    private ParagraphListAdapter adapter;

    private static final String TAG = "GotoExplanationDialog";

    private GotoTranslationDialogListener listener;

    public interface GotoTranslationDialogListener {
        void onChooseParagraph(int paragraph);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Window window = getDialog().getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.requestFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dlg_choose_paragraph, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof GotoTranslationDialogListener) {
            listener = (GotoTranslationDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement GotoDialogFragment.GotoDialogListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            bookid = args.getString("bookid");
            pageNumber = args.getInt("pagenumber");
        }

        TextView tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText(Rabbit.uni2zg(tv_title.getText().toString()));
        Button btn_close = view.findViewById(R.id.btn_close);
        btn_close.setText(MDetect.getDeviceEncodedText(btn_close.getText().toString()));
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        ListView listView = view.findViewById(R.id.list_view);
        tv_empty = view.findViewById(R.id.tv_empty);
        tv_empty.setText(MDetect.getDeviceEncodedText(getString(R.string.no_paragraph)));
//        tv_empty.setVisibility(View.GONE);
        listView.setEmptyView(tv_empty);
        para_list =  DBOpenHelper.getInstance(getContext()).getParagraphs(bookid, pageNumber);
        adapter = new ParagraphListAdapter(context, para_list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                listener.onChooseParagraph(para_list.get(position));
            }
        });

    }
}
