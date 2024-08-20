package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.SuttaListAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Sutta;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;


public class SuttaDialogFragment extends DialogFragment implements SuttaListAdapter.OnItemClickListener{
    private ArrayList<Sutta> all_sutta = new ArrayList<>();
    private ArrayList<Sutta> suttas = new ArrayList<>();
    private SuttaListAdapter adapter;
    private  SuttaDialogListener listener;

    @Override
    public void onItemClick(Sutta sutta) {
        listener.onClickedSutta(sutta);
        dismiss();
    }

    public interface SuttaDialogListener {
        void onClickedSutta(Sutta sutta);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.BOTTOM);
        // need for rounder corner
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.requestFeature(Window.FEATURE_NO_TITLE);
        // set "origin" to top left corner, so to speak
//        window.getDecorView().setBackgroundResource(android.R.color.transparent);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 90;
        window.setAttributes(params);
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        return inflater.inflate(R.layout.dlg_sutta_list, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
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
        int dialogHeight = (int) (metrics.heightPixels * 0.65 );
        // set width and height for dialog
        params.width = dialogWidth;
        params.height = dialogHeight;
        window.setAttributes(params);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SuttaDialogFragment.SuttaDialogListener) {
            listener = (SuttaDialogFragment.SuttaDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement SuttaDialogFragment.SuttaDialogListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();
        MDetect.init(context);

        final RecyclerView sutta_list = view.findViewById(R.id.sutta_list);
        sutta_list.setLayoutManager(new LinearLayoutManager(context));
        sutta_list.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        all_sutta = DBOpenHelper.getInstance(getContext()).getAllSutta();
        adapter = new SuttaListAdapter( all_sutta,this);
        sutta_list.setAdapter(adapter);

        SearchView filterInput = view.findViewById(R.id.filter_input);
        filterInput.setQueryHint(MDetect.getDeviceEncodedText("သုတ်နာမည် ရိုက်ရှာရန်"));
        filterInput.setFocusable(true);
//        searchInput.requestFocusFromTouch();

        filterInput.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryWord) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String filter) {

                if (!MDetect.isUnicode()) {
                    filter = Rabbit.zg2uni(filter);
                }

doFilter(filter);
                return false;
            }
        });
/*
        sutta_list.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scrolling up
                    searchContainerView.setVisibility(View.INVISIBLE);
//                    tabLayout.setVisibility(View.GONE);
                } else {
                    // Scrolling down
                    searchContainerView.setVisibility(View.VISIBLE);
//                    tabLayout.setVisibility(View.VISIBLE);
                }
            }
        });
*/
    }
    private void doFilter(String filter) {
        suttas.clear();
        if (filter.isEmpty()) {
            adapter.setFilteredWordList(all_sutta);
            adapter.setFilterText("");
        } else {
            Log.d("TAG", "doFilter: " + filter);
            Log.d("TAG", "all sutta count: " + suttas.size());
            for (Sutta sutta : all_sutta) {
//                Log.d("TAG", sutta.getName());
                if (sutta.getName().contains(filter)) {
                    suttas.add(sutta);
                }
            }
            Log.d("TAG", "doFilter: " + suttas.size());
            adapter.setFilteredWordList(suttas);
            adapter.setFilterText(filter);
        }

    }
}