package elevator;

public class Floor implements IUpdatable {
    private boolean isButtonPressed;        //нажата ли кнопка
    private int label;                      //номер этажа
    private Elevator[] elevators;           //значение состояний и дисплеев можно будет получить через этот массив
                                            //на случай, если будет много лифтов, для избавления от избыточных полей
    private Elevator waitedElevator;        //ожидаемый лифт
    private ElevatorController controller;  //главный контроллер лифтов для всего дома

    public Floor(int label, Elevator[] elevators, ElevatorController controller) {
        this.elevators = elevators;
        this.label = label;
        this.controller = controller;
        isButtonPressed = false;
    }

    //Public
    @Override
    public void updateState() {
        if (waitedElevator == null && isButtonPressed) {
            getTheNearestFreeElevator();
        }
        if (waitedElevator != null) {
            if (waitedElevator.isBusy() || !isButtonPressed) {
                clearWaitedElevator();
            }
        }
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
        isButtonPressed = true;
    }

    //Private
    private void getTheNearestFreeElevator() {  //близжайший свободный лифт
        clearWaitedElevator();
        for (Elevator elevator : elevators) {
            if (!elevator.isBusy()) {
                if (
                        waitedElevator == null ||
                                waitedElevator.getDistanceToTheFloor(this) > elevator.getDistanceToTheFloor(this)
                ) {
                    waitedElevator = elevator;
                }
            }
        }
    }

    private void clearWaitedElevator() {
        if (waitedElevator != null) {
            waitedElevator.unpressFloorButton(label);
            waitedElevator = null;
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
}
