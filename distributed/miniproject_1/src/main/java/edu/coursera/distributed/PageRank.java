package edu.coursera.distributed;

import org.apache.spark.api.java.JavaPairRDD;
import scala.Tuple2;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A wrapper class for the implementation of a single iteration of the iterative
 * PageRank algorithm.
 */
public final class PageRank {
    /**
     * Default constructor.
     */
    private PageRank() {
    }

    /**
     * TODO Given an RDD of websites and their ranks, compute new ranks for all
     * websites and return a new RDD containing the updated ranks.
     * <p>
     * Recall from lectures that given a website B with many other websites
     * linking to it, the updated rank for B is the sum over all source websites
     * of the rank of the source website divided by the number of outbound links
     * from the source website. This new rank is damped by multiplying it by
     * 0.85 and adding that to 0.15. Put more simply:
     * <p>
     * new_rank(B) = 0.15 + 0.85 * sum(rank(A) / out_count(A)) for all A linking to B
     * <p>
     * For this assignment, you are responsible for implementing this PageRank
     * algorithm using the Spark Java APIs.
     * <p>
     * The reference solution of sparkPageRank uses the following Spark RDD
     * APIs. However, you are free to develop whatever solution makes the most
     * sense to you which also demonstrates speedup on multiple threads.
     * <p>
     * 1) JavaPairRDD.join
     * 2) JavaRDD.flatMapToPair
     * 3) JavaPairRDD.reduceByKey
     * 4) JavaRDD.mapValues
     *
     * @param sites The connectivity of the website graph, keyed on unique
     *              website IDs.
     * @param ranks The current ranks of each website, keyed on unique website
     *              IDs.
     * @return The new ranks of the websites graph, using the PageRank
     * algorithm to update site ranks.
     */
    public static JavaPairRDD<Integer, Double> sparkPageRank(
            final JavaPairRDD<Integer, Website> sites,
            final JavaPairRDD<Integer, Double> ranks) {

        JavaPairRDD<Integer, Double> newRanks = sites.join(ranks).flatMapToPair(kv -> {
            Website edges = kv._2._1;
            Double currentRank = kv._2()._2();

            List<Tuple2<Integer, Double>> contribs = new LinkedList<>();
            Iterator<Integer> iterator = edges.edgeIterator();

            while (iterator.hasNext()) {
                final Integer target = iterator.next();
                double computedRank = currentRank / edges.getNEdges();
                contribs.add(new Tuple2<>(target, computedRank));
            }

            return contribs;
        });

        return newRanks.reduceByKey((Double r1, Double r2) -> r1 + r2)
                .mapValues(v -> 0.15 + 0.85 * v);
    }
}
