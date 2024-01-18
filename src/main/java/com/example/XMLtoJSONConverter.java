package com.example;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.json.JSONPointer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class XMLtoJSONConverter {

    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                System.out.println("Usage: <task> <XML file path> [<JSON Pointer/Replacement JSON>]");
                return;
            }

            String task = args[0];
            String xmlFilePath = args[1];
            String outputFilePath = "";

            switch (task) {
                case "task1":
                    outputFilePath = "target/output/task1.json";
                    convertXMLToJSON(xmlFilePath, outputFilePath);
                    break;
                case "task2":
                    if (args.length < 3) {
                        System.out.println("JSON Pointer argument missing for extract task.");
                        return;
                    }
                    outputFilePath = "target/output/task2.json";
                    extractAndWriteSubObject(xmlFilePath, outputFilePath, args[2]);
                    break;
                case "task3":
                    if (args.length < 3) {
                        System.out.println("Key path argument missing for checkAndSave task.");
                        return;
                    }
                    outputFilePath = "target/output/task3.json";
                    checkAndSave(xmlFilePath, outputFilePath, args[2]);
                    return;
                case "task4":
                    outputFilePath = "target/output/task4.json";
                    addPrefixToKeys(xmlFilePath, outputFilePath);
                    break;
                case "task5":
                    if (args.length < 3) {
                        System.out.println("Key path argument missing for replaceSubObject task.");
                        return;
                    }
                    System.out.println("Enter the JSON string for replacement:");
                    Scanner scanner = new Scanner(System.in);
                    String jsonString = scanner.nextLine();
                    JSONObject replacement = new JSONObject(jsonString);
                    outputFilePath = "target/output/task5.json";
                    replaceSubObjectAndWrite(xmlFilePath, outputFilePath, args[2], replacement);
                    break;
                default:
                    System.out.println("Invalid task specified.");
                    return;
            }

            System.out.println(task + " completed. Output file: " + outputFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Converts XML to JSON and writes to a file
    public static void convertXMLToJSON(String xmlFilePath, String jsonFilePath) throws IOException {
        JSONObject jsonObject = readXMLFile(xmlFilePath);
        writeJSONToFile(jsonObject, jsonFilePath);
    }

    // Extracts a sub-object using JSONPointer and writes it to a file
    public static void extractAndWriteSubObject(String xmlFilePath, String jsonFilePath, String jsonPointerExpression) throws IOException {
        JSONObject jsonObject = readXMLFile(xmlFilePath);
        JSONPointer pointer = new JSONPointer(jsonPointerExpression);
        JSONObject subObject = (JSONObject) pointer.queryFrom(jsonObject);
        writeJSONToFile(subObject, jsonFilePath);
    }

    public static void checkAndSave(String xmlFilePath, String jsonFilePath, String keyPath) throws IOException {
        JSONObject jsonObject = readXMLFile(xmlFilePath);
        JSONPointer pointer = new JSONPointer(keyPath);
        Object result = pointer.queryFrom(jsonObject);
    
        if (result != null) {
            // Extract the last part of the key path to use as the key in the saved JSON
            String[] keys = keyPath.split("/");
            String lastKey = keys[keys.length - 1];

            if(lastKey.chars().allMatch(Character::isDigit )) {
                lastKey = keys[keys.length - 2];
            }
            // Create a new JSONObject to store the result
            JSONObject outputObject = new JSONObject();
            outputObject.put(lastKey, result);
    
            writeJSONToFile(outputObject, jsonFilePath);
            System.out.println("task3 completed. Output file: " + jsonFilePath);
        } else {
            System.out.println("No such key path exists or the key path is invalid: " + keyPath);
        }
    }

    // Adds a prefix to all keys in the JSON object
    public static void addPrefixToKeys(String xmlFilePath, String jsonFilePath) throws IOException {
        JSONObject jsonObject = readXMLFile(xmlFilePath);
        JSONObject prefixedJson = addPrefixToKeysRecursive(jsonObject, "swe262_");
        writeJSONToFile(prefixedJson, jsonFilePath);
    }

    private static JSONObject addPrefixToKeysRecursive(JSONObject jsonObject, String prefix) {
        JSONObject result = new JSONObject();
        for (String key : jsonObject.keySet()) {
            Object value = jsonObject.get(key);
            if (value instanceof JSONObject) {
                value = addPrefixToKeysRecursive((JSONObject) value, prefix);
            } else if (value instanceof JSONArray) {
                value = addPrefixToKeysInArray((JSONArray) value, prefix);
            }
            result.put(prefix + key, value);
        }
        return result;
    }

    private static JSONArray addPrefixToKeysInArray(JSONArray jsonArray, String prefix) {
        JSONArray result = new JSONArray();
        for (Object item : jsonArray) {
            if (item instanceof JSONObject) {
                result.put(addPrefixToKeysRecursive((JSONObject) item, prefix));
            } else {
                result.put(item);
            }
        }
        return result;
    }

    // Replaces a sub-object at a specified path with another JSON object
    public static void replaceSubObjectAndWrite(String xmlFilePath, String jsonFilePath, String keyPath, JSONObject replacement) throws IOException {
        JSONObject jsonObject = readXMLFile(xmlFilePath);
        String[] keys = keyPath.split("/");
        Object current = jsonObject;
    
        for (int i = 1; i < keys.length - 1; i++) {
            if (current instanceof JSONObject) {
                current = ((JSONObject) current).get(keys[i]);
            } else if (current instanceof JSONArray) {
                current = ((JSONArray) current).get(Integer.parseInt(keys[i]));
            }
        }
    
        if (current instanceof JSONObject) {
            ((JSONObject) current).put(keys[keys.length - 1], replacement);
        } else if (current instanceof JSONArray) {
            ((JSONArray) current).put(Integer.parseInt(keys[keys.length - 1]), replacement);
        }
    
        writeJSONToFile(jsonObject, jsonFilePath);
    }   

    // Helper method to read an XML file and convert it to a JSONObject
    private static JSONObject readXMLFile(String xmlFilePath) throws IOException {
        File xmlFile = new File(xmlFilePath);
        FileInputStream fis = new FileInputStream(xmlFile);
        byte[] data = new byte[(int) xmlFile.length()];
        fis.read(data);
        fis.close();
        String xml = new String(data, "UTF-8");
        return XML.toJSONObject(xml);
    }

    // Helper method to write a JSONObject to a file
    private static void writeJSONToFile(JSONObject jsonObject, String jsonFilePath) throws IOException {
        try (FileWriter file = new FileWriter(jsonFilePath)) {
            file.write(jsonObject.toString(4));
            file.flush();
        }
    }
}