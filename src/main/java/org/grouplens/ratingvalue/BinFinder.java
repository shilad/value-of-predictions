package org.grouplens.ratingvalue;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.grouplens.lenskit.RatingPredictor;
import org.grouplens.lenskit.baseline.BaselinePredictor;
import org.grouplens.lenskit.baseline.ItemUserMeanPredictor;
import org.grouplens.lenskit.core.LenskitRecommenderEngineFactory;
import org.grouplens.lenskit.data.pref.PreferenceDomain;
import org.grouplens.lenskit.eval.AlgorithmBuilder;
import org.grouplens.lenskit.eval.EvalOptions;
import org.grouplens.lenskit.eval.EvalTaskFailedException;
import org.grouplens.lenskit.eval.data.CSVDataSource;
import org.grouplens.lenskit.eval.data.CSVDataSourceBuilder;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldTask;
import org.grouplens.lenskit.eval.data.crossfold.CrossfoldTaskBuilder;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataBuilder;
import org.grouplens.lenskit.eval.data.traintest.GenericTTDataSet;
import org.grouplens.lenskit.eval.data.traintest.TTDataSet;
import org.grouplens.lenskit.eval.traintest.TrainTestEvalBuilder;
import org.grouplens.lenskit.eval.traintest.TrainTestEvalTask;
import org.grouplens.lenskit.knn.item.ItemItemRatingPredictor;
import org.grouplens.lenskit.knn.params.NeighborhoodSize;
import org.grouplens.lenskit.knn.params.SimilarityDamping;
import org.grouplens.lenskit.norm.BaselineSubtractingNormalizer;
import org.grouplens.lenskit.norm.VectorNormalizer;
import org.grouplens.lenskit.params.UserVectorNormalizer;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BinFinder {
//    private static final int NUM_USERS = 5000;
    private static final int NUM_USERS = 500;

    private final CSVDataSource dataSource;
    private final PreferenceDomain inDomain;
    private final PreferenceDomain outDomain;

    private List<TTDataSet> splits;

    private PreferenceDomainMapper mapping;     // best current mapping.
    private double mutualInfo = 0.0;            // mutual information from best mapping.


    public BinFinder(CSVDataSource dataSource, PreferenceDomain inDomain, PreferenceDomain outDomain) {
        this.dataSource = dataSource;
        this.inDomain = inDomain;
        this.outDomain = outDomain;
    }

    public void makeSplits() throws EvalTaskFailedException {
        CrossfoldTask task = new CrossfoldTaskBuilder("x-fold")
                .setSource(dataSource)
                .setTrain("bin_finder.train.%d.csv")
                .setTest("bin_finder.test.%d.csv")
                .setPartitions(3)
                .setHoldout(10)
                .build();

        task.execute(new EvalOptions().setForce(true).setThreadCount(-1));
        splits = task.get();
    }

    public double tryNewMappings() throws EvalTaskFailedException {
        List<PreferenceDomainMapper> candidates = new ArrayList<PreferenceDomainMapper>();
        if (mapping == null) {
            candidates.add(new PreferenceDomainMapper(inDomain, outDomain));
        } else {
            int current [] = mapping.getMapping();

            // Try expanding to the left
            for (int i = 1; i < current.length; i++) {
                if (current[i-1] != current[i]) {
                    int adjusted [] = Arrays.copyOf(current, current.length);
                    adjusted[i-1] = current[i];
                    candidates.add(new PreferenceDomainMapper(inDomain, outDomain, adjusted));
                }
            }

            // Try expanding to the right
            for (int i = 0; i < current.length-1; i++) {
                if (current[i] != current[i+1]) {
                    int adjusted [] = Arrays.copyOf(current, current.length);
                    adjusted[i+1] = current[i];
                    candidates.add(new PreferenceDomainMapper(inDomain, outDomain, adjusted));
                }
            }
        }

        PreferenceDomainMapper topCandidate = null;
        double topCandidateMI = -1.0;
        for (PreferenceDomainMapper c : candidates) {
            double mi = evalCandidateMapping(c);
            System.err.println("domain mapper " + Arrays.toString(c.getMapping()) + " returned " + mi);
            if (mi > mutualInfo && mi > topCandidateMI) {
                topCandidateMI = mi;
                topCandidate = c;
            }
        }

        if (topCandidate == null) {
            return 0.0;
        } else {
            double improvement = topCandidateMI / mutualInfo - 1.0;
            mapping = topCandidate;
            mutualInfo = topCandidateMI;

            return improvement;
        }
    }

    private double evalCandidateMapping(PreferenceDomainMapper pdm) throws EvalTaskFailedException {
        TrainTestEvalBuilder ttBuilder = new TrainTestEvalBuilder("ttbuilder");
        MutualInformationMetric metric = new MutualInformationMetric(outDomain, new PreferenceDomain(0.5, 5.0, 0.5));
        ttBuilder.addMetric(metric);
        for (TTDataSet ttds : splits) {
            GenericTTDataSet gttds = (GenericTTDataSet) ttds;
            TTDataSet newTTDs = new GenericTTDataBuilder("ttbuilder")
                    .setTest(new RescaledDataSource(pdm, gttds.getTestData()))
                    .setTrain(new RescaledDataSource(pdm, gttds.getTrainData()))
                    .build();
            ttBuilder.addDataset(newTTDs);
        }
        ttBuilder.setOutput(new File("foo.txt"));
        ttBuilder.setPredictOutput(new File("bar.txt"));

        AlgorithmBuilder algBuilder = new AlgorithmBuilder("algbuilder");
        LenskitRecommenderEngineFactory factory = algBuilder.getFactory();
        algBuilder.getFactory().setComponent(RatingPredictor.class, ItemItemRatingPredictor.class);
        factory.setComponent(UserVectorNormalizer.class, VectorNormalizer.class, BaselineSubtractingNormalizer.class);
        factory.setComponent(BaselinePredictor.class, ItemUserMeanPredictor.class);
        factory.set(SimilarityDamping.class, 100);
        factory.set(NeighborhoodSize.class, 30);

        ttBuilder.addAlgorithm(algBuilder.build());


        TrainTestEvalTask task = ttBuilder.build();
        task.execute(new EvalOptions().setThreadCount(-1));
        return metric.getMean();
    }

    public static void main(String args[]) throws EvalTaskFailedException {

        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(Level.INFO);

        if (args.length != 8) {
            System.err.println("usage: java BinFinder path delimiter " +
                               "input-min input-max input-precision " +
                               "output-min output-max output-precision");
        }

        String path = args[0];
        String delim = args[1].equals("\\t") ? "\t" : args[1];

        PreferenceDomain inDomain = new PreferenceDomain(
                Double.valueOf(args[2]),
                Double.valueOf(args[3]),
                Double.valueOf(args[4]));

        PreferenceDomain outDomain = new PreferenceDomain(
                Double.valueOf(args[5]),
                Double.valueOf(args[6]),
                Double.valueOf(args[7]));

        CSVDataSource ds = new CSVDataSourceBuilder(path)
                                    .setDelimiter(delim)
                                    .setDomain(inDomain)
                                    .build();

        BinFinder main = new BinFinder(ds, inDomain, outDomain);
        main.makeSplits();

        while (main.tryNewMappings() > 0.0) {
            System.err.println("=============================================");
            System.err.println("current best is " + Arrays.toString(main.mapping.getMapping()) + " with MI " + main.mutualInfo);
            System.err.println("=============================================");
        }
        System.err.println("=============================================");
        System.err.println("current best is " + Arrays.toString(main.mapping.getMapping()) + " with MI " + main.mutualInfo);
        System.err.println("=============================================");

    }
}
