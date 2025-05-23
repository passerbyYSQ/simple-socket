package top.ysqorz.socket;

import top.ysqorz.socket.client.DefaultTcpClient;
import top.ysqorz.socket.client.TcpClient;
import top.ysqorz.socket.io.AbstractSendCallback;
import top.ysqorz.socket.io.ReceivedCallback;
import top.ysqorz.socket.io.exception.AckTimeoutException;
import top.ysqorz.socket.io.packet.AckReceivedPacket;
import top.ysqorz.socket.io.packet.FileReceivedPacket;
import top.ysqorz.socket.io.packet.StringReceivedPacket;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import static top.ysqorz.socket.Constant.FILE_ARGS;
import static top.ysqorz.socket.Constant.TEXT_ARGS;

/**
 * --file D:\EXE\2025-05-13\ZwTeamWork\ZWTeammate-1.6.0-windows-x86_64-20250513.exe
 * --file F:\Linux\rhel-server-6.7-x86_64-dvd.iso
 *
 * @author yaoshiquan
 * @date 2025/5/9
 */
public class ClientTest {
    public static void main(String[] args) throws IOException {
        try (TcpClient client = new DefaultTcpClient("127.0.0.1", 9090, true)) {
            client.setReceivedCallback(new ReceivedCallback() {
                @Override
                public void onTextReceived(StringReceivedPacket packet) {
                    System.out.println("[From server]: " + packet.getEntity());
                }

                @Override
                public void onFileReceived(FileReceivedPacket packet) {
                    System.out.println("[From server]: " + packet.getEntity().getAbsolutePath());
                }

                @Override
                public void onAckReceived(boolean isTimeout, AckReceivedPacket ackPacket) {
                    System.out.println("[From server]: Ack, IsTimeout: " + isTimeout);
                }
            });
            client.start();
            BufferedReader bufReader = new BufferedReader(new InputStreamReader(System.in));
            while(true) {
                String text = bufReader.readLine();
                if (text == null) {
                    break;
                }
                // 空字符串不发送
                if (text.isEmpty()) {
                    continue;
                }
                // 退出客户端
                if ("exit".equalsIgnoreCase(text)) {
                    break;
                } else if (text.startsWith(TEXT_ARGS)) {
                    text = text.substring(TEXT_ARGS.length()).trim();
                    String finalText = text;
                    client.sendText(text, new AbstractSendCallback(-1) {
                        @Override
                        public void onAck(long cost) {
                            System.out.println("收到Ack：" + finalText);
                        }

                        @Override
                        public void onFailure(Exception ex) {
                            if (ex instanceof AckTimeoutException) {
                                System.out.println("Ack超时：" + finalText);
                            }
                        }
                    });
                } else if (text.startsWith(FILE_ARGS)) {
                    text = text.substring(FILE_ARGS.length()).trim();
                    String finalText1 = text;
//                    client.sendFile(new File(text), new AbstractAckCallback(-1) {
//                        @Override
//                        public void onAck(long cost) {
//                            System.out.println("线程名称：" +  Thread.currentThread().getName());
//                            System.out.println("收到Ack：" + finalText1 + ", cost " + cost + " ms");
//                        }
//
//                        @Override
//                        public void onTimeout(long cost, boolean receivedAck) {
//                            System.out.println("线程名称：" +  Thread.currentThread().getName());
//                            System.out.println("Ack超时：" + finalText1 + ", cost " + cost + " ms");
//                        }
//                    });
                    try {
                        client.sendFileSyncAck(new File(text), 60);
                        System.out.println("同步等待Ack");
                    } catch (AckTimeoutException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
