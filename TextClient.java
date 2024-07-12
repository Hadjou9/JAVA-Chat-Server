import java.io.*;
import java.net.*;

class TextClient {
    public static void main(String argv[]) throws Exception {

        String username;
        String password;
        String serverResponse;
        String message;
        boolean isLogedIn = false;

        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket clientSocket = new Socket("127.0.0.1", 8000);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        while (true) {

            System.out.println("=================");
            System.out.println("0. Connect to the server");
            System.out.println("1. Get the user list");
            System.out.println("2. Send a message");
            System.out.println("3. Get my messages");
            System.out.println("4. Exit");
            if (!isLogedIn){
                System.out.println("5. Create an account");
            }
            System.out.print("Please enter a choice: ");

            String option = inFromUser.readLine();
            System.out.println();
            if ((option.equals("2") || option.equals("3")) && !isLogedIn){
                System.out.println("You have to connect to the server first");
                option = "0";
            }
            outToServer.writeBytes(option + "\n");

            switch (option) {

                case "0":
                    System.out.println("Connecting...");
                    System.out.println("===============");
                    while (true) {
                        System.out.print("Please enter the username: ");
                        username = inFromUser.readLine();
                        outToServer.writeBytes(username + '\n');
                        System.out.print("Please enter the password: ");
                        password = inFromUser.readLine();
                        outToServer.writeBytes(password + '\n');
                        serverResponse = inFromServer.readLine();
                        if (serverResponse.equals("Success")){
                            System.out.println();
                            System.out.println("Access Granted");
                            isLogedIn = true;
                            break;
                        }
                        else {
                            System.out.println("Access Denied – Username/Password Incorrect");
                            outToServer.writeBytes(option + "\n");
                        }
                    }

                    break;

                case "1":
                    System.out.println("Getting the list of users...");
                    System.out.println("============================");
                    int numberOfusers;
                    numberOfusers = inFromServer.read();
                    System.out.println("number of users is: "+ numberOfusers);
                    if (numberOfusers == 0){
                        System.out.println("No users yet!");
                    }
                    else {
                        for (int i = 0; i < numberOfusers; i++) {
                            serverResponse = inFromServer.readLine();
                            System.out.println(serverResponse);
                        }
                    }
                    break;

                case "2":
                    System.out.print("Enter a username you want to send a message to: ");
                    while(true){
                    username = inFromUser.readLine();
                    if (username.equalsIgnoreCase("Q")){
                        outToServer.writeBytes("Q" + '\n');
                        break;
                    }
                    outToServer.writeBytes(username + '\n');

                    // Check if user exist
                    if (inFromServer.read() == 0){
                        System.out.println("User does not exist!");
                        System.out.print("Enter a username you want to send a message to" +
                                " or Q to go back to main menu: ");
                        outToServer.writeBytes(option + "\n");
                        continue;
                    }
                    else{
                         System.out.print("Enter the message you want to send: ");
                         message = inFromUser.readLine();
                         outToServer.writeBytes(message + '\n');
                         System.out.println("Status: Message sent successfully !");
                         break;
                        }
                    }
                    break;

                case "3":
                    System.out.println("Here are your messages!");
                    System.out.println("=======================");

                    int numOfSender ;
                    int numOfmsgPerSender;
                    numOfSender = inFromServer.read();
                    if (numOfSender == 0){
                        System.out.println("You don't have any message yet");
                    }
                    else{
                        for (int i = 0; i < numOfSender; i++){
                            serverResponse = inFromServer.readLine();
                            System.out.println(serverResponse);
                            numOfmsgPerSender = inFromServer.read();
                            for (int j = 0; j < numOfmsgPerSender; j++){
                                serverResponse = inFromServer.readLine();
                                System.out.println(serverResponse);
                            }
                        }
                    }

                    break;

                case "4":
                    isLogedIn = false;
                    clientSocket.close();
                    break;

                case "5":
                    if (isLogedIn){
                    break;}
                    else{
                        System.out.print("Enter your username (3 or more chars) ");
                        username = inFromUser.readLine();
                        outToServer.writeBytes(username + '\n');
                        System.out.print("Enter your password (3 or more chars) ");
                        password = inFromUser.readLine();
                        outToServer.writeBytes(password + '\n');
                        serverResponse = inFromServer.readLine();
                        System.out.println(serverResponse);
                        break;
                    }

            }
            if (option.equals("4")) {
                break;
            }
        }
    }
}

