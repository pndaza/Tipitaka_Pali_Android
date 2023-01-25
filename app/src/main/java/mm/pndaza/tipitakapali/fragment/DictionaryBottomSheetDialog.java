package mm.pndaza.tipitakapali.fragment;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.SearchView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.database.DBOpenHelper;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.PaliUtil;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SharePref;

public class DictionaryBottomSheetDialog extends BottomSheetDialogFragment {

    private String[] books = {"တိပိဋက ပါဠိ-မြန်မာ အဘိဓာန်", "ဦးဟုတ်စိန် ပါဠိ-မြန်မာအဘိဓာန်", "ဓာတွတ္ထပန်းကုံး", "ပါဠိဓာတ်အဘိဓာန်",
            "PTS Pali-English Dictionary", "Concise Pali-English Dictionary", "Pali-English Dictionary"};

    private WebView webView;
    private String stemWord = null;
    private SharePref sharePref;

    private boolean hide;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        sharePref = SharePref.getInstance(getContext());
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.dlg_dict, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        webView = view.findViewById(R.id.web_view);

        Bundle args = getArguments();
        String word = args.getString("word");
        hide = args.getBoolean("hide");     // from DictSearchFragment

//        Log.d("clicked Word: ",  word  + " count is " + word.length());

        // sometime first character - ​ေ ြ  of next word contain;
        word = word.replaceAll(" [\u1031\u103c]$", "");
        // get stemword(pakatirupa) from pada
//        Log.d("stemWord: ", "stem word of " + word + " is " + PaliUtil.getStemWord(word));

        String definition = getDefinition(word);
        String fontStyle = sharePref.getPrefFontStyle();
        if(fontStyle.equals("zawgyi")){
            definition = Rabbit.uni2zg(definition);
        }
        webView.loadDataWithBaseURL("file:///android_asset/web/", definition,
                "text/html", "UTF-8", null);
        final WebSettings webSettings = webView.getSettings();
        int fontSize = SharePref.getInstance(view.getContext()).getPrefFontSize();
        webSettings.setDefaultFontSize(fontSize);


        SearchView searchView = view.findViewById(R.id.sv_search);
        if (hide) {
            searchView.setVisibility(View.GONE);
        } else {
            searchView.setQuery(MDetect.getDeviceEncodedText(word), false);
            searchView.setIconified(false);
            webView.requestFocus(); // to hide soft keyboard
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String queryText) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (!MDetect.isUnicode()) {
                    query = Rabbit.zg2uni(query);
                }

                String definition = getDefinition(query);
                String fontStyle = sharePref.getPrefFontStyle();
                if(fontStyle.equals("zawgyi")){
                    definition = Rabbit.uni2zg(definition);
                }
                webView.loadDataWithBaseURL("file:///android_asset/web/", definition,
                        "text/html", "UTF-8", null);

                return false;
            }
        });


        AppCompatImageButton imageButton = view.findViewById(R.id.btn_tipi_dict);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isWordExist = DBOpenHelper.getInstance(getContext()).isExistInTipitakaAbidan(stemWord);
                if (isWordExist) {
                    Intent intent = new Intent("mm.pndaza.tipitakaabidan.ReaderActivity");
                    try {
                        intent.putExtra("lookup_word", PaliUtil.getStemWord(stemWord));
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        notExistTipiAbidanApp();
                    }
                } else {

                    Toast toast = Toast.makeText(getContext(),MDetect.getDeviceEncodedText("တိပိဋကပါဠိ-မြန်မာအဘိဓာန်၌ ယခုကြည့်လိုသောပုဒ် မပါဝင်ပါ။"), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    toast.show();
                }
            }
        });
    }

    private String getDefinition(String word) {

        if (!hide) {
            // not discrete dict search mode
            word = PaliUtil.getStemWord(word);
        }
        stemWord = word;

        String sql = "SELECT word, definition, book FROM dictionary WHERE word = '" + word + "' ORDER BY book ASC";
        SQLiteDatabase sqLiteDatabase = DBOpenHelper.getInstance(getContext()).getReadableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(sql, null);

        if (cursor == null || !cursor.moveToFirst()) {
            // does not match any
            // so change ending_vowel ( digha or rassa) and lookup again if match
            if (PaliUtil.isEndWithDigha(word)) {
                Log.d("stem Word: ", "rassa of " + word + " is " + PaliUtil.convertToRassa(word));
                word = PaliUtil.convertToRassa(word);
                stemWord = word;
                sql = "SELECT word, definition, book FROM dictionary WHERE word = '" + word + "' ORDER BY book ASC";
                sqLiteDatabase = DBOpenHelper.getInstance(getContext()).getReadableDatabase();
                cursor = sqLiteDatabase.rawQuery(sql, null);

            } else if (PaliUtil.isEndWithRassa(word)) {
                Log.d("stem Word: ", "digha of " + word + " is " + PaliUtil.convertToDigha(word));
                word = PaliUtil.convertToDigha(word);
                stemWord = word;
                sql = "SELECT word, definition, book FROM dictionary WHERE word = '" + "' ORDER BY book ASC";
                sqLiteDatabase = DBOpenHelper.getInstance(getContext()).getReadableDatabase();
                cursor = sqLiteDatabase.rawQuery(sql, null);
            }
        }

        String definitionData = getFormattedData(cursor);

        cursor.close();

        return definitionData;

    }

    private String getFormattedData(Cursor cursor) {

        String fontStyle = sharePref.getPrefFontStyle();
        String theme = sharePref.getPrefNightModeState()? "night_" : "";
        StringBuilder cssFileBuilder = new StringBuilder();
        cssFileBuilder.append("style_dict_");
        cssFileBuilder.append(theme);
        cssFileBuilder.append(fontStyle);
        cssFileBuilder.append(".css");

        StringBuilder dictData = new StringBuilder();
        // add css style
        dictData.append("<link rel=\"stylesheet\" href=\"")
                .append(cssFileBuilder.toString())
                .append("\"s>");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                dictData.append("<p class=\"book\">" +
                        books[cursor.getInt(cursor.getColumnIndex("book")) - 1] + "</p>");
                dictData.append(cursor.getString(cursor.getColumnIndex("definition")));
            } while (cursor.moveToNext());
        }

        return dictData.toString();
    }

    private void notExistTipiAbidanApp() {
        new AlertDialog.Builder(getContext())
                .setMessage(MDetect.getDeviceEncodedText("တိပိဋကအဘိဓာန်ဆော့ဝဲလ်ထည့်သွင်းရန် လိုအ်ပါသည်။"))
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setPositiveButton("OK", null)
                .show();
    }
}
