package com.github.leoyakubov.twofactorauth.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"password", "secret"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Document(collection = "users")
public class User {

    public User(User user) {
        this.id = user.id;
        this.username = user.username;
        this.password = user.password;
        this.email = user.email;
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
        this.active = user.active;
        this.userProfile = user.userProfile;
        this.roles = user.roles;
        this.mfa = user.mfa;
        this.secret = user.secret;
        this.recoveryCodes = user.recoveryCodes == null ? new HashSet<>() : new HashSet<>(user.recoveryCodes);
    }

    @Id
    @EqualsAndHashCode.Include
    private String id;

    @NotBlank
    @Size(max = 15)
    @Indexed(unique = true)
    private String username;

    @NotBlank
    @Size(max = 100)
    @JsonIgnore
    private String password;

    @NotBlank
    @Size(max = 40)
    @Email
    @Indexed(unique = true)
    private String email;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    private boolean active;
    private Profile userProfile;
    private Set<Role> roles;
    private boolean mfa;
    private String secret;
    @JsonIgnore
    @Builder.Default
    private Set<String> recoveryCodes = new HashSet<>();
}
