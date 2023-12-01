package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;
import bg.sofia.uni.fmi.mjt.itinerary.graph.WeightedGraphBase;
import bg.sofia.uni.fmi.mjt.itinerary.vehicle.VehicleType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SequencedCollection;
import java.util.Set;

public class RideRight extends WeightedGraphBase<City, Journey> implements ItineraryPlanner {

    private List<Journey> schedule;
    private Set<City> cities;
    private Map<City, Journey> parent;
    private Map<City, BigDecimal> priceTo;
    private Set<City> visitedCities;

    public RideRight() {

        super();
        schedule = new ArrayList<>();

    }

    public RideRight(List<Journey> schedule) {

        super();
        this.schedule = new ArrayList<>();
        this.schedule = schedule;

        this.cities = new HashSet<>();
        for (Journey journey : schedule) {
            cities.add(journey.from());
            cities.add(journey.to());
        }

        initializeGraphFromList();

    }

    private void initializeGraphFromList() {

        for (Journey edge : schedule) {
            addEdge(edge.from(), edge);
        }

    }

    @Override
    public SequencedCollection<Journey> findCheapestPath(City start, City destination, boolean allowTransfer)
        throws NoPathToDestinationException, CityNotKnownException {

        SequencedCollection<Journey> cheapestPath = new ArrayList<Journey>();
        if (start.equals(destination)) {
            return cheapestPath;
        }

        if (!cities.contains(start) || !cities.contains(destination)) {
            throw new CityNotKnownException("City doesn't exist");
        }

        if (!allowTransfer && !hasConnection(start, destination)) {
            throw new NoPathToDestinationException("Can't find a path to destination");
        } else if (!allowTransfer && hasConnection(start, destination)) {

            SequencedCollection<Journey> cheapestDirectConnection = findCheapestDirectConnection(start, destination);
            return Collections.unmodifiableSequencedCollection(cheapestDirectConnection);
        }

        aStarAlgorithm(start, destination);

        if (!parent.containsKey(destination)) {
            throw new NoPathToDestinationException("Can't find a path to destination");
        }

        cheapestPath = recoverRoute(start, destination);

        return Collections.unmodifiableSequencedCollection(cheapestPath);

    }

    private SequencedCollection<Journey> findCheapestDirectConnection(City start, City destination) {

        Journey currentOptimum = null;
        List<Journey> neighbours = edges.get(start);

        for (Journey currentJourney : neighbours) {
            if (currentJourney.to().equals(destination)) {
                if (currentOptimum == null) {
                    currentOptimum = currentJourney;
                } else {
                    BigDecimal currentPrice = currentOptimum.price();
                    currentPrice = currentPrice.add(currentPrice.multiply(currentOptimum.vehicleType().getGreenTax()));

                    BigDecimal newPrice = currentJourney.price();
                    newPrice = newPrice.add(newPrice.multiply(currentJourney.vehicleType().getGreenTax()));

                    if (currentPrice.compareTo(newPrice) > 0) {
                        currentOptimum = currentJourney;
                    }
                }
            }
        }

        SequencedCollection<Journey> toReturn = new ArrayList<Journey>();
        toReturn.add(currentOptimum);
        return toReturn;

    }

    private boolean hasConnection(City start, City destination) {

        List<Journey> neighbours = new ArrayList<Journey>();
        neighbours = edges.get(start);

        for (Journey currentJourney : neighbours) {
            if (currentJourney.to().equals(destination)) {
                return true;
            }
        }

        return false;

    }

    private void aStarAlgorithm(City start, City destination) {

        initializeAStarStructures();
        PriorityQueue<AStarNode<City>> cityQueue = new PriorityQueue<AStarNode<City>>();

        assignNeighbourAndPrice(start, new Journey(VehicleType.BUS, start, start, new BigDecimal(0)),
            new BigDecimal(0));

        BigDecimal heuristic = start.calculateHeuristic(destination);
        cityQueue.add(new AStarNode<City>(start, heuristic));

        while (!cityQueue.isEmpty()) {
            AStarNode<City> currentNode = cityQueue.peek();
            cityQueue.remove(currentNode);

            if (visitedCities.contains(currentNode.node)) {
                continue;
            }
            visitedCities.add(currentNode.node);

            List<Journey> neighbours = edges.get(currentNode.node);

            for (Journey currentJourney : neighbours) {

                completeChecks(cityQueue, currentNode, currentJourney, destination);

            }
        }

    }

    private void completeChecks(PriorityQueue<AStarNode<City>> cityQueue, AStarNode<City> currentNode,
                                Journey currentJourney, City destination) {

        City neighbour = currentJourney.to();
        BigDecimal newPrice = calculateNewPriceToNode(currentNode, currentJourney);

        if (priceTo.get(neighbour) == null) {

            assignNeighbourAndPrice(neighbour, currentJourney, newPrice);
            updatePriorityQueue(cityQueue, neighbour, newPrice, destination);

        } else if (priceTo.get(neighbour).compareTo(newPrice) >= 0) {

            if (priceTo.get(neighbour).compareTo(newPrice) == 0) {
                if (!compareParents(neighbour, currentNode.node.name())) {
                    return;
                }
            }
            updateNeighbourAndPrice(neighbour, currentJourney, newPrice);
            updatePriorityQueue(cityQueue, neighbour, newPrice, destination);

        }

    }

    private void initializeAStarStructures() {

        parent = new HashMap<City, Journey>();
        priceTo = new HashMap<City, BigDecimal>();
        visitedCities = new HashSet<City>();

    }

    private void updatePriorityQueue(PriorityQueue<AStarNode<City>> cityQueue,
                                     City neighbour, BigDecimal newPrice, City destination) {

        BigDecimal heuristic = newPrice.add(neighbour.calculateHeuristic(destination));
        cityQueue.add(new AStarNode<City>(neighbour, heuristic));
    }

    private BigDecimal calculateNewPriceToNode(AStarNode<City> currentNode, Journey currentJourney) {

        BigDecimal journeyPrice = currentJourney.price(); ///current edge
        journeyPrice = journeyPrice.add(journeyPrice.multiply(currentJourney.vehicleType().getGreenTax()));
        return priceTo.get(currentNode.node).add(journeyPrice); ///price to parentNode + edge

    }

    private void assignNeighbourAndPrice(City neighbour, Journey currentJourney, BigDecimal newPrice) {

        parent.put(neighbour, currentJourney);
        priceTo.put(neighbour, newPrice);

    }

    private void updateNeighbourAndPrice(City neighbour, Journey currentJourney, BigDecimal newPrice) {

        parent.replace(neighbour, currentJourney);
        priceTo.replace(neighbour, newPrice);

    }

    private boolean compareParents(City neighbour, String name) {

        return parent.get(neighbour).from().name().compareTo(name) > 0;

    }

    private SequencedCollection<Journey> recoverRoute(City start, City destination) {

        List<Journey> finalRoute = new ArrayList<Journey>();

        City target = destination;
        while (!target.equals(parent.get(target).from())) {
            finalRoute.add(parent.get(target));
            target = parent.get(target).from();
        }

        Collections.reverse(finalRoute);

        return finalRoute;

    }

    public void printRides() {

        for (Map.Entry<City, List<Journey>> cityJourneyList : edges.entrySet()) {
            List<Journey> journeyList = cityJourneyList.getValue();
            System.out.println(cityJourneyList.getKey().name());

            for (Journey currentJourney : journeyList) {
                System.out.printf("%s %s %n", currentJourney.to().name(),
                    currentJourney.vehicleType().toString());
            }
            System.out.println("/------------------------------------");
        }

    }

}