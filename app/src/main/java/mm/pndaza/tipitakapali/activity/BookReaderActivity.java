package mm.pndaza.tipitakapali.activity;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.snackbar.Snackbar;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.Arrays;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.PageAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.fragment.GotoDialogFragment;
import mm.pndaza.tipitakapali.fragment.GotoExplanationDialogFragment;
import mm.pndaza.tipitakapali.fragment.GotoTranslationDialogFragment;
import mm.pndaza.tipitakapali.fragment.MoreBottomSheetDialogFragment;
import mm.pndaza.tipitakapali.fragment.SettingDialogFragment;
import mm.pndaza.tipitakapali.fragment.TabManagementDialog;
import mm.pndaza.tipitakapali.fragment.TocDialogFragment;
import mm.pndaza.tipitakapali.model.Book;
import mm.pndaza.tipitakapali.model.Page;
import mm.pndaza.tipitakapali.model.Tab;
import mm.pndaza.tipitakapali.model.Toc;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;


public class BookReaderActivity extends AppCompatActivity
        implements GotoDialogFragment.GotoDialogListener,
        TocDialogFragment.TocDialogListener,
        MoreBottomSheetDialogFragment.ActionChooseListener,
        TabManagementDialog.TabManagementListener,
        GotoExplanationDialogFragment.GotoExplanationDialogListener,
        GotoTranslationDialogFragment.GotoTranslationDialogListener {

    private static final int LAUNCH_MAIN_ACTIVITY = 1;
    private static final int LAUNCH_SETTING_ACTIVITY = 2;

    private Context context = BookReaderActivity.this;

    private ArrayList<Page> pages;
    private static ViewPager viewPager;

    private String bookid;
    private String bookName;
    private int firstPage;
    private int lastPage;
    private int currentPage = 1;
    private String searchText = "";
    private int firstParagraph = 0;
    private int lastParagraph = 0;
    private int paragraphNumber;

    private static final int PARAGRAPH = 1;

    private ArrayList<Tab> tabs = new ArrayList<>();
    private int currentTabLocation = 0;

    private static final String NSY_PALI = "mula";
    private static final String NSY_ATTHA = "attha";
    private static final String NSY_TIKA = "tika";
    private static final String NSY_ANNYA = "annya";

    private static final String TAG = "BookReader";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_reader);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MDetect.init(this);
        ArrayList<Tab> savedTabs = DBOpenHelper.getInstance(this).getAllTab();
        if (!savedTabs.isEmpty()) {
            tabs.addAll(savedTabs);
        }

        loadBook(getIntent());
        addToTab();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        Log.d("lifecycle", "onActivityResult invoked. tabs are " + tabs.size());

        if (requestCode == LAUNCH_MAIN_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("lifecycle", "onActivityResult invoked and Activity.RESULT_OK)");
                loadBook(data);
                addToTab();
                saveTab();
            }
        } else if (requestCode == LAUNCH_SETTING_ACTIVITY) {
            recreate();
        }

    }

    void setupGotoButton() {

        ImageButton btn_goto = findViewById(R.id.btn_goto);
        btn_goto.setOnClickListener(view -> {

            Bundle args = new Bundle();
            args.putInt("firstPage", firstPage);
            args.putInt("lastPage", lastPage);
            args.putInt("firstParagraph", firstParagraph);
            args.putInt("lastParagraph", lastParagraph);

            FragmentManager fm = getSupportFragmentManager();
            GotoDialogFragment gotoDialog = new GotoDialogFragment();
            gotoDialog.setArguments(args);
            gotoDialog.show(fm, "Goto");
        });

    }

    private void setupSeekBar() {

        DiscreteSeekBar seekBar = findViewById(R.id.seedbar);
        seekBar.setMin(firstPage);
        seekBar.setMax(lastPage);
        //if min value is not 1, something wrong with seekbar_progress_indicator
        if (currentPage != 0) {
            seekBar.setProgress(currentPage - firstPage);
        } else {
            seekBar.setProgress(firstPage + 1);
            seekBar.setProgress(firstPage);
        }

        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                viewPager.setCurrentItem(seekBar.getProgress() - firstPage);
                currentPage = viewPager.getCurrentItem() + firstPage;

            }
        });

        setupSeekSync(seekBar);
    }


    private void setupSeekSync(DiscreteSeekBar seekBar) {
        ViewPager viewPager = findViewById(R.id.vpPager);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                seekBar.setProgress(firstPage + i);
                addToRecent(i + 1);
                currentPage = firstPage + i;
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }


    private void setupMoreButton() {
        ImageButton btn_more = findViewById(R.id.btn_more);
        btn_more.setOnClickListener(v -> {
            showMoreBottomSheetDialog();
        });
    }

    void setupTabButton() {

        Button btn_tab = findViewById(R.id.btn_tab);
        int tabCount = tabs.size();
        btn_tab.setText(String.valueOf((tabCount)));
        btn_tab.setOnClickListener(view -> showTabDialog());

    }

    private void loadBook(Intent intent) {
        loadIntentData(intent);
        loadOtherBookInfo();
        loadBookToView();
    }

    private void loadIntentData(Intent intent) {
        Bundle args = intent.getExtras();
        if (args != null) {
            bookid = args.getString("book_id");
            currentPage = args.getInt("current_page");
            searchText = args.getString("search_text");
            paragraphNumber = args.getInt("paragraph_number", 0);
        }
    }

    private void loadOtherBookInfo() {
        Book book = DBOpenHelper.getInstance(this).getBookInfo(bookid);
        bookName = book.getName();
        firstPage = book.getFirstPage();
        lastPage = book.getLastPage();

        if (currentPage == 0) {
            currentPage = firstPage;
        }

        if ( paragraphNumber != 0){
            currentPage = DBOpenHelper.getInstance(this).getPageNumber(bookid, paragraphNumber);
        }
        // get paragraph number
//        int[] paras = DBOpenHelper.getInstance(this).getParaRange(bookid);
        firstParagraph = DBOpenHelper.getInstance(this).getFirstParagraphNumber(bookid);
        lastParagraph = DBOpenHelper.getInstance(this).getLastParagraphNumber(bookid);
    }

    private void loadBookToView() {

        setTitle(MDetect.getDeviceEncodedText(bookName));

        pages = new ArrayList<>();
        pages = DBOpenHelper.getInstance(this).getPages(bookid);
        PageAdapter pageAdapter = new PageAdapter(context, pages, searchText);
        pageAdapter.notifyDataSetChanged();

        viewPager = findViewById(R.id.vpPager);
        viewPager.setAdapter(pageAdapter);
        if (currentPage != 0) {
            viewPager.setCurrentItem(currentPage - firstPage);
        } else {
            viewPager.setCurrentItem(0);
        }

        setupGotoButton();
        setupSeekBar();
        setupMoreButton();
        setupTabButton();

    }

    private boolean isExist(ArrayList<Tab> savedTabs) {

        if (savedTabs != null && !savedTabs.isEmpty()) {
            for (Tab tab : savedTabs) {
                if (tab.getBookID().equals(bookid) && tab.getCurrentPage() == currentPage) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getTabLocation() {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getBookID().equals(bookid) && tabs.get(i).getCurrentPage() == currentPage) {
                return i;
            }
        }
        return 0;
    }

    private void addToTab() {

        if (tabs.isEmpty()) {
            tabs.add(new Tab(bookid, bookName, currentPage));
            currentTabLocation = 0;
        } else {
            if (!isExist(tabs)) {
                ArrayList<Tab> temp = new ArrayList<>();
                temp.addAll(tabs);
                tabs.clear();
                tabs.add(new Tab(bookid, bookName, currentPage));
                tabs.addAll(temp);
                currentTabLocation = 0;
            } else {
                // update current tab location
                currentTabLocation = getTabLocation();
            }
        }

        updateToolbar();

    }

    private void updateCurrentTab() {
        tabs.set(currentTabLocation, new Tab(bookid, bookName, currentPage));
    }

    private void saveTab() {
        DBOpenHelper.getInstance(this).removeAllTab();
        DBOpenHelper.getInstance(this).addAllTab(tabs);
    }

/*    private ArrayList<Tab> getOtherSavedTab(ArrayList<Tab> savedTabs) {
        ArrayList<Tab> other = new ArrayList<>();
        other.addAll(savedTabs);
        for (int i = 0; i < savedTabs.size(); i++) {
            if (savedTabs.get(i).getBookID().equals(bookid) && savedTabs.get(i).getCurrentPage() == currentPage) {
                other.remove(i);
                break;
            }
        }
        return other;
    }*/

    @Override
    public void onSubmitGotoDialog(int input, int type) {

        int page = input;

        if (type == PARAGRAPH) {
            //page = getPageNumber(input);
            page = DBOpenHelper.getInstance(context).getPageNumber(bookid, input);
        }

        viewPager.setCurrentItem(page - firstPage);
    }


    @Override
    public void onTocItemClick(int page) {

        viewPager.setCurrentItem(page - firstPage);

    }

    private void showMoreBottomSheetDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("bookid", bookid);
        MoreBottomSheetDialogFragment dialogFragment = new MoreBottomSheetDialogFragment();
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "more");
    }


    private void updateToolbar() {
        Button btn_tab = findViewById(R.id.btn_tab);
        int tabCount = tabs.size();
        btn_tab.setText(String.valueOf((tabCount)));

        TextView textView = findViewById(R.id.tv_title_book_name);
        textView.setText(Rabbit.uni2zg(bookName));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void addToRecent(int pageNumber) {
        DBOpenHelper.getInstance(context).addToRecent(bookid, pageNumber);
    }

    private void addToBookmark(int pageNumber) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context, R.style.AlertDialogTheme);

        String message = "မှတ်လိုသောစာသား ရိုက်ထည့်ပါ။";
        String comfirm = "သိမ်းမယ်";
        String cancel = "မသိမ်းတော့ဘူး";
        if (!MDetect.isUnicode()) {
            message = Rabbit.uni2zg(message);
            comfirm = Rabbit.uni2zg(comfirm);
            cancel = Rabbit.uni2zg(cancel);
        }

        dialogBuilder.setMessage(message);
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        dialogBuilder.setMessage(message)
                .setView(input)
                .setCancelable(true)
                .setPositiveButton(comfirm,
                        (dialog, id) -> {
                            String note = input.getText().toString();
                            DBOpenHelper.getInstance(context).
                                    addToBookmark(note, bookid, pageNumber);
                            Snackbar.make(viewPager, MDetect.getDeviceEncodedText("သိမ်းမှတ်ပြီးပါပြီ။"), Snackbar.LENGTH_LONG).show();

                        })
                .setNegativeButton(cancel, (dialog, id) -> {
                });
        dialogBuilder.show();

        input.setOnFocusChangeListener((v, hasFocus) -> input.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }));
        input.requestFocus();

    }

    private void copyToClipboard() {

        String pageContent = pages.get(viewPager.getCurrentItem()).getContent();
        // remove html tag
        pageContent = pageContent.replaceAll("<[^>]*?>", "");
        if (!MDetect.isUnicode()) {
            pageContent = Rabbit.uni2zg(pageContent);
        }
        String plainText = pageContent.replaceAll("<.+?>", "");
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ပိဋက", plainText);
        clipboard.setPrimaryClip(clip);
        Snackbar.make(viewPager, MDetect.getDeviceEncodedText("ကော်ပီကူးယူပြီးပါပြီ။"), Snackbar.LENGTH_SHORT).show();

    }

    private void showTabDialog() {
        updateCurrentTab();
        Bundle args = new Bundle();
        args.putParcelableArrayList("tabs", tabs);

        FragmentManager fm = getSupportFragmentManager();
        TabManagementDialog tabManagementDialog = new TabManagementDialog();
        tabManagementDialog.setArguments(args);
        tabManagementDialog.show(fm, "tabs");
    }


