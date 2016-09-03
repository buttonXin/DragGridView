package com.karl.draggridview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Karl on 2016/9/2.
 */
public class GridViewAdapter extends BaseAdapter {
    private Context context;
    private List<String> mList;
    private int hidePosition = AdapterView.INVALID_POSITION;

    public GridViewAdapter(Context context, List<String> mList) {
        this.context = context;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item,null);
            holder.textView = (TextView) convertView.findViewById(R.id.tv_item);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }
        //hide时隐藏Text
        if (position != hidePosition){
            holder.textView.setText(mList.get(position));
        }else {
            holder.textView.setText("");
        }
//        holder.textView.setId(position);
        return convertView;
    }
    private class ViewHolder{
        TextView textView;
    }

    public void hideView(int Position){
        hidePosition = AdapterView.INVALID_POSITION;
        notifyDataSetChanged();
    }
    public void removeView(int position){
        mList.remove(position);
        notifyDataSetChanged();
    }
    public void showHideView(){
        hidePosition = AdapterView.INVALID_POSITION;
        notifyDataSetChanged();
    }
    //更新拖动时的gridView
    public void swapView(int draggedPosition,int destPos){
        //从前往后移动，其他item依次前移
        if (draggedPosition < destPos){
            mList.add(destPos+1,getItem(draggedPosition).toString());
            mList.remove(draggedPosition);
        }else if(draggedPosition >destPos){
            mList.add(destPos,getItem(draggedPosition).toString());
            mList.remove(draggedPosition+1);
        }
        hidePosition = destPos;
        notifyDataSetChanged();
    }
}
