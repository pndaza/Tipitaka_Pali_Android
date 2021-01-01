package mm.pndaza.tipitakapali.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.utils.SharePref;

public class AddBookmarkFragment extends DialogFragment {
    View dlgView;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        getDialog().setCancelable(false);
        dlgView = inflater.inflate(R.layout.dlg_addbookmark, container, false);

        return dlgView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initUI();
    }

    private void initUI() {

        SharePref sharePref = SharePref.getInstance(this.getContext());
        int fontSize = sharePref.getPrefFontSize();
        boolean nightModeState = sharePref.getPrefNightModeState();
    }

}
