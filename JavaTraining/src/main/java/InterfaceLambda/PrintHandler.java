package InterfaceLambda;

public class PrintHandler {

    public static void main(String[] args) {

        Printable operation;
        operation = (x)-> {
            System.out.println("Hello, " + x + " !");
        };

        operation.printer("aaa");

    }
}
