package mm.pndaza.tipitakapali.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import mm.pndaza.tipitakapali.R;
import mm.pndaza.tipitakapali.fragment.BookListFragment;
import mm.pndaza.tipitakapali.fragment.BookmarkFragment;
import mm.pndaza.tipitakapali.fragment.HomeFragment;
import mm.pndaza.tipitakapali.fragment.MoreFragment;
import mm.pndaza.tipitakapali.fragment.RecentFragment;
import mm.pndaza.tipitakapali.fragment.SearchFragment;
import mm.pndaza.tipitakapali.fragment.SuttaDialogFragment;
import mm.pndaza.tipitakapali.fragment.TocDialogFragment;
import mm.pndaza.tipitakapali.model.Sutta;
import mm.pndaza.tipitakapali.utils.MDetect;
import mm.pndaza.tipitakapali.utils.SharePref;

public class MainActivity extends AppCompatActivity implements
        BookListFragment.BookListFragmentListener,
        RecentFragment.OnRecentItemClickListener,
        BookmarkFragment.OnBookmarkItemClickListener,
        SuttaDialogFragment.SuttaDialogListener
{

    private static final String TAG = "MainActivity";
    private Boolean TAB_MODE = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (SharePref.getInstance(this).getPrefNightModeState()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(findViewById(R.id.toolbar));
        MDetect.init(this);
        setTitle(MDetect.getDeviceEncodedText(getString(R.string.app_name_mm)));

        // fix
//        DBOpenHelper.getInstance(this).createTabTable();

        Intent intent = getIntent();
        TAB_MODE = intent.getBooleanExtra("TAB_MODE", false);

        if (savedInstanceState == null) {
//            Log.d(TAG, "onCreate: " + " before commit homefragment");
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, new HomeFragment()).commit();
//            Log.d(TAG, "onCreate: " + "after commit homefragment");
        }

        BottomNavigationView navView = findViewById(R.id.navigation);

        navView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    selectedFragment = new HomeFragment();
                    break;
                case R.id.navigation_bookmark:
                    selectedFragment = new BookmarkFragment();
                    break;
                case R.id.navigation_recent:
                    selectedFragment = new RecentFragment();
                    break;
                case R.id.navigation_search:
                    selectedFragment = new SearchFragment();
                    break;
                case R.id.navigation_more:
                    selectedFragment = new MoreFragment();
                    break;
            }
            changeFragment(selectedFragment);
            return true;
        });

        KeyboardVisibilityEvent.setEventListener(
                this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
                        // some code depending on keyboard visiblity status
                        if(isOpen){
                        navView.setVisibility(View.GONE);}
                        else {
                            navView.setVisibility(View.VISIBLE);
                        }
                    }
                });
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

    private void changeFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_layout, fragment).commit();
    }


    @Override
    public void onBookmarkItemClick(String bookid, int pageNumber) {
        startReadBookActivity(bookid, pageNumber, "");
    }

    @Override
    public void onRecentItemClick(String bookid, int pageNumber) {
        startReadBookActivity(bookid, pageNumber, "");
    }


    @Override
    public void onBookItemClick(String bookid) {
        startReadBookActivity(bookid, 0, "");
    }

    private void startReadBookActivity(String bookid, int pageNumber, String queryWord) {

        Bundle args = new Bundle();
        args.putString("book_id", bookid);
        args.putInt("current_page", pageNumber);
        args.putString("search_text", queryWord);

        Intent intent = new Intent(this, BookReaderActivity.class);
        intent.putExtras(args);

        if(TAB_MODE){
            setResult(Activity.RESULT_OK,intent);
            finish();
        } else {
            startActivity(intent);
        }
    }

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_layout);
        if ( currentFragment instanceof HomeFragment) {
            finish();
        } else {
            BottomNavigationView navView = findViewById(R.id.navigation);
            navView.setSelectedItemId(R.id.navigation_home);
        }
        super.onBackPressed();
    }


    @Override
    public void onClickedSutta(Sutta sutta) {

        startReadBookActivity(sutta.getBookID(), sutta.getPageNumber(), sutta.getName());
    }
}
