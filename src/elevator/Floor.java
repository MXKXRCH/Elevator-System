package elevator;

import elevator.exceptions.NoSuchFloorException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Floor implements IUpdatable {
    private boolean isButtonPressed;        //нажата ли кнопка
    private int label;                      //номер этажа
    private Elevator[] elevators;           //значение состояний и дисплеев можно будет получить через этот массив
                                            //на случай, если будет много лифтов, для избавления от избыточных полей
    private Elevator waitedElevator;        //ожидаемый лифт
    private ElevatorController controller;  //главный контроллер лифтов для всего дома

    private Queue<Integer> wishedFloors;

    public Floor(int label, Elevator[] elevators, ElevatorController controller) {
        this.elevators = elevators;
        this.label = label;
        this.controller = controller;
        isButtonPressed = false;
        wishedFloors = new ConcurrentLinkedQueue<>();
    }

    //Public
    @Override
    public void updateState() {
        if (waitedElevator == null && isButtonPressed) {
            System.out.println("На этаже " + label + " производится поиск свободного лифта");
            getTheNearestFreeElevator();
        }
        if (!isButtonPressed && waitedElevator != null) {
            if (waitedElevator.getRunningFloor() == this) {
                if (waitedElevator.getState() == ElevatorState.STAY_WITH_OPENNED_DOORS && !wishedFloors.isEmpty()) {
                    waitedElevator.pressFloorButton(wishedFloors.remove());
                    waitedElevator.setIsMoveDetected(false);
                    clearWaitedElevator();
                }
            } else  {
                clearWaitedElevator();
            }
        }
    }

    public void addWishedFloor(int n) throws NoSuchFloorException {
        System.out.println("На этаже " + label + " изъявлено желание отправиться на " + n + " этаж");
        controller.getFloorByLabel(n);
        wishedFloors.add(n);
        pressTheButton();
    }

    public String displayInfo() { //Получить информацию для всех дисплеев
        StringBuilder buf = new StringBuilder();
        buf.append("Информация для этажа №").append(label);
        for (int i = 0; i < elevators.length; i++) {
            buf
                    .append("\t - Лифт №")
                    .append(i)
                    .append(" на этаже ")
                    .append(elevators[i].getRunningFloor().getLabel())
                    .append("\n");
        }
        return buf.toString();
    }

    public void pressTheButton() {  //нажать кнопку
        System.out.println("На этаже " + label + " нажата кнопка");
        isButtonPressed = true;
    }

    public void clearFloor() {
        clearWaitedElevator();
        isButtonPressed = false;
    }

    public void clearWaitedElevator() {
        if (waitedElevator != null) {
            waitedElevator.removeDestination(this);
            waitedElevator = null;
        }
    }

    //Private
    private void getTheNearestFreeElevator() {  //близжайший свободный лифт
        clearWaitedElevator();
        for (Elevator elevator : elevators) {
            if (!elevator.isBusy() && !controller.isElevatorBused(elevator)) {
                if (
                        waitedElevator == null ||
                        waitedElevator.getDistanceToTheFloor(this) > elevator.getDistanceToTheFloor(this)
                ) {
                    waitedElevator = elevator;
                }
            }
        }
        if (waitedElevator != null) {
            waitedElevator.addDestination(this);
            System.out.println("На этаже " + label + " выбран лифт №" + waitedElevator.getId());
        }
    }

    //Getters/setters
    public boolean isButtonPressed() {
        return isButtonPressed;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public void setButtonPressed(boolean buttonPressed) {
        isButtonPressed = buttonPressed;
    }

    public void setWaitedElevator(Elevator elevator) {
        this.waitedElevator = elevator;
    }

    public Elevator getWaitedElevator() {
        return this.waitedElevator;
    }
}
