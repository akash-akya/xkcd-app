package com.akash.xkcd;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.akash.xkcd.database.Xkcd;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ComicsListAdapter extends RecyclerView.Adapter<ComicsListAdapter.ViewHolder>
                implements SectionIndexer{
    private static final String TAG = "ComicsListAdapter";
    private final DateFormat mDateFormat;
    private final OnItemClickListener mListener;
    private List<Xkcd> mComics;

    ComicsListAdapter(DateFormat dateFormat, OnItemClickListener listener, Context context, boolean isFavoriteList) {
        ComicsList comicsList = ComicsList.getInstance(context.getApplicationContext());
        mComics = isFavoriteList? comicsList.getFavoriteComics() : comicsList.getAsList();
        mDateFormat = dateFormat;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comic_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Xkcd comic = mComics.get(position);
        holder.mComicItem = comic;

        holder.mTitle.setText(comic.title);
        holder.mNum.setText(String.valueOf(comic.num));

        String curDate = String.format("%s/%s/%s",comic.year, comic.month, comic.day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd", Locale.getDefault());
        Date date = null;
        try {
            date = sdf.parse(curDate);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String localDate = mDateFormat.format(date);
        holder.mDate.setText(localDate);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    mListener.onItemClick(holder.mComicItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mComics.size();
    }

    @Override
    public Object[] getSections() {
        return mComics.toArray();
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return 0;
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position >= mComics.size()) {
            position = mComics.size() - 1;
        }
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        @BindView(R.id.tv_comic_name) TextView mTitle;
        @BindView(R.id.tv_comic_number) TextView mNum;
        @BindView(R.id.tv_comic_date) TextView mDate;

        Xkcd mComicItem;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
            mView = view;
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTitle.getText() + "'";
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Xkcd comic);
    }
}
