package ty.henry.jumpingstats.competitions;


import java.math.BigDecimal;
import java.math.RoundingMode;

import ty.henry.jumpingstats.competitions.Competition;
import ty.henry.jumpingstats.jumpers.Jumper;

public class Result {

    private Jumper jumper;
    private Competition competition;
    private float distance;
    private float wind;
    private float speed;
    private float[] styleScores;
    private float gateCompensation;

    private float pForM;

    private static final int[] minPointK = {20, 26, 30, 35, 40, 50, 60, 70, 80, 100, 170};
    private static final float[] pointsForMeter = {4.8f, 4.4f, 4.0f, 3.6f, 3.2f, 2.8f, 2.4f, 2.2f, 2.0f, 1.8f, 1.2f};

    public Result(Jumper jumper, Competition competition, float distance, float wind,
                  float speed, float[] styleScores, float gateCompensation) {
        this.jumper = jumper;
        this.competition = competition;
        this.distance = distance;
        this.wind = wind;
        this.speed = speed;
        this.styleScores = styleScores;
        this.gateCompensation = gateCompensation;

        int i=0;
        while(i < minPointK.length && competition.getPointK() >= minPointK[i]) {
            i++;
        }
        this.pForM = pointsForMeter[i-1];
    }

    public Jumper getJumper() {
        return jumper;
    }

    public Competition getCompetition() {
        return competition;
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

    public float[] getStyleScores() {
        return styleScores;
    }

    public float pointsForDistance() {
        return 60 + pForM*(distance - competition.getPointK());
    }

    public float pointsForStyle() {
        int minInd = 0, maxInd = 1;
        if(styleScores[minInd] > styleScores[maxInd]) {
            minInd = 1;
            maxInd = 0;
        }
        for(int i=2; i<5; i++) {
            if(styleScores[i] > styleScores[maxInd]) {
                maxInd = i;
            }
            else if(styleScores[i] < styleScores[minInd]) {
                minInd = i;
            }
        }
        float sum = 0;
        for(int i=0; i<5; i++) {
            if(i!=minInd && i!=maxInd) {
                sum += styleScores[i];
            }
        }
        return sum;
    }

    public float pointsForWind() {
        float result;
        if(wind < 0) {
            result = competition.getHeadWindPoints() * wind;
        }
        else {
            result = competition.getTailWindPoints() * wind;
        }
        return new BigDecimal(result).setScale(1, RoundingMode.HALF_UP).floatValue();
    }

    public float pointsForGate() {
        return gateCompensation;
    }

    public float points() {
        return pointsForDistance() + pointsForGate() + pointsForStyle() + pointsForWind();
    }
}
