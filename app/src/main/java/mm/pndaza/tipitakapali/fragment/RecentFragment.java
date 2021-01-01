package mm.pndaza.tipitakapali.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Arrays;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.RecentAdapter;
import mm.pndaza.tipitakapali.callback.SwipeToRemoveCallback;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Recent;
import mm.pndaza.tipitakapali.model.Tab;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class RecentFragment extends Fragment{


    public interface OnRecentItemClickListener {
        void onRecentItemClick(String bookid, int pageNumber);
    }

    private Context context;
    private RecyclerView recentListView;
    private RecentAdapter adapter;
    TextView emptyInfoView;
    private ArrayList<Recent> recents;
    private OnRecentItemClickListener callbackListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.recent_mm)));
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_recent, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        //bind view
        recentListView = view.findViewById(R.id.listView_recent);
        recentListView.setLayoutManager(new LinearLayoutManager(context));
        recentListView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        applyRecentList();

        emptyInfoView = view.findViewById(R.id.empty_info);
        applyEmptyInfoView(emptyInfoView);


        ArrayList<Tab> tabs = DBOpenHelper.getInstance(context).getAllTab();
        int tabCount = tabs.isEmpty()? 0 : tabs.size();
        final AppCompatButton btn_tab = view.findViewById(R.id.btn_tab);
        btn_tab.setText(String.valueOf(tabCount));
        btn_tab.setOnClickListener(v -> {
            recents.clear();
            for(Tab tab: tabs){
                recents.add(new Recent(tab.getBookID(), tab.getBookName(), tab.getCurrentPage()));
            }
            adapter.notifyDataSetChanged();
        });

    }
    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity)getActivity()).getSupportActionBar().show();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_recent, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_clearAll) {
            clearRecent();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callbackListener = (OnRecentItemClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implemented OnRecentItemClickListener");

        }
    }

    private void applyRecentList() {
        recents = DBOpenHelper.getInstance(getContext()).getAllRecent();
        adapter = new RecentAdapter(recents);
//        final RecentAdapter adapter = new RecentAdapter(recents);
        recentListView.setAdapter(adapter);
        adapter.setOnClickListener(view -> {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            String bookid = recents.get(position).getBookid();
            int pageNumber = recents.get(position).getPageNumber();

            Log.d("pageNumber", "" + pageNumber);

            callbackListener.onRecentItemClick(bookid, pageNumber);
        });
    }

    private void clearRecent() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme);

        String message = "လက်တလော ကြည့်ရှုထားသည်များကို ဖယ်ရှားမှာလား";
        String comfirm = "ဖယ်ရှားမယ်";
        String cancel = "မလုပ်တော့ဘူး";
        if (!MDetect.isUnicode()) {
            message = Rabbit.uni2zg(message);
            comfirm = Rabbit.uni2zg(comfirm);
            cancel = Rabbit.uni2zg(cancel);
        }

        alertDialog.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(comfirm,
                        (dialog, id) -> {
                            DBOpenHelper.getInstance(context).removeAllRecent();
                            applyRecentList();
                            applyEmptyInfoView(emptyInfoView);
                        })
                .setNegativeButton(cancel, (dialog, id) -> {
                });
        alertDialog.show();
    }

    private void applyEmptyInfoView(TextView emptyInfoView) {

        String info = getString(R.string.recent_empty);
        if (!MDetect.isUnicode()) {
            info = Rabbit.uni2zg(info);
        }
        emptyInfoView.setText(info);
        emptyInfoView.setVisibility(recents.isEmpty() ? View.VISIBLE : View.INVISIBLE);
    }


}
