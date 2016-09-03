package com.karl.draggridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Created by Karl on 2016/9/2.
 */
public class DragGridView extends GridView {

    private static final int DRAG_IMG_SHOW = 1;
    private static final int DRAG_IMG_NOT_SHOW = 0;
    private static final String LOG_TAG = "DragGridView";
    private static final float AMP_FACTOR = 1.2f;

    private ImageView dragImageView;
    private WindowManager windowManager;
    private WindowManager.LayoutParams dragImageViewParams;
    private boolean isViewOnDrag = false;

    private int preDraggedOverPosition = AdapterView.INVALID_POSITION;
    private int downRawX;
    private int downRawY;
    private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
            //记录长按item位置
            preDraggedOverPosition = position;

            //获取被长按item的Drawing cache
            view.destroyDrawingCache();
            view.setDrawingCacheEnabled(true);
            //通过长按item，获取拖动item的bitmap
            Bitmap dragBitmap = Bitmap.createBitmap(view.getDrawingCache());

            //设置拖动item 的参数
            dragImageViewParams.gravity = Gravity.TOP|Gravity.LEFT;
            //设置拖动Item为原item 的1.2倍
            dragImageViewParams.width = (int) (AMP_FACTOR*dragBitmap.getWidth());
            dragImageViewParams.height = (int) (AMP_FACTOR*dragBitmap.getHeight());
            //设置触摸点为绘制拖动item中心
            dragImageViewParams.x = (downRawX - dragImageViewParams.width/2);
            dragImageViewParams.y = (downRawY - dragImageViewParams.height/2);
            dragImageViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    |WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    |WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
            dragImageViewParams.format = PixelFormat.TRANSLUCENT;
            dragImageViewParams.windowAnimations = 0;

            //dragImageView为被拖动item的容器，清空上一次的显示
            if ((int)dragImageView.getTag() == DRAG_IMG_SHOW){
                windowManager.removeView(dragImageView);
                dragImageView.setTag(DRAG_IMG_NOT_SHOW);
            }

            //设置本次被长按的item
            dragImageView.setImageBitmap(dragBitmap);
            //添加拖动item到屏幕
            windowManager.addView(dragImageView,dragImageViewParams);
            dragImageView.setTag(DRAG_IMG_SHOW);
            isViewOnDrag = true;

            //设置被长按item不显示
            ((GridViewAdapter)getAdapter()).hideView(position);
            return true;
        }
    };

    public DragGridView(Context context) {
        super(context);
        initView();
    }

    public DragGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }
    private void initView(){
        setOnItemLongClickListener(onItemLongClickListener);
        //初始化显示被拖动item的image view
        dragImageView = new ImageView(getContext());
        dragImageView.setTag(DRAG_IMG_NOT_SHOW);
        //初始化用于设置dragImageView的参数对象
        dragImageViewParams = new WindowManager.LayoutParams();
        //获取窗口管理对象，用于后面向窗口中添加DragImageView
        windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        //记录按下时的坐标
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            //获取触摸点相对于屏幕的坐标
            downRawX = (int) ev.getRawX();
            downRawY = (int) ev.getRawY();
        }else if ((ev.getAction() == MotionEvent.ACTION_MOVE)&&(isViewOnDrag ==true)) {//DragImageView处于被拖动时，更新DragView的位置
            Log.i(LOG_TAG," "+ev.getRawX()+" "+ev.getRawY());
            //设置触摸点为dragImageView中心
            dragImageViewParams.x = (int) (ev.getRawX() - dragImageView.getWidth()/2);
            dragImageViewParams.y = (int) (ev.getRawY() - dragImageView.getHeight()/2);
            //更新窗口显示
            windowManager.updateViewLayout(dragImageView,dragImageViewParams);
            //获取当前触摸点的item position
            int currentDraggedPosition = pointToPosition((int)ev.getX(),(int)ev.getY());
            //如果当前停留位置item不等于上次停留位置的item，交换本次和上次停留的item
            if ((currentDraggedPosition != AdapterView.INVALID_POSITION)&&(currentDraggedPosition!=preDraggedOverPosition)){
                ((GridViewAdapter)getAdapter()).swapView(preDraggedOverPosition,currentDraggedPosition);
                preDraggedOverPosition = currentDraggedPosition;
            }
        }else if ((ev.getAction() == MotionEvent.ACTION_UP)&&(isViewOnDrag==true)){//释放dragImageView
            ((GridViewAdapter)getAdapter()).showHideView();
            if ((int)dragImageView.getTag() == DRAG_IMG_SHOW){
                windowManager.removeView(dragImageView);
                dragImageView.setTag(DRAG_IMG_NOT_SHOW);
            }
            isViewOnDrag = false;
        }
        return super.onTouchEvent(ev);
    }

    boolean expanded = false;
    public boolean isExpanded()
    {
        return expanded;
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        // HACK! TAKE THAT ANDROID!
        if (isExpanded())
        {
            // Calculate entire height by providing a very large height hint.
            // View.MEASURED_SIZE_MASK represents the largest height possible.
            int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);

            ViewGroup.LayoutParams params = getLayoutParams();
            params.height = getMeasuredHeight();
        }
        else
        {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setExpanded(boolean expanded)
    {
        this.expanded = expanded;
    }
}
