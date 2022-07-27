public class MyUser {
    private int waitingForPhoto = 0;
    private int curHeight = 0;
    private int curWidth = 0;
    private final String userName;

    public MyUser(String name) {
        userName = name;
    }

    public int getWaitingForPhoto() {
        return waitingForPhoto;
    }

    public void setWaitingForPhoto(int waitingForPhoto) {
        this.waitingForPhoto = waitingForPhoto;
    }

    public int getCurHeight() {
        return curHeight;
    }

    public void setCurHeight(int curHeight) {
        this.curHeight = curHeight;
    }

    public int getCurWidth() {
        return curWidth;
    }

    public void setCurWidth(int curWidth) {
        this.curWidth = curWidth;
    }

    public String getUserName() {
        return userName;
    }
}
