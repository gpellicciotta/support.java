-- <0.1> Creating full database: setup UTF-8
/*!40101 SET NAMES utf8 */;
-- <0.1> Creating full database: setup UTF-8 MB4
/*!50503 SET NAMES utf8mb4 */;
-- <0.1> Creating full database: ensure to not check foreign keys while creating
/*!40014 SET FOREIGN_KEY_CHECKS=0 */;
-- <0.1> Creating full database: no auto value on zero
/*!40101 SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- <0.1> Creating full database: setup database if it doesn't exist yet
CREATE DATABASE IF NOT EXISTS `test` /*!40100 DEFAULT CHARACTER SET latin1 COLLATE latin1_bin */;
-- <0.1> Ensure to use for all further setup operations
USE `test`;

-- <0.1> Creating table `score`
CREATE TABLE IF NOT EXISTS `score` (`id` bigint(20) NOT NULL AUTO_INCREMENT,
                                    `game-name` varchar(50) COLLATE latin1_bin NOT NULL,
                                    `player-name` varchar(250) COLLATE latin1_bin NOT NULL,
                                     `score` int(11) NOT NULL DEFAULT 0,
                                     `achievement-time` timestamp NOT NULL DEFAULT current_timestamp(),
                                     PRIMARY KEY (`id`)
                                   ) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_bin COMMENT='Game Scores';

-- <0.1> Creating full database: ensure to check foreign keys again
/*!40014 SET FOREIGN_KEY_CHECKS=1 */;

-- <0.1> Delete all data
DELETE FROM `score`;

-- <0.1> Commit
COMMIT;
