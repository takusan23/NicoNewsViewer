package io.github.takusan23.niconewsviewer;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_news_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        getNewsList();
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
                            try {
                                XmlPullParser xmlPullParser = Xml.newPullParser();
                                xmlPullParser.setInput(new StringReader(response_string));
                                int count = xmlPullParser.getEventType();

                                //一時保存
                                ArrayList<String> titleList = new ArrayList<>();
                                ArrayList<String> linkList = new ArrayList<>();
                                ArrayList<String> creatorList = new ArrayList<>();

                                String name = null;
                                String title = null;
                                String link = null;
                                String creator = null;

                                while (count != XmlPullParser.END_DOCUMENT) {
                                    if (count == XmlPullParser.START_TAG) {
                                        //要素名
                                        name = xmlPullParser.getName();
                                    } else if (count == XmlPullParser.TEXT) {
                                        //中身を持ってくる
                                        if (name.equals("title")) {
                                            //ToolBerの文字も変える
                                            if (title == null) {
                                                //最初のtltle要素
                                                getActivity().setTitle(xmlPullParser.getText());
                                            }
                                            title = xmlPullParser.getText();
                                            titleList.add(title);
                                        }
                                        if (name.equals("link")) {
                                            link = xmlPullParser.getText();
                                            linkList.add(link);
                                        }
                                        if (name.equals("creator")) {
                                            creator = xmlPullParser.getText();
                                            creatorList.add(creator);
                                        }
                                        name = "";
                                    }
                                    count = xmlPullParser.next();
                                }

                                //配列作成
                                //System.out.println(creatorList.size());
                                for (int i = 0; i < creatorList.size(); i++) {
                                    ArrayList<String> item = new ArrayList<>();
                                    item.add("news_list");
                                    item.add(titleList.get(i));
                                    item.add(linkList.get(i + 1));  //←ずれるので一個足した値を入れる
                                    item.add(creatorList.get(i));
                                    itemList.add(item);
                                }

                                //更新
                                newsRecyclerViewAdapter.notifyDataSetChanged();
                                snackberProgress.dismissSnackberProgress();

                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
            }
        });
    }
}
