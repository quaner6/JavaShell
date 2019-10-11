package Model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;


@Setter
@Getter
@NoArgsConstructor
@ToString
public class ResultJson implements Serializable {
    private static final long serialVersionUID = 1L;
    private String out;
    private String error;

    public ResultJson(String out, String error) {
        this.out = out;
        this.error = error;
    }
}
