package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.TabAdapter;
import mm.pndaza.tipitakapali.callback.SwipeToRemoveCallback;
import mm.pndaza.tipitakapali.model.Tab;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class TabManagementDialog extends DialogFragment {

    public interface TabManagementListener {

        void onAddNewTab();

        void onTabSelect(String bookid, int currentPage);

        void onCancel();

    }

    private static Context context;
    TabManagementListener listener;

    ArrayList<Tab> tabs;

    private static final String TAG = "BookSearchFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(true);

        Window window = getDialog().getWindow();
        window.requestFeature(Window.FEATURE_NO_TITLE);
        // set "origin" to top left corner, so to speak
        window.setGravity(Gravity.TOP);
        window.getDecorView().setBackgroundResource(android.R.color.transparent);

        // after that, setting values for x and y works "naturally"
        WindowManager.LayoutParams params = window.getAttributes();
        params.x = 0;
        params.y = 90;
        window.setAttributes(params);

        return inflater.inflate(R.layout.dlg_tab_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        context = view.getContext();
        MDetect.init(context);

        Bundle args = getArguments();
        tabs = args.getParcelableArrayList("tabs");


        TextView textView = view.findViewById(R.id.tv_title);
        textView.setText(Rabbit.uni2zg(getString(R.string.opened_books)));
        textView.setTypeface(textView.getTypeface(), Typeface.BOLD);

        RecyclerView recyclerView = view.findViewById(R.id.listView_tabs);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));


        TabAdapter adapter = new TabAdapter(tabs);
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new TabAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.d(TAG, "onItemClick position: " + position);
                listener.onTabSelect(tabs.get(position).getBookID(), tabs.get(position).getCurrentPage());
                dismiss();

            }

            @Override
            public void onItemLongClick(int position, View v) {

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context, R.style.AlertDialogTheme);
                String title = "လမ်းညွှန်";
                String message = "စာရင်းမှ ပယ်ဖျက်လိုပါက ဘယ်ညာပွတ်ဆွဲပြီး ပယ်ဖျက်နိုင်ပါသည်။";
                String confirm = "ကောင်းပါပြီ";
                if (!MDetect.isUnicode()) {
                    title = Rabbit.uni2zg(title);
                    message = Rabbit.uni2zg(message);
                    confirm = Rabbit.uni2zg(confirm);
                }
                alertDialog.setTitle(title)
                        .setMessage(message)
                        .setCancelable(true)
                        .setPositiveButton(confirm,
                                (dialog, id) -> dismiss());
                alertDialog.show();
            }
        });

        ItemTouchHelper itemTouchHelper = new
                ItemTouchHelper(new SwipeToRemoveCallback(context, adapter));
        itemTouchHelper.attachToRecyclerView(recyclerView);

        ImageButton btn_add = view.findViewById(R.id.add_tab);
        btn_add.setOnClickListener(v -> {

            listener.onAddNewTab();
            dismiss();
        });


    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );

    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);


        if (context instanceof TabManagementListener) {
            listener = (TabManagementListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement TabManagementDialog.TabManagementListener");
        }

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onCancel();
        dismiss();
    }
}
