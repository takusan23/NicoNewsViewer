package io.github.takusan23.niconewsviewer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.preference.PreferenceManager;
import android.view.View;

import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.Menu;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DarkModeSupport darkModeSupport;
    private SharedPreferences pref_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);

        darkModeSupport = new DarkModeSupport(this);
        darkModeSupport.setActivityTheme(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        //最後開いたのを読み込む
        if (pref_setting.getString("last_page_url", null) != null) {
            setNewsFragment(pref_setting.getString("last_page_url", null), pref_setting.getString("last_page_category", null));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        menu.findItem(R.id.offline_mode).setChecked(pref_setting.getBoolean("offline_mode", false));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        SharedPreferences.Editor editor = pref_setting.edit();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.offline_menu:
                OfflineNews offlineNews = new OfflineNews(this);
                break;
            case R.id.offline_mode:
                item.setChecked(!item.isChecked());
                editor.putBoolean("offline_mode", item.isChecked());
                editor.apply();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_game:
                setNewsFragment("https://news.nicovideo.jp/categories/70?rss=2.0", "ゲーム");
                break;
            case R.id.menu_society:
                setNewsFragment("https://news.nicovideo.jp/categories/10?rss=2.0", "政治");
                break;
            case R.id.menu_business:
                setNewsFragment("https://news.nicovideo.jp/categories/20?rss=2.0", "ビジネス");
                break;
            case R.id.menu_overseas:
                setNewsFragment("https://news.nicovideo.jp/categories/30?rss=2.0", "海外");
                break;
            case R.id.menu_sport:
                setNewsFragment("https://news.nicovideo.jp/categories/40?rss=2.0", "スポーツ");
                break;
            case R.id.menu_entame:
                setNewsFragment("https://news.nicovideo.jp/categories/50?rss=2.0", "エンタメ");
                break;
            case R.id.menu_net:
                setNewsFragment("https://news.nicovideo.jp/categories/60?rss=2.0", "ネット");
                break;
            case R.id.menu_ranking_one:
                setNewsFragment("https://news.nicovideo.jp/ranking/comment/hourly?rss=2.0", "no");
                break;
            case R.id.menu_ranking_24:
                setNewsFragment("https://news.nicovideo.jp/ranking/comment?rss=2.0", "no");
                break;
            case R.id.menu_all_new:
                setNewsFragment("https://news.nicovideo.jp/search?q=&rss=2.0&sort=-startTime", "no");
                break;
            case R.id.menu_all_comment_count:
                setNewsFragment("https://news.nicovideo.jp/search?q=&rss=2.0&sort=-commentCount", "no");
                break;
            case R.id.menu_all_comment_new:
                setNewsFragment("https://news.nicovideo.jp/search?q=&rss=2.0&sort=-recentCommentTime", "no");
                break;
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setNewsFragment(String url, String category) {
        Bundle bundle = new Bundle();
        bundle.putString("url", url);
        bundle.putString("category", category);
        NewsListFragment newsListFragment = new NewsListFragment();
        newsListFragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content_linearlayout, newsListFragment);
        fragmentTransaction.commit();
        //最後に開いたFragmentを記憶する
        SharedPreferences.Editor editor = pref_setting.edit();
        editor.putString("last_page_url", url);
        editor.putString("last_page_category", category);
        editor.apply();
    }
}
