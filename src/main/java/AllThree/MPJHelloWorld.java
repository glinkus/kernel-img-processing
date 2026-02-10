package AllThree;

import mpi.*;

public class MPJHelloWorld {

    public static void Get()
    {
        int rank = MPI.COMM_WORLD.Rank(); // Get the rank of the process
        int size = MPI.COMM_WORLD.Size(); // Get the total number of processes

        if (size < 2) {
            System.out.println("This example requires at least 2 processes.");
        } else {
            if (rank == 0) {
                // Process 0 sends a message
                String message = "Hello from process 0";
                MPI.COMM_WORLD.Send(message.getBytes(), 0, message.length(), MPI.BYTE, 1, 99);
                System.out.println("Process 0 sent message: " + message);
            } else if (rank == 1) {
                // Process 1 receives the message
                byte[] recvBuf = new byte[100]; // Buffer to store the received data
                MPI.COMM_WORLD.Recv(recvBuf, 0, 100, MPI.BYTE, 0, 99);
                String receivedMessage = new String(recvBuf).trim();
                System.out.println("Process 1 received message: " + receivedMessage);
            }
        }
    }
    public static void main(String[] args) {
        try {
            MPI.Init(args); // Initialize the MPJ environment

            Get();
            MPI.Finalize(); // Finalize the MPJ environment
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
