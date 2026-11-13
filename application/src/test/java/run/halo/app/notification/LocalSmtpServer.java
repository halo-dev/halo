package run.halo.app.notification;

import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Loopback-only SMTP test receiver. It never relays mail or contacts an external server. */
final class LocalSmtpServer implements AutoCloseable {
    private final ServerSocket server;
    private final Set<Socket> connections = ConcurrentHashMap.newKeySet();
    private final BlockingQueue<Message> messages = new LinkedBlockingQueue<>();
    private final AtomicReference<RejectedDelivery> nextRejection = new AtomicReference<>();

    LocalSmtpServer() throws IOException {
        server = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Thread.startVirtualThread(() -> {
            while (!server.isClosed()) {
                try {
                    var socket = server.accept();
                    connections.add(socket);
                    Thread.startVirtualThread(() -> receive(socket));
                } catch (IOException e) {
                    if (!server.isClosed()) {
                        throw new IllegalStateException(e);
                    }
                }
            }
        });
    }

    int port() {
        return server.getLocalPort();
    }

    RejectedDelivery rejectNextDelivery(String recipient) {
        var rejection = new RejectedDelivery(recipient);
        if (!nextRejection.compareAndSet(null, rejection)) {
            throw new IllegalStateException("A rejection is already pending");
        }
        return rejection;
    }

    Message awaitMessage(String recipient, String subjectPrefix) throws Exception {
        var deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(20);
        while (System.nanoTime() < deadline) {
            var message = messages.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
            if (message == null) {
                break;
            }
            if (recipient.equals(message.recipient()) && message.subject().startsWith(subjectPrefix)) {
                return message;
            }
        }
        throw new AssertionError("No matching SMTP message received within 20 seconds");
    }

    private void receive(Socket socket) {
        try (socket;
                var reader =
                        new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                var writer = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {
            socket.setSoTimeout((int) Duration.ofSeconds(25).toMillis());
            reply(writer, "220 localhost test SMTP");
            String recipient = null;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("EHLO") || line.startsWith("HELO")) {
                    reply(writer, "250-localhost\r\n250-AUTH PLAIN\r\n250 8BITMIME");
                } else if (line.startsWith("AUTH PLAIN")) {
                    if (line.equals("AUTH PLAIN")) {
                        reply(writer, "334 ");
                        reader.readLine();
                    }
                    reply(writer, "235 Authentication successful");
                } else if (line.startsWith("MAIL FROM:") || line.equals("RSET") || line.equals("NOOP")) {
                    reply(writer, "250 OK");
                } else if (line.startsWith("RCPT TO:")) {
                    recipient = line.substring(line.indexOf('<') + 1, line.indexOf('>'));
                    reply(writer, "250 OK");
                } else if (line.equals("DATA")) {
                    reply(writer, "354 End with a dot");
                    var data = new StringBuilder();
                    while ((line = reader.readLine()) != null && !line.equals(".")) {
                        data.append(line.startsWith("..") ? line.substring(1) : line)
                                .append("\r\n");
                    }
                    var message = new Message(recipient, data.toString());
                    var rejection = nextRejection.get();
                    if (rejection != null
                            && rejection.recipient.equals(recipient)
                            && nextRejection.compareAndSet(rejection, null)) {
                        rejection.attempt.complete(message);
                        rejection.release.get(20, TimeUnit.SECONDS);
                        reply(writer, "451 Temporary test failure; retry this message");
                    } else {
                        messages.add(message);
                        reply(writer, "250 Accepted");
                    }
                } else if (line.equals("QUIT")) {
                    reply(writer, "221 Bye");
                    return;
                } else {
                    reply(writer, "500 Unsupported test SMTP command");
                }
            }
        } catch (Exception e) {
            if (!server.isClosed()) {
                throw new IllegalStateException("Test SMTP receiver failed", e);
            }
        } finally {
            connections.remove(socket);
        }
    }

    private static void reply(PrintWriter writer, String response) {
        writer.print(response + "\r\n");
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        server.close();
        for (var connection : connections) {
            connection.close();
        }
    }

    static final class RejectedDelivery {
        private final String recipient;

        RejectedDelivery(String recipient) {
            this.recipient = recipient;
        }

        private final CompletableFuture<Message> attempt = new CompletableFuture<>();
        private final CompletableFuture<Void> release = new CompletableFuture<>();

        Message awaitAttempt() throws Exception {
            return attempt.get(20, TimeUnit.SECONDS);
        }

        void release() {
            release.complete(null);
        }
    }

    record Message(String recipient, String raw) {
        String subject() throws Exception {
            return mime().getSubject();
        }

        private MimeMessage mime() throws Exception {
            return new MimeMessage(
                    Session.getInstance(new Properties()),
                    new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
        }

        String text() throws Exception {
            return text(mime());
        }

        private static String text(Part part) throws Exception {
            if (part.isMimeType("text/plain")) {
                return (String) part.getContent();
            }
            if (part.getContent() instanceof Multipart multipart) {
                for (int i = 0; i < multipart.getCount(); i++) {
                    var text = text(multipart.getBodyPart(i));
                    if (!text.isEmpty()) {
                        return text;
                    }
                }
            }
            return "";
        }
    }
}
