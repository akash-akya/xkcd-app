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


public class ComicsListRecyclerViewAdapter extends RecyclerView.Adapter<ComicsListRecyclerViewAdapter.ViewHolder>
                implements SectionIndexer{

    private static final String TAG = "ComicsListRecyclerViewAdapter";
    private final DateFormat mDateFormat;
    private final OnItemClickListener mListener;
    private ArrayList<XkcdData> mComics;

    public ComicsListRecyclerViewAdapter(ArrayList<XkcdData> comics, DateFormat dateFormat, OnItemClickListener listener) {
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
        holder.mNum.setText(""+comic.getNum());

        String curDate = String.format("%s/%s/%s",comic.getYear(), comic.getMonth(), comic.getDay());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/mm/dd");
        Date testDate = null;
        try {
            testDate = sdf.parse(curDate);
        }catch(Exception ex){
            ex.printStackTrace();
        }

        String localDate = mDateFormat.format(testDate);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTitle;
        public final TextView mNum;
        public final TextView mDate;

        public XkcdData mComicItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mTitle = (TextView) view.findViewById(R.id.tv_comic_name);
            mNum = (TextView) view.findViewById(R.id.tv_comic_number);
            mDate = (TextView) view.findViewById(R.id.tv_comic_date);
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
