package net.autogroup.pdf.info.view.drag;

import android.support.v7.widget.RecyclerView;

public interface OnStartDragListener {

    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);

    void onRevemove();

    void onItemClick(String result);

}