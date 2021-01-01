package mm.pndaza.tipitakapali.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.database.DBOpenHelper;

public class MoreBottomSheetDialogFragment extends BottomSheetDialogFragment
        implements View.OnClickListener {

    private ActionChooseListener mListener;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_more, container, false);
    }

    @Override public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.btn_setting).setOnClickListener(this);
        view.findViewById(R.id.btn_copy).setOnClickListener(this);
        view.findViewById(R.id.btn_bookmark).setOnClickListener(this);
        view.findViewById(R.id.btn_nsy).setOnClickListener(this);
        view.findViewById(R.id.btn_toc).setOnClickListener(this);
        view.findViewById(R.id.btn_explanation).setOnClickListener(this);
        view.findViewById(R.id.btn_mm_tran).setOnClickListener(this);


        Bundle bundle = getArguments();
        String bookid = bundle.getString("bookid");
        if(bookid != null){
            if( !isExistExpBook(bookid)){
                view.findViewById(R.id.btn_explanation).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActionChooseListener) {
            mListener = (ActionChooseListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ActionChooseListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.btn_setting:
                // do your code
                mListener.onActionChoose("setting");
                dismiss();
                break;

            case R.id.btn_copy:
                mListener.onActionChoose("copy");
                dismiss();
                break;

            case R.id.btn_bookmark:
                mListener.onActionChoose("bookmark");
                dismiss();
                break;

            case R.id.btn_explanation:
                mListener.onActionChoose("explanation");
                dismiss();
                break;

            case R.id.btn_nsy:
                mListener.onActionChoose("nsy");
                dismiss();
                break;

            case R.id.btn_mm_tran:
                mListener.onActionChoose("mm_tran");
                dismiss();
                break;

            case R.id.btn_toc:
                mListener.onActionChoose("toc");
                dismiss();
                break;

            default:
                break;
        }

    }

    public interface ActionChooseListener {
        void onActionChoose(String item);
    }


    private boolean isExistExpBook(String bookid){
        Cursor cursor = DBOpenHelper.getInstance(getContext()).getReadableDatabase().rawQuery(
                "SELECT exp from pali_attha_tika_match WHERE base = ?", new String[] {bookid});
        if( cursor != null && cursor.getCount() > 0){
            return true;
        }

        return false;
    }
}
