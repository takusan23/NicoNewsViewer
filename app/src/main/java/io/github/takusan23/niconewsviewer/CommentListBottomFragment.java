package io.github.takusan23.niconewsviewer;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
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
                                //入れる
                                ArrayList<String> item = new ArrayList<>();
                                item.add("comment");
                                item.add(comment_text);
                                item.add("");
                                item.add("");
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


}