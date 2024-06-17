package com.example.test1;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.stream.Collectors;

public class MyToolWindow {
    private JPanel myToolWindowContent;
    private JPanel chatPanel;
    private JTextField inputField;
    private JButton sendButton;

    public MyToolWindow() {
        myToolWindowContent = new JPanel(new BorderLayout());

        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane chatScrollPane = new JScrollPane(chatPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        inputField = new JTextField(20);
        inputField.setToolTipText("Enter your message here");
        bottomPanel.add(inputField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.setToolTipText("Click to send your message");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        bottomPanel.add(sendButton, BorderLayout.EAST);

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        myToolWindowContent.add(chatScrollPane, BorderLayout.CENTER);
        myToolWindowContent.add(bottomPanel, BorderLayout.SOUTH);
    }

    public JPanel getContent() {
        return myToolWindowContent;
    }

    private void sendMessage() {
        String inputText = inputField.getText().trim();
        if (!inputText.isEmpty()) {
            addMessage(inputText, true); // Add user message
            inputField.setText(""); // Clear input field

            // Make API call to retrieve reply
            try {
                URL url = new URL("http://localhost:5000/api/tfidf_message");
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                con.setDoOutput(true);

                String postData = "question=" + inputText;
                try (OutputStream os = con.getOutputStream()) {
                    byte[] input = postData.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                    String response = br.lines().collect(Collectors.joining(System.lineSeparator()));
                    // Process the JSON response
                    processApiResponse(response);
                }

                con.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                addMessage("Error: Could not retrieve response from server", false);
            }
        }
    }

    private void processApiResponse(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);

            String answer = rootNode.get("answer").asText(); // Extract the "answer" field from JSON

            addMessage(answer, false); // Add received message to chatPanel (assuming it's a response message)
        } catch (IOException e) {
            e.printStackTrace();
            addMessage("Error: Could not process API response", false);
        }
    }

    private void addMessage(String message, boolean isUser) {
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));

        JLabel messageLabel = new JLabel(message);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        messageLabel.setForeground(Color.BLACK);

        messagePanel.add(messageLabel);
        messagePanel.setAlignmentX(isUser ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        if (isUser) {
            messagePanel.setBackground(new Color(31, 21, 169));
            messagePanel.setForeground(Color.WHITE);
            messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 50, 5, 10));
        } else {
            messagePanel.setBackground(new Color(202, 198, 255));
            messagePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 50));
        }

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        JScrollBar verticalScrollBar = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }
}
