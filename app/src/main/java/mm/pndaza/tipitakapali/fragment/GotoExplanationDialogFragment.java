package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.ExplanationListAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.ParagraphMapping;
import mm.pndaza.tipitakapali.repository.ParagraphMappingRepository;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;


public class GotoExplanationDialogFragment extends BottomSheetDialogFragment {

    private Context context;
    private String bookId;
    private int pageNumber;

//    final ArrayList<Explanation> explanations = new ArrayList<>();
    final ArrayList<ParagraphMapping> mappings = new ArrayList<>();
    private ExplanationListAdapter adapter;

    private static final String TAG = "GotoExplanationDialog";

    private GotoExplanationDialogListener listener;

    public interface GotoExplanationDialogListener {
        void onClickParagraph(ParagraphMapping mapping, boolean glanceMode);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dlg_goto_explanation, container, false);

        Window window = getDialog().getWindow();
//        WindowManager.LayoutParams params = window.getAttributes();
//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int screenHeight = displayMetrics.heightPixels;
//        Log.d(TAG, "onCreateView: " + screenHeight);
//        params.y = (int) (screenHeight * 0.3);
//        window.setAttributes(params);
//        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        window.requestFeature(Window.FEATURE_NO_TITLE);
        view.setBackgroundResource(R.drawable.rounded_top_corners);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (getDialog() != null && getDialog().getWindow() != null) {
            int width = calculateWidth();
            getDialog().getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        // Apply rounded corners to the container of the BottomSheet
        FrameLayout bottomSheet = getDialog().findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.rounded_top_corners);
        }
    }

    private int calculateWidth() {
        // Get the screen width in pixels
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int screenWidth = displayMetrics.widthPixels;

        // Convert dp to pixels
        int minWidthPx = (int) (300 * displayMetrics.density);
        int maxWidthPx = (int) (500 * displayMetrics.density);

        // Ensure the width is within the min and max bounds
        return Math.max(minWidthPx, Math.min(screenWidth, maxWidthPx));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        this.context = context;
        super.onAttach(context);
        if (context instanceof GotoExplanationDialogListener) {
            listener = (GotoExplanationDialogListener) context;
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
            bookId = args.getString("bookid");
            pageNumber = args.getInt("pagenumber");
        }

        TextView tv_title = view.findViewById(R.id.tv_title);
        tv_title.setText(Rabbit.uni2zg(tv_title.getText().toString()));
        Button btn_close = view.findViewById(R.id.btn_close);
        btn_close.setText(MDetect.getDeviceEncodedText(btn_close.getText().toString()));
        btn_close.setOnClickListener(view1 -> dismiss());

        ListView listView = view.findViewById(R.id.list_view);
        TextView tv_empty = view.findViewById(R.id.tv_empty);
        tv_empty.setText(MDetect.getDeviceEncodedText(getString(R.string.no_paragraph)));
//        tv_empty.setVisibility(View.GONE);
        listView.setEmptyView(tv_empty);
        adapter = new ExplanationListAdapter( mappings, mapping -> {
            Log.d(TAG, "onClick glance: " + mapping.paragraphNumber);
            listener.onClickParagraph(mapping, true);
            dismiss();
        }
        );

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                ParagraphMapping mapping = mappings.get(position);
                Log.d(TAG, "onClick item: " + mapping.paragraphNumber);
                listener.onClickParagraph(mapping, false);
            }
        });

        loadParagraphs();
    }

    private void loadParagraphs() {
        ParagraphMappingRepository repository =  new ParagraphMappingRepository(DBOpenHelper.getInstance(context));
        mappings.clear();
        mappings.addAll(repository.getParagraphMappings(bookId, pageNumber));
        adapter.notifyDataSetChanged();

    }

}
