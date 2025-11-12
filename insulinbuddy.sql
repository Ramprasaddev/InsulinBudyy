-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 16, 2025 at 09:14 AM
-- Server version: 8.0.42
-- PHP Version: 8.1.25

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `insulinbuddy`
--

-- --------------------------------------------------------

--
-- Table structure for table `glucose_level`
--

CREATE TABLE `glucose_level` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `glucose_value` double NOT NULL,
  `recorded_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `note` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `glucose_level`
--

INSERT INTO `glucose_level` (`id`, `username`, `glucose_value`, `recorded_time`, `note`) VALUES
(1, 'ramprasad', 10, '2025-08-04 14:34:48', ''),
(5, 'ramprasad', 10, '2025-08-04 15:00:03', ''),
(6, 'ramprasad', 120, '2025-08-04 15:11:08', ''),
(8, 'ramprasad', 120, '2025-08-05 13:37:55', ''),
(9, 'ramprasad', 120, '2025-08-05 14:32:35', ''),
(10, 'ramprasad', 120, '2025-08-05 14:33:07', ''),
(11, 'ramprasad', 120, '2025-08-05 14:40:11', ''),
(12, 'ramprasad', 135, '2025-08-05 14:42:43', ''),
(13, 'ramprasad', 139, '2025-08-05 14:43:45', ''),
(14, 'srilaxmi', 135, '2025-08-05 15:26:13', ''),
(15, 'ramprasad', 120, '2025-08-06 09:36:17', ''),
(16, 'ramprasad', 120, '2025-08-07 14:41:49', ''),
(17, 'ramprasad', 120, '2025-08-11 09:53:27', ''),
(18, 'ramprasad', 170, '2025-08-11 12:57:59', ''),
(19, 'ramprasad', 170, '2025-08-11 12:57:59', ''),
(20, 'ramprasad', 170, '2025-08-11 12:58:32', ''),
(21, 'ramprasad', 150, '2025-08-11 12:59:07', ''),
(22, 'ramprasad', 110, '2025-08-11 14:27:50', ''),
(23, 'ramprasad', 190, '2025-08-11 14:27:56', ''),
(24, 'ramprasad', 120, '2025-08-13 13:38:37', ''),
(25, 'gnani', 25, '2025-09-01 13:13:58', ''),
(26, 'gnani', 56, '2025-09-01 13:14:03', ''),
(27, 'gnani', 65, '2025-09-01 13:14:10', ''),
(28, 'gnani', 190, '2025-09-01 13:21:04', ''),
(29, 'ramprasad', 150, '2025-09-02 13:04:46', ''),
(30, 'ramprasad', 109, '2025-09-02 13:05:25', ''),
(31, 'ramprasad', 145, '2025-09-09 09:14:18', ''),
(32, 'ramprasad', 120, '2025-10-14 12:45:51', ''),
(33, 'ramprasad', 150, '2025-10-14 13:03:07', 'After lunch reading'),
(34, 'Gowthami', 120, '2025-10-14 14:09:40', ''),
(35, 'Rampra', 120, '2025-10-15 10:32:28', ''),
(36, 'Rampra', 120, '2025-10-16 12:40:15', ''),
(37, 'Rampra', 120, '2025-10-16 12:40:18', '');

-- --------------------------------------------------------

--
-- Table structure for table `insulin_intake`
--

CREATE TABLE `insulin_intake` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `intake_value` double NOT NULL,
  `intake_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `note` text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `insulin_intake`
--

