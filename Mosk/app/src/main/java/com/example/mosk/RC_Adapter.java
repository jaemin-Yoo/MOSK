package com.example.mosk;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class RC_Adapter extends RecyclerView.Adapter<RC_Adapter.ViewHolder> {

    private String[] DataTitleSet;
    private String[] DataContentsSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TextView contents;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            title=(TextView)view.findViewById(R.id.Txtitle);
            contents=(TextView)view.findViewById(R.id.Txcontents);
        }

    }

    /**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */
    public RC_Adapter(String[] dataSet,String[] dataContentsSet) {
        DataTitleSet = dataSet;
        DataContentsSet=dataContentsSet;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        LinearLayout view = (LinearLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.text_row_item , viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.title.setText(DataTitleSet[position]);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return DataTitleSet.length;
    }
}
