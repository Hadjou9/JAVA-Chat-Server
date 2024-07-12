import java.io.*;
import java.net.*;
import java.util.*;

public class TextServer {
    private static List <String> usernames ;
    private static Map<String, String> usersData ;
    private static Map<String, List<String>> receivedMessages;

    private static boolean inputValidation(String input){
        if (input.trim().length() < 3)
            return false;
        else
        return true;
    }

    public static String addUser(String userName, String password){

        userName = userName.trim();
            if (!inputValidation(userName)) {
                return "Invalid username!";
            }
            if (usernames.contains(userName)) {
                return "Username already taken. Choose a " +
                        "different one!";
            }

        password = password.trim();
            if (!inputValidation(password)) {
                return "Invalid password!";
            }

        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter("users.txt", true));
            bw.write(userName);
            bw.write(","+ password);
            bw.newLine();
            bw.close();
            // update the list of users
            setUsersData();
            return "User added. You can now connect to the server!";
        } catch (Exception ex) {

            return "An error occurred while adding user!";
        }
    }

    public static void storeMessage(String sender,String receiver,String message){
        try {
            BufferedWriter bw = new BufferedWriter(
                    new FileWriter("messages.txt", true));
            bw.write(sender);
            bw.write("," + receiver);
            bw.write("|" + message);
            bw.newLine();
            bw.close();


        } catch (Exception ex) {
            System.out.println("An error occurred while storing a message!");
        }
    }
    // set a Map of usernames and passwords
        // also set the list of users
    public static void setUsersData(){
        usersData = new HashMap<String, String>();
        usernames = new ArrayList<>();

        try{
            BufferedReader br = new BufferedReader(
                    new FileReader("users.txt"));

            String line;
            while((line = br.readLine()) != null){
                if (line.length() == 0)
                    continue;
                String username = line.substring(0, line.indexOf(","));
                String password = line.substring(line.indexOf(",")+1);
                usersData.put(username, password);

                usernames.add(username);
            }
            br.close();
        } catch (Exception ex){
            System.out.println("An error occurred while setting users data!");
        }
    }

    public static Map <String, String> getUsersData(){
        return usersData;
    }

    public static List<String> getUsernames(){
        return usernames;
    }

    public static boolean authentication(String username,
                                         String password){
        if (usersData.get(username) == null)
            return false;
        else if (! usersData.get(username).equals(password))
            return false;
        else
            return true;
    }

    public static Map<String, List<String>> getReceivedMessages(String user){
        receivedMessages = new HashMap<String, List<String>>();
        try{
            BufferedReader br = new BufferedReader(
                    new FileReader("messages.txt"));

            String line;
            while((line = br.readLine()) != null){
                if (line.length() == 0)
                    continue;
                String sender = line.substring(0, line.indexOf(","));
                String receiver = line.substring(line.indexOf(",")+1,
                        line.indexOf("|"));
                String message = line.substring(line.indexOf("|") + 1);
                if (receiver.equals(user)){
                    if (!receivedMessages.containsKey(sender)){
                    List<String> temp = new ArrayList<>();
                    temp.add(message);
                    receivedMessages.put(sender, temp);
                     } else
                        {
                        receivedMessages.get(sender).add(message);
                        }
                }
        }
            br.close();
        }catch (Exception ex){
            System.out.println("An error occurred while getting messages!");
        }



        return receivedMessages;

    }

    public static void main(String argv[]) throws Exception
    {
        String username;
        String password;
        String logedInUser = null;
        String message;
        boolean isLogedIn = false;

        ServerSocket welcomeSocket = new ServerSocket(8000);
        System.out.println("SERVER is running ... ");

        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader
                    (new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream
                    (connectionSocket.getOutputStream());
            String option;
            while (true) {
                //load the server's data
                setUsersData();
                //System.out.println("waiting...");
                System.out.println();
                option = inFromClient.readLine();
                switch (option) {

                    case "0":
                        username = inFromClient.readLine();
                        password = inFromClient.readLine();
                        System.out.println("User's choice is " + option);
                        System.out.printf("username = %s, password = %s\n", username, password);
                        isLogedIn = authentication(username,password);
                        if (isLogedIn){
                            System.out.println("Access Granted");
                            outToClient.writeBytes("Success" + '\n');
                            logedInUser = username;
                        }
                        else {
                            System.out.println("Access Denied -Invalid credentials");
                            outToClient.writeBytes("Failure" + '\n');
                        }
                        break;

                    case "1":
                        System.out.println("User's choice is " + option);
                        System.out.println("Returning list of users...");
                        if (usernames.size() == 0)
                            System.out.println("No users on the system");

                        outToClient.write(usernames.size());
                             for (String user : usernames){
                                 System.out.println(user);
                                outToClient.writeBytes(user + '\n');
                                }
                        break;

                    case "2":
                        System.out.println("User's choice is " + option);
                        username = inFromClient.readLine();
                        if (username.equalsIgnoreCase("Q"))
                            break;

                        boolean validUser = usernames.contains(username);
                        outToClient.writeByte(validUser ? 1 : 0);

                        if (validUser){
                            message = inFromClient.readLine();
                            storeMessage(logedInUser,username, message);
                            System.out.println("Message sent to " + username);
                            break;
                        }
                        System.out.println("Failled to send message to " + username);
                        break;

                    case "3":
                        System.out.println("User's choice is " + option);
                        System.out.println("Returning messages for " + logedInUser);
                        outToClient.write(getReceivedMessages(logedInUser).size());
                        for (Map.Entry<String, List<String>> entry :
                                getReceivedMessages(logedInUser).entrySet()){
                            outToClient.writeBytes("From " + entry.getKey() +":"+ '\n');
                            outToClient.write(entry.getValue().size());
                            for (String msg : entry.getValue()){
                                outToClient.writeBytes("    " + msg + '\n');
                            }
                        }
                        break;

                    case "4":
                        System.out.println("User's choice is " + option);
                        System.out.println(logedInUser + " logged out");
                        // log out the user
                        logedInUser = null;
                        connectionSocket.close();
                        break;

                    case "5":
                        if(logedInUser != null)
                            break;
                        System.out.println("User's choice is " + option);
                        username = inFromClient.readLine();
                        password = inFromClient.readLine();
                        String status = addUser(username, password);
                        outToClient.writeBytes( status + '\n');
                        if (status.equals(
                                "User added. You can now connect to the server!"))
                            System.out.printf("Status: %s created an account\n", username);
                        else {
                            System.out.printf("Status: %s failed to create an account\n", username);
                        }
                        break;
                }
                if (option.equals("4")) {
                    break;
                }
            }
        }

    }

}