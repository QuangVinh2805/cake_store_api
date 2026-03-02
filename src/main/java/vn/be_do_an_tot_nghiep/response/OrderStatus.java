package vn.be_do_an_tot_nghiep.response;

public enum OrderStatus {
    PREPARING(0),
    SHIPPING(1),
    DELIVERED(2),
    CANCELED(3);

    private final int value;

    OrderStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
