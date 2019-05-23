package io.github.takusan23.niconewsviewer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class CommentActivity extends AppCompatActivity {


    private ArrayList<ArrayList> itemList;
    private RecyclerView recyclerView;
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private SnackberProgress snackberProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        itemList = new ArrayList<>();
        recyclerView = findViewById(R.id.comment_recyclerview);
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        newsRecyclerViewAdapter = new NewsRecyclerViewAdapter(itemList);
        recyclerView.setAdapter(newsRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();

        snackberProgress = new SnackberProgress(recyclerView, this, getString(R.string.loading) + "\n" + getIntent().getStringExtra("url"));

        //取得
        getComment();

    }

    /*コメント取得*/
    private void getComment() {
        //インターネットから取得するので非同期処理
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                Document document = null;
                try {
                    //取得
                    document = Jsoup.connect(getIntent().getStringExtra("url")).get();
                    //ぱーす
                    //commentだけ？
                    final Elements elements = document.getElementById("comments").select("div.is-floating-box").select("news-comment");
                    final String title = document.getElementsByTag("title").text();
                    //UIスレッド
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(elements.size());
                            for (int i = 0; i < elements.size(); i++) {
                                //ぱーす
                                String comment_text = elements.get(i).select("news-comment").select("div.news-comment-body").html();
                                //入れる
                                ArrayList<String> item = new ArrayList<>();
                                item.add("comment");
                                item.add(comment_text);
                                item.add("");
                                item.add("");
                                itemList.add(item);
                            }
                            setTitle(title);
                            newsRecyclerViewAdapter.notifyDataSetChanged();
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
