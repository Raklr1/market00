package top.otsuland.market.dto;

import lombok.Data;

@Data
public class UserMetaResp {
    private String username;
    private String tel;
    private Integer follow;
    private Integer fans;
}
