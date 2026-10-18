package com.homektv.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.time.OffsetDateTime;

@Entity
@Table(name = "app_secrets")
public class AppSecret {
    @Id
    private String key;
    @Column(nullable = false)
    private byte[] ciphertext;
    @Column(nullable = false)
    private byte[] nonce;
    @Column(name = "key_version", nullable = false)
    private int keyVersion = 1;
    @Generated(event = {EventType.INSERT, EventType.UPDATE})
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public byte[] getCiphertext() { return ciphertext; }
    public void setCiphertext(byte[] ciphertext) { this.ciphertext = ciphertext; }
    public byte[] getNonce() { return nonce; }
    public void setNonce(byte[] nonce) { this.nonce = nonce; }
    public int getKeyVersion() { return keyVersion; }
    public void setKeyVersion(int keyVersion) { this.keyVersion = keyVersion; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
