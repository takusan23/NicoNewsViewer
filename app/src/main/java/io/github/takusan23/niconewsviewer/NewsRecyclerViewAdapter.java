package io.github.takusan23.niconewsviewer;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class NewsRecyclerViewAdapter extends RecyclerView.Adapter<NewsRecyclerViewAdapter.ViewHolder> {

    private ArrayList<ArrayList> itemList;

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
        //入れる
        holder.title.setText(item.get(1) + "\n" + item.get(3));
        int finalPosition = position;
        //クリックイベント
        if (item.get(0).contains("news_list")){
            holder.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent  = new Intent(v.getContext(),NewsActivity.class);
                    intent.putExtra("link",item.get(2));
                    v.getContext().startActivity(intent);
                }
            });
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
