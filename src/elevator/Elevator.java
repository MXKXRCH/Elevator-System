package elevator;

import java.util.HashSet;

public class Elevator implements IUpdatable {
    private int id;                     //id лифта
    private Floor runningFloor;             //текущий этаж (значение дисплея можно будет получить через эту ссылку)
    private ElevatorState state;            //состояние
    private ElevatorController controller;  //контроллер лифтов
    private boolean isMoveDetected;         //значение датчика
    private HashSet<Floor> destinations;    //список этажей, на которые лифт должен приехать
    private Floor runningDestination;       //текущий этаж назначения
    private int endLabel = 0;

    public Elevator(ElevatorController controller, int id) {
        this.id = id;
        this.state = ElevatorState.WAITING;
        this.isMoveDetected = false;
        this.controller = controller;
        this.destinations = new HashSet<>();
    }

    //Public
    @Override
    public void updateState() {
        StringBuilder message = new StringBuilder();
        switch (state) {
            case MOOVING_UP -> {
                message.append("Лифт №" + id + " движется вверх");
                setNewFloor(true);
            }
            case MOOVING_DOWN -> {
                message.append("Лифт №" + id + " движется ввниз");
                setNewFloor(false);
            }
            case OPENNING_DOORS -> {
                message.append("Лифт №" + id + " открыл двери");
                state = ElevatorState.STAY_WITH_OPENNED_DOORS;
            }
            case STAY_WITH_OPENNED_DOORS -> {
                message.append("Лифт №" + id + " стоит с открытыми дверьми, проверка датчика");
                if (!isMoveDetected) {
                    state = ElevatorState.CLOSING_DOORS;
                }
            }
            case CLOSING_DOORS -> {
                message.append("Лифт №" + id + " закрывает двери");
                state = ElevatorState.WAITING;
            }
            default -> {
                message.append("Лифт №" + id + " ожидает команды");
                if (!destinations.isEmpty()) { //если есть этажи, куда может приехать лифт
                    getNearestDestination();
                    if (runningFloor == runningDestination || runningDestination == null) {
                        checkFloor();
                        return;
                    }
                    if (isFloorUnder()) {
                        state = ElevatorState.MOOVING_DOWN;
                    } else {
                        state = ElevatorState.MOOVING_UP;
                    }
                } else {
                    checkFloor();
                }
            }
        }
        message.append(". Текущий этаж: ")
                .append(runningFloor.getLabel());
        System.out.println(message);
    }

    public void addDestination(Floor floor) {
        if (runningDestination == null) {
            runningDestination = floor;
        }
        destinations.add(floor);
    }

    public void removeDestination(Floor floor) {
        destinations.remove(floor);
    }

    public int getDistanceToTheFloor(Floor floor) { //растояние до этажа
        return Math.abs(runningFloor.getLabel() - floor.getLabel());
    }

    public boolean isBusy() { //занят ли лифт
        return state != ElevatorState.WAITING;
    }

    public void pressFloorButton(int i) { //нажать кнопку этажа
        try {
            destinations.add(controller.getFloorByLabel(i));
            System.out.println("В лифте №" + id + " нажата кнопка " + i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Private
    private void getNearestDestination() { //получить близжайший пункт назначения
        destinations.remove(runningFloor);
        runningDestination = null;
        for(Floor floor : destinations) {
            if (
                    runningDestination == null ||
                    getDistanceToTheFloor(floor) < getDistanceToTheFloor(runningDestination)
            ) {
                runningDestination = floor;
            }
        }
    }

    private boolean isFloorUnder() { //есть ли этажи внизу
        return runningFloor.getLabel() - runningDestination.getLabel() > 0;
    }

    private void checkFloor() { //проверка, можно ли остановиться на текущем этаже
        if (destinations.contains(runningFloor) || runningFloor.isButtonPressed()) {
            destinations.remove(runningFloor);
            runningFloor.clearFloor();
            System.out.println("Лифт №" + id + " начал открывать двери. Текущий этаж: " + runningFloor.getLabel());
            state = ElevatorState.OPENNING_DOORS;
            runningFloor.setWaitedElevator(this);
        }
    }

    private void setNewFloor(boolean isMovingUp) { //назначение текущего этажа
        Floor floor = controller.getNextFloor(runningFloor, isMovingUp);
        if (floor == null || !destinations.contains(runningDestination)) {
            state = ElevatorState.WAITING;
        } else  {
            runningFloor = floor;
            checkFloor();
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

    public void setIsMoveDetected(boolean val) {
        this.isMoveDetected = val;
    }

    public void setEndLabel(int endLabel) {
        this.endLabel = endLabel;
    }

    public int getId() {
        return id;
    }
}