INSERT INTO `insulin_intake` (`id`, `username`, `intake_value`, `intake_time`, `note`) VALUES
(1, 'ramprasad', 10, '2025-08-04 14:36:22', ''),
(2, 'ramprasad', 12, '2025-08-05 13:23:38', ''),
(3, 'ramprasad', 12, '2025-08-05 14:59:13', 'After lunch'),
(4, 'ramprasad', 10, '2025-08-05 15:02:11', 'morning'),
(5, 'ramprasad', 10, '2025-08-05 15:02:23', ''),
(6, 'ramprasad', 10, '2025-08-05 15:10:18', ''),
(7, 'srilaxmi', 10, '2025-08-05 15:26:06', 'y'),
(8, 'ramprasad', 10, '2025-08-06 09:36:33', ''),
(9, 'ramprasad', 88, '2025-08-08 13:46:21', ''),
(10, 'ramprasad', 25, '2025-08-08 14:28:11', ''),
(11, 'ramprasad', 18, '2025-08-08 14:28:29', ''),
(12, 'ramprasad', 10, '2025-08-11 09:53:18', ''),
(13, 'ramprasad', 88, '2025-08-13 10:37:04', ''),
(14, 'ramprasad', 15, '2025-08-13 13:38:25', ''),
(15, 'ramprasad', 10, '2025-08-14 10:36:32', 'f'),
(16, 'ramprasad', 10, '2025-08-20 13:23:46', ''),
(17, 'ramprasad', 88, '2025-08-20 14:38:21', ''),
(18, 'ramprasad', 88, '2025-08-20 14:38:26', ''),
(19, 'ramprasad', 80, '2025-08-20 14:38:34', ''),
(20, 'ramprasad', 80, '2025-08-20 14:38:35', ''),
(21, 'ramprasad', 80, '2025-08-20 14:38:36', ''),
(22, 'ramprasad', 80, '2025-08-20 14:38:37', ''),
(23, 'ramprasad', 80, '2025-08-20 14:38:38', ''),
(24, 'ramprasad', 80, '2025-08-20 14:38:39', ''),
(25, 'ramprasad', 20, '2025-08-20 16:48:27', 'good'),
(26, 'gnani', 20, '2025-09-01 13:13:42', ''),
(27, 'gnani', 50, '2025-09-01 13:13:47', ''),
(28, 'gnani', 100, '2025-09-01 13:13:52', ''),
(29, 'gnani', 10, '2025-09-01 13:20:55', ''),
(30, 'ramprasad', 20, '2025-09-02 13:04:18', ''),
(31, 'ramprasad', 89, '2025-09-09 00:19:23', ''),
(32, 'ramprasad', 18, '2025-09-09 09:13:53', ''),
(33, 'ramprasad', 12, '2025-10-14 12:42:58', ''),
(34, 'ramprasad', 12, '2025-10-14 12:43:55', ''),
(35, 'ramprasad', 10, '2025-10-14 13:12:08', ''),
(36, 'Gowthami', 10, '2025-10-14 14:09:28', ''),
(37, 'Rampra', 10, '2025-10-15 10:32:21', ''),
(38, 'Rampra', 12, '2025-10-15 12:35:09', ''),
(39, 'Rampra', 10, '2025-10-16 12:39:51', '');

-- --------------------------------------------------------

--
-- Table structure for table `insulin_logs`
--

CREATE TABLE `insulin_logs` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `log_date` date NOT NULL,
  `log_time` time NOT NULL,
  `current_glucose` double NOT NULL,
  `carb_intake` double NOT NULL,
  `activity_minutes` double NOT NULL,
  `stress_level` varchar(20) DEFAULT NULL,
  `meal_type` varchar(20) DEFAULT NULL,
  `notes` text,
  `ai_dose` double NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `insulin_logs`
--

INSERT INTO `insulin_logs` (`id`, `username`, `log_date`, `log_time`, `current_glucose`, `carb_intake`, `activity_minutes`, `stress_level`, `meal_type`, `notes`, `ai_dose`, `created_at`) VALUES
(1, 'ramprasad', '2025-10-14', '11:39:21', 150, 60, 30, 'High', 'Lunch', 'Feeling tired', 5.8, '2025-10-14 09:39:21'),
(2, 'ramprasad', '2025-10-14', '11:40:11', 120, 88, 55, 'Low', 'Morning', '', 5.7, '2025-10-14 09:40:11'),
(3, 'ramprasad', '2025-10-14', '11:42:13', 120, 55, 33, 'Low', 'Morning', '', 3.683333333333333, '2025-10-14 09:42:13'),
(4, 'ramprasad', '2025-10-14', '11:46:42', 120, 60, 90, 'Low', 'Morning', '', 2.2, '2025-10-14 09:46:42'),
(5, 'ramprasad', '2025-10-14', '11:51:14', 120, 60, 33, 'Low', 'Morning', '', 4.1, '2025-10-14 09:51:14'),
(6, 'ramprasad', '2025-10-14', '11:53:25', 120, 60, 66, 'Low', 'Morning', '', 3, '2025-10-14 09:53:25');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `full_name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id`, `username`, `email`, `password`, `full_name`) VALUES
(1, 'ramm', 'ramprasaddharmapuri@gmail.com', '$2y$10$8JkMS1t7gvnLTjRp7dVSQ.7fWYLp9NpODdOo3C116aQQ/JnosrBPa', 'Ram Prasad'),
(2, 'ramprasad', 'ram@abc.com', '$2y$10$6oDL9A9mNf5TUf0kJhhbiOAwkXg32GB0hyt27xUKGEboIverwduRS', 'Ram Prasad'),
(3, 'dhatchu', 'd@gmail.com', '$2y$10$MZ6yDX5rF6A9JVV0lgqwp.DR2OKD0Ovgc5dQXME6yK.Dx6N9b7Iaq', 'dhatchu'),
(4, 'aakash', 'ak@gmail.com', '$2y$10$YzBhl.ZKRdZVn4RkqWZTBOM47TTQXK715FT/ORpuko0PMeWeOZDGG', 'aakash'),
(6, 'srilaxmi', 'sri@gmail.com', '$2y$10$YI6Qi2.OUQXV7hGwbEP8U.syKhTx05RIDHgPzS4utfVX3lTHdqVTO', 'srilaxmi'),
(7, 'gnani', 'v.gnaneswarreddy18@gmail.com', '$2y$10$OZloflhzv78lzg6xSF06x.1JTCJmZvlkIcICMEdnjYP5YhlBiK1hu', 'Valipireddy Gnaneswarreddy'),
(9, 'ramprasadd', 'ram@gmail.com', '$2y$10$CTleJFDfgaeAbaLGhwfxJ.EBgrz5TFnzKYGlVShnXFLEcMoxHx1ri', 'ramprasad'),
(13, 'ramp', 'ramp@gmail.com', '$2y$10$UcKJ3XBRb3W6j.xtApsTtuHlbfhizjrvLIJbgGMSY0xZF0Hm9lOsO', 'ram'),
(14, 'karthik', 'karthik.p309k@gmail.com', '$2y$10$VLW40m8R6yAfVL5i/sTPN.Ma3mdCtMhF6oNacTFFVbGRq3ULRD2SC', 'karthik p'),
(15, 'gowthami', 'ram123@gmail.com', '$2y$10$wfwzu.987uhSGgrx9GI0Muc2gQiRMLv4uaYYz9ltVSmySAEJRQyBy', 'lakshmi Padmavathikaruturi'),
(16, 'Rampra', 'ram1@gmail.com', '$2y$10$xqeGRl2DjA.ZwmCkDioLMuAiO.z50BPXUbIMaR0H3v6xlMTuIx.f2', 'rammmmm');

