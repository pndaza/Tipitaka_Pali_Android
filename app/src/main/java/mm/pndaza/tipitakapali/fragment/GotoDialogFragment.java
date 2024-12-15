package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatDelegate;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.SharePref;

public class GotoDialogFragment extends DialogFragment {

    private static int firstPage;
    private static int lastPage;
    private static int firstParagraph;
    private static int lastParagraph;

    private static final int PAGE = 0;
    private static final int PARAGRAPH = 1;

    private GotoDialogListener listener;

    public interface GotoDialogListener {
        void onSubmitGotoDialog(int input, int type);
        void onNavigateToPage(int pageNumber);
        void onNavigateToParagraph(int paragraphNumber);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        Window window = getDialog().getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.requestFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.dlg_goto, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        if (context instanceof GotoDialogListener) {
            listener = (GotoDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement GotoDialogFragment.GotoDialogListener");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RadioButton btn_page = view.findViewById(R.id.radiobtn_page);
        RadioButton btn_para = view.findViewById(R.id.radiobtn_para);

        btn_page.setText(MDetect.getDeviceEncodedText(getString(R.string.page)));
        btn_para.setText(MDetect.getDeviceEncodedText(getString(R.string.paragraph)));

        Bundle args = getArguments();

        if (args != null) {
            firstPage = args.getInt("firstPage");
            lastPage = args.getInt("lastPage");
            firstParagraph = args.getInt("firstParagraph");
            lastParagraph = args.getInt("lastParagraph");
        }

        final EditText editText = view.findViewById(R.id.goto_num);
        final RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        final Button btn_go = view.findViewById(R.id.btn_go);
        final Button btn_cancel = view.findViewById(R.id.btn_cancel);

        btn_go.setText(MDetect.getDeviceEncodedText(getString(R.string.goto_go)));
        btn_cancel.setText(MDetect.getDeviceEncodedText(getString(R.string.goto_cancel)));

        // set hint for editText
        editText.setHint(MDetect.getDeviceEncodedText(
                String.format("(%d-%d) စာမျက်နှာ", firstPage, lastPage)));
        // show soft keyboard
        editText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);


        // change hint for editText
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if (i == R.id.radiobtn_page) {
                    editText.setHint(MDetect.getDeviceEncodedText(
                            String.format("(%d-%d) စာမျက်နှာ", firstPage, lastPage)));
                    editText.getText().clear();
                } else {
                    editText.setHint(MDetect.getDeviceEncodedText(
                            String.format("(%d-%d) စာပိုဒ်", firstParagraph, lastParagraph)));
                    editText.getText().clear();
                }
            }
        });

        // check input is valid and enable Go button
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                boolean isValidInput = false;
                int invalidColor = Color.parseColor("#969696");
                int validNightColor = Color.parseColor("#FF5823");
                int validDayColor = Color.parseColor("#E91E63");
                int color = invalidColor;

                String inputStr = charSequence.toString();
//                Log.v("onTextChanged", "input is - '" + inputStr + "' and count is " + inputStr.length());

                if (inputStr.length() == 0 || inputStr.length() > 4) {
                    isValidInput = false;
                } else {

                    int input = Integer.parseInt(inputStr);

                    int mode = radioGroup.getCheckedRadioButtonId();
                    switch (mode) {
                        case R.id.radiobtn_page:
                            isValidInput = isValidPageNumber(input);
                            break;
                        case R.id.radiobtn_para:
                            isValidInput = isValidParaNumber(input);
                            break;
                    }

                }

                if(isValidInput) {
                    color = SharePref.getInstance(getContext()).getPrefNightModeState() ? validNightColor : validDayColor;
                }
                btn_go.setEnabled(isValidInput);
                btn_go.setTextColor(color);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int selected = radioGroup.getCheckedRadioButtonId();
                int input = Integer.valueOf(editText.getText().toString().trim());

                if (selected == R.id.radiobtn_page){
                    listener.onNavigateToPage(input);
                } else{
                    listener.onNavigateToParagraph(input);
                }

                dismiss();
            }
        });
    }

    private boolean isValidPageNumber(int pageNumber) {
        return (pageNumber >= firstPage && pageNumber <= lastPage);
    }

    private boolean isValidParaNumber(int paraNumber) {
        return (paraNumber >= firstParagraph && paraNumber <= lastParagraph);
    }
}
