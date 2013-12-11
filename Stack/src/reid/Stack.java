package reid;

public class Stack {
    private int[] stack;
    private int tos;
    
    public Stack(int size) {
        stack = new int[size];
        tos = -1;
    }
    
    public void push(int num) {
        if (tos == stack.length - 1) {
            System.out.println("Stack is full.");
        } else {
            stack[++tos] = num;
        }
    }
    
    public int pop() {
        if (tos < 0) {
            System.out.println("Stack is underflow.");
            return 0;
        } else {
            return stack[tos--];
        }
    }
}
