package test;

class MyQueue<T> {
    // Define your own member variables to store elements
    // DO NOT use java.util.Queue itself as your member variable.
    public T [] data;
    public int size;
    public int last;
    
    public MyQueue() {
        this.size = 10;
        this.data = (T[]) new Object[this.size];
        this.last = -1;
    }

    public void enqueue(T e) {
        if (this.last == this.size - 1){
            T [] data0 = (T[]) new Object[this.size + 20];
            this.size += 20;
            for (int i = 0; i < this.last + 1; i++){
                data0[i] = this.data[i];
            }
            this.data = data0;
        }
        this.last += 1;
        this.data[this.last] = e;
    }
    
    public T dequeue() {
        T result = this.data[0];
        for (int i = 0; i < last; i++){
            this.data[i] = this.data[i+1];
        }
        this.data[last] = null;
        this.last -= 1;
        return result;
    }
    public T first(){
        return this.data[0];
    }
    public int size() {
        // Do not modify the function declaration
        // Implement Here.
        return this.last + 1;
    }
    public boolean isEmpty() {
        // Do not modify the function declaration
        // Implement Here.
        return (this.last == -1);
    }
    
}