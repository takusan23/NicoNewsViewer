package io.github.takusan23.niconewsviewer;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewsListFragment extends Fragment {

    private ArrayList<ArrayList> itemList;
    private RecyclerView recyclerView;
    private NewsRecyclerViewAdapter newsRecyclerViewAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private SnackberProgress snackberProgress;
    //オフラインモード
    private SharedPreferences pref_setting;
    private NewsSQLiteDataBase newsSQLiteDataBase;
    private SQLiteDatabase sqLiteDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(getContext());
        itemList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.news_recycleview);
        //ここから下三行必須
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        newsRecyclerViewAdapter = new NewsRecyclerViewAdapter(itemList);
        recyclerView.setAdapter(newsRecyclerViewAdapter);
        recyclerViewLayoutManager = recyclerView.getLayoutManager();

        //リスト読み込み
        snackberProgress = new SnackberProgress(recyclerView, getContext(), getString(R.string.loading) + "\n" + getArguments().getString("url"));
        if (!getArguments().getString("category").contains("no")) {
            if (pref_setting.getBoolean("offline_mode", false)) {
                getOfflineNews();
            } else {
                getNewsList();
            }
        } else {
            getNewsList();
        }

    }

    private void getNewsList() {
        Request request = new Request.Builder()
                .url(getArguments().getString("url"))
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String response_string = response.body().string();
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isSuccessful()) {
                            Toast.makeText(getContext(), getString(R.string.error) + "\n" + String.valueOf(response.code()), Toast.LENGTH_SHORT).show();
                        } else {
                            //RSSパース？
                            //一時保存
                            ArrayList<String> titleList = new ArrayList<>();
                            ArrayList<String> linkList = new ArrayList<>();
                            ArrayList<String> creatorList = new ArrayList<>();
                            //HTMLぱーす（RSSだけど
                            Document document = Jsoup.parse(response_string);
                            Elements items = document.getElementsByTag("item");
                            for (int i = 0; i < items.size(); i++) {
                                titleList.add(items.get(i).getElementsByTag("title").text());
                                creatorList.add(items.get(i).getElementsByTag("dc:creator").text());
                                // URL とれない　→　正規表現
                                String pattern = "https://news.nicovideo.jp/watch/nw([0-9]+)";
                                Matcher matcher = Pattern.compile(pattern).matcher(items.get(i).text());
                                if (matcher.find()){
                                    linkList.add(matcher.group());
                                }
                            }
                            //配列作成
                            for (int i = 0; i < linkList.size(); i++) {
                                ArrayList<String> item = new ArrayList<>();
                                item.add("news_list");
                                item.add(titleList.get(i));
                                item.add(linkList.get(i));
                                item.add(creatorList.get(i));
                                itemList.add(item);
                            }
                            //最初はいらない
                            itemList.remove(0);
                            //更新
                            newsRecyclerViewAdapter.notifyDataSetChanged();
                            snackberProgress.dismissSnackberProgress();

                        }
                    }
                });
            }
        });
    }

    private void getOfflineNews() {
        if (newsSQLiteDataBase == null) {
            newsSQLiteDataBase = new NewsSQLiteDataBase(getContext());
            newsSQLiteDataBase.setWriteAheadLoggingEnabled(false);
        }
        if (sqLiteDatabase == null) {
            sqLiteDatabase = newsSQLiteDataBase.getWritableDatabase();
        }
        Cursor cursor = sqLiteDatabase.query(
                "newsdb",
                new String[]{"title", "category", "creator", "link", "html"},
                "category=?",
                new String[]{getArguments().getString("category")},
                null,
                null,
                null
        );
        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++) {
            String title = cursor.getString(0);
            String category = cursor.getString(1);
            String creator = cursor.getString(2);
            String link = cursor.getString(3);
            String html = cursor.getString(4);
            ArrayList<String> item = new ArrayList<>();
            item.add("news_list");
            item.add(title);
            item.add(link);
            item.add(creator);
            item.add(html);
            itemList.add(item);
            cursor.moveToNext();
        }
        cursor.close();
        //更新
        newsRecyclerViewAdapter.notifyDataSetChanged();
        snackberProgress.dismissSnackberProgress();
    }
}
