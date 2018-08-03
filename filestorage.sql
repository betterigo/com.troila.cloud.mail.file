/*
Navicat MySQL Data Transfer

Source Server         : 172.27.108.93
Source Server Version : 50722
Source Host           : 172.27.108.93:3306
Source Database       : filestorage

Target Server Type    : MYSQL
Target Server Version : 50722
File Encoding         : 65001

Date: 2018-08-03 11:16:48
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for file_info
-- ----------------------------
DROP TABLE IF EXISTS `file_info`;
CREATE TABLE `file_info` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `file_name` varchar(255) NOT NULL COMMENT '文件名称',
  `md5` varchar(64) NOT NULL COMMENT '文件MD5值',
  `size` bigint(20) DEFAULT '0' COMMENT '文件大小',
  `gmt_create` datetime DEFAULT NULL,
  `status` varchar(32) DEFAULT '0',
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=110 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of file_info
-- ----------------------------
INSERT INTO `file_info` VALUES ('1', 'BASE_FOLDER', '111111', '0', '2018-08-03 11:16:15', 'SUCCESS', null);

-- ----------------------------
-- Table structure for file_info_ext
-- ----------------------------
DROP TABLE IF EXISTS `file_info_ext`;
CREATE TABLE `file_info_ext` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `base_fid` int(10) unsigned NOT NULL,
  `original_file_name` text NOT NULL,
  `suffix` varchar(32) DEFAULT NULL,
  `file_type` varchar(32) DEFAULT NULL,
  `acl` varchar(32) DEFAULT NULL COMMENT 'null 为私有',
  `gmt_expired` datetime DEFAULT NULL COMMENT 'null 为永久',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `base_fid` (`base_fid`) USING BTREE,
  KEY `gmt_expired` (`gmt_expired`) USING BTREE,
  CONSTRAINT `file_fk_1` FOREIGN KEY (`base_fid`) REFERENCES `file_info` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=141 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of file_info_ext
-- ----------------------------

-- ----------------------------
-- Table structure for folder
-- ----------------------------
DROP TABLE IF EXISTS `folder`;
CREATE TABLE `folder` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `pid` int(11) DEFAULT '0' COMMENT '此文件夹的父文件夹id',
  `uid` int(11) NOT NULL,
  `name` varchar(255) NOT NULL,
  `type` varchar(255) DEFAULT NULL COMMENT '文件夹分类',
  `is_empty` tinyint(1) DEFAULT '0',
  `is_deleted` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of folder
-- ----------------------------

-- ----------------------------
-- Table structure for folder_file
-- ----------------------------
DROP TABLE IF EXISTS `folder_file`;
CREATE TABLE `folder_file` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `folder_id` int(11) unsigned NOT NULL,
  `file_id` int(10) unsigned NOT NULL,
  `is_deleted` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  `gmt_delete` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `file_id` (`file_id`),
  KEY `folder_fk` (`folder_id`),
  CONSTRAINT `file_fk` FOREIGN KEY (`file_id`) REFERENCES `file_info_ext` (`id`) ON UPDATE CASCADE,
  CONSTRAINT `folder_fk` FOREIGN KEY (`folder_id`) REFERENCES `folder` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of folder_file
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `nick` varchar(255) DEFAULT NULL,
  `user_code` varchar(255) DEFAULT NULL COMMENT '来自哪里的用户',
  `disable` tinyint(1) DEFAULT '0',
  `gmt_create` datetime DEFAULT NULL,
  `gmt_modify` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `user_code_index` (`user_code`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of user
-- ----------------------------

-- ----------------------------
-- View structure for v_file_detail_info
-- ----------------------------
DROP VIEW IF EXISTS `v_file_detail_info`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_file_detail_info` AS select `file_info`.`file_name` AS `file_name`,`file_info`.`md5` AS `md5`,`file_info`.`size` AS `size`,`file_info_ext`.`id` AS `id`,`file_info_ext`.`base_fid` AS `base_fid`,`file_info_ext`.`original_file_name` AS `original_file_name`,`file_info_ext`.`suffix` AS `suffix`,`file_info_ext`.`file_type` AS `file_type`,`file_info_ext`.`gmt_create` AS `gmt_create`,`file_info_ext`.`gmt_modify` AS `gmt_modify`,`file_info_ext`.`gmt_delete` AS `gmt_delete`,`file_info`.`status` AS `status`,`file_info_ext`.`acl` AS `acl`,`file_info_ext`.`gmt_expired` AS `gmt_expired` from (`file_info` join `file_info_ext`) where (`file_info`.`id` = `file_info_ext`.`base_fid`) ;

-- ----------------------------
-- View structure for v_user_file
-- ----------------------------
DROP VIEW IF EXISTS `v_user_file`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `v_user_file` AS select `folder_file`.`id` AS `id`,`folder_file`.`folder_id` AS `folder_id`,`folder_file`.`file_id` AS `file_id`,`v_file_detail_info`.`file_name` AS `file_name`,`folder_file`.`is_deleted` AS `is_deleted`,`folder`.`uid` AS `uid`,`folder`.`name` AS `folder_name`,`folder`.`type` AS `folder_type`,`v_file_detail_info`.`md5` AS `md5`,`v_file_detail_info`.`size` AS `size`,`v_file_detail_info`.`original_file_name` AS `original_file_name`,`v_file_detail_info`.`suffix` AS `suffix`,`v_file_detail_info`.`file_type` AS `file_type`,`v_file_detail_info`.`gmt_create` AS `gmt_create`,`v_file_detail_info`.`gmt_modify` AS `gmt_modify`,`v_file_detail_info`.`gmt_delete` AS `gmt_delete`,`v_file_detail_info`.`status` AS `status`,`v_file_detail_info`.`acl` AS `acl`,`v_file_detail_info`.`gmt_expired` AS `gmt_expired` from ((`folder` join `folder_file` on((`folder_file`.`folder_id` = `folder`.`id`))) join `v_file_detail_info`) where (`folder_file`.`file_id` = `v_file_detail_info`.`id`) ;
