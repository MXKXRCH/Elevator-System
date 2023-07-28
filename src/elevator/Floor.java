package elevator;

import elevator.exceptions.NoSuchFloorException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

//Класс этажа
public class Floor implements IUpdatable {
    private boolean isButtonPressed;            //нажата ли кнопка
    private int label;                          //номер этажа

    private final Elevator[] elevators;         //значение состояний и дисплеев можно будет получить через этот массив
    //на случай, если будет много лифтов, для избавления от избыточных полей
    private Elevator waitedElevator;            //ожидаемый лифт
    private final Queue<Integer> wishedFloors;
    private final ElevatorController controller;//главный контроллер лифтов для всего дома

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
        if (waitedElevator == null && isButtonPressed) {    //поиск лифта (если кнопка нажата)
            System.out.println("На этаже " + label + " производится поиск свободного лифта");
            getTheNearestFreeElevator();
        }
        if (!isButtonPressed && waitedElevator != null) {
            if (waitedElevator.getRunningFloor() == this) { //проверка, находится ли найденный лифт на этаже
                if (waitedElevator.getState() == ElevatorState.STAY_WITH_OPENED_DOORS && !wishedFloors.isEmpty()) {
                    waitedElevator.pressFloorButton(wishedFloors.remove());
                    waitedElevator.setIsMoveDetected(false);
                    clearWaitedElevator();
                }
            } else  {
                clearWaitedElevator();
            }
        }
    }

    //Основные методы
    //Нажать кнопку вызова лифта
    public void pressTheButton() {
        System.out.println("На этаже " + label + " нажата кнопка");
        isButtonPressed = true;
    }

    //Получить статус кабины лифта по индексу
    public ElevatorState getElevatorState(int n) {
        try {
            return elevators[n].getState();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Получить этаж кабины лифта по индексу
    public Floor getElevatorFloor(int n) {
        try {
            return elevators[n].getRunningFloor();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    //Статус кнопки вызова лифта
    public boolean isButtonPressed() {
        return isButtonPressed;
    }

    //Вспомогательные методы
    public void addWishedFloor(int n) throws NoSuchFloorException { //добавить желаемый этаж (для моделирования)
        System.out.println("На этаже " + label + " изъявлено желание отправиться на " + n + " этаж");
        controller.getFloorByLabel(n);
        wishedFloors.add(n);
        pressTheButton();
    }

    public String displayInfo() { //получить информацию для всех дисплеев
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

    public void clearFloor() { //сброс состояния
        clearWaitedElevator();
        isButtonPressed = false;
    }

    public void clearWaitedElevator() { //очистка информации о лифте
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
