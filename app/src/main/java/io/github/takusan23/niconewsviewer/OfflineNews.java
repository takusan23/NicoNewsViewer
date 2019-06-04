package io.github.takusan23.niconewsviewer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
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

import static android.content.Context.NOTIFICATION_SERVICE;

public class OfflineNews {
    private Context context;
    private NewsSQLiteDataBase newsSQLiteDataBase;
    private SQLiteDatabase sqLiteDatabase;
    private NotificationManager notificationManager;
    private Notification.Builder progress_Builder;

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
        //非同期処理する
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                showNotification("RSS取得開始");
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
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        String response_string = response.body().string();
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

                            } catch (XmlPullParserException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //for文終わったら各ニュースのHTMLを取得しに行く
                getNewsHTML();

                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
        showNotification("各ニュースのHTMLデータ取得開始 : " + category_List.size() + " 件");
        System.out.println("各ニュースのHTMLデータ取得開始 : " + category_List.size() + " 件");
        showProgressNotification();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... aVoid) {
                for (int i = 0; i < category_List.size(); i++) {
                    setNotificationProgress(i);
                    System.out.println("進捗 : " + String.valueOf(i));
                    Request request = new Request.Builder()
                            .url(html_url_List.get(i))
                            .get()
                            .build();
                    OkHttpClient okHttpClient = new OkHttpClient();
                    int finalI = i;
                    try {
                        Response response = okHttpClient.newCall(request).execute();
                        if (!response.isSuccessful()) {
                            showToast(context.getString(R.string.error) + "\n" + String.valueOf(response.code()));
                        } else {
                            ContentValues values = new ContentValues();
                            values.put("memo", "");
                            values.put("title", title_List.get(finalI));
                            values.put("category", category_List.get(finalI));
                            values.put("creator", creator_List.get(finalI));
                            values.put("link", link_List.get(finalI));
                            values.put("html", response.body().string());
                            sqLiteDatabase.insert("newsdb", null, values);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                showNotification("HTML取得が終わりました。総数 : " + category_List.size() + " 件");
                super.onPostExecute(aVoid);
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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

    /*通知作成*/
    private void showNotification(String message) {
        String notification_channel = "niconews_offline_progress";
        notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        //通知ちゃんねる
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(notification_channel) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(notification_channel, "offline_progress_notification", NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setName("オフライン進捗通知");
                notificationChannel.setDescription("進捗状況を通知します");
                notificationManager.createNotificationChannel(notificationChannel);
            }
            Notification.Builder builder = new Notification.Builder(context, notification_channel)
                    .setContentTitle("オフライン準備進捗")
                    .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                    .setContentText(message);
            notificationManager.notify(R.string.app_name, builder.build());
        } else {
            Notification.Builder builder = new Notification.Builder(context)
                    .setContentTitle("オフライン準備進捗")
                    .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                    .setContentText(message);
            notificationManager.notify(R.string.app_name, builder.build());
        }
    }

    private void showProgressNotification() {
        String notification_channel = "niconews_offline_progress";
        //通知ちゃんねる
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            if (notificationManager.getNotificationChannel(notification_channel) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(notification_channel, "offline_progress_notification", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.setName("オフライン進捗通知");
                notificationChannel.setDescription("進捗状況を通知します");
                notificationManager.createNotificationChannel(notificationChannel);
            }
            progress_Builder = new Notification.Builder(context, notification_channel)
                    .setContentTitle("オフライン準備進捗")
                    .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                    .setContentText("HTML取得進捗");
            notificationManager.notify(R.string.app_name, progress_Builder.build());
        } else {
            progress_Builder = new Notification.Builder(context)
                    .setContentTitle("オフライン準備進捗")
                    .setSmallIcon(R.drawable.ic_file_download_black_24dp)
                    .setContentText("HTML取得進捗");
            notificationManager.notify(R.string.app_name, progress_Builder.build());
        }
    }

    private void setNotificationProgress(int progress) {
        progress_Builder.setProgress(category_List.size(), progress, false);
        notificationManager.notify(R.string.app_name, progress_Builder.build());
    }

}
