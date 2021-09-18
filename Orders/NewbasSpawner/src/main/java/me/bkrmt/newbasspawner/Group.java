package me.bkrmt.newbasspawner;

public class Group {
    private final double price;
    private final String name;

    public Group(double price, String name) {
        this.price = price;
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public String getName() {
        return name;
    }
}
