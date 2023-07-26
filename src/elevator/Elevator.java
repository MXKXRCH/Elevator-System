package elevator;

import java.util.HashSet;

public class Elevator implements IUpdatable {
    private Floor runningFloor;             //текущий этаж (значение дисплея можно будет получить через эту ссылку)
    private ElevatorState state;            //состояние
    private ElevatorController controller;  //контроллер лифтов
    private boolean isMoveDetected;         //значение датчика
    private HashSet<Floor> destinations;    //список этажей, на которые лифт должен приехать
    //пусть нагруженность измеряется не при помощи веса груза, а при помощи количества людей, для упрощения модели
    private int maxPeopleCount;             //максимальное количество людей, которые могут быть в лифте
    private int peopleCount;                //сколько людей в лифте

    public Elevator(ElevatorController controller) {
        this.state = ElevatorState.WAITING;
        this.isMoveDetected = false;
        this.controller = controller;
        this.destinations = new HashSet<>();
        maxPeopleCount = 3;
    }

    //Public
    @Override
    public void updateState() {
        switch (state) {
            case MOOVING_UP -> {
                setNewFloor(true);
            }
            case MOOVING_DOWN -> {
                setNewFloor(false);
            }
            case OPENNING_DOORS -> {
                state = ElevatorState.STAY_WITH_OPENNED_DOORS;
            }
            case STAY_WITH_OPENNED_DOORS -> {
                if (!isMoveDetected) {
                    state = ElevatorState.CLOSING_DOORS;
                }
            }
            case CLOSING_DOORS -> {
                state = ElevatorState.WAITING;
            }
            default -> {
                if (!destinations.isEmpty()) { //если есть этажи, куда может приехать лифт
                    if (isFloorsUnder()) {
                        state = ElevatorState.MOOVING_DOWN;
                    } else {
                        state = ElevatorState.MOOVING_UP;
                    }
                }
            }
        }
    }

    public boolean addOnePeople() {
        if (peopleCount + 1 <= maxPeopleCount && state == ElevatorState.STAY_WITH_OPENNED_DOORS) {
            peopleCount++;
            isMoveDetected = true;
            return true;
        }
        return false;
    }

    public boolean removeOnePeople() {
        if (peopleCount - 1 >= 0 && state == ElevatorState.STAY_WITH_OPENNED_DOORS) {
            peopleCount--;
            isMoveDetected = true;
            return true;
        }
        return false;
    }

    public int getDistanceToTheFloor(Floor floor) {
        return Math.abs(runningFloor.getLabel() - floor.getLabel());
    }

    public boolean isBusy() {                      //занят ли лифт
        return
                peopleCount == maxPeopleCount ||    //если полностью нагружен, то да
                state == ElevatorState.MOOVING_UP;  //если едет вверх - тоже
                //нет смысла останавливаться на каждом этаже при движении вверх,
                //вероятнее всего, люди будут стремиться спуститься вниз
                //тогда, для оптимизации времени перевозки, будет возможность везти нескольких людей с более высоких
                //этажей на первый
    }


    public void unpressFloorButton(int i) { //убрать нажатие кнопки этажа
        try {
            destinations.remove(controller.getFloorByLabel(i));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void pressFloorButton(int i) { //нажать кнопку этажа
        try {
            destinations.add(controller.getFloorByLabel(i));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Private
    private void checkFloor() { //проверка, можно ли остановиться на текущем этаже
        if (destinations.contains(runningFloor)) {
            destinations.remove(runningFloor);
            runningFloor.setButtonPressed(false);
            state = ElevatorState.OPENNING_DOORS;
        }
    }

    private boolean isFloorsUnder() { //проверка, есть ли активные этажи внизу
        if (runningFloor.getLabel() == 1) {
            return false;
        }
        for (Floor floor : destinations) {
            if (floor.getLabel() < runningFloor.getLabel()) {
                return true;
            }
        }
        return false;
    }

    private void setNewFloor(boolean isMovingUp) { //назначение текущего этажа
        Floor floor = controller.getNextFloor(runningFloor, isMovingUp);
        if (floor == null) {
            state = ElevatorState.WAITING;
        } else  {
            runningFloor = floor;
            if (!isMovingUp) {
                checkFloor();
            }
        }
    }

    //Getters/setters
    public Floor getRunningFloor() {
        return runningFloor;
    }

    public void setRunningFloor(Floor runningFloor) {
        this.runningFloor = runningFloor;
    }

    public ElevatorState getState() {
        return state;
    }

    public void setState(ElevatorState state) {
        this.state = state;
    }

    public int getMaxPeopleCount() {
        return maxPeopleCount;
    }

    public void setMaxPeopleCount(int maxPeopleCount) {
        this.maxPeopleCount = maxPeopleCount;
    }
}