/*    private void showSettingDialog() {
        FragmentManager fm = getSupportFragmentManager();
        SettingDialogFragment settingDialog = new SettingDialogFragment();
        settingDialog.show(fm, "Setting");
    }*/

    private void showTocDialog() {


        ArrayList<Toc> tocs = DBOpenHelper.getInstance(this).getToc(bookid);

        Bundle args = new Bundle();
        args.putParcelableArrayList("tocs", tocs);

        FragmentManager fm = getSupportFragmentManager();
        TocDialogFragment tocDialog = new TocDialogFragment();
        tocDialog.setArguments(args);
        tocDialog.show(fm, "TOC");
    }

    private void openNsy(String bookid, int pageNumber) {

        Bundle bundle = new Bundle();
        bundle.putString("book_id", bookid);
        bundle.putInt("page_number", pageNumber);
        Intent intent = null;
        String nsyCategory = bookid.split("_")[0];
        switch (nsyCategory) {
            case NSY_PALI:
                intent = new Intent("mm.pndaza.palitawnissaya.NsySelectActivity");
                break;
            case NSY_ATTHA:
                intent = new Intent("mm.pndaza.atthakathanissaya.NsySelectActivity");
                break;
            case NSY_TIKA:
                if (isExistTikaNsy(bookid)) {
                    intent = new Intent("mm.pndaza.tikanissaya.NsySelectActivity");
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(MDetect.getDeviceEncodedText("ယခုကျမ်းစာ၏နိဿယကို\nဆော့ဝဲလ်၌ မထည့်သွင်းရသေးပါ။"))
                            // A null listener allows the button to dismiss the dialog and take no further action.
                            .setPositiveButton("OK", null)
                            .show();
                }

                break;
            case NSY_ANNYA:
                if ((isExistInAtthaNsy(bookid))) {
                    intent = new Intent("mm.pndaza.atthakathanissaya.NsySelectActivity");
                } else if (isExistInTikaNsy(bookid)) {
                    intent = new Intent("mm.pndaza.tikanissaya.NsySelectActivity");
                } else {
                    showNoNsy();
                }
                break;
        }
        if (intent != null) {
            intent.putExtras(bundle);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                showNoNsyApp((nsyCategory));
            }
        }
    }

    private void openMmTranslation() {

        if (!DBOpenHelper.getInstance(context).isExistTranslationBook(bookid)) {
            Toast toast = Toast.makeText(context, MDetect.getDeviceEncodedText("ယခုကျမ်းစာအတွက် မြန်မာပြန် မရှိပါ။"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } else {
            Bundle args = new Bundle();
            args.putString("bookid", bookid);
            args.putInt("pagenumber", currentPage);

            FragmentManager fm = getSupportFragmentManager();
            GotoTranslationDialogFragment dialog = new GotoTranslationDialogFragment();
            dialog.setArguments(args);
            dialog.show(fm, "chooseParagraph");
        }
    }

    private boolean isExistTikaNsy(String bookid) {

        ArrayList<String> annyaWithNsy = new ArrayList<>(
                Arrays.asList(getResources().getStringArray(R.array.tika_with_nsy)));
        return annyaWithNsy.contains(bookid);
    }

    private boolean isExistInAtthaNsy(String bookid) {

        ArrayList<String> annyaWithNsy = new ArrayList<>(
                Arrays.asList(getResources().getStringArray(R.array.annya_with_nsy_attha_group)));
        return annyaWithNsy.contains(bookid);
    }

    private boolean isExistInTikaNsy(String bookid) {
        ArrayList<String> annyaWithNsy = new ArrayList<>(
                Arrays.asList(getResources().getStringArray(R.array.annya_with_nsy_tika_group)));
        return annyaWithNsy.contains(bookid);
    }

    private void showNoNsy() {
        new AlertDialog.Builder(this)
                .setMessage(MDetect.getDeviceEncodedText("ယခုကျမ်းစာ၏နိဿယကို ဆော့ဝဲလ်၌ မထည့်သွင်းရသေးပါ။\n(သို့)ယခုကျမ်းစာအတွက် နိဿယမရှိပါ။"))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setPositiveButton("OK", null)
                .show();
    }

    private void showNoNsyApp(String nsyCategory) {

        final String url;
        switch (nsyCategory) {
//            case NSY_PALI:
//                url = "http://www.mediafire.com/file/q8grhvlrvsgl0tw";
//                break;
            case NSY_ATTHA:
                url = "http://www.mediafire.com/file/yk0v37m04kjan8o";
                break;
            case NSY_TIKA:
                url = "http://www.mediafire.com/file/ez5pqqqtvgfj6mw";
                break;
            default:
                url = "http://www.mediafire.com/file/q8grhvlrvsgl0tw";
                break;
        }


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case DialogInterface.BUTTON_POSITIVE:
                        // User clicked the Yes button
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(browserIntent);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        // User clicked the No button
                        break;
                }
            }
        };
        new AlertDialog.Builder(this)
                .setMessage(MDetect.getDeviceEncodedText(getString(R.string.no_nsy_app)))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("⁠မဒေါင်းတော့ဘူး", dialogClickListener)
                .setPositiveButton("ဒေါင်းမယ်", dialogClickListener)
                .show();
    }

    private void showNoTranBook() {
        new AlertDialog.Builder(this)
                .setMessage(MDetect.getDeviceEncodedText("တိပိဋကမြန်မာပြန် ဆော့ဝဲလ် ထည့်သွင်းရန် လိုအပ်ပါသည်။"))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setPositiveButton("OK", null)
                .show();
    }


    private void showExplanation() {

        ArrayList<String> expBooks = DBOpenHelper.getInstance(context).getExplanationBooks(bookid);
        if (expBooks.size() <= 0) {
            Toast toast = Toast.makeText(context, MDetect.getDeviceEncodedText("ပါဠိမှအဋ္ဌကထာသို့ ကူးပြောင်းခြင်းကိုသာ ကြည့်ရှုနိုင်ပါတယ်။"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } else {
            Bundle args = new Bundle();
            args.putString("bookid", bookid);
            args.putInt("pagenumber", currentPage);

            FragmentManager fm = getSupportFragmentManager();
            GotoExplanationDialogFragment dialog = new GotoExplanationDialogFragment();
            dialog.setArguments(args);
            dialog.show(fm, "gotoParagraph");
        }

    }

    @Override
    public void onActionChoose(String action) {

        switch (action) {
            case "setting":
                Intent intent = new Intent(this, SettingActivity.class);
                startActivityForResult(intent, LAUNCH_SETTING_ACTIVITY);
//                showSettingDialog();
                break;
            case "copy":
                copyToClipboard();
                break;
            case "bookmark":
                addToBookmark(viewPager.getCurrentItem() + firstPage);
                break;
            case "explanation":
                showExplanation();
                break;
            case "nsy":
                openNsy(bookid, currentPage);
                break;
            case "mm_tran":
                openMmTranslation();
                break;
            case "toc":
                showTocDialog();
                break;
        }

    }

    @Override
    public void onAddNewTab() {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("TAB_MODE", true);
        startActivityForResult(intent, LAUNCH_MAIN_ACTIVITY);
    }

    @Override
    public void onTabSelect(String bookid, int currentPage) {

        Bundle args = new Bundle();
        args.putString("book_id", bookid);
        args.putInt("current_page", currentPage);
        args.putString("search_text", "");
        loadBook(new Intent().putExtras(args));
        currentTabLocation = getTabLocation();
        updateToolbar();
    }

    @Override
    public void onCancel() {

        if (tabs.isEmpty()) {
            addToTab();
        }
        updateToolbar();
    }

    @Override
    public void onClickParagraph(String bookid, int pageNumber) {
        Bundle args = new Bundle();
        args.putString("book_id", bookid);
        args.putInt("current_page", pageNumber);
        args.putString("search_text", "");
        loadBook(new Intent().putExtras(args));
        addToTab();
        saveTab();
        currentTabLocation = getTabLocation();
        updateToolbar();
    }


    @Override
    public void onChooseParagraph(int paragraph) {
        String tran_bookid = DBOpenHelper.getInstance(this).getTranslationBookID(bookid);
        Bundle bundle = new Bundle();
        bundle.putString("bookID", tran_bookid);
        bundle.putInt("paragraph", paragraph);
//        Log.d(TAG, "onChooseParagraph: " + tran_bookid);
//        Log.d(TAG, "onChooseParagraph: " + paragraph);
        Intent intent = new Intent("mm.pndaza.tipitakamyanmar.ReadBookActivity");
        if (intent != null) {
            intent.putExtras(bundle);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                showNoTranBook();
            }
        }
    }
}
