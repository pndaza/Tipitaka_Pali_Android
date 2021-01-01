package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.TocAdapter;
import mm.pndaza.tipitakapali.model.Toc;

import static androidx.constraintlayout.widget.Constraints.TAG;


public class TocDialogFragment extends BottomSheetDialogFragment implements TocAdapter.OnItemClickListener {


    private TocDialogListener listener;

    public interface TocDialogListener {

        void onTocItemClick(int page);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

/*        getDialog().setCanceledOnTouchOutside(true);

        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.BOTTOM|Gravity.RIGHT);
        window.getDecorView().setBackgroundResource(android.R.color.transparent);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 90;
        window.setAttributes(params);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);*/


        return inflater.inflate(R.layout.dlg_toc, container, false);
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof TocDialogListener) {
            listener = (TocDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement GotoDialogFragment.GotoDialogListener");
        }

    }

    @Override
    public void onResume() {
        super.onResume();

/*        Window window = getDialog().getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float xdpi = metrics.xdpi;
        int deviceWidth = metrics.widthPixels;
        double minScaleFactor = 2.0;
        double maxScaleFactor = 3.0;
        double clearSpace = 0.2;
        double scaleFactor = minScaleFactor;
        while ( minScaleFactor <= maxScaleFactor){
            if((scaleFactor + clearSpace) * xdpi < deviceWidth) {
                scaleFactor = minScaleFactor;
            }
            minScaleFactor += 0.2;
        }

        int dialogWidth = (int)(xdpi * scaleFactor);
        int dialogHeight = (int) (metrics.heightPixels * 0.7 );
        // set width and height for dialog
        params.width = dialogWidth;
        params.height = dialogHeight;
        window.setAttributes(params);*/
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);

        Bundle args = getArguments();
        ArrayList<Toc> tocs = args.getParcelableArrayList("tocs");

        TocAdapter tocAdapter = new TocAdapter(tocs, this);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setAdapter(tocAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

    }

    @Override
    public void onItemClick(Toc toc) {
        int pageNumber = toc.getPage();
        listener.onTocItemClick(pageNumber);
        dismiss();
    }

}
