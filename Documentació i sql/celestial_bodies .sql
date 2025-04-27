-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: 127.0.0.1
-- Temps de generació: 27-04-2025 a les 23:44:20
-- Versió del servidor: 10.4.32-MariaDB
-- Versió de PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de dades: `solar_system`
--

-- --------------------------------------------------------

--
-- Estructura de la taula `celestial_bodies`
--

CREATE TABLE `celestial_bodies` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `mass` double NOT NULL COMMENT 'kg',
  `semi_major_axis` double NOT NULL COMMENT 'm',
  `eccentricity` double NOT NULL,
  `inclination` double DEFAULT 0 COMMENT 'radians',
  `mean_anomaly` double DEFAULT 0 COMMENT 'radians',
  `radius` int(11) NOT NULL COMMENT 'display pixels',
  `color` varchar(20) NOT NULL,
  `x` double NOT NULL DEFAULT 0 COMMENT 'current x position (m)',
  `y` double NOT NULL DEFAULT 0 COMMENT 'current y position (m)',
  `vx` double NOT NULL DEFAULT 0 COMMENT 'current x velocity (m/s)',
  `vy` double NOT NULL DEFAULT 0 COMMENT 'current y velocity (m/s)',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Bolcament de dades per a la taula `celestial_bodies`
--

INSERT INTO `celestial_bodies` (`id`, `name`, `mass`, `semi_major_axis`, `eccentricity`, `inclination`, `mean_anomaly`, `radius`, `color`, `x`, `y`, `vx`, `vy`, `created_at`) VALUES
(112, 'Sun', 1.989e30, 0, 0, 0, 0, 20, '#FFFF00', 0, 0, 0, 0, '2025-04-27 20:57:03'),
(113, 'Mercury', 3.3011e23, 57910000000, 0.2056, 0, 0, 5, '#A9A9A9', 46003704000, 0, 0, 0, '2025-04-27 20:57:03'),
(114, 'Venus', 4.8675e24, 108210000000, 0.0067, 0, 0, 8, '#FFA500', 107484993000, 0, 0, 0, '2025-04-27 20:57:03'),
(115, 'Earth', 5.972e24, 149600000000, 0.0167, 0, 0, 8, '#1E90FF', 147101680000, 0, 0, 0, '2025-04-27 20:57:03'),
(116, 'Mars', 6.417e23, 227940000000, 0.0935, 0, 0, 6, '#FF4500', 206627610000, 0, 0, 0, '2025-04-27 20:57:03'),
(117, 'Jupiter', 1.899e27, 778570000000, 0.0489, 0, 0, 15, '#DAA520', 740497927000, 0, 0, 0, '2025-04-27 20:57:03'),
(118, 'Saturn', 5.685e26, 1433500000000, 0.0565, 0, 0, 12, '#F0E68C', 1352507250000, 0, 0, 0, '2025-04-27 20:57:03'),
(119, 'Uranus', 8.682e25, 2872500000000, 0.0457, 0, 0, 10, '#AFEEEE', 2741226750000, 0, 0, 0, '2025-04-27 20:57:03'),
(120, 'Neptune', 1.024e26, 4495100000000, 0.0113, 0, 0, 10, '#0000CD', 4444305370000, 0, 0, 0, '2025-04-27 20:57:03');

--
-- Índexs per a les taules bolcades
--

--
-- Índexs per a la taula `celestial_bodies`
--
ALTER TABLE `celestial_bodies`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_name` (`name`),
  ADD KEY `idx_mass` (`mass`);

--
-- AUTO_INCREMENT per les taules bolcades
--

--
-- AUTO_INCREMENT per la taula `celestial_bodies`
--
ALTER TABLE `celestial_bodies`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=121;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
