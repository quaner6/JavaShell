package Model;


import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class ExecuteResult {
    private int exitCode;
    private ResultJson json;

    public ExecuteResult(int exitCode, ResultJson json) {
        this.exitCode = exitCode;
        this.json = json;
    }
}
