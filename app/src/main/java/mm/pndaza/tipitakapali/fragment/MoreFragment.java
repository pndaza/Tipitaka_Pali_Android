package mm.pndaza.tipitakapali.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;

import com.google.android.material.appbar.AppBarLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.activity.BookReaderActivity;
import mm.pndaza.tipitakapali.activity.SettingActivity;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.Rabbit;

public class MoreFragment extends ListFragment {


    private static Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        context = getContext();
        MDetect.init(context);
        getActivity().setTitle(MDetect.getDeviceEncodedText("နောက်ထပ်"));

        String[] mores = {"အပြင်အဆင်", "ကျေးဇူးတင်လွှာ", "လမ်းညွှန်", "ဗားရှင်း"};
        // convert to zawgyi
        for (int i = 0; i < mores.length; i++) {
            mores[i] = Rabbit.uni2zg(mores[i]);
        }

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getContext(), R.layout.simple_list_item, mores);
        setListAdapter(adapter);


        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

        AppBarLayout.LayoutParams params =
                (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
        params.setScrollFlags(0);

    }

    @Override
    public void onStart() {
        super.onStart();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(getActivity(), SettingActivity.class);
                        startActivity(intent);
//                        showSettingDialog();
                        break;
                    case 1:
                        showCreditDialog();
                        break;
                    case 2:
                        showHelpDialog();
                        break;
                    case 3:
                        showVersionDialog();
                        break;
                }
            }
        });
    }

    private void showVersionDialog() {
        VersionDialog infoDialog = new VersionDialog(getContext());
        infoDialog.show();
    }


    private void showCreditDialog() {

        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/web/credit.html");

       /* webView.loadDataWithBaseURL("file:///android_asset/web/credit.html",
                "", "text/html", "UTF-8", null);*/
        // display the WebView in an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(webView)
                .setCancelable(true)
                .setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .show();

    }

    private void showHelpDialog() {

        String data = "thank";

        WebView webView = new WebView(context);
        webView.loadUrl("file:///android_asset/web/help.html");

       /* webView.loadDataWithBaseURL("file:///android_asset/web/credit.html",
                "", "text/html", "UTF-8", null);*/
        // display the WebView in an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(webView)
                .setCancelable(true)
                .setPositiveButton("Dismiss", (dialog, which) -> dialog.dismiss())
                .show();

    }
}
