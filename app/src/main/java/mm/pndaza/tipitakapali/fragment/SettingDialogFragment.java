package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;
import mm.pndaza.tipitakapali.utils.SharePref;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SettingDialogFragment extends DialogFragment {

    private View dlgView;
    private SharePref sharePref;

    private RadioGroup radioGroupTheme;
    private RadioGroup radioGroupFontStyle;
    private CheckBox checkBoxFontFix;
    private DiscreteSeekBar seekBar;
    private WebView sample_view;
    private Button btn_save;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getDialog().setCancelable(true);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dlgView = inflater.inflate(R.layout.dlg_setting, container, false);

        return dlgView;
    }

/*    @Override
    public void onResume() {
        super.onResume();
    }*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Context context = view.getContext();
        MDetect.init(context);
        if (!MDetect.isUnicode()) {
            changeTextEncoding();
        }

        radioGroupTheme = dlgView.findViewById(R.id.rg_theme);
        radioGroupFontStyle = dlgView.findViewById(R.id.rg_font_style);
        checkBoxFontFix = dlgView.findViewById(R.id.checkbox_font_fix);
        sample_view = view.findViewById(R.id.webview_sample);
        btn_save = view.findViewById(R.id.btn_save);
        seekBar = view.findViewById(R.id.fontsize_seekbar);
        seekBar.setMax(35);
        seekBar.setMin(5);

        loadSavedSettings();

        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                final WebSettings webSettings = sample_view.getSettings();
                webSettings.setDefaultFontSize(value);
                sharePref.setPrefFontSize(value);
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {

            }
        });

        radioGroupTheme.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            saveAndApplyTheme(checkedId);
        });

        radioGroupFontStyle.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            saveAndApplyTheme(checkedId);
        });


        checkBoxFontFix.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharePref.setPrefFontFix(isChecked);
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().recreate();
                dismiss();
            }
        });

    }

    private void loadSavedSettings() {

        sharePref = SharePref.getInstance(this.getContext());
        int fontSize = sharePref.getPrefFontSize();
        seekBar.setProgress(fontSize);

        boolean nightModeState = sharePref.getPrefNightModeState();
        if (nightModeState) {
            radioGroupTheme.check(R.id.radio_theme_night);
        } else {
            radioGroupTheme.check((R.id.radio_theme_day));
        }

        String fontStyle = sharePref.getPrefFontStyle();
        if(fontStyle.equals("unicode")){
            radioGroupFontStyle.check(R.id.radio_font_unicode);
        } else {
            radioGroupFontStyle.check(R.id.radio_font_zawgyi);
        }

        checkBoxFontFix.setChecked(sharePref.getPrefFontFix());

        StringBuilder sample = new StringBuilder();
        // add css style
        String cssFile = nightModeState ? "style_sample_night.css" : "style_sample.css";
        sample.append("<link rel=\"stylesheet\" href=\"")
                .append(cssFile)
                .append("\"s>")
                .append("<p class=\"sample\">နမူနာစာသား</p>");
        sample_view.loadDataWithBaseURL("file:///android_asset/web/",
                Rabbit.uni2zg(sample.toString()), "text/html", "UTF-8", null);
        final WebSettings webSettings = sample_view.getSettings();
        webSettings.setDefaultFontSize(fontSize);

    }


    private void saveAndApplyTheme(int checkedId) {
        boolean nightModeState = sharePref.getPrefNightModeState();
        switch (checkedId) {
            case R.id.radio_theme_day:
                sharePref.setPrefNightModeState(false);
                nightModeState = false;
                break;
            case R.id.radio_theme_night:
                sharePref.setPrefNightModeState(true);
                nightModeState = true;
                break;
            case R.id.radio_font_zawgyi:
                sharePref.setPrefFontStyle("zawgyi");
                break;
            case R.id.radio_font_unicode:
                sharePref.setPrefFontStyle("unicode");
                Log.d(TAG, "saveAndApplyTheme: unicode");
                break;
        }

        setTheme(nightModeState);
    }

    private void setTheme(boolean nightState) {
        if (nightState) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void changeTextEncoding() {
        // change display text for zawgyi device
        TextView tvSettingTitle = dlgView.findViewById(R.id.tv_settingTitle);
        TextView tvThemeTitle = dlgView.findViewById(R.id.tv_themeTitle);
        RadioButton rbThemeDay = dlgView.findViewById(R.id.radio_theme_day);
        RadioButton rbThemeNight = dlgView.findViewById(R.id.radio_theme_night);
        TextView tvFontStyleTitle = dlgView.findViewById(R.id.tv_font_style_title);
        RadioButton rbZawgyi = dlgView.findViewById(R.id.radio_font_zawgyi);
        RadioButton rbUnicode = dlgView.findViewById(R.id.radio_font_unicode);
        TextView tvFontSizeTitle = dlgView.findViewById(R.id.tv_fontSizeTitle);
        TextView tvFontSizeSmall = dlgView.findViewById(R.id.tv_fontSizeSmall);
        TextView tvFontSizeNormal = dlgView.findViewById(R.id.tv_fontSizeNormal);
        TextView tvFontSizeLarge= dlgView.findViewById(R.id.tv_fontSizeLarge);
        Button btn_save = dlgView.findViewById(R.id.btn_save);

/*        String settingTitle = getString(R.string.settingTitle);
        String themeTitle = getString(R.string.themeTitle);
        String themeDay = getString(R.string.themeDay);
        String themeNight = getString(R.string.themeNight);
        String fontSizeTitle = getString(R.string.fontSize);
        String fontSizeSmall = getString(R.string.fontSizeSmall);
        String fontSizeNormal = getString(R.string.fontSizeNormal);
        String fontSizeLarge = getString(R.string.fontSizeLarge);
        String save = getString(R.string.save);*/

        tvSettingTitle.setText(Rabbit.uni2zg(getString(R.string.search_title)));
        tvThemeTitle.setText(Rabbit.uni2zg(getString(R.string.themeTitle)));
        rbThemeDay.setText(Rabbit.uni2zg(getString(R.string.themeDay)));
        rbThemeNight.setText(Rabbit.uni2zg(getString(R.string.themeNight)));
        tvFontStyleTitle.setText(Rabbit.uni2zg(getString(R.string.fontStyleTitle)));
        rbZawgyi.setText(Rabbit.uni2zg(getString(R.string.zawgyi)));
        rbUnicode.setText(Rabbit.uni2zg(getString(R.string.unicode)));
        tvFontSizeTitle.setText(Rabbit.uni2zg(getString(R.string.fontSize)));
        tvFontSizeSmall.setText(Rabbit.uni2zg(getString(R.string.fontSizeSmall)));
        tvFontSizeNormal.setText(Rabbit.uni2zg(getString(R.string.fontSizeNormal)));
        tvFontSizeLarge.setText(Rabbit.uni2zg(getString(R.string.fontSizeLarge)));
        btn_save.setText(Rabbit.uni2zg(getString(R.string.save)));
    }

}
