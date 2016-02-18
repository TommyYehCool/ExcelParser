DROP TABLE IF EXISTS `land`.`personal_taipei_4`;
CREATE TABLE `land`.`personal_taipei_4` (
  `VC_OWNER` varchar(50) NOT NULL DEFAULT ' ',
  `VC_COUNTIES` varchar(10) NOT NULL DEFAULT ' ',
  `VC_TOWNSHIPS` varchar(10) NOT NULL DEFAULT ' ',
  `VC_SEGMENT` varchar(50) NOT NULL DEFAULT ' ',
  `VC_LAND_NO` varchar(10) NOT NULL DEFAULT ' ',

  `N_LAND_PRICE` decimal(10,0) NOT NULL DEFAULT '0',
  `N_AREA` decimal(10,2) NOT NULL DEFAULT '0.00',
  `VC_LAND_USE_PARTITION` varchar(200) NOT NULL DEFAULT ' ',
  `N_NUMBERS_OF_BUILDING` decimal(10,0) NOT NULL DEFAULT '0',
  `N_PRIVATE_LEGAL_PERSON` decimal(10,0) NOT NULL DEFAULT '0',

  `N_NATURAL_PERSON` decimal(10,0) NOT NULL DEFAULT '0',
  `VC_ALREADY_CONTACT` varchar(1) NOT NULL DEFAULT 'N',
  `VC_PHONE_NO` varchar(20) NOT NULL DEFAULT ' ',
  `VC_EXTENSION_UNIT_UNDERTAKER` varchar(50) NOT NULL DEFAULT ' ',
  `VC_CHAT_CONTENT` varchar(200) NOT NULL DEFAULT ' ',

  `N_CONTACT_DATE` decimal(10,0) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8;