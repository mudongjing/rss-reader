package xyz.worker.rssReader.filter;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestMess {
    private Boolean succ;
    private String mess;
}
