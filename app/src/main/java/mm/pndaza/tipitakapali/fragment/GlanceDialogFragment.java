package mm.pndaza.tipitakapali.fragment;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.model.Page;
import mm.pndaza.tipitakapali.model.ParagraphMapping;
import mm.pndaza.tipitakapali.repository.PageRepository;
import mm.pndaza.tipitakapali.utils.NumberUtil;
import mm.pndaza.tipitakapali.utils.SharePref;

public class GlanceDialogFragment extends BottomSheetDialogFragment {
    public ParagraphMapping mapping;

    public GlanceDialogFragment(ParagraphMapping mapping) {
        this.mapping = mapping;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.dlg_book_page
                , container, false);


    }

@Override
public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);

    AppCompatTextView title = view.findViewById(R.id.tv_title_book_name);

    WebView webView = view.findViewById(R.id.web_view);
    title.setText(mapping.toBookName);
    String pageContent = getPages();
    String style = getStyle();
    String formattedContent = formatContent(pageContent, style);
    webView.loadDataWithBaseURL("file:///android_asset/web/", formattedContent,
            "text/html", "UTF-8", null);

    Button close = view.findViewById(R.id.btn_close);
    close.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            dismiss();
        }
    });
}

String getPages(){
    PageRepository repository = new PageRepository(DBOpenHelper.getInstance(this.getContext()));
    ArrayList<Page> pages = repository.getPages(mapping.toBookId, mapping.toPageNumber, 2);

    StringBuilder builder = new StringBuilder();
    for( Page page: pages){
        builder.append("<p class=\"pageheader\">")
                .append(NumberUtil.toMyanmar(page.getPageNumber()))
                .append("</p>\n")
                .append(page.getContent())
                .append("\n<p>&nbsp;</p>");
    }

        return  builder.toString();
}

    private String getStyle() {

        SharePref sharePref = SharePref.getInstance(this
                .getContext());
        String theme = sharePref.getPrefNightModeState()? "night_" : "";
        String fontStyle = sharePref.getPrefFontStyle();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("style_");
        stringBuilder.append(theme);
        stringBuilder.append(fontStyle);
        stringBuilder.append(".css");

        return stringBuilder.toString();
    }

    private String formatContent(String content, String cssStyle) {

        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n")
                .append("<head>\n")
                .append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"></meta>\n")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n")
                .append("<link rel=\"stylesheet\" href=\"")
                .append(cssStyle)
                .append("\"s>")
                .append("<body>\n")
                .append(content)
                .append("<script type = \"text/javascript\" src=\"click.js\"></script>")
                .append("\n</body>\n</html>");

        return sb.toString();
    }
}