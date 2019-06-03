package io.github.takusan23.niconewsviewer;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.util.Xml;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.logging.Handler;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class OfflineNews {
    private Context context;
    private NewsSQLiteDataBase newsSQLiteDataBase;
    private SQLiteDatabase sqLiteDatabase;

    private ArrayList<String> title_List;
    private ArrayList<String> category_List;
    private ArrayList<String> html_url_List;
    private ArrayList<String> html_List;
    private ArrayList<String> link_List;
    private ArrayList<String> creator_List;

    public OfflineNews(Context context) {
        this.context = context;
        title_List = new ArrayList<>();
        category_List = new ArrayList<>();
        html_url_List = new ArrayList<>();
        html_List = new ArrayList<>();
        link_List = new ArrayList<>();
        creator_List = new ArrayList<>();
        getNews();
    }

    private void getNews() {
        if (newsSQLiteDataBase == null) {
            newsSQLiteDataBase = new NewsSQLiteDataBase(context);
            newsSQLiteDataBase.setWriteAheadLoggingEnabled(false);
        }
        if (sqLiteDatabase == null) {
            sqLiteDatabase = newsSQLiteDataBase.getWritableDatabase();
        }
        sqLiteDatabase.delete("newsdb", null, null);
        //取得していく
        getNicoNewsHTMLtoDB();
    }

    private void getNicoNewsHTMLtoDB() {
        Toast.makeText(context, "RSSデータ取得開始", Toast.LENGTH_SHORT).show();
        System.out.println("RSSデータ取得開始");
        String url[] = {"10", "20", "30", "40", "50", "60", "70"};
        String category[] = {"政治", "ビジネス", "海外", "スポーツ", "エンタメ", "ネット", "ゲーム"};
        for (int news_count = 0; news_count < url.length; news_count++) {
            System.out.println("RSS進捗 : " + String.valueOf(news_count));
            String rss_url = "https://news.nicovideo.jp/categories/" + url[news_count] + "?rss=2.0";
            Request request = new Request.Builder()
                    .url(rss_url)
                    .get()
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            int finalNews_count = news_count;
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, context.getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String response_string = response.body().string();
                    if (!response.isSuccessful()) {
                        showToast(context.getString(R.string.error) + "\n" + String.valueOf(response.code()));
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
                            for (int i = 1; i < creatorList.size(); i++) {
                                ArrayList<String> item = new ArrayList<>();
                                item.add("news_list");
                                title_List.add(titleList.get(i));
                                html_url_List.add(linkList.get(i + 1));  //←ずれるので一個足した値を入れる
                                category_List.add(category[finalNews_count]);
                                creator_List.add(creatorList.get(i));
                                link_List.add(linkList.get(i));
                            }

                            //for文終了した？
                            if (finalNews_count == 6) {
                                //ニュース記事HTML取得
                                getNewsHTML();
                                //DB保存
                                //insertNews();
                            }

                        } catch (XmlPullParserException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

    }

    private void showToast(String message) {
        ((AppCompatActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getNewsHTML() {
        showToast("各ニュースのHTMLデータ取得開始 : " + category_List.size() + " 件");
        for (int i = 0; i < category_List.size(); i++) {
            System.out.println("進捗 : " + String.valueOf(i));
            Request request = new Request.Builder()
                    .url(html_url_List.get(i))
                    .get()
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient();
            int finalI = i;
            okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    ContentValues values = new ContentValues();
                    values.put("memo", "");
                    values.put("title", title_List.get(finalI));
                    values.put("category", category_List.get(finalI));
                    values.put("creator", creator_List.get(finalI));
                    values.put("link", link_List.get(finalI));
                    values.put("html", response.body().string());
                    sqLiteDatabase.insert("newsdb", null, values);
                }
            });
        }
        showToast("HTML取得が終わりました。総数 : " + category_List.size() + " 件");
    }

    private void insertNews() {
        for (int i = 0; i < html_List.size(); i++) {
            ContentValues values = new ContentValues();
            values.put("memo", "");
            values.put("title", title_List.get(i));
            values.put("category", category_List.get(i));
            values.put("html", html_List.get(i));
            sqLiteDatabase.insert("newsdb", null, values);
        }
    }
}
