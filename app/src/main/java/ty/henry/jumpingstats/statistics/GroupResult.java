package ty.henry.jumpingstats.statistics;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Stream;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

import static java.util.stream.Collectors.*;

public class GroupResult {

    private List<Jumper> jumpers;
    private List<Competition> competitions;
    private StatsFragment.YValueGetter function;

    public GroupResult(List<Jumper> jumpers, List<Competition> competitions,
                       StatsFragment.YValueGetter valueGetter) {
        this.jumpers = jumpers;
        this.competitions = competitions;
        this.function = valueGetter;
    }

    public float getAvg() throws NoResultForJumperException {
        DoubleSummaryStatistics summaryStatistics = getSingleNumberResultStream()
                .collect(summarizingDouble(SingleNumberResult::getResult));
        if(summaryStatistics.getCount() == 0) {
            throw new NoResultForJumperException();
        }
        return (float) summaryStatistics.getAverage();
    }

    public SingleNumberResult getMax() throws NoResultForJumperException {
        SingleNumberResult result = getSingleNumberResultStream()
                .max(SingleNumberResult::compareTo).orElse(null);
        if(result == null) {
            throw new NoResultForJumperException();
        }
        return result;
    }

    public SingleNumberResult getMin() throws NoResultForJumperException {
        SingleNumberResult result = getSingleNumberResultStream()
                .min(SingleNumberResult::compareTo).orElse(null);
        if(result == null) {
            throw new NoResultForJumperException();
        }
        return result;
    }

    private Stream<SingleNumberResult> getSingleNumberResultStream() {
        return jumpers.stream().flatMap(this::getResultsForJumper);
    }

    private Stream<SingleNumberResult> getResultsForJumper(Jumper jumper) {
        ArrayList<SingleNumberResult> results = new ArrayList<>();
        for(Competition comp : competitions) {
            for(int i=1; i<3; i++) {
                try {
                    float res = function.getValue(jumper, comp, i);
                    results.add(new SingleNumberResult(jumper, comp, res));
                }
                catch (NoResultForJumperException ex) {

                }
            }
        }
        return results.stream();
    }
}
