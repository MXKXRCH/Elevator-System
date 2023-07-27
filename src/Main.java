import elevator.ElevatorController;

public class Main {

    public static void main(String[] args) {
        //Заполнение параметров
	    ElevatorController controller = new ElevatorController(20, 2);
        try {
            controller.addWishedFloors(1, 14);
            controller.addWishedFloors(15, 1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        /*
        * Пусть обновление информации происходит за один шаг симуляции
        * Допустим, что время открытия/закрытия дверей и входа/выхода человека будет едино
        * и равно времени одного шага
        * Аналогично со скоростью проезда лифтом одного этажа
        * Будем считать, что скорость движения обоих лифтов одинакова
        */
        int simStep = 1;
        while (simStep <= 38) {
            System.out.println("Шаг №" + simStep);
            controller.updateState();
            simStep++;
        }
    }
}
