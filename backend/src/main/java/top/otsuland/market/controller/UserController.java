package top.otsuland.market.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import top.otsuland.market.common.JwtUtils;
import top.otsuland.market.common.Result;
import top.otsuland.market.dto.PageResult;
import top.otsuland.market.dto.UserFollowVO;
import top.otsuland.market.dto.UserFollowVO2;
import top.otsuland.market.dto.UserLoginResp;
import top.otsuland.market.dto.UserProfResp;
import top.otsuland.market.entity.User;
import top.otsuland.market.entity.UserProfile;
import top.otsuland.market.service.UserService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 测试 - 获取用户列表
     */
    @GetMapping
    public Result<?> list() {
        List<User> users = userService.getUsersList();
        return Result.success(users);
    }

    /**
     * 注册
     * ok
     */
    @PostMapping("/register")
    public Result<?> register(@RequestBody User user) {
        int code = userService.register(user);
        switch(code) {
            case 1: return Result.set(code, "注册成功！");
            case -3: return Result.set(code, "缺少信息！");
            case -1: return Result.set(code, "用户名已经被使用！");
            case -2: return Result.set(code, "该电话号码已经被注册！");
            default: return Result.fail();
        }
    }
    
    /**
     * 登录
     * ok
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user) {
        int code = userService.login(user);
        if(code > 0) {
            String token = JwtUtils.geneJWT(userService.withId(user));
            UserLoginResp ulr = new UserLoginResp();
            ulr.setToken(token);
            ulr.setUid(code);
            return Result.set(1, "登录成功！", ulr);
        } else if(code == -1) {
            return Result.set(code, "密码为空！");
        } else if(code == -2) {
            return Result.set(code, "缺失信息！");
        } else if(code == -3) {
            return Result.set(code, "用户不存在！");
        } else if(code == -4) {
            return Result.set(code, "密码不正确！");
        } else {
            return Result.fail();
        }
    }

    /**
     * 修改基本信息
     * ok
     */
    @PutMapping
    public Result<?> meta(@RequestAttribute("id") Integer uid, @RequestBody User user) {
        if(userService.meta(uid, user) == 1) {
            return Result.set(1, "修改成功！", userService.getMeta(uid));
        }
        return Result.set(0, "修改失败！");
    }

    /**
     * 修改个人简介
     * ok
     */
    @PutMapping("/prof")
    public Result<?> prof(@RequestAttribute("id") Integer id, @RequestBody UserProfile userProfile) {
        int code = userService.prof(id, userProfile);
        if(code == 1) {
            return Result.set(code, "修改成功！");
        }
        return Result.set(code, "用户不存在！");
    }

    /**
     * 获取个人简介
     */
    @GetMapping("/prof/{uid}")
    public Result<?> getProf(@PathVariable Integer uid) {
        UserProfile uprof =  userService.getProf(uid);
        if(uprof == null) {
            return Result.set(0, "获取失败！");
        }
        UserProfResp upr = new UserProfResp(uprof);
        upr.setUsername(userService.getMeta(uid).getUsername());
        upr.setFollow(userService.getMeta(uid).getFollow());
        upr.setFans(userService.getMeta(uid).getFans());
        upr.setTel(userService.getMeta(uid).getTel());
        return Result.set(1, "获取成功！", upr);
    }

    @GetMapping("/prof")
    public Result<?> getProfWithoutUid(@RequestAttribute("id") Integer uid) {
        UserProfile uprof =  userService.getProf(uid);
        if(uprof == null) {
            return Result.set(0, "获取失败！");
        }
        UserProfResp upr = new UserProfResp(uprof);
        upr.setUsername(userService.getMeta(uid).getUsername());
        upr.setFollow(userService.getMeta(uid).getFollow());
        upr.setFans(userService.getMeta(uid).getFans());
        upr.setTel(userService.getMeta(uid).getTel());
        return Result.set(1, "获取成功！", upr);
    }

    /**
     * 上传头像
     * ok
     */
    @PostMapping("/icon")
    public Result<?> icon(@RequestAttribute("id") Integer id, @RequestParam MultipartFile pic) {
        try {
            int code = userService.icon(id, pic);
            switch (code) {
                case 1: return Result.set(code, "上传成功！");
                case -1: return Result.set(code, "用户不存在！");
                default: return Result.fail();
            }
        } catch (IOException e) {
            return Result.set(-2, "输入输出异常！");
        }
    }



    /**
     * 下载头像
     */
    @GetMapping("/icon/{uid}")
    public ResponseEntity<?> loadIcon2(@PathVariable Integer uid) {
        byte[] image = userService.getIcon(uid);
        if(image == null) {
            return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(null);
        }
        String filename = "";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDispositionFormData("inline", filename);
        return new ResponseEntity<>(image, headers, HttpStatus.OK);
    }

    /**
     * 关注用户
     */
    @PutMapping("/follow/{fid}")
    public Result<?> follow(@RequestAttribute("id") Integer uid, @PathVariable Integer fid) {
        int code = userService.follow(uid, fid);
        if(code == 1) {
            return Result.set(1, "关注成功！");
        }
        if(code == -1) {
            return Result.set(-1, "已关注！");
        }
        return Result.set(0, "关注失败！");
    }

    /**
     * 取消关注
     */
    @DeleteMapping("/follow/{fid}")
    public Result<?> disfollow(@RequestAttribute("id") Integer uid, @PathVariable Integer fid) {
        int code = userService.disfollow(uid, fid);
        if(code == 1) {
            return Result.set(1, "取消关注！");
        }
        if(code == -1) {
            return Result.set(-1, "未关注！");
        }
        return Result.set(0, "取消失败！");
    }

    /**
     * 分页获取关注列表
     */
    @GetMapping("/follow/{uid}")
    public Result<?> getfollower(@PathVariable Integer uid,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<UserFollowVO> pageParam = new Page<>(page, size);
        Page<UserFollowVO> resultPage = userService.getFolloweePage(pageParam, uid);
        return Result.set(1, "获取成功！", PageResult.of(resultPage));
    }

    /**
     * 分页获取粉丝列表
     */
    @GetMapping("/fans/{uid}")
    public Result<?> getfollowing(@PathVariable Integer uid,
        @RequestParam(defaultValue = "1") Integer page,
        @RequestParam(defaultValue = "10") Integer size
    ) {

        Page<UserFollowVO2> pageParam = new Page<>(page, size);
        Page<UserFollowVO2> resultPage = userService.getFollowerPage(pageParam, uid);
        return Result.set(1, "获取成功！", PageResult.of(resultPage));
    }

    /**
     * 判断是否关注
     */
    @GetMapping("/follow/if/{fid}")
    public Result<?> isFollowing(@RequestAttribute("id") Integer uid, @PathVariable Integer fid) {
        if(userService.isFollowing(uid, fid) == 1) {
            return Result.set(1, "已关注");
        }
        return Result.set(0, "未关注");
    }

}
