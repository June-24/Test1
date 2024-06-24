package com.example.demo2;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
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
    private JScrollPane scrollPane;
    public MyToolWindow() {
        myToolWindowContent = new JPanel(new BorderLayout());
        chatPanel = new JPanel();
        //chatPanel.autoScrolls(true);
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(chatPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel bottomPanel = new JPanel(new BorderLayout());

        inputField = new JTextField(20);
        inputField.setToolTipText("Enter your message here");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        sendButton = new JButton("Send") {
            @Override
            public void updateUI() {
                setUI(new BasicButtonUI());
            }
        };
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(21, 117, 169));
        sendButton.setOpaque(true);
        sendButton.setBorderPainted(false);// Set the background color to blue
        sendButton.setForeground(Color.WHITE); // Set the text color to white for better contrast
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

        myToolWindowContent.add(scrollPane, BorderLayout.CENTER);
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
                addMessage("What is RAID structure in OS? What are the different levels of RAID configuration?\n" +
                        "\n" +
                        "RAID (Redundant Arrays of Independent Disks) is a method used to store data on Multiple hard disks therefore it is considered as data storage virtualization technology that combines multiple hard disks. It simply balances data protection, system performance, storage space, etc. It is used to improve the overall performance and reliability of data storage. It also increases the storage capacity of the system and its main purpose is to achieve data redundancy to reduce data loss. \n" +
                        "\n" +
                        "Different levels of RAID\n" +
                        "\n" +
                        "Nowadays, RAID is available in various schemes or RAID level as given below:", false);
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
            addMessage("n the vast tapestry of life, each person is a unique thread, contributing to the intricate design of our collective existence. My name is [Your Name], and this essay offers a glimpse into who I am, my experiences, values, aspirations, and the elements that have shaped my journey thus far.\n" +
                    "Early Life and Background\n" +
                    "\n" +
                    "I was born and raised in [Your Hometown], a place that holds a special place in my heart for its sense of community and rich cultural heritage. Growing up in a family that valued education and hard work, I was encouraged to pursue my interests and develop a strong sense of responsibility from a young age. My parents, who have always been my role models, instilled in me the importance of perseverance, kindness, and integrity.\n" +
                    "Education and Intellectual Curiosity\n" +
                    "\n" +
                    "My educational journey has been a significant part of my life. I attended [Your School] where I developed a love for learning and a curiosity about the world. Subjects like [Favorite Subjects] fascinated me, and I found joy in exploring new ideas and concepts. This intellectual curiosity led me to pursue [Your Major/Field of Study] at [Your University], where I have been able to delve deeper into my academic interests and engage in research that excites me.", false);
        }
    }

    public void addMessage(String message, boolean isUser) {
        JPanel messagePanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Dimension arcs = new Dimension(15, 15);
                int width = getWidth();
                int height = getHeight();
                Graphics2D graphics = (Graphics2D) g;
                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                graphics.setColor(getBackground());
                graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
                graphics.setColor(getForeground());
                graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height);
            }
        };

        String prefix = isUser ? "Me:" : "Bot:";
        JLabel messageLabel = new JLabel("<html><b>" + prefix + "</b><br>" + message + "</html>");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Calculate preferred size for messageLabel
        messageLabel.setSize(new Dimension(0, 0));
        Dimension preferredSize = messageLabel.getPreferredSize();
        int maxWidth = scrollPane.getViewport().getWidth() - 100; // Adjust as needed
        int maxHeight = 500; // Maximum height for messagePanel

        if (preferredSize.width > maxWidth) {
            messageLabel.setText("<html><b>" + prefix + "</b><br>" + wrapText(message, maxWidth) + "</html>");
            preferredSize = messageLabel.getPreferredSize();
        }

        if (preferredSize.height > maxHeight) {
            messageLabel.setText("<html><b>" + prefix + "</b><br>" + wrapText(message, maxWidth, maxHeight) + "</html>");
            preferredSize = messageLabel.getPreferredSize();
        }

        messagePanel.add(messageLabel, BorderLayout.CENTER);

        // Add JButton for non-user messages
        if (!isUser) {
            JButton actionButton = new JButton("<html><u>Read more</u></html>"){
                @Override
                public void updateUI() {
                    setUI(new BasicButtonUI());
                }
            };
            actionButton.setBackground(Color.WHITE);
            actionButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10)); // Add margin around the button
            actionButton.setForeground(new Color(1, 92, 141));
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.setBackground(Color.WHITE);
            //buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // Add margin around the button panel
            buttonPanel.add(actionButton, BorderLayout.WEST);

            messagePanel.add(buttonPanel, BorderLayout.SOUTH);
        }

        messagePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        messagePanel.setOpaque(false);
        messagePanel.setBackground(isUser ? new Color(21, 117, 169) : Color.WHITE);
        messageLabel.setForeground(isUser ? Color.WHITE : Color.BLACK);

        // Set preferred size for messagePanel based on messageLabel
        messagePanel.setPreferredSize(new Dimension(preferredSize.width+20, preferredSize.height + 50)); // Adjust height for button

        JPanel newPanel = new JPanel(new BorderLayout());
        if (isUser) {
            newPanel.add(Box.createHorizontalGlue(), BorderLayout.WEST);
            newPanel.add(messagePanel, BorderLayout.EAST);
        } else {
            newPanel.add(messagePanel, BorderLayout.WEST);
            newPanel.add(Box.createHorizontalGlue(), BorderLayout.EAST);
        }

        chatPanel.add(newPanel);
        chatPanel.revalidate();
        chatPanel.repaint();

        // Ensure the scroll bar moves to the bottom as new messages are added
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMaximum()));
    }


    private String wrapText(String text, int maxWidth) {
        Font font = new Font("Arial", Font.PLAIN, 14);
        FontMetrics metrics = new JLabel().getFontMetrics(font);
        int lineHeight = metrics.getHeight();
        StringBuilder wrappedText = new StringBuilder("<html>");

        String[] words = text.split(" ");
        int lineWidth = 0;

        for (String word : words) {
            int wordWidth = metrics.stringWidth(word + " ");
            if (lineWidth + wordWidth > maxWidth) {
                wrappedText.append("<br>");
                lineWidth = 0;
            }
            wrappedText.append(word).append(" ");
            lineWidth += wordWidth;
        }

        wrappedText.append("</html>");
        return wrappedText.toString();
    }
    private String wrapText(String text, int maxWidth, int maxHeight) {
        Font font = new Font("Arial", Font.PLAIN, 14);
        FontMetrics metrics = new JLabel().getFontMetrics(font);
        int lineHeight = metrics.getHeight();
        StringBuilder wrappedText = new StringBuilder("<html>");

        String[] words = text.split(" ");
        int lineWidth = 0;
        int currentHeight = lineHeight;

        for (String word : words) {
            int wordWidth = metrics.stringWidth(word + " ");
            if (lineWidth + wordWidth > maxWidth) {
                wrappedText.append("<br>");
                lineWidth = 0;
                currentHeight += lineHeight;
                if (currentHeight > maxHeight) {
                    wrappedText.append("..."); // Indicate that the text is truncated
                    break;
                }
            }
            wrappedText.append(word).append(" ");
            lineWidth += wordWidth;
        }

        wrappedText.append("</html>");
        return wrappedText.toString();
    }
}

