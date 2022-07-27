import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.File;
import java.util.*;


public class ImageToTextBot extends TelegramLongPollingBot {
    Set<MyUser> users = new HashSet<>();
    MyUser curUser;

    @Override
    public String getBotUsername() {
        return "SpermaImageToTextBot";
    }

    @Override
    public String getBotToken() {
        return "5516099928:AAHNGxNa2q9WVhEFR1jHN5EeNL5XYIxL2LY";
    }

    @Override
    public void onUpdateReceived(Update update) {
        curUser = null;
        String chatId = update.getMessage().getChatId().toString();
        String userName = update.getMessage().getFrom().getUserName();
        for (MyUser user : users) {
            if (user.getUserName().equals(userName)) {
                curUser = user;
            }
        }
        if (curUser == null) {
            curUser = new MyUser(userName);
            users.add(curUser);
        }
        if (update.getMessage().hasText()) {
            String command = update.getMessage().getText().toLowerCase(Locale.ROOT);
            if (command.equals("/start")) {
                sendText(chatId, "Привет, я могу превратить твое изображение в текст. " +
                        "Могу, чтобы это выглядело красиво в самом телеграмме. " +
                        "Могу скинуть в виде файла с любым разрешением.");
                return;
            }
            if (command.equals("/sendtext")) {
                sendText(chatId, "Кидай фото.");
                curUser.setWaitingForPhoto(1);
                return;
            } else if (command.equals("/sendfile")) {
                sendText(chatId, "Итак, вам надо написать разрешение в формате: " +
                        "\"width x height y\" или \"width x\" или \"height x\"");
                sendText(chatId, "Таким образом, если вы укажете размер обеих сторон в сиволах, " +
                        "то изображение может стать кривым. " +
                        "Если же вы укажете только одну из сторон, " +
                        "то вторая сторона посчитается с сохранением отношения сторон.");
                curUser.setWaitingForPhoto(2);
                return;
            } else if (curUser.getWaitingForPhoto() == 2) {
                curUser.setCurHeight(0);
                curUser.setCurWidth(0);
                Scanner sc = new Scanner(command.toLowerCase(Locale.ROOT));
                String[] arr = new String[4];
                int count = 0;
                while (count < 4 && sc.hasNext()) {
                    arr[count++] = sc.next();
                }
                if (arr[3] == null) {
                    if (arr[0].equals("width")) {
                        try {
                            curUser.setCurWidth(Integer.parseInt(arr[1]));
                        } catch (Exception e) {
                            curUser.setCurWidth(0);
                        }
                    } else if (arr[0].equals("height")) {
                        try {
                            curUser.setCurHeight(Integer.parseInt(arr[1]));
                        } catch (Exception e) {
                            curUser.setCurHeight(0);
                        }
                    }
                } else {
                    if (arr[0].equals("width")) {
                        try {
                            curUser.setCurWidth(Integer.parseInt(arr[1]));
                        } catch (Exception e) {
                            curUser.setCurWidth(0);
                        }
                    }
                    if (arr[2].equals("height")) {
                        try {
                            curUser.setCurHeight(Integer.parseInt(arr[3]));
                        } catch (Exception e) {
                            curUser.setCurHeight(0);
                        }
                    }
                }
                if (curUser.getCurHeight() == 0 && curUser.getCurWidth() == 0) {
                    sendText(chatId, "Итак, вам надо написать разрешение в формате: " +
                            "\"width x height y\" или \"width x\" или \"height x\"");
                    sendText(chatId, "Таким образом, если вы укажете размер обеих сторон в сиволах," +
                            " то изображение может стать кривым. Если же вы укажете только одну из сторон," +
                            " то вторая сторона посчитается с сохранением отношения сторон");
                    return;
                }
                sendText(chatId, "Кидай фото");
                curUser.setWaitingForPhoto(3);
                return;
            } else {
                sendText(chatId, "Пожалуйста, отправляйте только указанные команды");
                curUser.setWaitingForPhoto(0);
                return;
            }
        }
        if (curUser.getWaitingForPhoto() % 2 == 1) {
            ArrayList<PhotoSize> photos = (ArrayList<PhotoSize>) update.getMessage().getPhoto();
            GetFile getFile = new GetFile(photos.get(photos.size() - 1).getFileId());
            String filePath = null;
            try {
                filePath = execute(getFile).getFilePath();
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            File file = null;
            try {
                assert filePath != null;
                file = downloadFile(filePath);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            if (curUser.getWaitingForPhoto() == 1) {
                String imageToText = printImage(file, false);
                sendText(chatId, imageToText);
            } else if (curUser.getWaitingForPhoto() == 3) {
                String imageToText = printImage(file, true);
                sendFile(chatId, imageToText);
            }
            curUser.setWaitingForPhoto(0);
            return;
        }
        sendText(chatId, "Пожалуйста, отправляйте только указанные команды");
        curUser.setWaitingForPhoto(0);
    }

    public void sendText(String chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendFile(String chatId, String text) {
        File file = new File("file.txt");
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            writer.write(text);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(chatId);
        sendDocument.setDocument(new InputFile(file));
        try {
            execute(sendDocument);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String printImage(File file, boolean good) {
        StringBuilder ans = new StringBuilder();
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] bright = {"÷", "1", "0", "$"};
        if (good) {
            bright = new String[]{"@", "$", "#", "*", "!", "=", ";", ":", "~", "-", ",", ".", " "};
        }
        assert bufferedImage != null;
        int[][] red = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];
        int[][] green = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];
        int[][] blue = new int[bufferedImage.getHeight()][bufferedImage.getWidth()];
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int pixel = bufferedImage.getRGB(x, y);
                Color color = new Color(pixel, true);
                red[y][x] = color.getRed();
                green[y][x] = color.getGreen();
                blue[y][x] = color.getBlue();
            }
        }
        double pixelWidth = 1.0, pixelHeight = 3.0;
        double width = bufferedImage.getWidth(), height = bufferedImage.getHeight(), widthChar = 57.0,
                heightChar = (height / width) * widthChar * (pixelWidth / pixelHeight);
        if (good) {
            width = bufferedImage.getWidth();
            height = bufferedImage.getHeight();
            if (curUser.getCurHeight() != 0 && curUser.getCurWidth() != 0) {
                heightChar = curUser.getCurHeight();
                widthChar = curUser.getCurWidth();
            } else if (curUser.getCurHeight() == 0) {
                widthChar = curUser.getCurWidth();
                heightChar = (height / width) * widthChar * (pixelWidth / pixelHeight);
            } else {
                heightChar = curUser.getCurHeight();
                widthChar = (width / height) * heightChar * (pixelHeight / pixelWidth);
            }
        }
        double dx = width / widthChar, dy = height / heightChar;
        for (int i = 0; i < heightChar; i++) {
            for (int j = 0; j < widthChar; j++) {
                double dist = Math.sqrt(red[(int) (i * dy)][(int) (j * dx)] * red[(int) (i * dy)][(int) (j * dx)]
                        + green[(int) (i * dy)][(int) (j * dx)] * green[(int) (i * dy)][(int) (j * dx)]
                        + blue[(int) (i * dy)][(int) (j * dx)] * blue[(int) (i * dy)][(int) (j * dx)]);
                double coeff = (dist / 442.0);
                ans.append(bright[(int) (bright.length * coeff)]);
            }
            ans.append("\n");
        }
        return ans.toString();
    }
}
