package ty.henry.jumpingstats.statistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.*;

public class Classifier<T, R> {

    private Map<R, List<T>> rToTList;

    public Classifier(List<T> toClassify, Function<T, R> classificationFunction) {
        rToTList = toClassify.stream().collect(groupingBy(classificationFunction));
    }

    public List<R> getValues() {
        return new ArrayList<>(rToTList.keySet());
    }

    public List<T> getGroup(R value) {
        return rToTList.get(value);
    }
}
