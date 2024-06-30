package com.bbangle.bbangle.util;

public class CustomQueue {

    private int front;
    private int rear;
    private final Object[] queue;

    public CustomQueue() {
        this.queue = new Object[1000]; // Assuming a maximum size of 100, adjust as needed
        this.front = 0;
        this.rear = 0;
    }

    public void enqueue(Object value) {
        this.queue[this.rear++] = value;
    }

    public Object dequeue() {
        Object value = this.queue[this.front];
        this.queue[this.front++] = null;
        return value;
    }

    public boolean isEmpty() {
        return this.front == this.rear;
    }

}
