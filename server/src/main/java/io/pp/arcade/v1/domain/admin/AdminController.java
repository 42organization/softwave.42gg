package io.pp.arcade.v1.domain.admin.management;

import io.pp.arcade.v1.domain.admin.dto.AdminCheckerDto;
import io.pp.arcade.v1.domain.security.jwt.TokenService;
import io.pp.arcade.v1.domain.user.dto.UserDto;
import io.pp.arcade.v1.global.util.HeaderUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Controller
@AllArgsConstructor
public class AdminController {
    private TokenService tokenService;
    @GetMapping("/admin")
    public void adminPage(HttpServletRequest request, HttpServletResponse response, @RequestParam String token) throws IOException {
        UserDto user = tokenService.findAdminByAccessToken(token);
        HttpSession session = request.getSession();
        session.setAttribute("user", AdminCheckerDto.builder().intraId(user.getIntraId()).roleType(user.getRoleType()).build());
        response.sendRedirect("/admin/main");
    }
}
