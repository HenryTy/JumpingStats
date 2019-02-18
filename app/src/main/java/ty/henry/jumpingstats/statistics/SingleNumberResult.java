package ty.henry.jumpingstats.statistics;

import android.support.annotation.NonNull;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public class SingleNumberResult implements Comparable<SingleNumberResult> {

    private Jumper jumper;
    private Competition competition;
    private Float result;

    public SingleNumberResult(Jumper jumper, Competition competition, Float result) {
        this.jumper = jumper;
        this.competition = competition;
        this.result = result;
    }

    public Jumper getJumper() {
        return jumper;
    }

    public Competition getCompetition() {
        return competition;
    }

    public Float getResult() {
        return result;
    }

    @Override
    public String toString() {
        return String.format("%.2f (%s, %s)", result, jumper.toString(), competition.toString());
    }

    @Override
    public int compareTo(@NonNull SingleNumberResult other) {
        if(result > other.result) return 1;
        if(result < other.result) return -1;
        return 0;
    }
}
