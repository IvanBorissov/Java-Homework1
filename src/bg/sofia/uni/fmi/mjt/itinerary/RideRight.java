package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.exception.CityNotKnownException;
import bg.sofia.uni.fmi.mjt.itinerary.exception.NoPathToDestinationException;
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

    List<Journey> schedule;
    Set<City> cities;
    Map<City, Journey> parrent;
    Map<City, BigDecimal> priceTo;

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

    @Override
    public SequencedCollection<Journey> findCheapestPath(City start, City destination, boolean allowTransfer)
        throws NoPathToDestinationException, CityNotKnownException {

        if (!cities.contains(start) || !cities.contains(destination)) {
            throw new CityNotKnownException("City doesn't exist");
        }

        if (!allowTransfer && !hasConnection(start, destination)) {
            throw new NoPathToDestinationException("Can't find a path to destination");
        }

        SequencedCollection<Journey> cheapestPath = new ArrayList<Journey>();
        aStarAlgorithm(start, destination);

        if (!parrent.containsKey(destination)) {
            throw new NoPathToDestinationException("Can't find a path to destination");
        }

        cheapestPath = recoverRoute(start, destination);

        return Collections.unmodifiableSequencedCollection(cheapestPath);

    }

    private boolean hasConnection(City start, City destination) {

        List<Journey> neighbours = new ArrayList<Journey>();
        neighbours = vertices.get(start);

        for (Journey currentJourney : neighbours) {
            if (currentJourney.to().equals(destination)) {
                return true;
            }
        }

        return false;

    }

    private void aStarAlgorithm(City start, City destination) {

        parrent = new HashMap<City, Journey>();
        priceTo = new HashMap<City, BigDecimal>();
        Set<City> usedCities = new HashSet();
        PriorityQueue<AStarNode<City>> cityQueue = new PriorityQueue<AStarNode<City>>();

        parrent.put(start, new Journey(VehicleType.BUS, start, start, new BigDecimal(0)));
        priceTo.put(start, BigDecimal.valueOf(0));
        BigDecimal heuristic = BigDecimal.valueOf(start.calculateHeuristic(destination));
        cityQueue.add(new AStarNode<City>(start, heuristic));

        while (!cityQueue.isEmpty()) {
            AStarNode<City> currentNode = cityQueue.peek();
            cityQueue.remove(currentNode);

            if (usedCities.contains(currentNode.node)) {
                continue;
            }

            usedCities.add(currentNode.node);

            List<Journey> neighbours = vertices.get(currentNode.node);

            for (Journey currentJourney : neighbours) {

                City neighbour = currentJourney.to();
                BigDecimal journeyPrice = currentJourney.price();
                journeyPrice = journeyPrice.add(journeyPrice.multiply(currentJourney.vehicleType().getGreenTax()));
                BigDecimal newPrice = priceTo.get(currentNode.node).add(journeyPrice);

                if (priceTo.get(neighbour) == null) {

                    parrent.put(neighbour, currentJourney);
                    priceTo.put(neighbour, newPrice);
                    heuristic = newPrice.add(BigDecimal.valueOf(neighbour.calculateHeuristic(destination)));
                    cityQueue.add(new AStarNode<City>(neighbour, heuristic));

                } else if (priceTo.get(neighbour).compareTo(newPrice) >= 0) {

                    parrent.replace(neighbour, currentJourney);
                    priceTo.replace(neighbour, newPrice);
                    heuristic = newPrice.add(BigDecimal.valueOf(neighbour.calculateHeuristic(destination)));
                    cityQueue.add(new AStarNode<City>(neighbour, heuristic));

                }
            }

        }

    }

    private SequencedCollection<Journey> recoverRoute(City start, City destination) {

        List<Journey> finalRoute = new ArrayList<Journey>();

        City target = destination;
        while (!target.equals(parrent.get(target).from())) {
            finalRoute.add(parrent.get(target));
            target = parrent.get(target).from();
        }

        Collections.reverse(finalRoute);

        return finalRoute;

    }

    public void printRides() {

        for (Map.Entry<City, List<Journey>> cityJourneyList : vertices.entrySet()) {
            List<Journey> journeyList = cityJourneyList.getValue();
            System.out.println(cityJourneyList.getKey().name());

            for (Journey currentJourney : journeyList) {
                System.out.printf("%s %s %n", currentJourney.to().name(),
                    currentJourney.vehicleType().toString());
                //System.out.println(currentJourney.vehicleType().toString());
                //System.out.println(currentJourney.toString());
            }
            System.out.println("/------------------------------------");
        }

    }

    private void initializeGraphFromList() {

        for (Journey edge : schedule) {
            addEdge(edge.from(), edge);
        }

    }

}