package ty.henry.jumpingstats;


import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;

public class TextImageAdapter<T extends TextImageAdapter.TextImage> extends RecyclerView.Adapter<TextImageAdapter.ViewHolder> {

    private ArrayList<T> data;
    private Listener listener;

    private boolean multiselection = false;
    private HashSet<T> selectedItems = new HashSet<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    public interface TextImage {
        int TYPE_HEADER = 0;
        int TYPE_ITEM = 1;
        String[] getText(Context context);
        int getImage();
        int getType();
    }

    public interface Listener {
        void onClick(int position);
    }

    public TextImageAdapter(ArrayList<T> data) {
        this.data = data;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public void setMultiselection(boolean multiselection) {
        this.multiselection = multiselection;
        if(!multiselection) {
            selectedItems = new HashSet<>();
        }
    }

    public HashSet<T> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = viewType==TextImage.TYPE_HEADER ? R.layout.recycler_text : R.layout.recycler_text_image;
        View itemView = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final View itemView = viewHolder.itemView;
        TextView textView1 = itemView.findViewById(R.id.text1);
        String[] text = data.get(position).getText();
        textView1.setText(text[0]);
        if(viewHolder.getItemViewType()==TextImage.TYPE_HEADER) {
            return;
        }
        if(selectedItems.contains(data.get(position))) {
            itemView.setBackgroundColor(Color.GRAY);
        }
        else {
            itemView.setBackgroundColor(Color.WHITE);
        }
        TextView textView2 = itemView.findViewById(R.id.text2);
        ImageView imageView = itemView.findViewById(R.id.image);
        int imageId = data.get(position).getImage();
        imageView.setImageResource(imageId);
        if(text.length > 1) {
            textView2.setVisibility(View.VISIBLE);
            textView2.setText(text[1]);
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(multiselection) {
                    if(selectedItems.contains(data.get(viewHolder.getAdapterPosition()))) {
                        selectedItems.remove(data.get(viewHolder.getAdapterPosition()));
                        itemView.setBackgroundColor(Color.WHITE);
                    }
                    else {
                        selectedItems.add(data.get(viewHolder.getAdapterPosition()));
                        itemView.setBackgroundColor(Color.GRAY);
                    }
                }
                else {
                    if(listener != null) {
                        listener.onClick(viewHolder.getAdapterPosition());
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).getType();
    }

}
