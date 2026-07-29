package nofrills.misc;

public class MutableReference<T> {
    private T value;

    public MutableReference(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }

    public void set(T value) {
        this.value = value;
    }
}
