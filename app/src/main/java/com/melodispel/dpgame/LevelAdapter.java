package com.melodispel.dpgame;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class LevelAdapter extends RecyclerView.Adapter<LevelAdapter.LevelAdapterViewHolder> {

    private final Context context;
    private Cursor levelCursor;
    private LevelAdapterOnClickHandler itemClickHandler;

    public interface LevelAdapterOnClickHandler {
        void onItemCLick(int level);
    }

    public LevelAdapter(Context context, LevelAdapterOnClickHandler onClickHandler) {
        this.context = context;
        itemClickHandler = onClickHandler;
    }

    public void setData(Cursor newData) {
        Cursor old = levelCursor;

        levelCursor = newData;
        notifyDataSetChanged();

        if (old != null) {
            old.close();
        }
    }

    @Override
    public LevelAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        int viewId = R.layout.level_list_item;

        View view = LayoutInflater.from(context).inflate(viewId, viewGroup, false);
        return new LevelAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(LevelAdapterViewHolder holder, int position) {
        levelCursor.moveToPosition(position);
        holder.tvLevel.setText(String.valueOf(levelCursor.getInt(levelCursor.getColumnIndex(LevelListActivity.COLUMN_NAME_LEVEL))));
    }

    @Override
    public int getItemCount() {
        if (levelCursor !=null) {
            return levelCursor.getCount();
        } else {
            return 0;
        }
    }

    class LevelAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tvLevel;

        public LevelAdapterViewHolder(View itemView) {
            super(itemView);
            tvLevel = (TextView)itemView.findViewById(R.id.tv_level_list_item);
            tvLevel.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            levelCursor.moveToPosition(getAdapterPosition());
            itemClickHandler.onItemCLick(levelCursor.getInt(levelCursor.getColumnIndex(LevelListActivity.COLUMN_NAME_LEVEL)));
        }
    }
}
