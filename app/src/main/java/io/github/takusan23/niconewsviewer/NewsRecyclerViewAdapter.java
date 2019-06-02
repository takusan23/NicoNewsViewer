package io.github.takusan23.niconewsviewer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ArrayList> itemList;
    private SharedPreferences pref_setting;

    public NewsRecyclerViewAdapter(ArrayList<ArrayList> list) {
        itemList = list;
    }

    @NonNull
    @Override
    public NewsRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view, parent.getContext());
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull NewsRecyclerViewAdapter.ViewHolder holder, int position) {
        //冒頭を消す
        ArrayList<String> item = itemList.get(position);
        String memo = item.get(0);
        pref_setting = PreferenceManager.getDefaultSharedPreferences(holder.title.getContext());
        //ニュースリスト
        if (memo.contains("news_list")) {
            holder.title.setText(item.get(1) + "\n" + item.get(3));
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), NewsActivity.class);
                    intent.putExtra("link", item.get(2));
                    if (pref_setting.getBoolean("offline_mode", false)) {
                        intent.putExtra("html", item.get(4));
                    }
                    v.getContext().startActivity(intent);
                }
            });
        }
        if (memo.contains("comment")) {
            holder.title.setText(Html.fromHtml(item.get(3), Html.FROM_HTML_MODE_COMPACT) + "\n" + item.get(1));
        }
    }

    @Override
    public int getItemCount() {
        //冒頭を消したのでその分引く
        return (itemList.size());
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;

        public ViewHolder(@NonNull View itemView, Context context) {
            super(itemView);
            title = itemView.findViewById(R.id.recycler_view_title);
        }
    }
}
