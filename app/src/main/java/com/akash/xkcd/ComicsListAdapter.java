package com.akash.xkcd;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.akash.xkcd.util.XkcdData;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


public class ComicsListAdapter extends RecyclerView.Adapter<ComicsListAdapter.ViewHolder>
                implements SectionIndexer{
    private static final String TAG = "ComicsListAdapter";
    private final DateFormat mDateFormat;
    private final OnItemClickListener mListener;
    private ArrayList<XkcdData> mComics;

    ComicsListAdapter(ArrayList<XkcdData> comics, DateFormat dateFormat, OnItemClickListener listener) {
        mComics = comics;
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
        XkcdData comic = mComics.get(position);
        holder.mComicItem = comic;

        holder.mTitle.setText(comic.getTitle());
        holder.mNum.setText(String.valueOf(comic.getNum()));

        String curDate = String.format("%s/%s/%s",comic.getYear(), comic.getMonth(), comic.getDay());
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
        return ListViewActivity.getComicNumbers(mComics).toArray();
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

        XkcdData mComicItem;

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
        void onItemClick(XkcdData comic);
    }
}