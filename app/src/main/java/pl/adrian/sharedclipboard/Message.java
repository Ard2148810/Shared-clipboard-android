package pl.adrian.sharedclipboard;

public class Message {
    private String type;
    private String content;

    Message(String type, String content) {
        setType(type);
        setContent(content);
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
