package elevator.exceptions;

public class NoSuchFloorException extends Exception {
    public NoSuchFloorException(int label, int maxFloor) {
        super(
                "Invalid floor: " + label + "\n"
                + "The minimum floor is 1\n"
                + "The maximum floor is " + maxFloor
        );
    }
}
