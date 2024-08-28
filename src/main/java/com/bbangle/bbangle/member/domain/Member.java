package com.bbangle.bbangle.member.domain;

import com.bbangle.bbangle.member.dto.InfoUpdateRequest;
import com.bbangle.bbangle.member.dto.MemberInfoRequest;
import com.bbangle.bbangle.member.exception.UserValidator;
import com.bbangle.bbangle.common.domain.BaseEntity;
import com.bbangle.bbangle.token.oauth.domain.OauthServerType;
import com.bbangle.bbangle.wishlist.domain.WishListFolder;
import com.bbangle.bbangle.wishlist.domain.WishListStore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Table(name = "member")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "name")
    private String name;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "sex")
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(name = "birth")
    private String birth;

    @Column(name = "profile")
    private String profile;

    @Column(name = "provider", columnDefinition = "varchar(20)")
    @Enumerated(EnumType.STRING)
    private OauthServerType provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "is_deleted", columnDefinition = "tinyint")
    private boolean isDeleted;

    @OneToMany(mappedBy = "member")
    List<Withdrawal> withdrawals = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    List<WishListFolder> wishListFolders = new ArrayList<>();

    @OneToMany(mappedBy = "member")
    List<WishListStore> wishListStores = new ArrayList<>();

    @Builder
    public Member(
            Long id, String email, String phone, String name, String nickname,
            String birth, boolean isDeleted, String profile, OauthServerType provider, String providerId
    ) {
        this.id = id;
        this.email = email;
        this.phone = phone;
        this.name = name;
        this.nickname = nickname;
        this.birth = birth;
        this.isDeleted = isDeleted;
        this.profile = profile;
        this.providerId = providerId;
        this.provider = provider;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("user"));
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void updateFirst(MemberInfoRequest request) {
        if (request.birthDate() != null) {
            UserValidator.validateBirthDate(request.birthDate());
        }
        UserValidator.validateNickname(request.nickname());
        this.sex = request.sex();
        this.birth = request.birthDate();
        this.nickname = request.nickname();
    }

    public void updateProfile(String imgUrl) {
        this.profile = imgUrl;
    }

    public void update(InfoUpdateRequest request) {
        if (request.birthDate() != null) {
            UserValidator.validateBirthDate(request.birthDate());
            this.birth = request.birthDate();
        }

        if (request.phoneNumber() != null) {
            UserValidator.validatePhoneNumber(request.phoneNumber());
            this.phone = request.phoneNumber();
        }

        if (request.nickname() != null) {
            UserValidator.validateNickname(request.nickname());
            this.nickname = request.nickname();
        }
    }

    public void delete(){
        this.isDeleted = true;
        this.email = "-";
        this.phone = "-";
        this.name = "-";
        this.nickname = "-";
        this.birth = "-";
        this.providerId = "-";
    }

}
