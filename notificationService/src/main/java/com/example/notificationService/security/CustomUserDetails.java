package com.example.notificationService.security;

import com.example.notificationService.entity.ProfileImage;
import com.example.notificationService.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@AllArgsConstructor
@Getter
@Builder
@Setter
public class CustomUserDetails implements UserDetails {
    private Long idx;
    private String email;
    private String password;
    private String userName;
    private ProfileImage profileImage;

    public CustomUserDetails(User user) {
        this.idx = user.getIdx();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.userName = user.getUserName();
        this.profileImage = user.getProfileImage();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList(); // 빈 컬렉션 반환
    }

    @Override
    public String getPassword(){
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getUserName() {
        return userName;
    }

}