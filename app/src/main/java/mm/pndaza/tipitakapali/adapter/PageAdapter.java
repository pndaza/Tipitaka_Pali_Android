package mm.pndaza.tipitakapali.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.activity.BookReaderActivity;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.fragment.DictionaryBottomSheetDialog;
import mm.pndaza.tipitakapali.model.Page;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SharePref;

public class PageAdapter extends PagerAdapter {

    private static final String TAG = "PageAdapter";
    Context context;
    LayoutInflater layoutInflater;
    ArrayList<Page> pages;
    String queryWord;
    boolean bar_status;
    LinearLayout book_toolbar;

    private static int fontSize;

    private static String style;
    private final String GOTO_ID = "goto_001";


    public PageAdapter(Context context, ArrayList<Page> pages, String queryWord) {
        this.context = context;
        this.pages = pages;
        this.queryWord = queryWord;
        bar_status = true;
        layoutInflater = LayoutInflater.from(this.context);
        style = getStyle();
        Log.d(TAG, "PageAdapter: " + queryWord);
        book_toolbar = ((BookReaderActivity) context).findViewById(R.id.control_bar);
        fontSize = SharePref.getInstance(context).getPrefFontSize();
    }

    // Returns the number of pages to be displayed in the ViewPager.
    @Override
    public int getCount() {
        return pages.size();
    }

    // Returns true if a particular object (page) is from a particular page
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    // This method should create the page for the given position passed to it as an argument.
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        // Inflate the layout for the page
        View itemView = layoutInflater.inflate(R.layout.page, container, false);

        String content = pages.get(position).getContent();
        if (content == null) {
            Log.d(TAG, "instantiateItem: page id" + pages.get(position).get_id());
            content = DBOpenHelper.getInstance(context).getPageContent(pages.get(position).get_id());
            pages.get(position).setContent(content);
        }

        if (queryWord != null) {
            if (queryWord.length() > 0) {
                content = setHighlight(content, queryWord);
            }
        }

        int pageNumber = pages.get(position).getPageNumber();
        String formattedContent = formatContent(content, style, pageNumber);
        String fontStyle = SharePref.getInstance(context).getPrefFontStyle();
        if (fontStyle.equals("zawgyi")){
            formattedContent = Rabbit.uni2zg(formattedContent);
            if(SharePref.getInstance(context).getPrefFontFix()){
                formattedContent =  formattedContent.replaceAll("([\u1031\u103b\u107e\u1080])", "$1\u2060");
            }
        }

//        formattedContent =  formattedContent.replaceAll("([\u1031\u103b\u107e\u1080])", "$1\u2060");

        //find and populate data into webview
        WebView webView = itemView.findViewById(R.id.wv_page);
        webView.loadDataWithBaseURL("file:///android_asset/web/",
                formattedContent, "text/html", "UTF-8", null);
        webView.setScrollbarFadingEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebViewInterface(), "ReadBookInterface");

        final WebSettings webSettings = webView.getSettings();
//        String fontSize = SharePref.getInstance(context).getPrefFontSize();
        // Set the font size (in sp).
        webSettings.setDefaultFontSize(fontSize);

        // Add the page to the container
        container.addView(itemView);

        if (formattedContent.contains(GOTO_ID)) {
            webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
//                webView.loadUrl("javascript:scrollAnchor(" + id + ");");
                    webView.loadUrl("javascript:document.getElementById(\"" + GOTO_ID + "\").scrollIntoView()");
                }
            });
        }

        // Return the page
        return itemView;
    }

    // Removes the page from the container for the given position.
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private String formatContent(String content, String cssStyle, int pagenum) {

        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n")
                .append("<head>\n")
                .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta>\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<link rel=\"stylesheet\" href=\"")
                .append(cssStyle)
                .append("\"s>")
                .append("<body>\n")
                .append("<p class=\"pageheader\">")
                .append(NumberUtil.toMyanmar(pagenum))
                .append("</p>")
                .append(content)
                .append("\n<p>&nbsp;</p>")
                .append("<script type = \"text/javascript\" src=\"click.js\"></script>")
                .append("\n</body>\n</html>");

        return sb.toString();
    }

    private String getStyle() {

        SharePref sharePref = SharePref.getInstance(context);
        String theme = sharePref.getPrefNightModeState()? "night_" : "";
        String fontStyle = sharePref.getPrefFontStyle();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("style_");
        stringBuilder.append(theme);
        stringBuilder.append(fontStyle);
        stringBuilder.append(".css");

        return stringBuilder.toString();
    }

    public void updateHighlightedText(String textToHighlight){
        queryWord = textToHighlight;
    }
    private String setHighlight(String content, String textToHighlight) {

        // TODO optimize highlight for some query text
        String highlightedText = "<span class = \"highlighted\">" + textToHighlight + "</span>";
        Boolean found = content.contains(textToHighlight);

        if ( !found ) {
            Log.d("if not found highlight", "yes");
            // removing တိ at end
             String trimHighlight = textToHighlight.replaceAll("(န္တိ|တိ)$", "");
            highlightedText = "<span class = \"highlighted\">" + trimHighlight + "</span>";

            content = content.replace(trimHighlight, highlightedText);
            content = content.replaceFirst(
                    "<span class = \"highlighted\">", "<span id=\"goto_001\" class=\"highlighted\">");

            return content;

        }
        content = content.replace(textToHighlight, highlightedText);
        content = content.replaceFirst(
                "<span class = \"highlighted\">", "<span id=\"goto_001\" class=\"highlighted\">");
        return content;
    }

    class WebViewInterface {

        @JavascriptInterface
        public void showDict(String msg) {
            // TODO: to display dictionary

//            Toast.makeText(context, Rabbit.zg2uni(msg.replaceAll("\u2060","")), Toast.LENGTH_LONG).show();

            Bundle args = new Bundle();
            String fontStyle = SharePref.getInstance(context).getPrefFontStyle();
            if(fontStyle.equals("zawgyi")){
                msg = Rabbit.zg2uni(msg.replaceAll("\u2060",""));
            }
            args.putString("word", msg);

            DictionaryBottomSheetDialog dialog = new DictionaryBottomSheetDialog();
            dialog.setArguments(args);
            dialog.show(((AppCompatActivity)context).getSupportFragmentManager(), "Dictionary");


        }

        @JavascriptInterface
        public void showHideBars() {
            ((Activity) context).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (bar_status) {
                        bar_status = false;
                        ((BookReaderActivity) context).getSupportActionBar().hide();
                        book_toolbar.setVisibility(View.GONE);
                    } else {
                        bar_status = true;
                        ((BookReaderActivity) context).getSupportActionBar().show();
                        book_toolbar.setVisibility(View.VISIBLE);
                    }
                }
            });


        }
    }
}
