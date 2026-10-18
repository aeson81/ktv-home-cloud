package com.homektv.repo;

import com.homektv.domain.AppSecret;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppSecretRepository extends JpaRepository<AppSecret, String> {
}
