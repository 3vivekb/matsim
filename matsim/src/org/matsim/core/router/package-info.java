/**
 * Contains different routing algorithms and {@linkplain org.matsim.population.algorithms.PlanAlgorithm PlanAlgorithms}
 * to use the routing algorithms on plans.
 * <br>
 * The routing algorithms, responsible for finding the least-cost-path between two nodes in the network, all
 * implement the interface {@link org.matsim.core.router.util.LeastCostPathCalculator}. Currently implemented are
 * {@linkplain PlansCalcRouteDijkstra Dijkstra's shortest path algorithm} and some optimizing variants of it
 * (e.g. {@linkplain AStarLandmarks A* with Landmarks}).
 * <br>
 * As the routing algorithms are all time-dependent, they need not only weights on the links, but time-dependent
 * weights and additionally the (estimated) travel times on these links. This data is provided by the interfaces
 * {@link org.matsim.core.router.util.TravelTime}, {@link org.matsim.core.router.util.TravelCost} and
 * {@link org.matsim.core.router.util.TravelMinCost}. A few commonly used implementations of these interfaces can
 * be found in the subpackage {@link org.matsim.core.router.costcalculators costcalculators}.
 * <br>
 * Nearly in all cases, the routes of all legs of a plan should be calculated. Thus, for the most commonly used
 * routing algorithms, there are wrapper classes that wrap the routing algorithm into a
 * {@link org.matsim.population.algorithms.PlanAlgorithm} for easy usage (e.g.
 * {@link org.matsim.router.PlansCalcRouteDijkstra} and {@link org.matsim.router.PlansCalcRouteLandmarks}).
 * <br>
 * Note that the routing algorithms are generally seen as <b>not thread-safe</b>! If threads are used, one
 * must ensure that each thread uses its own instance of a routing algorithm.
 */
package org.matsim.core.router;
