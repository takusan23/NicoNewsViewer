package io.github.takusan23.niconewsviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        textView = findViewById(R.id.news_activity_textview);
        fab = findViewById(R.id.fab);
        coordinatorLayout = findViewById(R.id.news_activity_coordinator_layout);
        snackberProgress = new SnackberProgress(coordinatorLayout, this, getString(R.string.loading) + "\n" + getIntent().getStringExtra("link"));

        getNews();

        //Fabクリックイベント
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NewsActivity.this,CommentActivity.class);
                intent.putExtra("url", getIntent().getStringExtra("link"));
                startActivity(intent);
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

}
