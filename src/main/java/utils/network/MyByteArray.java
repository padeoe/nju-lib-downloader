package utils.network;

/**
 * @author Nifury
 *         Date: 2015/12/17
 */
public class MyByteArray {
    private byte[] buffer = new byte[4096];
    private int position = 0;

    public void ensureCapacity(int capacity) {
        if (buffer.length - position < capacity) {
            byte[] tmp = new byte[Math.max(buffer.length * 2, buffer.length + capacity)];
            System.arraycopy(buffer, 0, tmp, 0, position);
            buffer = tmp;
        }
    }

    public void addOffset(int delta) {
        position += delta;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getOffset() {
        return position;
    }

    public int getSize() {
        return position;
    }

    public static void main(String[] args) {
        MyByteArray array = new MyByteArray();
    }
}
