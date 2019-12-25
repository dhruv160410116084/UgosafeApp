package droidad.contract.carrental.Holders;

public class CarHolder {
    String carName ;
    int seater;
    int price;

    public CarHolder(String carName, int seater, int price) {
        this.carName = carName;
        this.seater = seater;
        this.price = price;
    }

    public void setCarName(String carName) {
        this.carName = carName;
    }

    public void setSeater(int seater) {
        this.seater = seater;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getCarName() {
        return carName;
    }

    public int getSeater() {
        return seater;
    }

    public int getPrice() {
        return price;
    }
}
