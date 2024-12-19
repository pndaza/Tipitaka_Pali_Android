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
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.adapter.PageAdapter;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.fragment.GlanceDialogFragment;
import mm.pndaza.tipitakapali.fragment.GotoDialogFragment;
import mm.pndaza.tipitakapali.fragment.GotoExplanationDialogFragment;
import mm.pndaza.tipitakapali.fragment.GotoTranslationDialogFragment;
import mm.pndaza.tipitakapali.fragment.MoreBottomSheetDialogFragment;
import mm.pndaza.tipitakapali.fragment.TabManagementDialog;
import mm.pndaza.tipitakapali.fragment.TocDialogFragment;
import mm.pndaza.tipitakapali.model.Book;
import mm.pndaza.tipitakapali.model.Page;
import mm.pndaza.tipitakapali.model.Paragraph;
import mm.pndaza.tipitakapali.model.ParagraphMapping;
import mm.pndaza.tipitakapali.model.Tab;
import mm.pndaza.tipitakapali.model.Toc;
import mm.pndaza.tipitakapali.repository.MyanmarBookRepository;
import mm.pndaza.tipitakapali.repository.NissayaRepository;
import mm.pndaza.tipitakapali.repository.PageMapRepository;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SharePref;


public class BookReaderActivity extends AppCompatActivity
        implements GotoDialogFragment.GotoDialogListener,
        TocDialogFragment.TocDialogListener,
        MoreBottomSheetDialogFragment.ActionChooseListener,
        TabManagementDialog.TabManagementListener,
        GotoExplanationDialogFragment.GotoExplanationDialogListener,
        GotoTranslationDialogFragment.GotoTranslationDialogListener {

    private static final int LAUNCH_MAIN_ACTIVITY = 1;
    private static final int LAUNCH_SETTING_ACTIVITY = 2;

    private final Context context = BookReaderActivity.this;

    private ArrayList<Page> pages;
    private static ViewPager viewPager;
    private PageAdapter pageAdapter;
    private String bookID;
    private String bookName;
    private int firstPage;
    private int lastPage;
    private int currentPage = 1;
    private String searchText = "";
    private int firstParagraph = 0;
    private int lastParagraph = 0;
    private int paragraphNumber;
    private int paragraphIndex;

    private boolean isOpenedByDeepLink = false;
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

        if (SharePref.getInstance(this).getPrefNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

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
    protected void onResume() {
        if (SharePref.getInstance(this).getPrefNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isOpenedByDeepLink && isTaskRoot()) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        super.onBackPressed();

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

    private void handleIntent(Intent intent) {
        if (intent.getData() != null) {
            handleDeepLink(intent);
        } else if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            bookID = bundle.getString("book_id");
            currentPage = bundle.getInt("current_page");
            searchText = bundle.getString("search_text");
            paragraphNumber = bundle.getInt("paragraph_number", 0);
            isOpenedByDeepLink = bundle.getBoolean("deeplink", false);

        }
    }

    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        if (data != null) {
//            String scheme = data.getScheme();
//            String host = data.getHost();
//            String path = data.getPath();

            // Parse the query parameters
            bookID = data.getQueryParameter("id");
            String paragraphStr = data.getQueryParameter("paragraph");
            String paragraphIndexStr = data.getQueryParameter("paragraph_index");
            String pageNumber = data.getQueryParameter("page");
            if (paragraphStr != null && paragraphIndexStr != null) {
                try {

                    paragraphNumber = Integer.parseInt(paragraphStr);
                    paragraphIndex = Integer.parseInt(paragraphIndexStr);
                    Log.d("onCreate:", "from deeplink- " + "bookId: " + bookID
                            + " paragraph: " + paragraphNumber + " paragraphIndex: " + paragraphIndex);
                } catch (NumberFormatException e) {
                    Log.e("DeepLink", "Invalid page number: " + paragraphStr, e);
                    // Handle the error (e.g., show an error message to the user)
                }
            } else if (pageNumber != null) {
                currentPage = Integer.parseInt(pageNumber);
            } else {
                Log.e("DeepLink", "Invalid deep link parameters");
                // Handle the error (e.g., show an error message to the user)
            }

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
        handleIntent(intent);
        loadOtherBookInfo();
        loadBookToView();
    }

//    private void loadIntentData(Intent intent) {
//        Bundle args = intent.getExtras();
//        if (args != null) {
//            bookID = args.getString("book_id");
//            currentPage = args.getInt("current_page");
//            searchText = args.getString("search_text");
//            paragraphNumber = args.getInt("paragraph_number", 0);
//            isOpenedByDeepLink = args.getBoolean("deeplink", false);
//        }
//    }

    private void loadOtherBookInfo() {
        Book book = DBOpenHelper.getInstance(this).getBookInfo(bookID);
        bookName = book.getName();
        firstPage = book.getFirstPage();
        lastPage = book.getLastPage();

        if (currentPage == 0) {
            currentPage = firstPage;
        }

        if (paragraphNumber != 0) {
            currentPage = DBOpenHelper.getInstance(this).getPageNumber(bookID, paragraphNumber);
            searchText = NumberUtil.toMyanmar(paragraphNumber);
        }
        // get paragraph number
//        int[] paras = DBOpenHelper.getInstance(this).getParaRange(bookid);
        firstParagraph = DBOpenHelper.getInstance(this).getFirstParagraphNumber(bookID);
        lastParagraph = DBOpenHelper.getInstance(this).getLastParagraphNumber(bookID);
    }

    private void loadBookToView() {

        setTitle(MDetect.getDeviceEncodedText(bookName));

        pages = new ArrayList<>();
        pages = DBOpenHelper.getInstance(this).getPages(bookID);
        pageAdapter = new PageAdapter(context, pages, searchText, currentPage);
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
                if (tab.getBookID().equals(bookID) && tab.getCurrentPage() == currentPage) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getTabLocation() {
        for (int i = 0; i < tabs.size(); i++) {
            if (tabs.get(i).getBookID().equals(bookID) && tabs.get(i).getCurrentPage() == currentPage) {
                return i;
            }
        }
        return 0;
    }

    private void addToTab() {

        if (tabs.isEmpty()) {
            tabs.add(new Tab(bookID, bookName, currentPage));
            currentTabLocation = 0;
        } else {
            if (!isExist(tabs)) {
                ArrayList<Tab> temp = new ArrayList<>();
                temp.addAll(tabs);
                tabs.clear();
                tabs.add(new Tab(bookID, bookName, currentPage));
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
        tabs.set(currentTabLocation, new Tab(bookID, bookName, currentPage));
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
            page = DBOpenHelper.getInstance(context).getPageNumber(bookID, input);
        }
        pageAdapter.updatePageToHighlight(page);
        pageAdapter.updateHighlightedText(NumberUtil.toMyanmar(input));
        viewPager.setCurrentItem(page - firstPage);
    }

    @Override
    public void onNavigateToPage(int pageNumber) {
        pageAdapter.updatePageToHighlight(pageNumber);
        pageAdapter.updateHighlightedText(NumberUtil.toMyanmar(pageNumber));
        viewPager.setCurrentItem(pageNumber - firstPage);
    }

    @Override
    public void onNavigateToParagraph(int paragraphNumber) {

        int pageNumber = DBOpenHelper.getInstance(context).getPageNumber(bookID, paragraphNumber);
        Log.d(TAG, "onNavigateToParagraph: para: " + paragraphNumber + " - page: " + pageNumber);
        pageAdapter.updatePageToHighlight(pageNumber);
        pageAdapter.updateHighlightedText(NumberUtil.toMyanmar(paragraphNumber));
        viewPager.setCurrentItem(pageNumber - firstPage);
    }


    @Override
    public void onTocItemClick(int page, String title) {

        String textToHighlight = title;
        textToHighlight = textToHighlight.replaceAll("[၀-၉]+။ ", "");
        textToHighlight = textToHighlight.replaceAll("\\[[၀-၉]+\\]", "");
        textToHighlight = textToHighlight.trim();
        Log.d(TAG, "onTocItemClick: " + textToHighlight);
        pageAdapter.updatePageToHighlight(page);
        pageAdapter.updateHighlightedText(textToHighlight);
        viewPager.setCurrentItem(page - firstPage);

    }

    private void showMoreBottomSheetDialog() {
        Bundle bundle = new Bundle();
        bundle.putString("bookid", bookID);
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
        DBOpenHelper.getInstance(context).addToRecent(bookID, pageNumber);
    }

    private void addToBookmark(int pageNumber) {

        AlertDialog.Builder dialogBuilder = new MaterialAlertDialogBuilder(context, R.style.AlertDialogTheme);

        String message = "မှတ်လိုသောစာသား ရိုက်ထည့်ပါ။";
        String comfirm = "သိမ်းမယ်";
        String cancel = "မသိမ်းတော့ဘူး";
        MDetect.init(this);
        if (!MDetect.isUnicode()) {
            message = Rabbit.uni2zg(message);
            comfirm = Rabbit.uni2zg(comfirm);
            cancel = Rabbit.uni2zg(cancel);
        }

        dialogBuilder.setMessage(message);
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        input.setLayoutParams(lp);
//        input.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        dialogBuilder.setMessage(message)
                .setView(input)
                .setCancelable(true)
                .setPositiveButton(comfirm,
                        (dialog, id) -> {
                            String note = input.getText().toString();
                            DBOpenHelper.getInstance(context).
                                    addToBookmark(note, bookID, pageNumber);
                            Snackbar.make(viewPager, MDetect.getDeviceEncodedText("သိမ်းမှတ်ပြီးပါပြီ။"), Snackbar.LENGTH_LONG).show();

                        })
                .setNegativeButton(cancel, (dialog, id) -> {
                });
        AlertDialog dialog = dialogBuilder.show();
        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM);

        input.setOnFocusChangeListener((v, hasFocus) -> input.post(() -> {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }));
        input.requestFocus();

    }

    private void sendReport(String bookName, int pageNumber) {

        String formUrl = String.format(
                Locale.US,
                "https://docs.google.com/forms/d/e/1FAIpQLSdNiEAo_NKshXt8pR5Qd5NBrmUV6SAeHBHj2KIm0P8c7W-FLg/viewform?usp=pp_url&entry.954794219=%s&entry.1035874146=%d", bookName, pageNumber);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(formUrl));
        startActivity(intent);

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


        ArrayList<Toc> tocs = DBOpenHelper.getInstance(this).getToc(bookID);

        Bundle args = new Bundle();
        args.putParcelableArrayList("tocs", tocs);

        FragmentManager fm = getSupportFragmentManager();
        TocDialogFragment tocDialog = new TocDialogFragment();
        tocDialog.setArguments(args);
        tocDialog.show(fm, "TOC");
    }

    private void openNsy(String bookId, int pageNumber) {

        NissayaRepository nissayaRepository = new NissayaRepository(DBOpenHelper.getInstance(context));

        if (!nissayaRepository.haveNissaya(bookId)) {
            new AlertDialog.Builder(this)
                    .setMessage(MDetect.getDeviceEncodedText("ယခုကျမ်းစာ၏ နိဿယ မရှိပါ။\n" +
                            "သို့မဟုတ်\nယခုကျမ်းစာ၏နိဿယကို\nဆော့ဝဲလ်၌ မထည့်သွင်းရသေးပါ။"))
                    // A null listener allows the button to dismiss the dialog and take no further action.
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }
        String basket = nissayaRepository.getBasket(bookId);
        Bundle bundle = new Bundle();
        bundle.putString("book_id", bookId);
        bundle.putInt("page_number", pageNumber);
        bundle.putBoolean("deeplink", true);
        Intent intent = null;
        // for deep link
        String scheme = "";
        String host = "";
        switch (basket) {
            case NSY_PALI:
                intent = new Intent("mm.pndaza.palitawnissaya.NsySelectActivity");
                scheme = "palinissaya";
                host = "mm.pndaza.palinissaya";
                break;
            case NSY_ATTHA:
                intent = new Intent("mm.pndaza.atthakathanissaya.NsySelectActivity");
                scheme = "atthanissay";
                host = "mm.pndaza.atthanissaya";
                break;
            case NSY_TIKA:
                intent = new Intent("mm.pndaza.tikanissaya.NsySelectActivity");
                scheme = "tikanissaya";
                host = "mm.pndaza.tikanissaya";

                break;
            case NSY_ANNYA:
                intent = new Intent("mm.pndaza.annyanissaya.NsySelectActivity");
                scheme = "annyanissaya";
                host = "mm.pndaza.annyanissaya";
                break;
        }

        if (intent != null) {
            intent.putExtras(bundle);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(intent);
                Log.d(TAG, "nissaya app is successfully opened  using intent of activity name");
            } catch (ActivityNotFoundException e) {
                Log.d(TAG, "cannot open nissaya app using intent of activity");
                Log.d(TAG, "now trying to open nissaya app using deeplink");
//                String  scheme = "tikanissaya";
//                String host = "mm.pndaza.tikanissaya";
                String path = "open";
                Uri deepLinkUri = new Uri.Builder()
                        .scheme(scheme)
                        .authority(host)
                        .path(path)
                        .appendQueryParameter("id", this.bookID)
                        .appendQueryParameter("page", String.valueOf(pageNumber))
                        .build();
                Log.d(TAG, "uri: " + deepLinkUri.toString());
                Intent deepLinktIntent = new Intent(Intent.ACTION_VIEW, deepLinkUri);
                deepLinktIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (deepLinktIntent.resolveActivity(getPackageManager()) != null) {
                    context.startActivity(deepLinktIntent);
                } else {
                    System.out.println("No app found to handle this deep link");
                    showNoNsyApp((basket));
                }
            }
        }

    }

    private void openMmTranslation() {
        MyanmarBookRepository myanmarBookRepository = new MyanmarBookRepository(DBOpenHelper.getInstance(context));
        if (!myanmarBookRepository.haveTranslation(bookID)) {
            Toast toast = Toast.makeText(context, MDetect.getDeviceEncodedText("ယခုကျမ်းစာအတွက် မြန်မာပြန် မရှိပါ။"
                    + "(သို့မဟုတ်) ဆော့ဝဲ၌ မထည့်ရသေးပါ။"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            return;
        }

        String linkType = myanmarBookRepository.getLinkType(bookID);
        if(linkType.equals("page")){
            String myanmarBookId = myanmarBookRepository.getMyanmarBookID(bookID, currentPage);
            PageMapRepository pageMapRepository = new PageMapRepository(DBOpenHelper.getInstance(this));
            int mmPageNumber = pageMapRepository.getMyanmarPageNumber(bookID, currentPage);
            if(mmPageNumber == 0 ){
                showAlertDialog("ယခုစာမျက်နှာ၏ မြန်မာပြန်မရှိပါ။");
                return;
            }
            String scheme = "tipitakamyanmar";
            String host = "mm.pndaza.tipitakamyanmar";
            String path = "open";
            Uri deepLinkUri = new Uri.Builder()
                    .scheme(scheme)
                    .authority(host)
                    .path(path)
                    .appendQueryParameter("id", myanmarBookId)
                    .appendQueryParameter("page", String.valueOf(mmPageNumber))
                    .build();
            Log.d(TAG, "uri: page number " + mmPageNumber);
            Log.d(TAG, "uri: " + deepLinkUri.toString());
            Intent deepLinktIntent = new Intent(Intent.ACTION_VIEW, deepLinkUri);
            deepLinktIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            if (deepLinktIntent.resolveActivity(getPackageManager()) != null) {
                context.startActivity(deepLinktIntent);
            } else {
                Log.d("Exception", "No app found to handle this deep link");
                showNoTranBook();
            }
            return;
        }


        Bundle args = new Bundle();
        args.putString("bookid", bookID);
        args.putInt("pagenumber", currentPage);

        FragmentManager fm = getSupportFragmentManager();
        GotoTranslationDialogFragment dialog = new GotoTranslationDialogFragment();
        dialog.setArguments(args);
        dialog.show(fm, "chooseParagraph");

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
            case NSY_PALI:
                url = "https://play.google.com/store/apps/details?id=mm.pndaza.palitawnissaya";
                break;
            case NSY_ATTHA:
                url = "https://play.google.com/store/apps/details?id=mm.pndaza.atthakathanissaya";
                break;
            case NSY_TIKA:
                url = "https://play.google.com/store/apps/details?id=mm.pndaza.tikanissaya";
                break;
            case NSY_ANNYA:
                url = "https://play.google.com/store/apps/details?id=mm.pndaza.annyanissaya";
                break;
            default:
                url = "";
                break;
        }


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
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
                .setNegativeButton("မဒေါင်းတော့ဘူး", dialogClickListener)
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

    private void showAlertDialog(String message) {
        new AlertDialog.Builder(this)
                .setMessage(MDetect.getDeviceEncodedText(message))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setPositiveButton("OK", null)
                .show();
    }


    private void showExplanation() {

        ArrayList<String> expBooks = DBOpenHelper.getInstance(context).getExplanationBooks(bookID);
        if (expBooks.size() <= 0) {
            Toast toast = Toast.makeText(context, MDetect.getDeviceEncodedText("ပါဠိမှအဋ္ဌကထာသို့ ကူးပြောင်းခြင်းကိုသာ ကြည့်ရှုနိုင်ပါတယ်။"), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        } else {
            Bundle args = new Bundle();
            args.putString("bookid", bookID);
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

            case "report":
                sendReport(bookName, currentPage);
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
                openNsy(bookID, currentPage);
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
    public void onClickParagraph(ParagraphMapping mapping, boolean glanceMode) {
        Bundle args = new Bundle();
        args.putString("book_id", mapping.toBookId);
        args.putInt("current_page", mapping.toPageNumber);
        args.putString("search_text", NumberUtil.toMyanmar(mapping.paragraphNumber));
        if (glanceMode) {
            GlanceDialogFragment dialog = new GlanceDialogFragment(mapping);
            dialog.show(getSupportFragmentManager(), "Glance Dialog");
            return;
        }
        loadBook(new Intent().putExtras(args));
        addToTab();
        saveTab();
        currentTabLocation = getTabLocation();
        updateToolbar();
    }


    @Override
    public void onChooseParagraph(Paragraph paragraph) {
        MyanmarBookRepository repository = new MyanmarBookRepository(DBOpenHelper.getInstance(this));
        String myanmarBookId = repository.getMyanmarBookID(bookID, currentPage);
        /*
       // open using bundle
        Bundle bundle = new Bundle();
        bundle.putString("bookID", tran_bookid);
        bundle.putInt("paragraph", paragraph.number);
        bundle.putInt("paragraphIndex", paragraph.index);
        Log.d(TAG, "onChooseParagraph: index: " + paragraph.index);
        bundle.putBoolean("deeplink", true);
//        Log.d(TAG, "onChooseParagraph: " + tran_bookid);
//        Log.d(TAG, "onChooseParagraph: " + paragraph);
        Intent intent = new Intent("mm.pndaza.tipitakamyanmar.ReadBookActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtras(bundle);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            showNoTranBook();
        }
        */
        // launch using custom url scheme
        String scheme = "tipitakamyanmar";
        String host = "mm.pndaza.tipitakamyanmar";
        String path = "open";
        Uri deepLinkUri = new Uri.Builder()
                .scheme(scheme)
                .authority(host)
                .path(path)
                .appendQueryParameter("id", myanmarBookId)
                .appendQueryParameter("paragraph", String.valueOf(paragraph.number))
                .appendQueryParameter("paragraph_index", String.valueOf(paragraph.index))
                .build();
        Log.d(TAG, "uri: " + deepLinkUri.toString());
        Intent deepLinktIntent = new Intent(Intent.ACTION_VIEW, deepLinkUri);
        deepLinktIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (deepLinktIntent.resolveActivity(getPackageManager()) != null) {
            context.startActivity(deepLinktIntent);
        } else {
            Log.d("Exception", "No app found to handle this deep link");
            showNoTranBook();
        }
    }
}
