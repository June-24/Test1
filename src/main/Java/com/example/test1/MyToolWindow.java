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
import java.awt.*;
import javax.swing.border.AbstractBorder;

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
            addMessage(answer, false);
        } catch (IOException e) {
            e.printStackTrace();
            addMessage("Error: Could not process API response", false);
        }
    }

    public void addMessage(String message, boolean isUser) {
        JPanel messagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(getBackground());
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.X_AXIS));
        String a = isUser ? "Me:" : "Bot:";
        JLabel messageLabel = new JLabel("<html><b>" + a + "</b><br>" + message + "</html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        Dimension fixedwidthsize = new Dimension(messagePanel.getWidth(), 5000);
//        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        messageLabel.setBorder(new RoundedBorder(15, Color.BLACK, 2));

        messagePanel.add(messageLabel);
        messagePanel.setPreferredSize(fixedwidthsize);
        messagePanel.setAlignmentX(isUser ? Component.LEFT_ALIGNMENT : Component.RIGHT_ALIGNMENT);

        // Set background colors and foreground colors
        if (isUser) {
            messagePanel.setBackground(new Color(21, 117, 169));
            messageLabel.setForeground(Color.WHITE);
        } else {
            messagePanel.setBackground(new Color(255, 255, 255));
            messageLabel.setForeground(Color.BLACK);
        }

        // Apply rounded border with black outline
        messagePanel.setBorder(new RoundedBorder(15, Color.BLACK, 2));

        chatPanel.add(messagePanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        JScrollBar verticalScrollBar = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
        verticalScrollBar.setValue(verticalScrollBar.getMaximum());
    }
}
