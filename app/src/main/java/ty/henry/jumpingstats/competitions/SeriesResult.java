package ty.henry.jumpingstats.competitions;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ty.henry.jumpingstats.jumpers.Jumper;

public class SeriesResult {
    private int series;
    private float distance;
    private float wind;
    private float speed;
    private List<Float> styleScores;
    private float gateCompensation;
    private Result result;

    private float pForM;

    private static final int[] minPointK = {20, 26, 30, 35, 40, 50, 60, 70, 80, 100, 170};
    private static final float[] pointsForMeter = {4.8f, 4.4f, 4.0f, 3.6f, 3.2f, 2.8f, 2.4f, 2.2f, 2.0f, 1.8f, 1.2f};

    public SeriesResult(int series, float distance, float wind,
                  float speed, List<Float> styleScores, float gateCompensation, Result result) {
        Result.checkSeriesArgument(series);
        checkStyleScoresArgument(styleScores);
        this.series = series;
        this.distance = distance;
        this.wind = wind;
        this.speed = speed;
        this.styleScores = new ArrayList<>(styleScores);
        this.gateCompensation = gateCompensation;
        this.result = result;

        int i=0;
        Competition competition = result.getCompetition();
        while(i < minPointK.length && competition.getPointK() >= minPointK[i]) {
            i++;
        }
        this.pForM = pointsForMeter[i-1];
    }

    public Jumper getJumper() {
        return result.getJumper();
    }

    public Competition getCompetition() {
        return result.getCompetition();
    }

    public int getSeries() {
        return series;
    }

    public float getDistance() {
        return distance;
    }

    public float getWind() {
        return wind;
    }

    public float getSpeed() {
        return speed;
    }

    public List<Float> getStyleScores() {
        return Collections.unmodifiableList(styleScores);
    }

    public float pointsForDistance() {
        float pointsForAchievePointK = pForM == 1.2f ? 120f : 60f;
        return pointsForAchievePointK + pForM*(distance - result.getCompetition().getPointK());
    }

    public float pointsForStyle() {
        int minInd = 0, maxInd = 1;
        if(styleScores.get(minInd) > styleScores.get(maxInd)) {
            minInd = 1;
            maxInd = 0;
        }
        for(int i=2; i<5; i++) {
            if(styleScores.get(i) > styleScores.get(maxInd)) {
                maxInd = i;
            }
            else if(styleScores.get(i) < styleScores.get(minInd)) {
                minInd = i;
            }
        }
        float sum = 0;
        for(int i=0; i<5; i++) {
            if(i!=minInd && i!=maxInd) {
                sum += styleScores.get(i);
            }
        }
        return sum;
    }

    public float pointsForWind() {
        float points;
        if(wind < 0) {
            points = result.getCompetition().getHeadWindPoints() * wind;
        }
        else {
            points = result.getCompetition().getTailWindPoints() * wind;
        }
        return new BigDecimal(points).setScale(1, RoundingMode.HALF_UP).floatValue();
    }

    public float pointsForGate() {
        return gateCompensation;
    }

    public float points() {
        return pointsForDistance() + pointsForGate() + pointsForStyle() + pointsForWind();
    }

    private void checkStyleScoresArgument(List<Float> styleScores) {
        boolean correct = true;
        if(styleScores.size() != 5) {
            correct = false;
        }
        else {
            for(int i = 0; i < 5; i++) {
                if(styleScores.get(i) < 0 || styleScores.get(i) > 20) {
                    correct = false;
                    break;
                }
            }
        }
        if(!correct) {
            throw new IllegalArgumentException("Incorrect styleScores list");
        }
    }
}
