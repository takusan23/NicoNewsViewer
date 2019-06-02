package io.github.takusan23.niconewsviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ShareCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class NewsActivity extends AppCompatActivity {

    private TextView textView;
    private FloatingActionButton fab;
    private SnackberProgress snackberProgress;
    private CoordinatorLayout coordinatorLayout;
    private DarkModeSupport darkModeSupport;
    //Offline
    private SharedPreferences pref_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pref_setting = PreferenceManager.getDefaultSharedPreferences(this);
        darkModeSupport = new DarkModeSupport(this);
        darkModeSupport.setActivityTheme(this);

        setContentView(R.layout.activity_news);

        textView = findViewById(R.id.news_activity_textview);
        fab = findViewById(R.id.fab);
        coordinatorLayout = findViewById(R.id.news_activity_coordinator_layout);
        String snackbar_text = getString(R.string.loading);
        if (pref_setting.getBoolean("offline_mode", false)) {
            setHTML();
            snackbar_text += "\n" + "オフラインデータ";
        } else {
            getNews();
            snackbar_text += "\n" + getIntent().getStringExtra("link");
        }
        snackberProgress = new SnackberProgress(coordinatorLayout, this, snackbar_text);

        //Fabクリックイベント
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("url", getIntent().getStringExtra("link"));
                if (pref_setting.getBoolean("offline_mode", false)) {
                    bundle.putString("html", getIntent().getStringExtra("html"));
                }
                CommentListBottomFragment commentListBottomFragment = new CommentListBottomFragment();
                commentListBottomFragment.setArguments(bundle);
                commentListBottomFragment.show(getSupportFragmentManager(), "comment_fragment");
            }
        });
    }

    /*しゅとくする*/
    private void getNews() {
        //インターネットから取得するので非同期処理
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                Document document = null;
                try {
                    //取得
                    document = Jsoup.connect(getIntent().getStringExtra("link")).get();
                    //ぱーす
                    final String text = document.select("section.article-body.news-article-body").html();
                    final String title = document.getElementsByTag("title").text();
                    //UIスレッド
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //入れる
                            textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
                            setTitle(title);
                            snackberProgress.dismissSnackberProgress();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /*ニュース*/
    private void setHTML() {
        //取得
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                String html = getIntent().getStringExtra("html");

                Document document = Jsoup.parse(html);
                //ぱーす
                final String text = document.select("section.article-body.news-article-body").html();
                final String title = document.getElementsByTag("title").text();
                //UIスレッド
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //入れる
                        textView.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_COMPACT));
                        setTitle(title);
                        snackberProgress.dismissSnackberProgress();
                    }
                });
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_share) {
            ShareCompat.IntentBuilder builder = ShareCompat.IntentBuilder.from(this);
            //ダイアログの名前
            builder.setChooserTitle(getTitle());
            //シェアするときのタイトル
            builder.setSubject(getTitle().toString());
            //本文
            builder.setText(getIntent().getStringExtra("link"));
            //今回は文字なので
            builder.setType("text/plain");
            //ダイアログ
            builder.startChooser();
        }

        return super.onOptionsItemSelected(item);
    }


}
