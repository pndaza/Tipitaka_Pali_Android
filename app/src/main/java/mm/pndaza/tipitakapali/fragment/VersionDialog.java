package mm.pndaza.tipitakapali.fragment;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import mm.pndaza.tipitakapali.R;

public class VersionDialog extends Dialog {
    private final Context context;

    public VersionDialog(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dlg_version);

        // Set the dialog width to match the parent
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        WebView webView = findViewById(R.id.versionWebView);
        String htmlContent = loadHtmlFromAsset("version.html");
        String customCss = "<style>body { font-size: 20px; }</style>"; // Custom CSS for font size
        String fullHtmlContent = "<html><head>" + customCss + "</head><body>" + htmlContent + "</body></html>";
        webView.loadDataWithBaseURL(null, fullHtmlContent, "text/html", "UTF-8", null);


        Button closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private String loadHtmlFromAsset(String fileName) {
        StringBuilder htmlContent = new StringBuilder();
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String str;
            while ((str = br.readLine()) != null) {
                htmlContent.append(str);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return htmlContent.toString();
    }
}
