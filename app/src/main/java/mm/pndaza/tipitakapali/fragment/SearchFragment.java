package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.CategoryTabAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Book;
import mm.pndaza.tipitakapali.utils.MDetect;

public class SearchFragment extends Fragment {

    private Context context;
    private Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        getActivity().setTitle(MDetect.getDeviceEncodedText(getString(R.string.app_name_mm)));
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = view.getContext();

        initListView(view);

    }

    @Override
    public void onResume() {
        super.onResume();

        toolbar = getActivity().findViewById(R.id.toolbar);

        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL
                | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);


        FrameLayout frameLayout = getActivity().findViewById(R.id.fragment_layout);
        CoordinatorLayout.LayoutParams co_params = (CoordinatorLayout.LayoutParams) frameLayout.getLayoutParams();
        co_params.setBehavior(new AppBarLayout.ScrollingViewBehavior());

    }

    private void initListView(View view) {

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager viewPager = view.findViewById(R.id.viewPager);

        CategoryTabAdapter adapter = new CategoryTabAdapter(getActivity().getSupportFragmentManager(), 1);
        adapter.addFragment(new BookSearchFragment(), MDetect.getDeviceEncodedText("ကျမ်းစာ"));
        adapter.addFragment(new DictSearchFragment(), MDetect.getDeviceEncodedText("အဘိဓာန်"));
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }


}
