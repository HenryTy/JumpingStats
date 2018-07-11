package ty.henry.jumpingstats;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.ViewHolder> {

    private Listener listener;
    private Competition competition;
    private List<Jumper> jumperList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View itemView;

        public ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
    }

    public interface Listener {
        void onClick(int position);
    }

    public ResultsAdapter(Competition competition, List<Jumper> jumpers) {
        this.competition = competition;
        this.jumperList = jumpers;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_ranking, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        View itemView = viewHolder.itemView;
        TextView textView1 = itemView.findViewById(R.id.text1);
        TextView textView2 = itemView.findViewById(R.id.text2);
        TextView textView3 = itemView.findViewById(R.id.text3);

        textView1.setText(Integer.toString(position+1));
        textView2.setText(jumperList.get(position).getText()[0]);
        try {
            float points = jumperList.get(position).getPointsFromComp(competition);
            textView3.setText(Float.toString(points));
        } catch (Exception ex) {
            textView3.setText("-");
        }

        if(listener != null) {
            itemView.setOnClickListener(view -> {
                listener.onClick(viewHolder.getAdapterPosition());
            });
        }
    }

    @Override
    public int getItemCount() {
        return jumperList.size();
    }
}
