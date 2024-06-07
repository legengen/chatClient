import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MyFrame frame = new MyFrame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}

class ServerConnection {
    private static final String SERVER_IP = "110.41.49.118";
    private static final int PORT = 8189;
    private Socket socket;

    public ServerConnection() throws IOException {
        socket = new Socket(SERVER_IP, PORT);
    }

    public Socket getSocket() {
        return socket;
    }

    public void close() throws IOException {
        socket.close();
    }
}

class MyFrame extends JFrame {
    private static final int WIDTH = 500;
    private static final int HEIGHT = 300;

    private JButton button;
    private JTextArea textArea;
    private JTextArea messageArea;
    private ServerConnection client;

    public MyFrame() throws IOException {
        setTitle("legengen的聊天室");
        setSize(WIDTH, HEIGHT);

        JPanel panel = new JPanel();
        JPanel inputPanel = new JPanel();

        textArea = new JTextArea(1, 25); // 输入框
        button = new JButton("发送"); // 发送按钮
        button.addActionListener(new ButtonClickListener());

        messageArea = new JTextArea(1, 20); // 信息接收
        messageArea.setEditable(false);

        panel.setLayout(new BorderLayout()); // 主panel布局
        panel.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        inputPanel.setLayout(new FlowLayout()); // 输入窗口布局
        inputPanel.add(textArea);
        inputPanel.add(button);

        panel.add(inputPanel, BorderLayout.SOUTH);
        add(panel);

        client = new ServerConnection();
        new Thread(new ReceiveMessageTask()).start();
    }

    private class ButtonClickListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                PrintWriter out = new PrintWriter(client.getSocket().getOutputStream(), true);
                out.println(textArea.getText());
                textArea.setText(""); // 清空输入框
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class ReceiveMessageTask implements Runnable {
        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getSocket().getInputStream()));
                String message;
                while ((message = in.readLine()) != null) {
                    appendMessage(message); // 接收到消息后更新 UI
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                messageArea.append(message + "\n");
            }
        });
    }

    @Override
    public void dispose() {
        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.dispose();
    }
}
