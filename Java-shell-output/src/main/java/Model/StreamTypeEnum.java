package Model;


public enum StreamTypeEnum {
    OUT("out"),
    ERROR("error");

    StreamTypeEnum(String value) {
        this.value = value;
    }

    private String value;

    public String value() {
        return value;
    }
}
