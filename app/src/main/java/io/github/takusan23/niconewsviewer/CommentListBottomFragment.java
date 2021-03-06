package io.github.takusan23.niconewsviewer;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;

public class CommentListBottomFragment extends BottomSheetDialogFragment {

    private String url;
    private ArrayList<ArrayList> itemList;
    private RecyclerView recyclerView;
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private DarkModeSupport darkModeSupport;
    private TextView textView;
    //Offline
    private SharedPreferences pref_setting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_comment, container, false);
    }

/*
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // style.xmlに作ったStyleをセットする
        darkModeSupport = new DarkModeSupport(getContext());
        if (darkModeSupport.getNightMode() == Configuration.UI_MODE_NIGHT_YES) {
            setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.DarkMode);
        }
        return super.onCreateDialog(savedInstanceState);
    }
*/

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        url = getArguments().getString("url");

        itemList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.comment_recyclerview);
        textView = view.findViewById(R.id.comment_title);
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        newsRecyclerViewAdapter = new NewsRecyclerViewAdapter(itemList);
        recyclerView.setAdapter(newsRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();

        //取得
        if (pref_setting.getBoolean("offline_mode", false)) {
            getOfflineComment();
        } else {
            getComment();
        }

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
                    document = Jsoup.connect(url).get();
                    //ぱーす
                    //commentだけ？
                    final Elements elements = document.getElementById("comments").select("div.is-floating-box").select("news-comment");
                    final String title = document.getElementsByTag("title").text();
                    //UIスレッド
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println(elements.size());
                            for (int i = 0; i < elements.size(); i++) {
                                //ぱーす
                                String comment_text = elements.get(i).select("news-comment").select("div.news-comment-body").html();
                                String createAt = elements.get(i).select("news-comment").select("ul.news-comment-metadata").select("li.news-comment-name").select("p.user-name").html();
                                //入れる
                                ArrayList<String> item = new ArrayList<>();
                                item.add("comment");
                                item.add(comment_text);
                                item.add("");
                                item.add(createAt);
                                itemList.add(item);
                            }
                            //setTitle(title);
                            newsRecyclerViewAdapter.notifyDataSetChanged();
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /*Offline*/
    private void getOfflineComment() {
        //取得
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                String html = getArguments().getString("html");
                Document document = Jsoup.parse(html);
                //ぱーす
                //commentだけ？
                final Elements elements = document.getElementById("comments").select("div.is-floating-box").select("news-comment");
                final String title = document.getElementsByTag("title").text();
                //UIスレッド
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println(elements.size());
                        for (int i = 0; i < elements.size(); i++) {
                            //ぱーす
                            String comment_text = elements.get(i).select("news-comment").select("div.news-comment-body").html();
                            String name = elements.get(i).select("news-comment").select("ul.news-comment-metadata").select("li.news-comment-name").select("div.user-name").select("p").text();
                            String createAt = elements.get(i).select("news-comment").select("ul.news-comment-metadata").select("li.news-comment-name").select("div.user-name").select("a").text();
                            //入れる
                            ArrayList<String> item = new ArrayList<>();
                            item.add("comment");
                            item.add(comment_text);
                            item.add("");
                            item.add(name + " " + createAt);
                            itemList.add(item);
                        }
                        //setTitle(title);
                        newsRecyclerViewAdapter.notifyDataSetChanged();
                    }
                });
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}