-- --------------------------------------------------------

--
-- Table structure for table `user_profile`
--

CREATE TABLE `user_profile` (
  `id` int NOT NULL,
  `username` varchar(50) NOT NULL,
  `contact` varchar(20) DEFAULT NULL,
  `age` int DEFAULT NULL,
  `gender` varchar(10) DEFAULT NULL,
  `isf` double DEFAULT NULL,
  `icr` float DEFAULT NULL,
  `target` double DEFAULT NULL,
  `weight` double DEFAULT NULL,
  `type_of_diabetes` varchar(50) DEFAULT NULL,
  `diagnosis_year` int DEFAULT NULL,
  `diet_type` varchar(50) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `profile_completed` int DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `user_profile`
--

INSERT INTO `user_profile` (`id`, `username`, `contact`, `age`, `gender`, `isf`, `icr`, `target`, `weight`, `type_of_diabetes`, `diagnosis_year`, `diet_type`, `created_at`, `profile_completed`) VALUES
(1, 'ramm', '9182301919', 21, 'Male', 50, 10, 120, 65.5, NULL, NULL, NULL, '2025-07-31 04:55:08', 0),
(4, 'ramprasad', '9876543210', 65, 'Male', 50, 12, 110, 72, 'Type 2', 2015, 'Balanced', '2025-08-13 05:37:55', 1),
(7, 'Rampra', '5555555555', 22, 'Male', 12, 12, 120, 85, 'Type 1', 2005, 'the', '2025-10-15 10:23:42', 1);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `glucose_level`
--
ALTER TABLE `glucose_level`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indexes for table `insulin_intake`
--
ALTER TABLE `insulin_intake`
  ADD PRIMARY KEY (`id`),
  ADD KEY `username` (`username`);

--
-- Indexes for table `insulin_logs`
--
ALTER TABLE `insulin_logs`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- Indexes for table `user_profile`
--
ALTER TABLE `user_profile`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uk_username` (`username`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `glucose_level`
--
ALTER TABLE `glucose_level`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=38;

--
-- AUTO_INCREMENT for table `insulin_intake`
--
ALTER TABLE `insulin_intake`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=40;

--
-- AUTO_INCREMENT for table `insulin_logs`
--
ALTER TABLE `insulin_logs`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `user_profile`
--
ALTER TABLE `user_profile`
  MODIFY `id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `glucose_level`
--
ALTER TABLE `glucose_level`
  ADD CONSTRAINT `glucose_level_ibfk_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE;

--
-- Constraints for table `insulin_intake`
--
ALTER TABLE `insulin_intake`
  ADD CONSTRAINT `insulin_intake_ibfk_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE;

--
-- Constraints for table `user_profile`
--
ALTER TABLE `user_profile`
  ADD CONSTRAINT `user_profile_ibfk_1` FOREIGN KEY (`username`) REFERENCES `users` (`username`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
