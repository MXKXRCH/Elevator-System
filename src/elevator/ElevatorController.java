package elevator;

import elevator.exceptions.NoSuchFloorException;

public class ElevatorController implements IUpdatable {
    //массивы выбраны, чтобы можно было задать любое количество лифтов и этажей
    private Floor[] floors;         //список всех этажей
    private Elevator[] elevators;   //список всех лифтов

    public ElevatorController(int floorsCount, int elevatorsCount) {
        this.elevators = new Elevator[elevatorsCount];
        for (int i = 0; i < elevators.length; i++) {
            elevators[i] = new Elevator(this);
        }
        this.floors = new Floor[floorsCount];
        for (int i = 0; i < floorsCount; i++) {
            floors[i] = new Floor(i + 1, elevators, this);
        }
    }

    @Override
    public void updateState() {
        for (Elevator elevator : elevators) {
            elevator.updateState();
        }
        for (Floor floor : floors) {
            floor.updateState();
        }
    }

    //Public
    public void setMaxPeopleCount(int count, int elevatorId) {
        if (elevatorId < 0 || elevatorId >= elevators.length || count <= 0) {
            System.out.println("Cant set max people count");
        } else {
            elevators[elevatorId].setMaxPeopleCount(count);
        }
    }

    public Floor getNextFloor(Floor floor, boolean isMovingUp) {
        try {
            return isMovingUp ? getFloorByLabel(floor.getLabel() + 1) : getFloorByLabel(floor.getLabel() - 1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return floor;
    }

    public Floor getFloorByLabel(int label) throws NoSuchFloorException {
        if (label < 1 || label > floors.length) {
            throw new NoSuchFloorException(label, floors.length);
        }
        return floors[label - 1];
    }

    //Getters/setters
    public Floor[] getFloors() {
        return floors;
    }

    public void setFloors(Floor[] floors) {
        this.floors = floors;
    }

    public Elevator[] getElevators() {
        return elevators;
    }

    public void setElevators(Elevator[] elevators) {
        this.elevators = elevators;
    }
}