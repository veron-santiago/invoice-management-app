package io.github.veron_santiago.backend.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "company_name", nullable = false, unique = true, length = 100)
    private String companyName;

    @Column(nullable = false)
    @NotBlank
    @JsonIgnore
    private String password;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "logo_path")
    private String logoPath;

    private String address;

    @Builder.Default
    private boolean verified = false;

    @Column(name = "mp_access_token")
    private String mpAccessToken;
    @Column(name = "mp_refresh_token")
    private String mpRefreshToken;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("billNumber DESC")
    private List<Bill> bills;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Customer> customers;
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product> products;

}