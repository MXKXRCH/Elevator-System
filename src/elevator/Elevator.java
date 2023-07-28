package elevator;

import java.util.HashSet;

//Класс кабины лифта
public class Elevator implements IUpdatable {
    private final int id;                       //id лифта
    private final HashSet<Floor> destinations;  //список этажей, на которые лифт должен приехать
    private final ElevatorController controller;//контроллер лифтов

    private boolean isMoveDetected;             //значение датчика
    private Floor runningFloor;                 //текущий этаж (значение дисплея можно будет получить через эту ссылку)
    private ElevatorState state;                //состояние
    private Floor runningDestination;           //текущий этаж назначения

    private boolean noNeedMessage;              //служебное поле для логгирования

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
            case MOVING_UP -> {
                message.append("Лифт №").append(id).append(" движется вверх");
                setNewFloor(true);
            }
            case MOVING_DOWN -> {
                message.append("Лифт №").append(id).append(" движется ввниз");
                setNewFloor(false);
            }
            case OPENING_DOORS -> {
                message.append("Лифт №").append(id).append(" открыл двери");
                state = ElevatorState.STAY_WITH_OPENED_DOORS;
            }
            case STAY_WITH_OPENED_DOORS -> {
                message.append("Лифт №").append(id).append(" стоит с открытыми дверьми, проверка датчика");
                if (!isMoveDetected) {
                    state = ElevatorState.CLOSING_DOORS;
                }
            }
            case CLOSING_DOORS -> {
                message.append("Лифт №").append(id).append(" закрывает двери");
                state = ElevatorState.WAITING;
            }
            default -> {
                message.append("Лифт №").append(id).append(" ожидает команды");
                if (!destinations.isEmpty()) { //если есть этажи, куда может приехать лифт
                    getNearestDestination();
                    if (runningFloor == runningDestination || runningDestination == null) {
                        checkFloor();
                        return;
                    }
                    if (isFloorUnder()) {
                        state = ElevatorState.MOVING_DOWN;
                    } else {
                        state = ElevatorState.MOVING_UP;
                    }
                } else {
                    checkFloor();
                }
            }
        }
        message.append(". Текущий этаж: ")
                .append(runningFloor.getLabel());
        if (noNeedMessage) {
            noNeedMessage = false;
        } else {
            System.out.println(message);
        }
    }

    //Основные методы
    //Нажать кнопку этажа
    public void pressFloorButton(int i) {
        try {
            destinations.add(controller.getFloorByLabel(i));
            System.out.println("В лифте №" + id + " нажата кнопка " + i);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    //Кнопка закрытия дверей
    public void pressClosingDoorsButton() {
        if (state == ElevatorState.STAY_WITH_OPENED_DOORS && !isMoveDetected) {
            state = ElevatorState.CLOSING_DOORS;
        }
    }

    //Кнопка открытия дверей
    public void pressOpeningDoorsButton() {
        if (state == ElevatorState.WAITING || state == ElevatorState.CLOSING_DOORS) {
            state = ElevatorState.OPENING_DOORS;
        }
    }

    //Кнопка вызова диспетчера (сброс состояния)
    public void pressCallButton() {
        //Симуляция работы диспетчера
        destinations.clear();
        //Устранение поломок/неисправностей
        state = ElevatorState.WAITING;
        for (Floor floor : controller.getFloors()) {
            if (floor.getWaitedElevator() == this) {
                floor.clearFloor();
            }
            //Необходимо пройтись по всем этажам, тк ошибка в работе
            //могла возникнуть из-за одновременного ожидания на разных этажах
        }
    }

    //Значение датчика
    public boolean isMoveDetected() {
        return this.isMoveDetected;
    }

    //Вспомогательные методы
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
        if (
            destinations.contains(runningFloor) || //если в назначенных этажах есть текущий или на этаже нажата кнопка
            (runningFloor.isButtonPressed() && state != ElevatorState.MOVING_UP)  //и лифт едет вниз
        ) {
            destinations.remove(runningFloor);
            runningFloor.clearFloor();
            System.out.println("Лифт №" + id + " начал открывать двери. Текущий этаж: " + runningFloor.getLabel());
            state = ElevatorState.OPENING_DOORS;
            runningFloor.setWaitedElevator(this);
            noNeedMessage = true;
        }
        //при движении вверх нет смысла останавливаться на тех этажах, которые не указаны в назначенных, так как
        //люди с большей вероятностью будут желать спуститься
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

    public int getId() {
        return id;
    }
}
