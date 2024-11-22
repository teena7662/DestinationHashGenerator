import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Random;

public class DestinationHashGenerator {

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        if (args.length != 2) {
            System.out.println("Usage: java DestinationHashGenerator <roll_number> <json_file_path>");
            return;
        }

        String rollNumber = args[0].toLowerCase(); // Ensure roll number is in lowercase
        String jsonFilePath = args[1];

        // Create ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();

        // Parse JSON file
        JsonNode rootNode = objectMapper.readTree(new File(jsonFilePath));

        // Traverse JSON and get the destination value
        String destinationValue = traverseJson(rootNode);

        if (destinationValue.isEmpty()) {
            System.out.println("No 'destination' key found in the JSON.");
            return;
        }

        // Generate a random 8-character alphanumeric string
        String randomString = generateRandomString(8);

        // Concatenate the roll number, destination value, and random string
        String toHash = rollNumber + destinationValue + randomString;

        // Generate the MD5 hash
        String md5Hash = generateMD5Hash(toHash);

        // Output the result in the format: <hash>;<random string>
        System.out.println(md5Hash + ";" + randomString);
    }

    private static String traverseJson(JsonNode node) {
        if (node.isObject()) {
            Iterator<Entry<String, JsonNode>> fields = node.fields();
            StringBuilder sb = new StringBuilder();

            while (fields.hasNext()) {
                Entry<String, JsonNode> field = fields.next();
                String fieldName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if ("destination".equals(fieldName)) {
                    sb.append(fieldValue.asText());
                }

                // Recursively process nested nodes
                sb.append(traverseJson(fieldValue));
            }
            return sb.toString();
        } else if (node.isArray()) {
            StringBuilder sb = new StringBuilder();
            for (JsonNode element : node) {
                sb.append(traverseJson(element));
            }
            return sb.toString();
        }
        return "";
    }

    private static String generateRandomString(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }
        return sb.toString();
    }

    private static String generateMD5Hash(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashBytes = md.digest(input.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }
}
