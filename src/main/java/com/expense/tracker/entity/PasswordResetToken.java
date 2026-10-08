package com.expense.tracker.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "password_reset_tokens",
    indexes = @Index(name = "idx_reset_token", columnList = "token", unique = true))
public class PasswordResetToken {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String token;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private AppUser user;

  @Column(nullable = false)
  private LocalDateTime expiresAt;

  @Column(nullable = false)
  private boolean used = false;

  public PasswordResetToken() {}

  public PasswordResetToken(String token, AppUser user, LocalDateTime expiresAt) {
    this.token = token;
    this.user = user;
    this.expiresAt = expiresAt;
  }

  public String getToken() {
    return token;
  }

  public AppUser getUser() {
    return user;
  }

  public LocalDateTime getExpiresAt() {
    return expiresAt;
  }

  public boolean isUsed() {
    return used;
  }

  public void setUsed(boolean v) {
    used = v;
  }
}
