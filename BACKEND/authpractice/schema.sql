-- Create database
CREATE DATABASE IF NOT EXISTS authpractice;
USE authpractice;

-- Users table
CREATE TABLE `user` (
                        `id` varchar(255) NOT NULL,
                        `email` varchar(255) NOT NULL,
                        `password` varchar(255) DEFAULT NULL,
                        `role` enum('USER','ADMIN') NOT NULL DEFAULT 'USER',
                        `isverified` tinyint(1) DEFAULT '0',
                        `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `auth_provider` enum('LOCAL','GOOGLE','GITHUB','FACEBOOK') DEFAULT 'LOCAL',
                        `oauth_id` varchar(255) DEFAULT NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `email_UNIQUE` (`email`) /*!80000 INVISIBLE */,
                        KEY `idx_oauth_lookup` (`auth_provider`,`oauth_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- OTP Verifications table
CREATE TABLE `otp_verifications` (
                                     `id` varchar(255) NOT NULL,
                                     `email` varchar(255) NOT NULL,
                                     `otp_code` varchar(6) NOT NULL,
                                     `attempt_count` int DEFAULT '0',
                                     `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
                                     `expires_at` timestamp NOT NULL,
                                     PRIMARY KEY (`id`),
                                     KEY `idx_email_expires` (`email`,`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

-- Refresh Tokens table
CREATE TABLE `refresh_tokens` (
                                  `id` varchar(255) NOT NULL,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `expires_at` datetime(6) NOT NULL,
                                  `last_used_at` datetime(6) DEFAULT NULL,
                                  `token_hash` longtext NOT NULL,
                                  `user_id` varchar(255) NOT NULL,
                                  PRIMARY KEY (`id`),
                                  KEY `FKjwc9veyjcjfkej6rnnbsijfvh` (`user_id`),
                                  CONSTRAINT `FKjwc9veyjcjfkej6rnnbsijfvh` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
