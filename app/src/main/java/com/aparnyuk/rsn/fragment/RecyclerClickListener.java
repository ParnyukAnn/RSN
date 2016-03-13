package com.aparnyuk.rsn.fragment;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;


public class RecyclerClickListener implements RecyclerView.OnItemTouchListener {

      private OnItemClickListener mOnClickListener;
      GestureDetector mGestureDetector;

      public interface OnItemClickListener {
          void onItemClick(View view, int position);
      }

      public RecyclerClickListener(Context context, OnItemClickListener listener) {
          mOnClickListener = listener;
          mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
              @Override
              public boolean onSingleTapUp(MotionEvent e) {
                  Log.d("not","single");
                  return true;
              }

              @Override
              public void onLongPress(MotionEvent e) {
                  Log.d("not","logn");
                  super.onLongPress(e);

              }
          });
      }

/*      @Override
      public boolean onInterceptTouchEvent(RecyclerView view, MotionEvent e) {
          View childView = view.findChildViewUnder(e.getX(), e.getY());
          if (childView != null && mOnClickListener != null && mGestureDetector.onTouchEvent(e)) {
              mOnClickListener.onItemClick(childView, view.getChildPosition(childView));
              return true;
          }
          return false;
      }*/


    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View clickedChild = rv.findChildViewUnder(e.getX(), e.getY());
            if (clickedChild != null && !clickedChild.dispatchTouchEvent(e) && mGestureDetector.onTouchEvent(e)) {
                int clickedPosition = rv.getChildPosition(clickedChild);
                if (clickedPosition != RecyclerView.NO_POSITION) {
                    mOnClickListener.onItemClick( clickedChild, clickedPosition);
                    return true;
                }
            }
        return false;
    }


      @Override
      public void onTouchEvent(RecyclerView view, MotionEvent event) {
          // MotionEvent хранит координаты X-Y
          if (event.getAction() == MotionEvent.ACTION_DOWN) {
              String text = "В x = " + event.getX() + " and y = " + event.getY();
              Log.d("Note",text);
          }

      }

      @Override
      public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

      }

  }